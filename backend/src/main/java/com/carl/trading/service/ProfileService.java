package com.carl.trading.service;

import com.carl.trading.mapper.CustomerMapper;
import com.carl.trading.model.Customer;
import com.carl.trading.web.dto.ProfileDto;
import com.carl.trading.web.dto.UpdateProfileRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final CustomerMapper customerMapper;

    public ProfileService(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    public ProfileDto get(Customer customer) {
        return ProfileDto.from(customer);
    }

    @Transactional
    public ProfileDto update(Customer customer, UpdateProfileRequest request) {
        UpdateProfileRequest normalized = new UpdateProfileRequest(
                request.firstName().trim(), request.lastName().trim(), request.taxId().trim(),
                request.addressLine1().trim(),
                request.addressLine2() == null || request.addressLine2().isBlank()
                        ? null : request.addressLine2().trim(),
                request.city().trim(), request.state().trim().toUpperCase(), request.postalCode().trim());
        customerMapper.updateProfile(customer.id(), normalized);
        return ProfileDto.from(customerMapper.findById(customer.id()));
    }
}
