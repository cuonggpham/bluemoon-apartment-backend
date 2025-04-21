package com.dev.tagashira.dto.request;
 
 import com.dev.tagashira.constant.ApartmentEnum;
 import jakarta.validation.constraints.NotNull;
 import lombok.*;
 import lombok.experimental.FieldDefaults;
 import java.util.List;
 
 @Data
 @AllArgsConstructor
 @NoArgsConstructor
 @FieldDefaults(level = AccessLevel.PRIVATE)
 @Builder
 public class ApartmentCreateRequest {
     @NotNull
     Long addressNumber;
     double area;
     String status;
     @NotNull
     Long ownerId;
     List<Long> memberIds;
 }
