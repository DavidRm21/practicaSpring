package com.prueba.dataservices.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JsonNodeValidator {

    // Define las validaciones para cada campo
    private final Map<String, List<Consumer<JsonNode>>> validationRules = new HashMap<>();

    public JsonNodeValidator() {
        // Configura las validaciones
        validationRules.put("firstName", Arrays.asList(
                node -> {
                    if (node == null || node.asText().trim().isEmpty()) {
                        throw new RuntimeException("El campo firstName no puede estar vacío o ser nulo.");
                    }
                }
        ));

        validationRules.put("lastName", Arrays.asList(
                node -> {
                    String value = node.asText();
                    if (value.length() < 2 || value.length() > 50) {
                        throw new RuntimeException("El campo lastName debe tener entre 2 y 50 caracteres.");
                    }
                }
        ));

        validationRules.put("age", Arrays.asList(
                node -> {
                    if (node == null || node.asInt() <= 0) {
                        throw new RuntimeException("El campo age debe ser un número positivo.");
                    }
                }
        ));

        validationRules.put("address", Arrays.asList(
                node -> {
                    String value = node.asText();
                    if (value.length() > 50) {
                        throw new RuntimeException("El campo address no puede tener más de 50 caracteres.");
                    }
                }
        ));

        validationRules.put("phoneNumber", Arrays.asList(
                node -> {
                    String value = node.asText();
                    if (value.length() > 12) {
                        throw new RuntimeException("El campo phoneNumber no puede tener más de 12 caracteres.");
                    }
                }
        ));

        validationRules.put("email", Arrays.asList(
                node -> {
                    String value = node.asText();
                    if (!value.matches("^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$")) {
                        throw new RuntimeException("El campo email debe tener un formato de correo electrónico válido.");
                    }
                }
        ));
    }

    public void validate(JsonNode jsonNode) {
        validationRules.forEach((fieldName, validators) -> {
            JsonNode field = jsonNode.get(fieldName);
            if (field != null) {
                validators.forEach(validator -> validator.accept(field));
            } else {
                // Si el campo es obligatorio y no está presente, puedes manejarlo aquí
                throw new RuntimeException("El campo " + fieldName + " es obligatorio y no puede ser nulo.");
            }
        });
    }
}
