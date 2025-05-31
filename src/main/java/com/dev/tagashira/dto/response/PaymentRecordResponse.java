package com.dev.tagashira.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRecordResponse {
    Long id;
    Long payerId;
    String payerName;
    Long feeId;
    String feeName;
    Long apartmentId;
    String apartmentNumber;
    LocalDate paymentDate;
    BigDecimal amount;
    String notes;
    Instant createdAt;
    Instant updatedAt;
} 