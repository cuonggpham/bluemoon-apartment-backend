package com.dev.tagashira.service;

import com.dev.tagashira.constant.PaymentEnum;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.UtilityBill;
import com.dev.tagashira.exception.ApartmentNotFoundException;
import com.dev.tagashira.exception.FileProcessingException;
import com.dev.tagashira.exception.UtilityBillNotFoundException;
import com.dev.tagashira.repository.ApartmentRepository;
import com.dev.tagashira.repository.UtilityBillRepository;
import com.dev.tagashira.util.FileParsingUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UtilityBillService {
    UtilityBillRepository utilityBillRepository;
    ApartmentRepository apartmentRepository;

    public List<UtilityBill> importExcel(MultipartFile file, String name) {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new FileProcessingException("Filename is null");
        }

        if (file.isEmpty()) {
            throw new FileProcessingException("Uploaded file is empty");
        }

        try {
            List<UtilityBill> bills;
            if (filename.toLowerCase().endsWith(".csv")) {
                try (InputStream in = file.getInputStream()) {
                    bills = FileParsingUtil.parseCSV(in, name);
                }
            } else if (filename.toLowerCase().matches(".*\\.(xls|xlsx)$")) {
                bills = FileParsingUtil.parseExcel(file, name);
            } else {
                throw new FileProcessingException("Unsupported file type: " + filename);
            }

            // Associate apartments with bills
            associateApartments(bills);
            
            utilityBillRepository.saveAll(bills);
            return bills;
        } catch (IOException e) {
            throw new FileProcessingException("I/O error reading file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }    
    
    private void associateApartments(List<UtilityBill> bills) {
        // Bulk-fetch apartments to avoid N+1
        Set<Long> apartmentIds = bills.stream()
                .map(UtilityBill::getApartmentId)
                .collect(Collectors.toSet());
        
        List<Apartment> foundApartments = apartmentRepository.findAllById(apartmentIds);
        Map<Long, Apartment> apartments = foundApartments.stream()
                .collect(Collectors.toMap(Apartment::getAddressNumber, Function.identity()));
                
        for (UtilityBill bill : bills) {
            Apartment apt = apartments.get(bill.getApartmentId());
            if (apt == null) {
                throw new ApartmentNotFoundException("Apartment not found: " + bill.getApartmentId());
            }
            bill.setApartment(apt);
        }
    }

    public PaginatedResponse<UtilityBill> fetchUtilityBills(Specification<UtilityBill> spec, Pageable pageable) {
        Page<UtilityBill> pageUtilityBill = utilityBillRepository.findAll(spec, pageable);
        PaginatedResponse<UtilityBill> page = new PaginatedResponse<>();
        page.setPageSize(pageable.getPageSize());
        page.setCurPage(pageable.getPageNumber());
        page.setTotalPages(pageUtilityBill.getTotalPages());
        page.setTotalElements(pageUtilityBill.getNumberOfElements());
        page.setResult(pageUtilityBill.getContent());
        return page;
    }

    public List<UtilityBill> fetchUtilityBillsByApartmentId(Long id) {
        return utilityBillRepository.findByApartmentId(id);
    }    
    
    @Transactional
    public UtilityBill updateUtilityBill (Long id) {
        UtilityBill utilityBill = utilityBillRepository.findById(id)
                .orElseThrow(() -> new UtilityBillNotFoundException("Not found id " + id));
        utilityBill.setPaymentStatus(PaymentEnum.Paid);
        return utilityBillRepository.save(utilityBill);
    }
}
