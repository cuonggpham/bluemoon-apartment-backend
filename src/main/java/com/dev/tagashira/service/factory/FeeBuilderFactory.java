package com.dev.tagashira.service.factory;

import com.dev.tagashira.entity.Fee;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FeeBuilderFactory {

    public Fee.FeeBuilder base() {
        LocalDate now = LocalDate.now();
        return Fee.builder()
                .isActive(true)
                .isRecurring(false)
                .effectiveFrom(now)
                .createdAt(now);
    }

    public Fee.FeeBuilder monthly() {
        return base().isRecurring(true);
    }

    /** Dùng khi cần “clone” 1 fee để update soft-copy */
    public Fee.FeeBuilder from(Fee original) {
        return Fee.builder()
                .id(original.getId())
                .name(original.getName())
                .description(original.getDescription())
                .feeTypeEnum(original.getFeeTypeEnum())
                .amount(original.getAmount())
                .unitPrice(original.getUnitPrice())
                .apartmentId(original.getApartmentId())
                .isRecurring(original.getIsRecurring())
                .isActive(original.getIsActive())
                .effectiveFrom(original.getEffectiveFrom())
                .effectiveTo(original.getEffectiveTo())
                .createdAt(original.getCreatedAt());
    }
}
