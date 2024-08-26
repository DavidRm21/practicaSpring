package com.prueba.dataservices.dto;

public record ApiError(
        String code,
        String message
) {
    @Override
    public String toString() {
        return "ApiError{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
