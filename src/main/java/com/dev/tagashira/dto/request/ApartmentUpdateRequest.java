package com.dev.tagashira.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ApartmentUpdateRequest {
    Long ownerId;
    Double area;
    Long ownerPhone;
    String status;
    List<Long> residents;
}
