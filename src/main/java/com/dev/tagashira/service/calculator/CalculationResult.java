package com.dev.tagashira.service.calculator;

import java.math.BigDecimal;

public record CalculationResult(
        BigDecimal amount,      // tổng tiền
        BigDecimal unitPrice,   // đơn giá (nếu có)
        String description
) {}
