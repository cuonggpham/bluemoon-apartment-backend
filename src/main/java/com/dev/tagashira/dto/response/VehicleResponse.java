package com.dev.tagashira.dto.response;

import com.dev.tagashira.constant.VehicleEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleResponse {
    String id;
    VehicleEnum category;
    LocalDate registerDate;
    ApartmentSummaryResponse apartment;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class ApartmentSummaryResponse {
        Long addressNumber;
        String status;
        Double area;
    }
}
