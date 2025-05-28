package com.dev.tagashira.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApartmentResponse {
    private Long addressNumber;
    private Double area;
    private String status;
    private String ownerPhone;
    private ResidentSummaryResponse owner;
    private List<ResidentSummaryResponse> residents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResidentSummaryResponse {
        private Long id;
        private String name;
        private String status;
        private String gender;
        private String cic;
    }
}