package com.carl.trading.web;

import com.carl.trading.security.CurrentCustomer;
import com.carl.trading.service.ProfileService;
import com.carl.trading.web.dto.ProfileDto;
import com.carl.trading.web.dto.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final CurrentCustomer currentCustomer;

    public ProfileController(ProfileService profileService, CurrentCustomer currentCustomer) {
        this.profileService = profileService;
        this.currentCustomer = currentCustomer;
    }

    @GetMapping
    public ProfileDto get() {
        return profileService.get(currentCustomer.require());
    }

    @PutMapping
    public ProfileDto update(@Valid @RequestBody UpdateProfileRequest request) {
        return profileService.update(currentCustomer.require(), request);
    }
}
