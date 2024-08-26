package com.prueba.dataservices.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prueba.dataservices.controller.BasicResponseDTO;
import com.prueba.dataservices.filter.advice.HttpInputMessageDecorator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@ControllerAdvice
public class RequestBodyController implements RequestBodyAdvice {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        String requestBody = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);

        // Decodifica si la solicitud viene en Base64
        if (requestBody.matches("^[-A-Za-z0-9+/]*={0,3}$")) {
            requestBody = new String(Base64.getDecoder().decode(requestBody), StandardCharsets.UTF_8);
        }

        try {
//            Convertir un objeto a un Json de solicitud
//            if (requestBody.matches("^\\{\\s*((\\w+\\s*=\\s*(true|false|\\d+|[a-zA-Z\\s]+|[\\w\\-_\\d\\s]+))(\\s*,\\s*\\w+\\s*=\\s*(true|false|\\d+|[a-zA-Z\\s]+|[\\w#$/()%&!*+@\\-_\\d\\s]+))*)\\s*\\}")){
//                String request = convertToValidJson(requestBody);
//                JsonNode jsonNode = objectMapper.readTree(request);
//                log.info("Jsonnode {}",jsonNode.get("exitoso"));
//                return new HttpInputMessageDecorator(inputMessage) {
//                    @Override
//                    public ByteArrayInputStream getBody() throws IOException {
//                        return new ByteArrayInputStream(objectMapper.writeValueAsString(jsonNode).getBytes(StandardCharsets.UTF_8));
//                    }
//                };
//            };
            BasicResponseDTO deserializedObject = objectMapper.readValue(requestBody, BasicResponseDTO.class);
            try {
                deserializedObject.validate();
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }

            return new HttpInputMessageDecorator(inputMessage) {
                @Override
                public ByteArrayInputStream getBody() throws IOException {
                    return new ByteArrayInputStream(objectMapper.writeValueAsString(deserializedObject.data()).getBytes(StandardCharsets.UTF_8));
                }
            };

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            JsonNode jsonNode = objectMapper.readTree(requestBody);
            return new HttpInputMessageDecorator(inputMessage) {
                @Override
                public ByteArrayInputStream getBody() throws IOException {
                    return new ByteArrayInputStream(objectMapper.writeValueAsBytes(jsonNode));
                }
            };
        }

    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    private static String convertToValidJson(String invalidString) {
        // Reemplaza `=` con `:` y añade comillas dobles alrededor de las claves
        return invalidString
                .replaceAll("(\\w+)\\s*=\\s*(true|false)", "\"$1\":$2") // Manejo de valores booleanos
                .replaceAll("(\\w+)\\s*=\\s*([\\w#@\\-_/\\d\\s]+)", "\"$1\":\"$2\""); // Manejo de valores no numéricos
    }
}
