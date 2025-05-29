package com.dev.tagashira.service;

import com.dev.tagashira.converter.VehicleConverter;
import com.dev.tagashira.dto.response.ApiResponse;
import com.dev.tagashira.dto.response.PaginatedResponse;
import com.dev.tagashira.dto.response.VehicleResponse;
import com.dev.tagashira.entity.Apartment;
import com.dev.tagashira.entity.Vehicle;
import com.dev.tagashira.exception.ApartmentNotFoundException;
import com.dev.tagashira.exception.InvalidDataException;
import com.dev.tagashira.exception.VehicleNotFoundException;
import com.dev.tagashira.repository.ApartmentRepository;
import com.dev.tagashira.repository.VehicleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VehicleService {
    VehicleRepository vehicleRepository;
    ApartmentRepository apartmentRepository;
    VehicleConverter vehicleConverter;    @Transactional
    public List<VehicleResponse> findAllByApartmentId(long apartmentId) {
        if (!this.apartmentRepository.existsById(apartmentId)) {
            throw new ApartmentNotFoundException("Apartment with id " + apartmentId + " does not exist");
        }
        List<Vehicle> vehicles = this.vehicleRepository.findAllByApartment_AddressNumber(apartmentId);
        return vehicleConverter.toResponseList(vehicles);
    }

    @Transactional
    public PaginatedResponse<VehicleResponse> getAll(Specification<Vehicle> spec, Pageable pageable) {
        Page<Vehicle> pageVehicle = vehicleRepository.findAll(spec,pageable);
        List<VehicleResponse> vehicleResponses = vehicleConverter.toResponseList(pageVehicle.getContent());
        
        return PaginatedResponse.<VehicleResponse>builder()
                .pageSize(pageable.getPageSize())
                .curPage(pageable.getPageNumber())
                .totalPages(pageVehicle.getTotalPages())
                .totalElements(pageVehicle.getNumberOfElements())
                .result(vehicleResponses)                
                .build();
    }

    @Transactional
    public VehicleResponse create(Vehicle vehicleRequest) {
        if (this.vehicleRepository.findById(vehicleRequest.getId()).isPresent()) {
            throw new InvalidDataException("Vehicle with id = " + vehicleRequest.getId()+ " already exists");
        }
        if (vehicleRequest.getId() == null){
            throw new InvalidDataException("Vehicle id is null");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleRequest.getId());
        vehicle.setCategory(vehicleRequest.getCategory());
        vehicle.setApartment(this.apartmentRepository.findById(vehicleRequest.getApartmentId()).orElseThrow(() -> new ApartmentNotFoundException("Apartment with id " + vehicleRequest.getApartmentId() + " does not exist")));
        Vehicle savedVehicle = this.vehicleRepository.save(vehicle);
        return vehicleConverter.toResponse(savedVehicle);
    }

    @Transactional
    public ApiResponse<String> deleteVehicle(Long id, Vehicle vehicleRequest) {
        Apartment apartment = this.apartmentRepository.findById(id).orElseThrow(() -> new ApartmentNotFoundException("Apartment with id " + id + " does not exist"));
        Vehicle vehicle = this.vehicleRepository.findById(vehicleRequest.getId()).orElse(null);
        List<Vehicle> vehicleList = apartment.getVehicleList();
        vehicleList.remove(vehicle);
        apartment.setVehicleList(vehicleList);
        apartmentRepository.saveAndFlush(apartment);
        assert vehicle != null;
        this.vehicleRepository.delete(vehicle);
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.OK.value());
        response.setMessage("delete vehicle success");
        response.setData(null);
        return response;
    }

    @Transactional
    public VehicleResponse update(String id, Vehicle vehicleRequest) {
        Vehicle vehicle = this.vehicleRepository.findById(id)
            .orElseThrow(() -> new VehicleNotFoundException("Vehicle with id = " + id + " not found"));
        if (vehicleRequest.getCategory() != null) {
            vehicle.setCategory(vehicleRequest.getCategory());
        }
        if (vehicleRequest.getApartmentId() != null) {
            Apartment apartment = this.apartmentRepository.findById(vehicleRequest.getApartmentId())
                .orElseThrow(() -> new ApartmentNotFoundException("Apartment with id " + vehicleRequest.getApartmentId() + " does not exist"));
            vehicle.setApartment(apartment);
        }
        // Optionally update registerDate if needed
        if (vehicleRequest.getRegisterDate() != null) {
            vehicle.setRegisterDate(vehicleRequest.getRegisterDate());
        }
        Vehicle savedVehicle = this.vehicleRepository.save(vehicle);
        return vehicleConverter.toResponse(savedVehicle);
    }
}
