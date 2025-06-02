package com.dev.tagashira.service;

import com.dev.tagashira.dto.request.FeeCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.dto.response.FeeResponse;
import com.dev.tagashira.entity.Fee;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.constant.VehicleEnum;
import com.dev.tagashira.exception.FeeNotFoundException;
import com.dev.tagashira.repository.FeeRepository;
import com.dev.tagashira.repository.VehicleRepository;
import com.dev.tagashira.repository.ApartmentRepository;
import com.dev.tagashira.converter.FeeConverter;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Service
@AllArgsConstructor
public class FeeService {
    private final FeeRepository feeRepository;
    private final VehicleRepository vehicleRepository;
    private final VehiclePriceSettingService vehiclePriceSettingService;
    private final ApartmentRepository apartmentRepository;
    private final FeeConverter feeConverter;

    public PaginatedResponse<FeeResponse> fetchAllFees(Specification<Fee> spec, Pageable pageable) {
        Page<Fee> pageFee = feeRepository.findAll(spec, pageable);
        PaginatedResponse<FeeResponse> page = new PaginatedResponse<>();
        page.setPageSize(pageable.getPageSize());
        page.setCurPage(pageable.getPageNumber());
        page.setTotalPages(pageFee.getTotalPages());
        page.setTotalElements(pageFee.getNumberOfElements());
        page.setResult(feeConverter.toResponseList(pageFee.getContent()));
        return page;
    }
    
    public Fee fetchFeeById (Long id) {
        return feeRepository.findById(id).orElseThrow(() -> new FeeNotFoundException("Fee with code = " + id + " is not found"));
    }

    public FeeResponse getFeeResponseById(Long id) {
        Fee fee = fetchFeeById(id);
        return feeConverter.toResponse(fee);
    }
    
