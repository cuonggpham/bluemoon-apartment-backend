package com.dev.tagashira.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRecordRequest {
    Long payerId;
    Long feeId;
    Long apartmentId;
    LocalDate paymentDate;
    BigDecimal amount;
    String notes;
} 