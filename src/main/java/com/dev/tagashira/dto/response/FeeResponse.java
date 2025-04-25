package com.dev.tagashira.dto.response;

import com.dev.tagashira.constant.FeeTypeEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level= AccessLevel.PRIVATE)
public class FeeResponse {
    String name;
    Long id;
    FeeTypeEnum feeType;
    double amount;
}
