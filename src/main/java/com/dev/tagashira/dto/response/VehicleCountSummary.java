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
    Long motorbikeCount;
    Long carCount;
    Long totalCount;
    
    public Long getTotalCount() {
        return (motorbikeCount != null ? motorbikeCount : 0) + 
               (carCount != null ? carCount : 0);
    }
} 