package com.dev.tagashira.constant;

import lombok.Getter;

public class RoleConstant {
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_ACCOUNTANT = "ROLE_ACCOUNTANT";
    
    @Getter
    public enum RoleName {
        ROLE_MANAGER("ROLE_MANAGER", "Tổ trưởng/Tổ phó - Quản lý hộ khẩu, nhân khẩu và phân quyền"),
        ROLE_ACCOUNTANT("ROLE_ACCOUNTANT", "Kế toán - Quản lý thu phí và thanh toán");
        
        private final String name;
        private final String description;
        
        RoleName(String name, String description) {
            this.name = name;
            this.description = description;
        }

    }
} 