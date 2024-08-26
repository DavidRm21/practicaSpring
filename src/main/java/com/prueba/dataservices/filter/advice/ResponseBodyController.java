package com.prueba.dataservices.filter.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

@Slf4j
@ControllerAdvice
@Component
public class ResponseBodyController implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            if (body instanceof String) {
        log.info("String: {}", body);
                return Base64.getEncoder().encodeToString(((String) body).getBytes(StandardCharsets.UTF_8));
            }
            else if (body instanceof byte[]) {
        log.info("byte: {}", body);
                return Base64.getEncoder().encodeToString((byte[]) body);
            }
            else {
        log.info("Object: {}", body);
                String json = new ObjectMapper().writeValueAsString(body);
                return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error encoding response body to Base64", e);
        }
    }
}
