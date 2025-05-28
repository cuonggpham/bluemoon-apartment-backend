package com.dev.tagashira.converter;

import com.dev.tagashira.dto.response.ResidentResponse;
import com.dev.tagashira.entity.Resident;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResidentConverter {

    public ResidentResponse toResponse(Resident resident) {
        if (resident == null) {
            return null;
        }        List<ResidentResponse.ApartmentSummaryResponse> apartments = null;
        if (resident.getApartments() != null) {
            apartments = resident.getApartments().stream()
                .map(apartment -> ResidentResponse.ApartmentSummaryResponse.builder()
                    .addressNumber(apartment.getAddressNumber())
                    .status(apartment.getStatus().toString())
                    .area(apartment.getArea())
                    .build())
                .collect(Collectors.toList());
        }

        return ResidentResponse.builder()
            .id(resident.getId())
            .name(resident.getName())
            .dob(resident.getDob())
            .gender(resident.getGender())
            .cic(resident.getCic())
            .status(resident.getStatus())
            .statusDate(resident.getStatusDate())
            .apartments(apartments)
            .build();
    }

    public List<ResidentResponse> toResponseList(List<Resident> residents) {
        if (residents == null) {
            return null;
        }
        return residents.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public ResidentResponse.ApartmentSummaryResponse toApartmentSummary(com.dev.tagashira.entity.Apartment apartment) {
        if (apartment == null) {
            return null;
        }        return ResidentResponse.ApartmentSummaryResponse.builder()
            .addressNumber(apartment.getAddressNumber())
            .status(apartment.getStatus().toString())
            .area(apartment.getArea())
            .build();
    }
}