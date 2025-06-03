package com.dev.tagashira.service.calculator;

import java.math.BigDecimal;
import java.time.YearMonth;

public record FeeGenerationParam(
        YearMonth billingMonth,
        BigDecimal unitPricePerSqm, // chỉ dùng cho FLOOR_AREA
        String customFeeName
) {}