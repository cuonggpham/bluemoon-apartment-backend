package com.dev.tagashira.dto.response;
 
 import com.dev.tagashira.entity.Fee;
 import lombok.*;
 import lombok.experimental.FieldDefaults;
 
 import java.time.LocalDate;
 import java.util.List;
 
 @Getter
 @Setter
 @NoArgsConstructor
 @AllArgsConstructor
 @Builder
 @FieldDefaults(level= AccessLevel.PRIVATE)
 public class InvoiceResponse {
     String id;
     String name;
     String description;
     LocalDate lastUpdated;
     List<Fee> feeList;
 }