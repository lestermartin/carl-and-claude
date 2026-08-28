package com.carl.trading.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Editable profile fields. Username and password are intentionally absent - they cannot be changed.
 */
public record UpdateProfileRequest(
        @NotBlank @Size(max = 64) String firstName,
        @NotBlank @Size(max = 64) String lastName,
        @NotBlank @Pattern(regexp = "\\d{3}-\\d{2}-\\d{4}", message = "tax ID must look like 123-45-6789") String taxId,
        @NotBlank @Size(max = 128) String addressLine1,
        @Size(max = 128) String addressLine2,
        @NotBlank @Size(max = 64) String city,
        @NotBlank @Pattern(regexp = "(?i)[a-z]{2}", message = "state must be a 2-letter code") String state,
        @NotBlank @Pattern(regexp = "\\d{5}(-\\d{4})?", message = "postal code must be 12345 or 12345-6789") String postalCode) {
}
