package com.dev.tagashira.service.calculator;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.constant.VehicleEnum;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Vehicle;
import com.dev.tagashira.exception.NoVehicleException;
import com.dev.tagashira.repository.VehicleRepository;
import com.dev.tagashira.service.VehiclePriceSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class VehicleParkingFeeCalculator implements FeeCalculator{

    private final VehicleRepository vehicleRepo;
    private final VehiclePriceSettingService priceSettingSvc;
    @Override
    public boolean supports(FeeTypeEnum type) {
        return type == FeeTypeEnum.VEHICLE_PARKING;
    }

    @Override
    public CalculationResult calculate(Apartment apt, FeeGenerationParam param) throws NoVehicleException {

        BigDecimal totalFee = BigDecimal.ZERO;
        StringBuilder desc = new StringBuilder(
                "Phí gửi xe tháng %s cho căn %s: ".formatted(param.billingMonth(), apt.getAddressNumber())
        );

        for (VehicleEnum v: VehicleEnum.values()){
            long count = vehicleRepo.countByApartmentAndVehicleType(apt.getAddressNumber(), v);
            if (count == 0) {
                continue; // Không có xe loại này thì bỏ qua
            }

            BigDecimal priceEach = priceSettingSvc.getPriceForVehicle(v);
            BigDecimal sub = priceEach.multiply(BigDecimal.valueOf(count));
            totalFee = totalFee.add(sub);

            desc.append("%s: %d x %s = %s; "
                    .formatted(toVn(v), count, priceEach, sub));
        }

        if (totalFee.compareTo(BigDecimal.ZERO) == 0)
            throw new NoVehicleException(apt.getAddressNumber());

        return new CalculationResult(
                totalFee,
                null, // Không có đơn giá riêng cho từng loại xe
                desc.toString()
        );
    }

    private String toVn(VehicleEnum v) { return v == VehicleEnum.Car ? "Ô tô" : "Xe máy"; }
}
