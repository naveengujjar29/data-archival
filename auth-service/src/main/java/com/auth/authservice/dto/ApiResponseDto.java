package com.auth.authservice.dto;

public class ApiResponseDto {


    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ApiResponseDto(String message) {
        this.message = message;
    }
}
