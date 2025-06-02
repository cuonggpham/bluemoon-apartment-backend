package com.dev.tagashira.constant;

public enum FeeTypeEnum {
    // Legacy fee types (general purpose)
    MANDATORY_GENERAL,    // Phí bắt buộc chung
    VOLUNTARY_GENERAL,    // Phí tự nguyện chung
    
    // Monthly recurring fee types (specific)
    VEHICLE_PARKING,      // Phí gửi xe (tính theo số lượng xe)
    FLOOR_AREA,          // Phí theo diện tích sàn (tính theo m²)
    MANAGEMENT_FEE,      // Phí quản lý chung cư
    MAINTENANCE_FEE,     // Phí bảo trì
    SECURITY_FEE,        // Phí bảo vệ
    CLEANING_FEE,        // Phí vệ sinh
    
    // Utility fees
    WATER_FEE,           // Phí nước
    ELECTRICITY_FEE,     // Phí điện
    INTERNET_FEE,        // Phí internet
    
    // Special fees
    PENALTY_FEE,         // Phí phạt
    DEPOSIT_FEE,         // Phí đặt cọc
    REGISTRATION_FEE     // Phí đăng ký
}
