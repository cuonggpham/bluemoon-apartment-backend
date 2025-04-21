package com.dev.tagashira.dto.response;

import lombok.*;
 import lombok.experimental.FieldDefaults;
 
 import java.util.List;
 
 @Getter
 @Setter
 @NoArgsConstructor
 @AllArgsConstructor
 @Builder
 @FieldDefaults(level = AccessLevel.PRIVATE)
 public class PaginatedResponse <T> {
     int totalPages;
     int pageSize;
     int curPage;
     int totalElements;
     List<T> result;
 }