package com.dev.tagashira.service.factory;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.service.calculator.FeeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FeeCalculatorFactory {

    /** Spring inject toàn bộ bean implement FeeCalculator */
    private final List<FeeCalculator> calculators;

    public FeeCalculator getCalculator(FeeTypeEnum type) {
        return calculators.stream()
                .filter(c -> c.supports(type))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported fee type: " + type));
    }
}
