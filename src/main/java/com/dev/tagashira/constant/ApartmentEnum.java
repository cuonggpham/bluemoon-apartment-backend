package com.dev.tagashira.constant;

public enum ApartmentEnum {
    Residential, Business;
    public static ApartmentEnum fromString(String status){
        for(ApartmentEnum val : ApartmentEnum.values()){
            if(val.name().equalsIgnoreCase(status))
                return val;
        }
        throw new IllegalArgumentException("Invalid status "+status);
    }

}
