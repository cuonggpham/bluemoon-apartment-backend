package com.dev.tagashira.service;

import com.dev.tagashira.constant.ApartmentEnum;
import com.dev.tagashira.constant.GenderEnum;
import com.dev.tagashira.constant.ResidentEnum;
import com.dev.tagashira.converter.ResidentConverter;
import com.dev.tagashira.dto.request.ResidentCreateRequest;
import com.dev.tagashira.dto.request.ResidentUpdateRequest;
import com.dev.tagashira.dto.request.ResidentWithApartmentCreateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.dto.response.ResidentResponse;
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

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ResidentService {
    ResidentRepository residentRepository;
    ApartmentRepository apartmentRepository;
    ResidentConverter residentConverter;    
    
    public PaginatedResponse<ResidentResponse> fetchAllResidents(Specification<Resident> spec, Pageable pageable) {
        //Page<Resident> pageResident = this.residentRepository.findAll(spec, pageable);
        Specification<Resident> notMovedSpec = (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("status"), ResidentEnum.Moved);

        Specification<Resident> combinedSpec = spec == null ? notMovedSpec : spec.and(notMovedSpec);

        Page<Resident> pageResident = this.residentRepository.findAll(combinedSpec, pageable);

        PaginatedResponse<ResidentResponse> page = new PaginatedResponse<>();
        page.setPageSize(pageable.getPageSize());
        page.setCurPage(pageable.getPageNumber());
        page.setTotalPages(pageResident.getTotalPages());
        page.setTotalElements(pageResident.getNumberOfElements());
        page.setResult(residentConverter.toResponseList(pageResident.getContent()));
        return page;
    }    
    
    public PaginatedResponse<ResidentResponse> fetchAll(Specification<Resident> spec, Pageable pageable) {
        Page<Resident> pageResident = this.residentRepository.findAll(spec, pageable);
        PaginatedResponse<ResidentResponse> page = new PaginatedResponse<>();
        page.setPageSize(pageable.getPageSize());
        page.setCurPage(pageable.getPageNumber());
        page.setTotalPages(pageResident.getTotalPages());
        page.setTotalElements(pageResident.getNumberOfElements());
        page.setResult(residentConverter.toResponseList(pageResident.getContent()));
        return page;
    }    
    
    @Transactional
    public ResidentResponse fetchResidentById(Long id) throws RuntimeException {
        Resident resident = this.residentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Resident with id = "+id+ " is not found"));
        return residentConverter.toResponse(resident);
    }

    @Transactional
    public Resident fetchResidentEntityById(Long id) throws RuntimeException {
        return this.residentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Resident with id = "+id+ " is not found"));
    }
      
    @Transactional
    public ResidentResponse createResident(ResidentCreateRequest residentCreate) throws RuntimeException {
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

        Resident savedResident = this.residentRepository.save(resident);

        // Handle apartment assignment using many-to-many relationship
        if (residentCreate.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(residentCreate.getApartmentId())
                    .orElseThrow(() -> new RuntimeException("Apartment with id " + residentCreate.getApartmentId() + " not found"));
            
            // Add resident to apartment's residentList set
            apartment.getResidentList().add(savedResident);
            apartmentRepository.save(apartment);
        }

        return residentConverter.toResponse(savedResident);
    }    
    
    @Transactional
    public ResidentResponse updateResident(ResidentUpdateRequest resident) throws Exception {
        Resident oldResident = this.fetchResidentEntityById(resident.getId());
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
                
                // Add resident to apartment's residentList set using many-to-many
                newApartment.getResidentList().add(oldResident);
                apartmentRepository.save(newApartment);
            }
        } else {
            throw new Exception("Resident with id = " + resident.getId() + " is not found");
        }
        Resident savedResident = this.residentRepository.save(oldResident);
        return residentConverter.toResponse(savedResident);
    }    
    
    @Transactional
    public ResidentResponse updateResidentById(Long id, ResidentCreateRequest residentUpdate) throws RuntimeException {
        Resident existingResident = this.fetchResidentEntityById(id);
        
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
        
        // Handle apartment assignment using many-to-many relationship
        if (residentUpdate.getApartmentId() != null) {
            // Remove from old apartments if needed (optional for many-to-many)
            // In many-to-many, a resident can belong to multiple apartments
            
            // Add to new apartment
            Apartment newApartment = apartmentRepository.findById(residentUpdate.getApartmentId())
                    .orElseThrow(() -> new RuntimeException("Apartment with id " + residentUpdate.getApartmentId() + " not found"));
            
            newApartment.getResidentList().add(existingResident);
            apartmentRepository.save(newApartment);
        }
        
        Resident savedResident = this.residentRepository.save(existingResident);
        return residentConverter.toResponse(savedResident);
    }
        
    @Transactional
    public ApiResponse<String> deleteResident(Long id) throws Exception {
        Resident resident = this.fetchResidentEntityById(id);
        
        // Check if resident owns any apartments
        List<Apartment> ownedApartments = apartmentRepository.findAllByOwnerId(id);
        if (!ownedApartments.isEmpty()) {
            // Prevent deletion if resident owns apartments without proper handling
            throw new RuntimeException("Cannot delete resident with ID " + id + 
                " because they own " + ownedApartments.size() + " apartment(s). " +
                "Please transfer ownership or remove owner assignment before deletion.");
        }
        
        resident.setIsActive(0);
        
        // Remove resident from all apartments using many-to-many relationship
        if (resident.getApartments() != null && !resident.getApartments().isEmpty()) {
            for (Apartment apartment : resident.getApartments()) {
                apartment.getResidentList().remove(resident);
                apartmentRepository.save(apartment);
            }
            resident.getApartments().clear();
        }
        
        residentRepository.save(resident);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("delete resident success");
        response.setData(null);
        return response;
    }
    
    /**
     * Force delete a resident, automatically removing owner assignments from all owned apartments.
     * This method should be used with caution and only by administrators.
     * 
     * @param id The ID of the resident to delete
     * @param forceDelete If true, automatically removes owner assignments; if false, throws exception if resident owns apartments
     * @return ApiResponse indicating success or failure
     * @throws Exception if resident not found or other errors occur
     */
    @Transactional
    public ApiResponse<String> deleteResidentWithOwnerCleanup(Long id, boolean forceDelete) throws Exception {
        Resident resident = this.fetchResidentEntityById(id);
        
        // Handle owned apartments
        List<Apartment> ownedApartments = apartmentRepository.findAllByOwnerId(id);
        if (!ownedApartments.isEmpty()) {
            if (!forceDelete) {
                throw new RuntimeException("Cannot delete resident with ID " + id + 
                    " because they own " + ownedApartments.size() + " apartment(s). " +
                    "Please transfer ownership or remove owner assignment before deletion, " +
                    "or use force delete to automatically remove owner assignments.");
            }
            
            // Force delete: Remove owner assignments from all owned apartments
            log.warn("Force deleting resident {} who owns {} apartments. Removing owner assignments.", 
                    id, ownedApartments.size());
            
            for (Apartment apartment : ownedApartments) {
                apartment.setOwner(null);
                apartmentRepository.save(apartment);
                log.info("Removed owner assignment from apartment {}", apartment.getAddressNumber());
            }
        }
        
        resident.setIsActive(0);
        
        // Remove resident from all apartments using many-to-many relationship
        if (resident.getApartments() != null && !resident.getApartments().isEmpty()) {
            for (Apartment apartment : resident.getApartments()) {
                apartment.getResidentList().remove(resident);
                apartmentRepository.save(apartment);
            }
            resident.getApartments().clear();
        }
        
        residentRepository.save(resident);
        
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("delete resident success" + 
            (ownedApartments.isEmpty() ? "" : " (removed owner assignments from " + ownedApartments.size() + " apartments)"));
        response.setData(null);
        return response;
    }
    
    @Transactional
    public ResidentResponse createResidentWithApartment(ResidentWithApartmentCreateRequest request) {
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

        // 3) Save resident first
        resident = residentRepository.save(resident);
        // 4) Link using many-to-many relationship
        apartment.getResidentList().add(resident);
        resident.getApartments().add(apartment);

        // 5) Save apartment
        Apartment savedApartment = apartmentRepository.save(apartment);
        log.info("Apartment saved: {} with {} residentList", savedApartment.getAddressNumber(), savedApartment.getResidentList().size());
        
        return residentConverter.toResponse(resident);
    }
    
    /**
     * Check if a resident owns any apartments
     * 
     * @param residentId The ID of the resident to check
     * @return true if the resident owns apartments, false otherwise
     */
    @Transactional
    public boolean isResidentOwner(Long residentId) {
        List<Apartment> ownedApartments = apartmentRepository.findAllByOwnerId(residentId);
        return !ownedApartments.isEmpty();
    }
    
    /**
     * Get all apartments owned by a resident
     * 
     * @param residentId The ID of the resident
     * @return List of apartments owned by the resident
     */
    @Transactional
    public List<Apartment> getApartmentsOwnedByResident(Long residentId) {
        return apartmentRepository.findAllByOwnerId(residentId);
    }
}
