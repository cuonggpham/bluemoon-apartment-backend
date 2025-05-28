package com.dev.tagashira.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ResidentApartmentAssignmentRequest {
    Long residentId;
    List<Long> apartmentIds;
}
