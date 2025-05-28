package com.dev.tagashira.converter;

import com.dev.tagashira.dto.response.VehicleResponse;
import com.dev.tagashira.entity.Vehicle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleConverter {

    public VehicleResponse toResponse(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        VehicleResponse.ApartmentSummaryResponse apartmentSummary = null;
        if (vehicle.getApartment() != null) {
            apartmentSummary = VehicleResponse.ApartmentSummaryResponse.builder()
                .addressNumber(vehicle.getApartment().getAddressNumber())
                .status(vehicle.getApartment().getStatus().toString())
                .area(vehicle.getApartment().getArea())
                .build();
        }

        return VehicleResponse.builder()
            .id(vehicle.getId())
            .category(vehicle.getCategory())
            .registerDate(vehicle.getRegisterDate())
            .apartment(apartmentSummary)
            .build();
    }

    public List<VehicleResponse> toResponseList(List<Vehicle> vehicles) {
        if (vehicles == null) {
            return null;
        }
        return vehicles.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
}
