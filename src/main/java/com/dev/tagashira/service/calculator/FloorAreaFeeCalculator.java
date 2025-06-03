package com.dev.tagashira.service.calculator;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.entity.Apartment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
class FloorAreaFeeCalculator implements FeeCalculator {

    @Override
    public boolean supports(FeeTypeEnum type) {
        return type == FeeTypeEnum.FLOOR_AREA;
    }

    @Override
    public CalculationResult calculate(Apartment apt, FeeGenerationParam p) {
        BigDecimal area = BigDecimal.valueOf(apt.getArea());
        BigDecimal amount = p.unitPricePerSqm().multiply(area);

        String base = (p.customFeeName() == null || p.customFeeName().isBlank())
                ? "Phí diện tích sàn" : p.customFeeName() + " for " + apt.getAddressNumber();

        String desc = "%s tháng %s: %.2f m² x %s = %s"
                .formatted(base + " (" + apt.getAddressNumber() + ")",
                        p.billingMonth(), area, p.unitPricePerSqm(), amount);

        return new CalculationResult(amount, p.unitPricePerSqm(), desc);
    }
}
