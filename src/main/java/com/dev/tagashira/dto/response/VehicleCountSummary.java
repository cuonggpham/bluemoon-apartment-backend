package com.dev.tagashira.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleCountSummary {
    Long apartmentId;
    Long bicycleCount;
    Long motorbikeCount;
    Long carCount;
    Long totalCount;
    
    public Long getTotalCount() {
        return (bicycleCount != null ? bicycleCount : 0) + 
               (motorbikeCount != null ? motorbikeCount : 0) + 
               (carCount != null ? carCount : 0);
    }
} 