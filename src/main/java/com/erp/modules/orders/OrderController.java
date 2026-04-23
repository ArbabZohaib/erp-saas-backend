package com.erp.modules.orders;

import com.erp.modules.orders.dto.OrderRequest;
import com.erp.modules.orders.dto.OrderResponse;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public List<OrderResponse> list(@RequestParam(required = false) UUID customerId) {
        if (customerId != null) {
            return orderService.listByCustomer(customerId);
        }
        return orderService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public OrderResponse get(@PathVariable UUID id) {
        return orderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','SALES_OFFICER')")
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }
}
