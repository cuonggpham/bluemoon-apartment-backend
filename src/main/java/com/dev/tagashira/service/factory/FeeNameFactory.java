package com.dev.tagashira.service.factory;

import com.dev.tagashira.constant.FeeTypeEnum;
import com.dev.tagashira.entity.Apartment;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
public class FeeNameFactory {

    public String build(FeeTypeEnum type, YearMonth month, String custom, Apartment apt) {
        String desc = switch (type) {
            case VEHICLE_PARKING -> "Phí gửi xe tháng " + month;
            case FLOOR_AREA     -> (customValid(custom) ? custom : "Phí diện tích sàn")
                    + " tháng " + month;
            case MANDATORY      -> "Phí bắt buộc " + month;
            case VOLUNTARY      -> "Phí tự nguyện " + month;
        };

        return desc + " (apartment: " + apt.getAddressNumber() + ")";
    }

    private boolean customValid(String s) { return s != null && !s.trim().isEmpty(); }
}

