package com.dev.tagashira.dto.request;

import com.dev.tagashira.constant.FeeTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeeCreateRequest {
    String name;
    String description;
    FeeTypeEnum feeTypeEnum;
    BigDecimal amount;
    BigDecimal unitPrice;
    Long apartmentId;
    Boolean isRecurring;
}
