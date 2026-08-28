package com.carl.trading.service;

import com.carl.trading.mapper.CustomerMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.security.JwtService;
import com.carl.trading.web.dto.LoginRequest;
import com.carl.trading.web.dto.LoginResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CustomerMapper customerMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Customer customer = customerMapper.findByUsername(request.username());
        if (customer == null || !passwordEncoder.matches(request.password(), customer.passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
        String token = jwtService.issue(customer.username());
        return new LoginResponse(token, customer.username(),
                customer.firstName() + " " + customer.lastName());
    }
}
