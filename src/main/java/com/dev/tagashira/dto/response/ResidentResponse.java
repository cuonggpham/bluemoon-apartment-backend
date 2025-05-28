package com.dev.tagashira.dto.response;

import com.dev.tagashira.constant.GenderEnum;
import com.dev.tagashira.constant.ResidentEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResidentResponse {
    Long id;
    String name;
    LocalDate dob;
    GenderEnum gender;
    String cic;
    ResidentEnum status;
    LocalDate statusDate;
    List<ApartmentSummaryResponse> apartments;
    
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