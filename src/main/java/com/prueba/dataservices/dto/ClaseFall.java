package com.prueba.dataservices.dto;

public record ClaseFall(
        String contextPath,
        String method,
        String body,
        String status,
        String message
) {
    @Override
    public String toString() {
        return "ClaseFall{" +
                "contextPath='" + contextPath + '\'' +
                ", method='" + method + '\'' +
                ", body='" + body + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
