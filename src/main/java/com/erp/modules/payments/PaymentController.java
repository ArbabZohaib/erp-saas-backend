package com.erp.modules.payments;

import com.erp.modules.payments.dto.PaymentRequest;
import com.erp.modules.payments.dto.PaymentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public List<PaymentResponse> list(@RequestParam UUID orderId) {
        return paymentService.listByOrder(orderId);
    }

    @GetMapping("/outstanding")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public BigDecimal outstanding(@RequestParam UUID customerId) {
        return paymentService.getOutstandingByCustomer(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public PaymentResponse create(@Valid @RequestBody PaymentRequest request) {
        return paymentService.record(request);
    }
}