    /**
     * Tạo Fee builder từ FeeCreateRequest
     */
    private Fee.FeeBuilder createFeeFromRequest(FeeCreateRequest request) {
        return createBaseFeeBuilder()
            .name(request.getName())
            .description(request.getDescription())
            .feeTypeEnum(request.getFeeTypeEnum())
            .amount(request.getAmount())
            .unitPrice(request.getUnitPrice())
            .apartmentId(request.getApartmentId())
            .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);
    }    
    
    public FeeResponse createFee (FeeCreateRequest feeCreateRequest) {
        Fee fee = createFeeFromRequest(feeCreateRequest).build();
        Fee savedFee = feeRepository.save(fee);
        return feeConverter.toResponse(savedFee);
    }
    
    public FeeResponse updateFee (Fee fee) {
        Fee oldFee = this.fetchFeeById(fee.getId());
        if(oldFee != null) {
            Fee updatedFee = copyFeeToBuilder(oldFee)
                .name(fee.getName() != null ? fee.getName() : oldFee.getName())
                .description(fee.getDescription() != null ? fee.getDescription() : oldFee.getDescription())
                .feeTypeEnum(fee.getFeeTypeEnum() != null ? fee.getFeeTypeEnum() : oldFee.getFeeTypeEnum())
                .amount(fee.getAmount() != null ? fee.getAmount() : oldFee.getAmount())
                .unitPrice(fee.getUnitPrice() != null ? fee.getUnitPrice() : oldFee.getUnitPrice())
                .apartmentId(fee.getApartmentId() != null ? fee.getApartmentId() : oldFee.getApartmentId())
                .isRecurring(fee.getIsRecurring() != null ? fee.getIsRecurring() : oldFee.getIsRecurring())
                .isActive(fee.getIsActive() != null ? fee.getIsActive() : oldFee.getIsActive())
                .effectiveFrom(fee.getEffectiveFrom() != null ? fee.getEffectiveFrom() : oldFee.getEffectiveFrom())
                .effectiveTo(fee.getEffectiveTo() != null ? fee.getEffectiveTo() : oldFee.getEffectiveTo())
                .updatedAt(LocalDate.now()) // Set current update date
                .build();
                
            Fee savedFee = this.feeRepository.save(updatedFee);
            return feeConverter.toResponse(savedFee);
        } else {
            throw new FeeNotFoundException("Fee with code = " + fee.getId() + " is not found");
        }
    }

    //No exception handling is needed in this method
    public ApiResponse<String> deleteFee(Long id) {
       Fee fee = this.fetchFeeById(id);
       this.feeRepository.delete(fee);

       ApiResponse<String> response = new ApiResponse<>();
       response.setCode(HttpStatus.OK.value());
       response.setMessage("delete fee success");
       response.setData(null);
       return response;
    }
    

    /**
     * Tạo phí hàng tháng cho tất cả apartment theo loại phí
     */
    @Transactional
    public List<Fee> generateMonthlyFeesForAllApartments(FeeTypeEnum feeType, String billingMonth, 
                                                        BigDecimal unitPricePerSqm) {
        List<Apartment> apartments = apartmentRepository.findAll();
        List<Fee> generatedFees = new ArrayList<>();
        
        for (Apartment apartment : apartments) {
            Fee fee = generateMonthlyFeeForApartment(apartment, feeType, billingMonth, unitPricePerSqm);
            if (fee != null) {
                generatedFees.add(fee);
            }
        }
        
        return generatedFees;
    }
    
    /**
     * Tạo phí hàng tháng cho một apartment cụ thể
     */
    @Transactional
    public Fee generateMonthlyFeeForApartment(Apartment apartment, FeeTypeEnum feeType, 
                                            String billingMonth, BigDecimal unitPricePerSqm) {
        
        // Kiểm tra đã tạo phí cho apartment này trong tháng này chưa
        String feeName = generateFeeName(feeType, billingMonth);
        if (feeRepository.findByNameAndApartmentId(feeName, apartment.getAddressNumber()).isPresent()) {
            throw new RuntimeException("Fee already exists for apartment " + apartment.getAddressNumber() + " in " + billingMonth);
        }
        
        BigDecimal calculatedAmount;
        BigDecimal unitPrice;
        String description;
        
        switch (feeType) {
            case VEHICLE_PARKING -> {
                // Lấy tất cả loại xe và tính tổng
                calculatedAmount = BigDecimal.ZERO;
                StringBuilder descBuilder = new StringBuilder("Phí gửi xe tháng " + billingMonth + ": ");
                
                for (VehicleEnum vehicleType : VehicleEnum.values()) {
                    Long vehicleCount = vehicleRepository.countByApartmentAndVehicleType(apartment.getAddressNumber(), vehicleType);
                    if (vehicleCount > 0) {
                        BigDecimal pricePerVehicle = vehiclePriceSettingService.getPriceForVehicle(vehicleType);
                        BigDecimal vehicleAmount = pricePerVehicle.multiply(BigDecimal.valueOf(vehicleCount));
                        calculatedAmount = calculatedAmount.add(vehicleAmount);
                        
                        descBuilder.append(String.format("%s: %d xe x %s VNĐ = %s VNĐ; ", 
                            getVehicleTypeInVietnamese(vehicleType), vehicleCount, pricePerVehicle, vehicleAmount));
                    }
                }
                
                description = descBuilder.toString();
                
                // Nếu không có xe nào, không tạo fee
                if (calculatedAmount.compareTo(BigDecimal.ZERO) == 0) {
                    return null;
                }
                
                // Set unitPrice as average price per vehicle for reference
                unitPrice = vehiclePriceSettingService.getPriceForVehicle(VehicleEnum.Motorbike); // Default reference
            }
            case FLOOR_AREA -> {
                if (unitPricePerSqm == null || unitPricePerSqm.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Unit price per sqm is required for floor area fees");
                }
                
                BigDecimal apartmentArea = BigDecimal.valueOf(apartment.getArea());
                calculatedAmount = unitPricePerSqm.multiply(apartmentArea);
                unitPrice = unitPricePerSqm; // Store unit price for reference
                description = String.format("Phí diện tích sàn tháng %s: %.2f m² x %s VNĐ/m² = %s VNĐ", 
                    billingMonth, apartmentArea.doubleValue(), unitPricePerSqm, calculatedAmount);
            }
            default -> throw new IllegalArgumentException("Unsupported monthly fee type: " + feeType);
        }
        
        // Tạo Fee record cụ thể cho apartment này
        Fee fee = createMonthlyFeeBuilder()
            .name(feeName)
            .description(description)
            .feeTypeEnum(feeType) // Use unified enum
            .amount(calculatedAmount) // Amount đã được tính toán cụ thể
            .unitPrice(unitPrice) // Store unit price for reference
            .apartmentId(apartment.getAddressNumber()) // Gắn với apartment cụ thể
            .build();
        
        return feeRepository.save(fee);
    }
    
    /**
     * Lấy tất cả phí hàng tháng chưa thanh toán của một apartment
     */
    public List<Fee> getUnpaidMonthlyFeesByApartment(Long apartmentId) {
        return feeRepository.findByApartmentIdAndIsRecurringTrueAndIsActiveTrueOrderByCreatedAtDesc(apartmentId);
    }
    
    /**
     * Lấy tất cả phí hàng tháng theo tháng
     */
    public List<Fee> getMonthlyFeesByMonth(String billingMonth) {
        return feeRepository.findByNameContainingAndIsRecurringTrueOrderByApartmentIdAsc(billingMonth);
    }
    
    /**
     * Vô hiệu hóa phí (khi đã thanh toán hoặc hủy)
     */
    public void deactivateFee(Long feeId) {
        Fee fee = fetchFeeById(feeId);
        
        Fee deactivatedFee = copyFeeToBuilder(fee)
            .isActive(false) // Deactivate the fee
            .effectiveTo(LocalDate.now()) // Set end date to now
            .updatedAt(LocalDate.now())
            .build();
            
        feeRepository.save(deactivatedFee);
    }
    

    /**
     * Tạo Fee builder với các giá trị mặc định
     */
    private Fee.FeeBuilder createBaseFeeBuilder() {
        return Fee.builder()
            .isActive(true)
            .isRecurring(false)
            .effectiveFrom(LocalDate.now())
            .createdAt(LocalDate.now());
    }
    
    /**
     * Tạo Fee builder cho monthly fee với các giá trị mặc định
     */
    private Fee.FeeBuilder createMonthlyFeeBuilder() {
        return createBaseFeeBuilder()
            .isRecurring(true);
    }
    
    /**
     * Copy tất cả fields từ Fee cũ sang builder mới
     */
    private Fee.FeeBuilder copyFeeToBuilder(Fee originalFee) {
        return Fee.builder()
            .id(originalFee.getId())
            .name(originalFee.getName())
            .description(originalFee.getDescription())
            .feeTypeEnum(originalFee.getFeeTypeEnum())
            .amount(originalFee.getAmount())
            .unitPrice(originalFee.getUnitPrice())
            .apartmentId(originalFee.getApartmentId())
            .isRecurring(originalFee.getIsRecurring())
            .isActive(originalFee.getIsActive())
            .effectiveFrom(originalFee.getEffectiveFrom())
            .effectiveTo(originalFee.getEffectiveTo())
            .createdAt(originalFee.getCreatedAt());
    }
    
    private String generateFeeName(FeeTypeEnum feeType, String billingMonth) {
        return switch (feeType) {
            case VEHICLE_PARKING -> "Phí gửi xe tháng " + billingMonth;
            case FLOOR_AREA -> "Phí diện tích sàn tháng " + billingMonth;
            case MANAGEMENT_FEE -> "Phí quản lý tháng " + billingMonth;
            case MAINTENANCE_FEE -> "Phí bảo trì tháng " + billingMonth;
            case SECURITY_FEE -> "Phí bảo vệ tháng " + billingMonth;
            case CLEANING_FEE -> "Phí vệ sinh tháng " + billingMonth;
            default -> feeType.name() + " tháng " + billingMonth;
        };
    }
    
    private String getVehicleTypeInVietnamese(VehicleEnum vehicleType) {
        return switch (vehicleType) {
            case Motorbike -> "Xe máy";
            case Car -> "Ô tô";
        };
    }
}


