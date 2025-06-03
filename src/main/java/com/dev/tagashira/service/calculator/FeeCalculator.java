package com.dev.tagashira.service.calculator;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.exception.NoVehicleException;

public interface FeeCalculator {
    boolean supports(FeeTypeEnum type);
    CalculationResult calculate(Apartment apt, FeeGenerationParam param) throws NoVehicleException;
}
