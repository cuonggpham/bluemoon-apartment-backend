package com.dev.tagashira.converter;

import com.dev.tagashira.dto.response.ApartmentResponse;
import com.dev.tagashira.entity.Apartment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApartmentConverter {

    public ApartmentResponse toResponse(Apartment apartment) {
        if (apartment == null) {
            return null;
        }

        List<ApartmentResponse.ResidentSummaryResponse> residents = null;
        if (apartment.getResidentList() != null) {
            residents = apartment.getResidentList().stream()
                .map(resident -> ApartmentResponse.ResidentSummaryResponse.builder()
                    .id(resident.getId())
                    .name(resident.getName())
                    .status(resident.getStatus().toString())
                    .gender(resident.getGender().toString())
                    .cic(resident.getCic())
                    .build())
                .collect(Collectors.toList());
        }

        ApartmentResponse.ResidentSummaryResponse owner = null;
        if (apartment.getOwner() != null) {
            owner = ApartmentResponse.ResidentSummaryResponse.builder()
                .id(apartment.getOwner().getId())
                .name(apartment.getOwner().getName())
                .status(apartment.getOwner().getStatus().toString())
                .gender(apartment.getOwner().getGender().toString())
                .cic(apartment.getOwner().getCic())
                .build();
        }

        return ApartmentResponse.builder()
            .addressNumber(apartment.getAddressNumber())
            .area(apartment.getArea())
            .status(apartment.getStatus().toString())
            .ownerPhone(apartment.getOwnerPhone() != null ? apartment.getOwnerPhone().toString() : null)
            .owner(owner)
            .residents(residents)
            .build();
    }

    public List<ApartmentResponse> toResponseList(List<Apartment> apartments) {
        if (apartments == null) {
            return null;
        }
        return apartments.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ApartmentResponse.ResidentSummaryResponse toResidentSummary(com.dev.tagashira.entity.Resident resident) {
        if (resident == null) {
            return null;
        }
        return ApartmentResponse.ResidentSummaryResponse.builder()
            .id(resident.getId())
            .name(resident.getName())
            .status(resident.getStatus().toString())
            .gender(resident.getGender().toString())
            .cic(resident.getCic())
            .build();
    }
}