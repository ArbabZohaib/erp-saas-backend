package com.erp.modules.expenses;

import com.erp.core.security.JwtPrincipal;
import com.erp.modules.expenses.dto.ExpenseAmountExtractionResponse;
import com.erp.modules.expenses.dto.ExpenseDecisionRequest;
import com.erp.modules.expenses.dto.ExpenseInvoiceAttachmentResponse;
import com.erp.modules.expenses.dto.ExpenseInvoiceDownload;
import com.erp.modules.expenses.dto.ExpenseMonthlyComparisonResponse;
import com.erp.modules.expenses.dto.ExpenseRequest;
import com.erp.modules.expenses.dto.ExpenseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ObjectMapper objectMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<ExpenseResponse> list() {
        return expenseService.list();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public List<ExpenseResponse> listMine(@AuthenticationPrincipal JwtPrincipal principal) {
        return expenseService.listMine(principal.userId());
    }

    @GetMapping("/analytics/monthly-comparison")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ExpenseMonthlyComparisonResponse monthlyComparison(
            @RequestParam String month,
            @RequestParam(required = false) UUID userId
    ) {
        return expenseService.monthlyComparison(month, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public ExpenseResponse create(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ExpenseRequest request
    ) {
        return expenseService.create(principal, request);
    }

    @PostMapping(value = "/with-invoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public ExpenseResponse createWithInvoice(
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam("expense") String expenseJson,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        ExpenseRequest request = objectMapper.readValue(expenseJson, ExpenseRequest.class);
        return expenseService.createWithInvoice(principal, request, file);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public ExpenseResponse update(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ExpenseRequest request
    ) {
        return expenseService.update(id, principal, request);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ExpenseResponse approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody(required = false) ExpenseDecisionRequest request
    ) {
        return expenseService.approve(id, principal, request == null ? new ExpenseDecisionRequest(null) : request);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ExpenseResponse reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestBody(required = false) ExpenseDecisionRequest request
    ) {
        return expenseService.reject(id, principal, request == null ? new ExpenseDecisionRequest(null) : request);
    }

    @PostMapping(value = "/extract-amount", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public ExpenseAmountExtractionResponse extractAmount(@RequestParam("file") MultipartFile file) {
        return expenseService.extractAmount(file);
    }

    @PostMapping(value = "/{id}/invoice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public ExpenseInvoiceAttachmentResponse uploadInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal principal,
            @RequestParam("file") MultipartFile file
    ) {
        return expenseService.uploadInvoice(id, principal, file);
    }

    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public ResponseEntity<ByteArrayResource> downloadInvoice(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        ExpenseInvoiceDownload invoice = expenseService.downloadInvoice(id, principal);
        ByteArrayResource body = new ByteArrayResource(invoice.content());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(invoice.contentType()))
                .header("Content-Disposition", "attachment; filename=\"" + invoice.fileName() + "\"")
                .contentLength(invoice.content().length)
                .body(body);
    }
}
