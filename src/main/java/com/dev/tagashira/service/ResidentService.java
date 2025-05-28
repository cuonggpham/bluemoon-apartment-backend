package com.dev.tagashira.service;

import com.dev.tagashira.constant.ApartmentEnum;
import com.dev.tagashira.constant.GenderEnum;
import com.dev.tagashira.constant.ResidentEnum;
import com.dev.tagashira.dto.request.ResidentCreateRequest;
import com.dev.tagashira.dto.request.ResidentUpdateRequest;
import com.dev.tagashira.dto.request.ResidentWithApartmentCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Resident;
import com.dev.tagashira.repository.ApartmentRepository;
import com.dev.tagashira.repository.ResidentRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import java.util.List;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ResidentService {
    ResidentRepository residentRepository;
    ApartmentRepository apartmentRepository;

    public PaginatedResponse<Resident> fetchAllResidents(Specification<Resident> spec, Pageable pageable) {
        //Page<Resident> pageResident = this.residentRepository.findAll(spec, pageable);
        Specification<Resident> notMovedSpec = (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("status"), ResidentEnum.Moved);

        Specification<Resident> combinedSpec = spec == null ? notMovedSpec : spec.and(notMovedSpec);

        Page<Resident> pageResident = this.residentRepository.findAll(combinedSpec, pageable);

        PaginatedResponse<Resident> page = new PaginatedResponse<>();
        page.setPageSize(pageable.getPageSize());
        page.setCurPage(pageable.getPageNumber());
        page.setTotalPages(pageResident.getTotalPages());
        page.setTotalElements(pageResident.getNumberOfElements());
        page.setResult(pageResident.getContent());
        return page;
    }

    public PaginatedResponse<Resident> fetchAll(Specification<Resident> spec, Pageable pageable) {
        Page<Resident> pageResident = this.residentRepository.findAll(spec, pageable);
        PaginatedResponse<Resident> page = new PaginatedResponse<>();
        page.setPageSize(pageable.getPageSize());
        page.setCurPage(pageable.getPageNumber());
        page.setTotalPages(pageResident.getTotalPages());
        page.setTotalElements(pageResident.getNumberOfElements());
        page.setResult(pageResident.getContent());
        return page;
    }

    @Transactional
    public Resident fetchResidentById(Long id) throws RuntimeException {
        return this.residentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Resident with id = "+id+ " is not found"));
    }    
    
    @Transactional
    public Resident createResident(ResidentCreateRequest residentCreate) throws RuntimeException {
        if (this.residentRepository.findById(residentCreate.getId()).isPresent()) {
            throw new RuntimeException("Resident with id = " + residentCreate.getId() + " already exists");
        }

        Resident resident = Resident.builder()
                .id(residentCreate.getId())
                .name(residentCreate.getName())
                .dob(residentCreate.getDob())
                .gender(residentCreate.getGender())
                .cic(residentCreate.getCic())
                .status(ResidentEnum.fromString(residentCreate.getStatus()))
                .build();
        resident.setIsActive(1);

        // Handle apartment assignment
        if (residentCreate.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(residentCreate.getApartmentId())
                    .orElseThrow(() -> new RuntimeException("Apartment with id " + residentCreate.getApartmentId() + " not found"));
            resident.setApartment(apartment);
        }

        Resident savedResident = this.residentRepository.save(resident);
        
        // Update apartment's resident list if apartment is assigned
        if (residentCreate.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(residentCreate.getApartmentId()).get();
            if (apartment.getResidentList() != null) {
                apartment.getResidentList().add(savedResident);
            } else {
                apartment.setResidentList(List.of(savedResident));
            }
            apartmentRepository.save(apartment);
        }

        return savedResident;
    }

    @Transactional
    public Resident updateResident(ResidentUpdateRequest resident) throws Exception {
        Resident oldResident = this.fetchResidentById(resident.getId());
        if (oldResident != null) {
            if (resident.getName() != null) oldResident.setName(resident.getName());
            if (resident.getDob() != null) oldResident.setDob(resident.getDob());
            if (resident.getStatus() != null) {
                oldResident.setStatus(ResidentEnum.fromString(resident.getStatus()));
            }
            if (resident.getGender() != null) {
                oldResident.setGender(resident.getGender());
            }
            if (resident.getCic() != null) {
                oldResident.setCic(resident.getCic());
            }
            if (resident.getAddressNumber() != null) {
                Apartment newApartment = apartmentRepository.findById(resident.getAddressNumber())
                        .orElseThrow(() -> new RuntimeException("Apartment with address number " + resident.getAddressNumber() + " not found"));
                List<Resident> residentList = newApartment.getResidentList();
                residentList.add(oldResident);
                newApartment.setResidentList(residentList);
                apartmentRepository.save(newApartment);
                oldResident.setApartment(newApartment);
            }
        } else {
            throw new Exception("Resident with id = " + resident.getId() + " is not found");
        }
        return this.residentRepository.save(oldResident);
    }

    @Transactional
    public Resident updateResidentById(Long id, ResidentCreateRequest residentUpdate) throws RuntimeException {
        Resident existingResident = this.fetchResidentById(id);
        
        // Update fields if provided
        if (residentUpdate.getName() != null) {
            existingResident.setName(residentUpdate.getName());
        }
        if (residentUpdate.getDob() != null) {
            existingResident.setDob(residentUpdate.getDob());
        }
        if (residentUpdate.getGender() != null) {
            existingResident.setGender(residentUpdate.getGender());
        }
        if (residentUpdate.getCic() != null) {
            existingResident.setCic(residentUpdate.getCic());
        }
        if (residentUpdate.getStatus() != null) {
            existingResident.setStatus(ResidentEnum.fromString(residentUpdate.getStatus()));
        }
        
        // Handle apartment assignment
        if (residentUpdate.getApartmentId() != null) {
            // Remove from old apartment if exists
            if (existingResident.getApartment() != null) {
                Apartment oldApartment = existingResident.getApartment();
                if (oldApartment.getResidentList() != null) {
                    oldApartment.getResidentList().remove(existingResident);
                    apartmentRepository.save(oldApartment);
                }
            }
            
            // Add to new apartment
            Apartment newApartment = apartmentRepository.findById(residentUpdate.getApartmentId())
                    .orElseThrow(() -> new RuntimeException("Apartment with id " + residentUpdate.getApartmentId() + " not found"));
            
            existingResident.setApartment(newApartment);
            
            if (newApartment.getResidentList() != null) {
                newApartment.getResidentList().add(existingResident);
            } else {
                newApartment.setResidentList(List.of(existingResident));
            }
            apartmentRepository.save(newApartment);
        }
        
        return this.residentRepository.save(existingResident);
    }    
    
    @Transactional
    public ApiResponse<String> deleteResident(Long id) throws Exception {
        Resident resident = this.fetchResidentById(id);
        resident.setIsActive(0);
        if (resident.getApartment() != null) {
            Apartment apartment = apartmentRepository.findById(resident.getApartmentId()).orElseThrow(() -> new RuntimeException("Apartment with id " + resident.getApartmentId() + " not found"));
            List<Resident> residentList = apartment.getResidentList();
            residentList.remove(resident);
            apartment.setResidentList(residentList);
        }
        resident.setApartment(null);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("delete resident success");
        response.setData(null);
        return response;
    }

    @Transactional
    public Resident createResidentWithApartment(ResidentWithApartmentCreateRequest request) {
        if (request.getApartmentOption() == null) {
            throw new IllegalArgumentException("Apartment option is required");
        }

        // 1) Build resident (not saved yet)
        Resident resident = Resident.builder()
                .id(request.getId())
                .name(request.getName())
                .dob(LocalDate.parse(request.getDob()))
                .cic(request.getCic())
                .gender(GenderEnum.valueOf(request.getGender()))
                .status(ResidentEnum.fromString(request.getStatus()))
                .isActive(1)
                .build();

        // 2) Determine apartment
        Apartment apartment;
        if ("existing".equals(request.getApartmentOption())) {
            if (request.getExistingApartmentId() == null) {
                throw new IllegalArgumentException("Existing apartment ID is required");
            }
            apartment = apartmentRepository.findById(request.getExistingApartmentId())
                    .orElseThrow(() -> new RuntimeException("Apartment not found: " + request.getExistingApartmentId()));
        } else if ("new".equals(request.getApartmentOption())) {
            if (request.getApartmentAddressNumber() == null || request.getApartmentArea() == null) {
                throw new IllegalArgumentException("Address number and area are required for new apartment");
            }
            apartment = Apartment.builder()
                    .addressNumber(request.getApartmentAddressNumber())
                    .area(request.getApartmentArea())
                    .status(ApartmentEnum.valueOf(request.getApartmentStatus()))
                    .owner(resident)
                    .ownerPhone(request.getOwnerPhone())
                    .build();
        } else {
            throw new IllegalArgumentException("Invalid apartment option: " + request.getApartmentOption());
        }

        // 3) Link bidirectionally
        resident = residentRepository.save(resident);

        resident.setApartment(apartment);
        apartment.getResidentList().add(resident);

        // 4) Save apartment (cascade persists resident)
        Apartment savedApartment = apartmentRepository.save(apartment);
        residentRepository.save(resident);
        log.info("Apartment saved: {} with {} residents", savedApartment.getAddressNumber(), savedApartment.getResidentList().size());
        return resident;
    }
}
