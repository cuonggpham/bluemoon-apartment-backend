package com.dev.tagashira.service;

import com.dev.tagashira.constant.ApartmentEnum;
import com.dev.tagashira.dto.request.ApartmentCreateRequest;
import com.dev.tagashira.dto.request.ApartmentUpdateRequest;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Resident;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApartmentService {
    ApartmentRepository apartmentRepository;
    ResidentRepository residentRepository;
    ResidentService residentService;    
    
    @Transactional
    public Apartment create(ApartmentCreateRequest request) {
        if (this.apartmentRepository.findById(request.getAddressNumber()).isPresent()) {
            throw new RuntimeException("Apartment with id = " + request.getAddressNumber() + " already exists");
        }
        
        Resident owner = null;
        List<Resident> members = new ArrayList<>();
        
        // Handle optional owner
        if (request.getOwnerId() != null) {
            owner = residentService.fetchResidentById(request.getOwnerId());
            members.add(owner);
        }

        Apartment apartment = Apartment.builder()
                .addressNumber(request.getAddressNumber())
                .area(request.getArea())
                .owner(owner)
                .ownerPhone(request.getOwnerPhone())
                .status(ApartmentEnum.fromString(request.getStatus()))
                .residentList(members)
                .build();

        Apartment saved = apartmentRepository.save(apartment);

        // Only update member apartment if there are members
        if (!members.isEmpty()) {
            members.forEach(member -> {
                member.setApartment(saved);
                residentRepository.save(member);  // Sync changes for each member
            });
        }

        return saved;
    }

    public PaginatedResponse<Apartment> getAll(Specification<Apartment> spec, Pageable pageable){
        Page<Apartment> pageApartment = apartmentRepository.findAll(spec,pageable);
        return PaginatedResponse.<Apartment>builder()
                .pageSize(pageable.getPageSize())
                .curPage(pageable.getPageNumber())
                .totalPages(pageApartment.getTotalPages())
                .totalElements(pageApartment.getNumberOfElements())
                .result(pageApartment.getContent())
                .build();
    }

    public Apartment getDetail(Long id){
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Can not find apartment with address: " + id));
    }

    @Transactional
    public Apartment update(Long addressID, ApartmentUpdateRequest request){
        Apartment apartment = apartmentRepository.findById(addressID)
                .orElseThrow(() -> new EntityNotFoundException("Not found apartment " + addressID));

        List<Long> requestResidents = Optional.ofNullable(request.getResidents())
                .orElse(Collections.emptyList());
        List<Resident> validResidents = residentRepository.findAllById(requestResidents);

        if (validResidents.isEmpty()){
            validResidents = new ArrayList<>();
        }

        // update owner + apartment status
        if (request.getOwnerId() != null) {
            Resident newOwner = residentService.fetchResidentById(request.getOwnerId());
            Resident currentOwner = apartment.getOwner();

            validResidents.add(newOwner);
            if (currentOwner != null && !currentOwner.getId().equals(newOwner.getId())) {
                currentOwner.setApartment(null); // Clear the current owner's apartment
                residentRepository.save(currentOwner);
            }

            apartment.setOwner(newOwner);
            newOwner.setApartment(apartment);
            residentRepository.save(newOwner); // Sync changes for the new owner
        } else {
            // Handle case where ownerId is null (removing owner)
            Resident currentOwner = apartment.getOwner();
            if (currentOwner != null) {
                currentOwner.setApartment(null);
                residentRepository.save(currentOwner);
                apartment.setOwner(null);
            }
        }
        if(request.getStatus()!=null) apartment.setStatus(ApartmentEnum.valueOf(request.getStatus()));
        if(request.getArea() != null) apartment.setArea(request.getArea());
        if(request.getOwnerPhone()!=null) apartment.setOwnerPhone(request.getOwnerPhone());

        List<Resident> residentList = Optional.ofNullable(apartment.getResidentList()).orElse(Collections.emptyList());

        apartment.setResidentList(validResidents);
        apartmentRepository.save(apartment);

        // Save residents
        validResidents.forEach(requestResident -> {
            requestResident.setApartment(apartment);
            residentRepository.save(requestResident);
        });
        // Remove resident who not in request list
        residentList.forEach(resident -> {
            if (!requestResidents.contains(resident.getId())) {
                resident.setApartment(null);   // set their address = null
                residentRepository.save(resident);
            }
        });

        return apartment;
    }
}
