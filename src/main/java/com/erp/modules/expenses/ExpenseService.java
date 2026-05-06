package com.erp.modules.expenses;

import com.erp.core.security.JwtPrincipal;
import com.erp.core.tenant.TenantContext;
import com.erp.core.users.UserRepository;
import com.erp.core.users.Role;
import com.erp.modules.expenses.dto.ExpenseAmountExtractionResponse;
import com.erp.modules.expenses.dto.ExpenseDecisionRequest;
import com.erp.modules.expenses.dto.ExpenseInvoiceAttachmentResponse;
import com.erp.modules.expenses.dto.ExpenseInvoiceDownload;
import com.erp.modules.expenses.dto.ExpenseMonthlyComparisonResponse;
import com.erp.modules.expenses.dto.ExpenseMonthlyComparisonRow;
import com.erp.modules.expenses.dto.ExpenseRequest;
import com.erp.modules.expenses.dto.ExpenseResponse;
import com.erp.modules.expenses.mapper.ExpenseMapper;
import com.erp.shared.exceptions.BusinessException;
import com.erp.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseInvoiceAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    private static final long MAX_INVOICE_BYTES = 10 * 1024 * 1024;
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?i)(?:₹|rs\\.?|inr)?\\s*([0-9]{1,3}(?:,[0-9]{2,3})*(?:\\.\\d{1,2})?|[0-9]+(?:\\.\\d{1,2})?)\\s*(?:/-)?"
    );
    private static final List<String> TOTAL_KEYWORDS = List.of("total", "grand total", "amount payable", "net");

    private static final List<String> ALLOWED_CATEGORIES = List.of(
            "TRAVEL",
            "LODGE_STAY",
            "MEALS",
            "LOCAL_TRANSPORT",
            "CLIENT_OUTING",
            "MOBILE_INTERNET",
            "OFFICE_SUPPLIES",
            "TRAINING",
            "OTHER"
    );

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list() {
        UUID orgId = requireOrg();
        Map<UUID, String> userEmailById = buildUserEmailMap(orgId);
        return expenseRepository.findByOrgIdOrderByDateDesc(orgId).stream()
                .map(expense -> toResponseWithMeta(expense, userEmailById))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listMine(UUID userId) {
        UUID orgId = requireOrg();
        Map<UUID, String> userEmailById = buildUserEmailMap(orgId);
        return expenseRepository.findByOrgIdAndUserIdOrderByDateDesc(orgId, userId).stream()
                .map(expense -> toResponseWithMeta(expense, userEmailById))
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseMonthlyComparisonResponse monthlyComparison(String monthIso, UUID userId) {
        UUID orgId = requireOrg();
        YearMonth month;
        try {
            month = YearMonth.parse(monthIso);
        } catch (Exception ex) {
            throw new BusinessException("month must be in YYYY-MM format");
        }
        YearMonth previous = month.minusMonths(1);
        LocalDate currentStart = month.atDay(1);
        LocalDate currentEnd = month.atEndOfMonth();
        LocalDate previousStart = previous.atDay(1);
        LocalDate previousEnd = previous.atEndOfMonth();

        List<Expense> currentExpenses = userId == null
                ? expenseRepository.findByOrgIdAndDateBetweenOrderByDateDesc(orgId, currentStart, currentEnd)
                : expenseRepository.findByOrgIdAndUserIdAndDateBetweenOrderByDateDesc(orgId, userId, currentStart, currentEnd);
        List<Expense> previousExpenses = userId == null
                ? expenseRepository.findByOrgIdAndDateBetweenOrderByDateDesc(orgId, previousStart, previousEnd)
                : expenseRepository.findByOrgIdAndUserIdAndDateBetweenOrderByDateDesc(orgId, userId, previousStart, previousEnd);

        Map<UUID, String> userEmailById = buildUserEmailMap(orgId);
        Map<UUID, BigDecimal> currentTotals = currentExpenses.stream().collect(
                java.util.stream.Collectors.groupingBy(Expense::getUserId,
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add))
        );
        Map<UUID, BigDecimal> previousTotals = previousExpenses.stream().collect(
                java.util.stream.Collectors.groupingBy(Expense::getUserId,
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add))
        );
        Map<UUID, Long> currentCounts = currentExpenses.stream().collect(
                java.util.stream.Collectors.groupingBy(Expense::getUserId, java.util.stream.Collectors.counting())
        );

        Set<UUID> users = new HashSet<>();
        users.addAll(currentTotals.keySet());
        users.addAll(previousTotals.keySet());

        List<ExpenseMonthlyComparisonRow> rows = users.stream()
                .map(uid -> {
                    BigDecimal cur = currentTotals.getOrDefault(uid, BigDecimal.ZERO);
                    BigDecimal prev = previousTotals.getOrDefault(uid, BigDecimal.ZERO);
                    BigDecimal delta = cur.subtract(prev);
                    BigDecimal deltaPercent = prev.signum() == 0
                            ? null
                            : delta.multiply(new BigDecimal("100")).divide(prev, 2, java.math.RoundingMode.HALF_UP);
                    String email = uid == null
                            ? "unknown-user"
                            : userEmailById.getOrDefault(uid, "user-" + uid.toString().substring(0, 8));
                    return new ExpenseMonthlyComparisonRow(uid, email, cur, prev, delta, deltaPercent, currentCounts.getOrDefault(uid, 0L));
                })
                .sorted(Comparator.comparing(ExpenseMonthlyComparisonRow::currentMonthAmount).reversed())
                .toList();

        BigDecimal currentTotal = currentExpenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal previousTotal = previousExpenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deltaAmount = currentTotal.subtract(previousTotal);
        BigDecimal deltaPercent = previousTotal.signum() == 0
                ? null
                : deltaAmount.multiply(new BigDecimal("100")).divide(previousTotal, 2, java.math.RoundingMode.HALF_UP);

        return new ExpenseMonthlyComparisonResponse(
                month.toString(),
                previous.toString(),
                userId == null ? null : userId.toString(),
                currentTotal,
                previousTotal,
                deltaAmount,
                deltaPercent,
                currentExpenses.size(),
                rows
        );
    }

    @Transactional
    public ExpenseResponse create(JwtPrincipal principal, ExpenseRequest request) {
        UUID orgId = requireOrg();

        UUID effectiveUserId;
        if (principal.role() == Role.SALES_OFFICER) {
            effectiveUserId = principal.userId();
        } else {
            if (request.userId() == null) {
                throw new BusinessException("userId is required");
            }
            effectiveUserId = request.userId();
        }
        validateDate(request.date());

        String normalizedCategory = normalizeCategory(request.category());
        if (!ALLOWED_CATEGORIES.contains(normalizedCategory)) {
            throw new BusinessException("Invalid category");
        }

        Expense e = expenseMapper.toEntity(request);
        e.setOrgId(orgId);
        e.setUserId(effectiveUserId);
        e.setCategory(normalizedCategory);
        e.setApprovalStatus(ExpenseApprovalStatus.PENDING_APPROVAL);
        e.setApprovedByUserId(null);
        e.setApprovedAt(null);
        e.setApprovalNote(null);
        e.setBillExtractedAmount(request.billExtractedAmount());
        e.setBillExtractionConfidence(normalizeConfidence(request.billExtractionConfidence()));
        e.setScrutinyLevel(
                computeScrutinyLevel(request.amount(), request.billExtractedAmount(), request.billExtractionConfidence())
        );
        return toResponseWithMeta(expenseRepository.save(e), null);
    }

    @Transactional
    public ExpenseResponse createWithInvoice(JwtPrincipal principal, ExpenseRequest request, MultipartFile file) {
        ExpenseResponse created = create(principal, request);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Invoice file is required");
        }
        uploadInvoice(created.id(), principal, file);
        return toResponseWithMeta(getEntity(created.id()), null);
    }

    @Transactional
    public ExpenseResponse update(UUID id, JwtPrincipal principal, ExpenseRequest request) {
        Expense e = getEntity(id);
        ensureCanEdit(principal, e);
        if (request.userId() == null && principal.role() != Role.SALES_OFFICER) {
            throw new BusinessException("userId is required");
        }

        UUID effectiveUserId = principal.role() == Role.SALES_OFFICER ? principal.userId() : request.userId();
        validateDate(request.date());

        String normalizedCategory = normalizeCategory(request.category());
        if (!ALLOWED_CATEGORIES.contains(normalizedCategory)) {
            throw new BusinessException("Invalid category");
        }

        expenseMapper.update(e, request);
        e.setUserId(effectiveUserId);
        e.setCategory(normalizedCategory);
        e.setApprovalStatus(ExpenseApprovalStatus.PENDING_APPROVAL);
        e.setApprovedByUserId(null);
        e.setApprovedAt(null);
        e.setApprovalNote(null);
        e.setBillExtractedAmount(request.billExtractedAmount());
        e.setBillExtractionConfidence(normalizeConfidence(request.billExtractionConfidence()));
        e.setScrutinyLevel(
                computeScrutinyLevel(request.amount(), request.billExtractedAmount(), request.billExtractionConfidence())
        );
        return toResponseWithMeta(expenseRepository.save(e), null);
    }

    @Transactional
    public ExpenseResponse approve(UUID id, JwtPrincipal principal, ExpenseDecisionRequest request) {
        Expense e = getEntity(id);
        if (e.getApprovalStatus() == ExpenseApprovalStatus.APPROVED) {
            throw new BusinessException("Expense is already approved");
        }
        e.setApprovalStatus(ExpenseApprovalStatus.APPROVED);
        e.setApprovedByUserId(principal.userId());
        e.setApprovedAt(Instant.now());
        e.setApprovalNote(cleanNote(request.note()));
        return toResponseWithMeta(expenseRepository.save(e), null);
    }

    @Transactional
    public ExpenseResponse reject(UUID id, JwtPrincipal principal, ExpenseDecisionRequest request) {
        Expense e = getEntity(id);
        if (e.getApprovalStatus() == ExpenseApprovalStatus.APPROVED) {
            throw new BusinessException("Approved expense cannot be rejected");
        }
        e.setApprovalStatus(ExpenseApprovalStatus.REJECTED);
        e.setApprovedByUserId(principal.userId());
        e.setApprovedAt(Instant.now());
        e.setApprovalNote(cleanNote(request.note()));
        return toResponseWithMeta(expenseRepository.save(e), null);
    }

    @Transactional
    public ExpenseInvoiceAttachmentResponse uploadInvoice(UUID expenseId, JwtPrincipal principal, MultipartFile file) {
        validateFile(file);
        Expense e = getEntity(expenseId);
        ensureCanEdit(principal, e);

        try {
            byte[] bytes = file.getBytes();
            String hash = sha256(bytes);
            String originalName = sanitizeFileName(file.getOriginalFilename());
            String contentType = normalizeContentType(file.getContentType(), originalName);
            Path root = invoiceRoot();
            Path folder = root.resolve(e.getOrgId().toString()).resolve(e.getId().toString());
            Files.createDirectories(folder);
            String storedName = System.currentTimeMillis() + "-" + originalName;
            Path target = folder.resolve(storedName);
            Files.copy(new java.io.ByteArrayInputStream(bytes), target, StandardCopyOption.REPLACE_EXISTING);

            ExpenseInvoiceAttachment att = new ExpenseInvoiceAttachment();
            att.setOrgId(e.getOrgId());
            att.setExpenseId(e.getId());
            att.setFileName(originalName);
            att.setContentType(contentType);
            att.setFileSizeBytes((long) bytes.length);
            att.setStoragePath(target.toString());
            att.setSha256(hash);
            att.setUploadedByUserId(principal.userId());
            ExpenseInvoiceAttachment saved = attachmentRepository.save(att);
            return new ExpenseInvoiceAttachmentResponse(
                    saved.getId(),
                    saved.getExpenseId(),
                    saved.getFileName(),
                    saved.getContentType(),
                    saved.getFileSizeBytes(),
                    saved.getSha256(),
                    saved.getCreatedAt()
            );
        } catch (IOException ex) {
            throw new BusinessException("Failed to save invoice");
        }
    }

    @Transactional(readOnly = true)
    public ExpenseInvoiceDownload downloadInvoice(UUID expenseId, JwtPrincipal principal) {
        Expense e = getEntity(expenseId);
        ensureCanRead(principal, e);
        UUID orgId = requireOrg();
        ExpenseInvoiceAttachment att = attachmentRepository.findFirstByOrgIdAndExpenseIdOrderByCreatedAtDesc(orgId, expenseId)
                .orElseThrow(() -> new NotFoundException("Invoice not found"));
        try {
            byte[] data = Files.readAllBytes(Path.of(att.getStoragePath()));
            return new ExpenseInvoiceDownload(att.getFileName(), att.getContentType(), data);
        } catch (IOException ex) {
            throw new BusinessException("Invoice file missing");
        }
    }

    @Transactional(readOnly = true)
    public ExpenseAmountExtractionResponse extractAmount(MultipartFile file) {
        validateFile(file);
        String text = "";
        try {
            String fileName = sanitizeFileName(file.getOriginalFilename());
            String contentType = normalizeContentType(file.getContentType(), fileName);
            if (contentType.equals("application/pdf")) {
                text = extractPdfText(file);
            } else {
                text = extractImageTextWithTesseract(file, fileName);
            }
        } catch (BusinessException ex) {
            return new ExpenseAmountExtractionResponse(null, "LOW", ex.getMessage());
        } catch (Exception ex) {
            return new ExpenseAmountExtractionResponse(null, "LOW", "Could not read invoice text");
        }

        Candidate best = pickBestAmountCandidate(text);
        if (best == null) {
            return new ExpenseAmountExtractionResponse(null, "LOW", "Amount not detected. Please enter manually.");
        }
        String confidence = best.score >= 70 ? "HIGH" : (best.score >= 45 ? "MEDIUM" : "LOW");
        return new ExpenseAmountExtractionResponse(best.amount, confidence, "Amount extracted from invoice");
    }

    private static void validateDate(LocalDate expenseDate) {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        if (expenseDate.isAfter(yesterday)) {
            throw new BusinessException("Expense date cannot be later than yesterday");
        }
    }

    private Expense getEntity(UUID id) {
        UUID orgId = requireOrg();
        return expenseRepository.findById(id)
                .filter(x -> x.getOrgId().equals(orgId))
                .orElseThrow(() -> new NotFoundException("Expense", id));
    }

    private static String normalizeCategory(String raw) {
        if (raw == null) return "";
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Invoice file is required");
        }
        if (file.getSize() > MAX_INVOICE_BYTES) {
            throw new BusinessException("Invoice is too large (max 10MB)");
        }
        String name = sanitizeFileName(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".pdf")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".png")
                || name.endsWith(".doc")
                || name.endsWith(".docx"))) {
            throw new BusinessException("Only PDF, JPG, JPEG, PNG, DOC and DOCX files are allowed");
        }
    }

    private static Path invoiceRoot() {
        String home = System.getProperty("user.home", ".");
        return Path.of(home, ".erp-saas", "invoices");
    }

    private static String sanitizeFileName(String original) {
        String fallback = "invoice";
        if (original == null || original.isBlank()) {
            return fallback;
        }
        String cleaned = original.replaceAll("[^a-zA-Z0-9._-]", "_");
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private static String normalizeContentType(String contentType, String fileName) {
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }
        if (lowerName.endsWith(".pdf")) return "application/pdf";
        if (lowerName.endsWith(".png")) return "image/png";
        if (lowerName.endsWith(".doc")) return "application/msword";
        if (lowerName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        return "image/jpeg";
    }

    private static String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new BusinessException("Failed to hash invoice");
        }
    }

    private static String extractPdfText(MultipartFile file) throws IOException {
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return Optional.ofNullable(stripper.getText(doc)).orElse("");
        }
    }

    private static String extractImageTextWithTesseract(MultipartFile file, String fileName) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("expense-ocr-");
        String suffix = fileName.toLowerCase(Locale.ROOT).endsWith(".png") ? ".png" : ".jpg";
        Path input = tempDir.resolve("input" + suffix);
        Path output = tempDir.resolve("out");
        Files.copy(file.getInputStream(), input, StandardCopyOption.REPLACE_EXISTING);

        Process process;
        try {
            process = new ProcessBuilder("tesseract", input.toString(), output.toString(), "--dpi", "300")
                    .redirectErrorStream(true)
                    .start();
        } catch (IOException ex) {
            throw new BusinessException("OCR engine not installed on server (tesseract missing)");
        }
        int code = process.waitFor();
        if (code != 0) {
            String outputText = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            throw new BusinessException("OCR failed: " + outputText);
        }
        Path txt = Path.of(output + ".txt");
        if (!Files.exists(txt)) {
            return "";
        }
        String text = Files.readString(txt);
        Files.deleteIfExists(input);
        Files.deleteIfExists(txt);
        Files.deleteIfExists(tempDir);
        return text;
    }

    private static Candidate pickBestAmountCandidate(String text) {
        if (text == null || text.isBlank()) return null;
        String[] lines = text.split("\\R");
        List<Candidate> found = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = AMOUNT_PATTERN.matcher(line);
            while (matcher.find()) {
                String raw = matcher.group(1).replace(",", "");
                BigDecimal amount;
                try {
                    amount = new BigDecimal(raw);
                } catch (Exception ex) {
                    continue;
                }
                if (amount.compareTo(BigDecimal.ONE) < 0 || amount.compareTo(new BigDecimal("10000000")) > 0) {
                    continue;
                }
                int score = 10;
                String lower = line.toLowerCase(Locale.ROOT);
                for (String kw : TOTAL_KEYWORDS) {
                    if (lower.contains(kw)) score += 40;
                }
                if (lower.contains("rs") || lower.contains("inr") || line.contains("₹")) score += 10;
                if (lower.matches(".*\\b(19|20)\\d{2}\\b.*")) score -= 20;
                if (i > (int) (lines.length * 0.6)) score += 10;
                found.add(new Candidate(amount, score));
            }
        }
        return found.stream().max(Comparator.comparingInt(c -> c.score)).orElse(null);
    }

    private void ensureCanRead(JwtPrincipal principal, Expense expense) {
        if (principal.role() == Role.ADMIN || principal.role() == Role.MANAGER) return;
        if (principal.userId().equals(expense.getUserId())) return;
        throw new BusinessException("You can only access your own expenses");
    }

    private void ensureCanEdit(JwtPrincipal principal, Expense expense) {
        if (expense.getApprovalStatus() == ExpenseApprovalStatus.APPROVED) {
            throw new BusinessException("Approved expenses cannot be edited");
        }
        if (principal.role() == Role.ADMIN || principal.role() == Role.MANAGER) return;
        if (principal.userId().equals(expense.getUserId())) return;
        throw new BusinessException("You can only edit your own expenses");
    }

    private static String cleanNote(String note) {
        if (note == null) return null;
        String v = note.trim();
        return v.isEmpty() ? null : v;
    }

    private static ExpenseScrutinyLevel computeScrutinyLevel(
            BigDecimal enteredAmount,
            BigDecimal extractedAmount,
            String extractionConfidence
    ) {
        if (extractedAmount == null) {
            return ExpenseScrutinyLevel.NO_BILL;
        }
        String conf = normalizeConfidence(extractionConfidence);
        if ("LOW".equals(conf) || conf == null) {
            return ExpenseScrutinyLevel.LOW_CONFIDENCE;
        }
        BigDecimal delta = enteredAmount.subtract(extractedAmount).abs();
        BigDecimal allowedDelta = enteredAmount.abs().multiply(new BigDecimal("0.01"));
        if (allowedDelta.compareTo(BigDecimal.ONE) < 0) {
            allowedDelta = BigDecimal.ONE;
        }
        if (delta.compareTo(allowedDelta) > 0) {
            return ExpenseScrutinyLevel.AMOUNT_MISMATCH;
        }
        return ExpenseScrutinyLevel.CLEAR;
    }

    private static String normalizeConfidence(String confidence) {
        if (confidence == null || confidence.isBlank()) return null;
        String v = confidence.trim().toUpperCase(Locale.ROOT);
        if (v.equals("HIGH") || v.equals("MEDIUM") || v.equals("LOW")) {
            return v;
        }
        return null;
    }

    private static UUID requireOrg() {
        UUID orgId = TenantContext.getOrgId();
        if (orgId == null) {
            throw new IllegalStateException("Tenant context missing");
        }
        return orgId;
    }

    private ExpenseResponse toResponseWithMeta(Expense expense, Map<UUID, String> userEmailById) {
        ExpenseResponse base = expenseMapper.toResponse(expense);
        boolean hasInvoice = attachmentRepository.existsByOrgIdAndExpenseId(expense.getOrgId(), expense.getId());
        String resolvedUser = userEmailById != null
                ? userEmailById.get(expense.getUserId())
                : userRepository.findById(expense.getUserId()).map(u -> u.getOrgId().equals(expense.getOrgId()) ? u.getEmail() : null).orElse(null);
        String userEmail = (resolvedUser != null && !resolvedUser.isBlank())
                ? resolvedUser
                : ("user-" + expense.getUserId().toString().substring(0, 8));
        return new ExpenseResponse(
                base.id(),
                base.orgId(),
                base.userId(),
                userEmail,
                base.amount(),
                base.category(),
                base.description(),
                base.date(),
                base.approvalStatus(),
                base.approvalNote(),
                base.approvedByUserId(),
                base.approvedAt(),
                base.billExtractedAmount(),
                base.billExtractionConfidence(),
                base.scrutinyLevel(),
                hasInvoice,
                base.createdAt(),
                base.updatedAt()
        );
    }

    private Map<UUID, String> buildUserEmailMap(UUID orgId) {
        return userRepository.findByOrgIdOrderByEmailAsc(orgId).stream()
                .collect(java.util.stream.Collectors.toMap(u -> u.getId(), u -> u.getEmail(), (a, b) -> a));
    }

    private record Candidate(BigDecimal amount, int score) {
    }
}
