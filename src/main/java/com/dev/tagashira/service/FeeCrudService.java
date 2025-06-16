package com.dev.tagashira.service;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.dto.request.FeeCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.FeeResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Fee;
import com.dev.tagashira.exception.FeeNotFoundException;
import com.dev.tagashira.exception.ApartmentNotFoundException;
import com.dev.tagashira.exception.FeeUpdateRestrictedException;
import com.dev.tagashira.repository.FeeRepository;
import com.dev.tagashira.converter.FeeConverter;
import com.dev.tagashira.service.factory.FeeBuilderFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD thuần cho Fee – KHÔNG chứa logic tính tiền theo tháng.
 */
@Service
@RequiredArgsConstructor
public class FeeCrudService {

    private final FeeRepository feeRepository;
    private final FeeConverter feeConverter;
    private final FeeBuilderFactory feeBuilderFactory;
    private final com.dev.tagashira.repository.ApartmentRepository apartmentRepository;

    public PaginatedResponse<FeeResponse> findAll(Specification<Fee> spec, Pageable pageable) {
        Page<Fee> page = feeRepository.findAll(spec, pageable);

        PaginatedResponse<FeeResponse> result = new PaginatedResponse<>();
        result.setCurPage(pageable.getPageNumber());
        result.setPageSize(pageable.getPageSize());
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getNumberOfElements());
        result.setResult(feeConverter.toResponseList(page.getContent()));
        return result;
    }

    public List<FeeResponse> findAll(Specification<Fee> spec) {
        return feeConverter.toResponseList(feeRepository.findAll(spec));
    }

    public FeeResponse findById(Long id) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new FeeNotFoundException("Fee " + id + " not found"));
        return feeConverter.toResponse(fee);
    }

    @Transactional
    public FeeResponse create(FeeCreateRequest req) {
        Apartment apartment = apartmentRepository.findById(req.getApartmentId())
                .orElseThrow(() -> new ApartmentNotFoundException("Apartment " + req.getApartmentId() + " not found"));

        Fee.FeeBuilder builder = feeBuilderFactory.base()
                .name(req.getName())
                .description(req.getDescription())
                .feeTypeEnum(req.getFeeTypeEnum())
                .amount(req.getAmount())
                .unitPrice(req.getUnitPrice())
                .apartment(apartment)
                .isRecurring(Boolean.TRUE.equals(req.getIsRecurring()));

        Fee saved = feeRepository.save(builder.build());
        return feeConverter.toResponse(saved);
    }

    @Transactional
    public List<FeeResponse> createVoluntaryFeeForAllApartments(FeeCreateRequest req) {
        if (req.getFeeTypeEnum() != FeeTypeEnum.VOLUNTARY) {
            throw new IllegalArgumentException("This method is only for VOLUNTARY fees");
        }

        // Get all active apartments
        List<Apartment> apartments = apartmentRepository.findAll();
        List<Fee> createdFees = new ArrayList<>();

        for (Apartment apartment : apartments) {
            // Create fee name with apartment number
            String feeNameForApartment = req.getName() + " (Apartment: " + apartment.getAddressNumber() + ")";
            
            // Check if fee already exists for this apartment
            if (feeRepository.findByNameAndApartmentId(feeNameForApartment, apartment.getAddressNumber()).isPresent()) {
                continue; // Skip if already exists
            }

            Fee.FeeBuilder builder = feeBuilderFactory.base()
                    .name(feeNameForApartment)
                    .description(req.getDescription())
                    .feeTypeEnum(req.getFeeTypeEnum())
                    .amount(req.getAmount())
                    .unitPrice(req.getUnitPrice())
                    .apartment(apartment)
                    .isRecurring(Boolean.TRUE.equals(req.getIsRecurring()));

            Fee saved = feeRepository.save(builder.build());
            createdFees.add(saved);
        }

        return feeConverter.toResponseList(createdFees);
    }

    @Transactional
    public FeeResponse update(Fee updatePayload) {
        Fee existing = feeRepository.findById(updatePayload.getId())
                .orElseThrow(() -> new FeeNotFoundException("Fee " + updatePayload.getId() + " not found"));

        // Check if fee has payment record - if yes, prevent update
        if (existing.isPaid()) {
            throw new FeeUpdateRestrictedException(
                "Cannot update fee '" + existing.getName() + "' (ID: " + existing.getId() + 
                ") because it has already been paid. Fee updates are not allowed once payment has been recorded."
            );
        }

        Apartment apartmentToUpdate = existing.getApartment();
        if (updatePayload.getApartmentId() != null && 
            !updatePayload.getApartmentId().equals(existing.getApartmentNumber())) {
            apartmentToUpdate = apartmentRepository.findById(updatePayload.getApartmentId())
                    .orElseThrow(() -> new ApartmentNotFoundException("Apartment " + updatePayload.getApartmentId() + " not found"));
        }

        Fee updated = feeBuilderFactory.from(existing)
                .name(nvl(updatePayload.getName(), existing.getName()))
                .description(nvl(updatePayload.getDescription(), existing.getDescription()))
                .feeTypeEnum(nvl(updatePayload.getFeeTypeEnum(), existing.getFeeTypeEnum()))
                .amount(nvl(updatePayload.getAmount(), existing.getAmount()))
                .unitPrice(nvl(updatePayload.getUnitPrice(), existing.getUnitPrice()))
                .apartment(apartmentToUpdate)
                .isRecurring(nvl(updatePayload.getIsRecurring(), existing.getIsRecurring()))
                .isActive(nvl(updatePayload.getIsActive(), existing.getIsActive()))
                .effectiveFrom(nvl(updatePayload.getEffectiveFrom(), existing.getEffectiveFrom()))
                .effectiveTo(nvl(updatePayload.getEffectiveTo(), existing.getEffectiveTo()))
                .updatedAt(LocalDate.now())
                .build();

        return feeConverter.toResponse(feeRepository.save(updated));
    }

    @Transactional
    public ApiResponse<String> delete(Long id) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new FeeNotFoundException("Fee " + id + " not found"));
        
        // Check if fee has payment record - if yes, prevent deletion
        if (fee.isPaid()) {
            throw new FeeUpdateRestrictedException(
                "Cannot delete fee '" + fee.getName() + "' (ID: " + fee.getId() + 
                ") because it has already been paid. Fee deletion is not allowed once payment has been recorded."
            );
        }
        
        feeRepository.delete(fee);

        ApiResponse<String> res = new ApiResponse<>();
        res.setCode(HttpStatus.OK.value());
        res.setMessage("Delete fee success");
        return res;
    }

    @Transactional
    public void deactivate(Long id) {
        Fee fee = feeRepository.findById(id)
                .orElseThrow(() -> new FeeNotFoundException("Fee " + id + " not found"));

        // Check if fee has payment record - if yes, prevent deactivation
        if (fee.isPaid()) {
            throw new FeeUpdateRestrictedException(
                "Cannot deactivate fee '" + fee.getName() + "' (ID: " + fee.getId() + 
                ") because it has already been paid. Fee deactivation is not allowed once payment has been recorded."
            );
        }

        Fee deactivated = feeBuilderFactory.from(fee)
                .isActive(false)
                .effectiveTo(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        feeRepository.save(deactivated);
    }

    public List<FeeResponse> getFeesByApartment(Long apartmentId) {
        List<Fee> fees = feeRepository.findByApartmentId(apartmentId);
        return feeConverter.toResponseList(fees);
    }

    public List<FeeResponse> getUnpaidFeesByApartment(Long apartmentId) {
        List<Fee> fees = feeRepository.findUnpaidFeesByApartmentId(apartmentId);
        return feeConverter.toResponseList(fees);
    }

    private static <T> T nvl(T value, T fallback) {
        return value != null ? value : fallback;
    }
}
