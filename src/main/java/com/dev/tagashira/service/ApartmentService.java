package com.dev.tagashira.service;

import com.dev.tagashira.constant.ApartmentEnum;
import com.dev.tagashira.converter.ApartmentConverter;
import com.dev.tagashira.dto.request.ApartmentCreateRequest;
import com.dev.tagashira.dto.request.ApartmentUpdateRequest;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.ApartmentResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Resident;
import com.dev.tagashira.exception.ApartmentNotFoundException;
import com.dev.tagashira.exception.InvalidDataException;
import com.dev.tagashira.repository.ApartmentRepository;
import com.dev.tagashira.repository.ResidentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApartmentService {
    ApartmentRepository apartmentRepository;
    ResidentRepository residentRepository;
    ResidentService residentService;
    ApartmentConverter apartmentConverter;
      
    @Transactional
    public ApartmentResponse create(ApartmentCreateRequest request) {
        if (this.apartmentRepository.findById(request.getAddressNumber()).isPresent()) {
            throw new RuntimeException("Apartment with id = " + request.getAddressNumber() + " already exists");
        }
        
        Resident owner = null;
        Set<Resident> residents = new HashSet<>();
        
        // Handle optional owner
        if (request.getOwnerId() != null) {
            owner = residentService.fetchResidentEntityById(request.getOwnerId());
            residents.add(owner);
        }

        Apartment apartment = Apartment.builder()
                .addressNumber(request.getAddressNumber())
                .area(request.getArea())
                .owner(owner)
                .ownerPhone(request.getOwnerPhone())
                .status(ApartmentEnum.fromString(request.getStatus()))
                .residentList(residents)
                .build();

        Apartment saved = apartmentRepository.save(apartment);

        // Update the many-to-many relationship from the resident side
        residents.forEach(resident -> {
            resident.getApartments().add(saved);
            residentRepository.save(resident);
        });

        return apartmentConverter.toResponse(saved);
    }    
    
    public PaginatedResponse<ApartmentResponse> getAll(Specification<Apartment> spec, Pageable pageable){
        Page<Apartment> pageApartment = apartmentRepository.findAll(spec,pageable);
        List<ApartmentResponse> apartmentResponses = apartmentConverter.toResponseList(pageApartment.getContent());
        
        return PaginatedResponse.<ApartmentResponse>builder()
                .pageSize(pageable.getPageSize())
                .curPage(pageable.getPageNumber())
                .totalPages(pageApartment.getTotalPages())
                .totalElements(pageApartment.getNumberOfElements())
                .result(apartmentResponses)
                .build();
    }    
    
    public ApartmentResponse getDetail(Long id){
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Can not find apartment with address: " + id));
        return apartmentConverter.toResponse(apartment);
    }    // Internal method for other services to fetch entity
    public Apartment fetchApartmentEntityById(Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new ApartmentNotFoundException("Cannot find apartment with id: " + id));
    }
    
    @Transactional
    public ApartmentResponse update(Long addressID, ApartmentUpdateRequest request){
        Apartment apartment = apartmentRepository.findById(addressID)
                .orElseThrow(() -> new EntityNotFoundException("Not found apartment " + addressID));

        // Update owner
        if (request.getOwnerId() != null) {
            Resident newOwner = residentService.fetchResidentEntityById(request.getOwnerId());
            apartment.setOwner(newOwner);
        } else if (request.getOwnerId() == null && apartment.getOwner() != null) {
            // Explicitly setting owner to null (removing owner)
            apartment.setOwner(null);
        }

        // Update other properties
        if(request.getStatus()!=null) apartment.setStatus(ApartmentEnum.valueOf(request.getStatus()));
        if(request.getArea() != null) apartment.setArea(request.getArea());
        if(request.getOwnerPhone()!=null) apartment.setOwnerPhone(request.getOwnerPhone());

        // Handle residents update logic
        // IMPORTANT: Only update residents if the residents list is explicitly provided in the request
        // This prevents accidental deletion of existing residents when updating other fields (like owner only)
        if (request.getResidents() != null) {
            // Case 1: Residents list is provided - perform full residents update
            List<Long> requestResidents = request.getResidents();
            List<Resident> validResidents = new ArrayList<>(residentRepository.findAllById(requestResidents));

            // Ensure owner is in the residentList set if owner exists
            if (apartment.getOwner() != null && !validResidents.contains(apartment.getOwner())) {
                validResidents.add(apartment.getOwner());
            }

            // Get current residentList
            Set<Resident> currentResidents = new HashSet<>(apartment.getResidentList());
            Set<Resident> newResidents = new HashSet<>(validResidents);

            // Remove residentList no longer associated with this apartment
            currentResidents.forEach(resident -> {
                if (!newResidents.contains(resident)) {
                    resident.getApartments().remove(apartment);
                    residentRepository.save(resident);
                }
            });

            // Add new residentList to the apartment
            newResidents.forEach(resident -> {
                resident.getApartments().add(apartment);
                residentRepository.save(resident);
            });

            // Update the apartment's residentList set
            apartment.setResidentList(newResidents);
        } else {
            // Case 2: Residents list is NOT provided - preserve existing residents, only ensure owner is included
            // This is the common case when updating owner only or other properties
            if (apartment.getOwner() != null) {
                Set<Resident> currentResidents = apartment.getResidentList();
                if (!currentResidents.contains(apartment.getOwner())) {
                    currentResidents.add(apartment.getOwner());
                    apartment.getOwner().getApartments().add(apartment);
                    residentRepository.save(apartment.getOwner());
                }
            }
        }

        Apartment savedApartment = apartmentRepository.save(apartment);
        return apartmentConverter.toResponse(savedApartment);
    }
      @Transactional
    public ApiResponse<String> deleteApartment(Long id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new ApartmentNotFoundException("Apartment with id " + id + " not found"));
        
        // Remove apartment from all residents' apartment sets
        if (apartment.getResidentList() != null && !apartment.getResidentList().isEmpty()) {
            for (Resident resident : apartment.getResidentList()) {
                resident.getApartments().remove(apartment);
                residentRepository.save(resident);
            }
            apartment.getResidentList().clear();
        }
        
        // Delete the apartment
        apartmentRepository.delete(apartment);
        
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("Apartment deleted successfully");
        response.setData(null);
        return response;
    }
}
