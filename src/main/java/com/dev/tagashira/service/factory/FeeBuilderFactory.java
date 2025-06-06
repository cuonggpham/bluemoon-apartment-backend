package com.dev.tagashira.service.factory;

import com.dev.tagashira.entity.Apartment;
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

    /** Dùng khi cần "clone" 1 fee để update soft-copy */
    public Fee.FeeBuilder from(Fee original) {
        return Fee.builder()
                .id(original.getId())
                .name(original.getName())
                .description(original.getDescription())
                .feeTypeEnum(original.getFeeTypeEnum())
                .amount(original.getAmount())
                .unitPrice(original.getUnitPrice())
                .apartment(original.getApartment())
                .isRecurring(original.getIsRecurring())
                .isActive(original.getIsActive())
                .effectiveFrom(original.getEffectiveFrom())
                .effectiveTo(original.getEffectiveTo())
                .createdAt(original.getCreatedAt());
    }
    
    // Thêm convenience method để tạo Fee với Apartment entity
    public Fee.FeeBuilder withApartment(Apartment apartment) {
        return base().apartment(apartment);
    }
    
    // Thêm convenience method để tạo monthly fee với Apartment entity
    public Fee.FeeBuilder monthlyWithApartment(Apartment apartment) {
        return monthly().apartment(apartment);
    }
}
