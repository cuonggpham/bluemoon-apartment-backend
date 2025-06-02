package com.dev.tagashira.converter;

import com.dev.tagashira.dto.response.FeeResponse;
import com.dev.tagashira.entity.Fee;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeeConverter {

    public FeeResponse toResponse(Fee fee) {
        if (fee == null) {
            return null;
        }

        return FeeResponse.builder()
            .id(fee.getId())
            .name(fee.getName())
            .description(fee.getDescription())
            .feeTypeEnum(fee.getFeeTypeEnum())
            .amount(fee.getAmount())
            .unitPrice(fee.getUnitPrice())
            .apartmentId(fee.getApartmentId())
            .isRecurring(fee.getIsRecurring())
            .isActive(fee.getIsActive())
            .effectiveFrom(fee.getEffectiveFrom())
            .effectiveTo(fee.getEffectiveTo())
            .createdAt(fee.getCreatedAt())
            .updatedAt(fee.getUpdatedAt())
            .build();
    }

    public List<FeeResponse> toResponseList(List<Fee> fees) {
        if (fees == null) {
            return null;
        }
        return fees.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Convert to summary response for payment records
     */
    public FeeResponse.FeeSummaryResponse toSummaryResponse(Fee fee) {
        if (fee == null) {
            return null;
        }

        return FeeResponse.FeeSummaryResponse.builder()
            .id(fee.getId())
            .name(fee.getName())
            .feeTypeEnum(fee.getFeeTypeEnum())
            .amount(fee.getAmount())
            .apartmentId(fee.getApartmentId())
            .isRecurring(fee.getIsRecurring())
            .build();
    }
} 