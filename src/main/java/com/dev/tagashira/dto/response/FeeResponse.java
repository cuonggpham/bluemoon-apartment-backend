package com.dev.tagashira.dto.response;

import com.dev.tagashira.constant.FeeTypeEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class FeeResponse {
    Long id;
    String name;
    String description;
    FeeTypeEnum feeTypeEnum;
    BigDecimal amount;
    BigDecimal unitPrice;
    Long apartmentId;
    Boolean isRecurring;
    Boolean isActive;
    LocalDate effectiveFrom;
    LocalDate effectiveTo;
    LocalDate createdAt;
    LocalDate updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class FeeSummaryResponse {
        Long id;
        String name;
        FeeTypeEnum feeTypeEnum;
        BigDecimal amount;
        Long apartmentId;
        Boolean isRecurring;
    }
}
