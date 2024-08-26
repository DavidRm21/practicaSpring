package com.prueba.dataservices.controller;

import com.prueba.dataservices.dto.ApiError;
import com.prueba.dataservices.dto.ExcepcionNegocio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExcepcionHanlder {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> hnadlerException(Exception e){
        return ResponseEntity.ok(new ApiError("99", e.getMessage()).toString());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> hnadlerRuntimeException(RuntimeException e){
        return ResponseEntity.ok(new ApiError("99", e.getMessage()).toString());
    }

    @ExceptionHandler(ExcepcionNegocio.class)
    public ResponseEntity<?> hnadlerExcepcionNegocio(ExcepcionNegocio e) {
        return ResponseEntity.ok(new ApiError(String.valueOf(e.getCodigo()), e.getMensaje()));
    }
}
