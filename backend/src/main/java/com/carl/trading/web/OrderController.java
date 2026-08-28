package com.carl.trading.web;

import com.carl.trading.security.CurrentCustomer;
import com.carl.trading.service.OrderService;
import com.carl.trading.web.dto.OrderRequest;
import com.carl.trading.web.dto.OrderResultDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CurrentCustomer currentCustomer;

    public OrderController(OrderService orderService, CurrentCustomer currentCustomer) {
        this.orderService = orderService;
        this.currentCustomer = currentCustomer;
    }

    /**
     * Places a buy or sell order. Note a {@code 200} response with {@code status == "REJECTED"}
     * means the order was recorded in the transaction log but not executed (unfavorable limit,
     * insufficient cash, or insufficient shares).
     */
    @PostMapping
    public OrderResultDto place(@Valid @RequestBody OrderRequest request) {
        return orderService.place(currentCustomer.require(), request);
    }
}
