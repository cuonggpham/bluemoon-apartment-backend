package com.dev.tagashira.service;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Fee;
import com.dev.tagashira.exception.DuplicateFeeException;
import com.dev.tagashira.exception.NoVehicleException;
import com.dev.tagashira.repository.ApartmentRepository;
import com.dev.tagashira.repository.FeeRepository;
import com.dev.tagashira.service.calculator.CalculationResult;
import com.dev.tagashira.service.calculator.FeeCalculator;
import com.dev.tagashira.service.calculator.FeeGenerationParam;
import com.dev.tagashira.service.factory.FeeBuilderFactory;
import com.dev.tagashira.service.factory.FeeCalculatorFactory;
import com.dev.tagashira.service.factory.FeeNameFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Use-case: sinh phí định kỳ (hàng tháng) cho 1 căn hộ hoặc toàn bộ chung cư.
 *
 * <p>Lớp này KHÔNG chứa công thức tính tiền – việc đó đã được
 * <b>FeeCalculator</b> delegation theo Strategy.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyFeeGeneratorService {

    private final ApartmentRepository apartmentRepo;
    private final FeeRepository feeRepo;
    private final FeeCalculatorFactory calculatorFactory;
    private final FeeNameFactory feeNameFactory;
    private final FeeBuilderFactory feeBuilderFactory;

    /**
     * Sinh phí hàng tháng cho <b>toàn bộ</b> căn hộ.
     *
     * @param feeType        loại phí cần sinh
     * @param billingMonth   tháng tính phí (YearMonth: 2025-06)
     * @param unitPricePerSqm đơn giá/m² (chỉ dùng cho FLOOR_AREA, có thể null)
     * @param customName     tên phí tuỳ chỉnh (nullable)
     * @return danh sách Fee được ghi vào DB
     */
    public List<Fee> generateForAll(FeeTypeEnum feeType,
                                    YearMonth billingMonth,
                                    BigDecimal unitPricePerSqm,
                                    String customName) {

        List<Apartment> apartments = apartmentRepo.findAll();
        List<Fee> results = new ArrayList<>();
        FeeGenerationParam param = new FeeGenerationParam(billingMonth, unitPricePerSqm, customName);

        FeeCalculator calculator = calculatorFactory.getCalculator(feeType);

        for (Apartment apt : apartments) {
            try {
                Fee fee = generateForApartmentInternal(apt, feeType, param, calculator);
                results.add(fee);
            } catch (DuplicateFeeException e) {
                // Skip apartment that already has this fee - continue with next apartment
                System.out.printf("Skipping apartment %d: Fee already exists - %s%n", 
                    apt.getAddressNumber(), e.getMessage());
            } catch (NoVehicleException e) {
                // Skip apartment with no vehicles - continue with next apartment  
                System.out.printf("Skipping apartment %d: No vehicles found - %s%n",
                    apt.getAddressNumber(), e.getMessage());
            }
        }
        return results;
    }

    /**
     * Sinh phí hàng tháng cho <b>một</b> căn hộ cụ thể.
     */
    public Fee generateForApartment(Long apartmentId,
                                    FeeTypeEnum feeType,
                                    YearMonth billingMonth,
                                    BigDecimal unitPricePerSqm,
                                    String customName) throws DuplicateFeeException, NoVehicleException {

        Apartment apartment = apartmentRepo.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found: " + apartmentId));

        FeeGenerationParam param = new FeeGenerationParam(billingMonth, unitPricePerSqm, customName);
        FeeCalculator calculator = calculatorFactory.getCalculator(feeType);

        return generateForApartmentInternal(apartment, feeType, param, calculator);
    }

    private Fee generateForApartmentInternal(Apartment apt,
                                             FeeTypeEnum feeType,
                                             FeeGenerationParam param,
                                             FeeCalculator calculator) throws DuplicateFeeException, NoVehicleException {

        String feeName = feeNameFactory.build(feeType, param.billingMonth(), param.customFeeName(), apt);

        // Check if fee already exists for this apartment
        if (feeRepo.findByNameAndApartmentId(feeName, apt.getAddressNumber()).isPresent()) {
            throw new DuplicateFeeException(feeName, apt.getAddressNumber());
        }

        // Calculate fee amount using calculator
        CalculationResult calc = calculator.calculate(apt, param);

        Fee fee = feeBuilderFactory.monthlyWithApartment(apt)
                .name(feeName)
                .description(calc.description())
                .feeTypeEnum(feeType)
                .amount(calc.amount())
                .unitPrice(calc.unitPrice())
                .build();

        return feeRepo.save(fee);
    }

    /**
     * Lấy tất cả phí hàng tháng chưa thanh toán của một apartment
     */
    public List<Fee> getUnpaidMonthlyFeesByApartment(Long apartmentId) {
        return feeRepo.findByApartmentIdAndIsRecurringTrueAndIsActiveTrueOrderByCreatedAtDesc(apartmentId);
    }

    /**
     * Lấy tất cả phí hàng tháng theo tháng
     */
    public List<Fee> getMonthlyFeesByMonth(String billingMonth) {
        return feeRepo.findByNameContainingAndIsRecurringTrueOrderByApartmentIdAsc(billingMonth);
    }
}
