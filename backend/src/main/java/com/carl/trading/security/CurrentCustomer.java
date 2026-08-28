package com.carl.trading.security;

import com.carl.trading.mapper.CustomerMapper;
import com.carl.trading.model.Customer;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Resolves the {@link Customer} for the currently authenticated request. */
@Component
public class CurrentCustomer {

    private final CustomerMapper customerMapper;

    public CurrentCustomer(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    public Customer require() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        Customer customer = customerMapper.findByUsername(authentication.getName());
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown account");
        }
        return customer;
    }
}
