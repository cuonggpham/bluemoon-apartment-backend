package com.dev.tagashira.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * ApiResponse is a generic class that represents the structure of an API response.
 * It contains a status code, a message, and the data of type T.
 *
 * @param <T> the type of the data in the response
 */
public class ApiResponse<T> {
    int code;
    String message;
    T data; 
}
