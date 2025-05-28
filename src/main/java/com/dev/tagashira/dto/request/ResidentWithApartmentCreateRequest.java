package com.dev.tagashira.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentWithApartmentCreateRequest {
    
    // Resident information
    @NotNull(message = "Resident ID is required")
    private Long id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Date of birth is required")
    private String dob;
    
    private String cic;
    
    @NotBlank(message = "Gender is required")
    private String gender;
    
    @Builder.Default
    private String status = "Resident";
    
    // Apartment option
    @NotBlank(message = "Apartment option is required")
    private String apartmentOption; // "existing" or "new"
    
    // For existing apartment
    private Long existingApartmentId;
    
    // For new apartment
    private Long apartmentAddressNumber;
    
    private Double apartmentArea;

    @Builder.Default
    private String apartmentStatus = "Residential";
    private Long ownerPhone;
}
