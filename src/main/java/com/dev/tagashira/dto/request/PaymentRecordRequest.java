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
    Long payerId;           // ID người thanh toán
    Long feeId;             // ID khoản phí
    LocalDate paymentDate;  // Ngày thanh toán
    BigDecimal amount;      // Số tiền thanh toán
    String notes;           // Ghi chú
} 