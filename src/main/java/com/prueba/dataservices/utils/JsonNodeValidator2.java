package com.prueba.dataservices.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JsonNodeValidator2 {

    // Collecion que define las reglas del campo especifico
    private final Map<String, List<Consumer<JsonNode>>> validationRules = new HashMap<>();

    public JsonNodeValidator2() {

        validationRules.put("personalInfo.firstName", Arrays.asList(
                node -> {
                    if (node == null || node.asText().trim().isEmpty()) {
                        throw new RuntimeException("El campo firstName no puede estar vacío o ser nulo.");
                    }
                }
        ));

        validationRules.put("personalInfo.lastName", Arrays.asList(
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

        validationRules.put("contactInfo.address", Arrays.asList(
                node -> {
                    String value = node.asText();
                    if (value.length() > 50) {
                        throw new RuntimeException("El campo address no puede tener más de 50 caracteres.");
                    }
                }
        ));

        validationRules.put("contactInfo.phoneNumber.line1", Arrays.asList(
                node -> {
                    String value = node.asText();
                    if (value.length() > 6) {
                        throw new RuntimeException("El campo line1 no puede tener más de 6 caracteres.");
                    }
                }
        ));

        validationRules.put("contactInfo.phoneNumber.line2", Arrays.asList(
                node -> {
                    String value = node.asText();
                    if (value.length() > 6) {
                        throw new RuntimeException("El campo line2 no puede tener más de 12 caracteres.");
                    }
                }
        ));

        validationRules.put("contactInfo.email", Arrays.asList(
                node -> {
                    String value = node.asText();
                    if (!value.matches("^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$")) {
                        throw new RuntimeException("El campo email debe tener un formato de correo electrónico válido.");
                    }
                }
        ));
    }

    public void validate(JsonNode jsonNode) {
        validateNode(jsonNode, "");
    }

    private void validateNode(JsonNode node, String parentKey) {
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldNode = entry.getValue();
                String fullKey = parentKey.isEmpty() ? fieldName : parentKey + "." + fieldName;

                // Validar el campo si tiene validaciones definidas
                if (validationRules.containsKey(fullKey)) {
                    validationRules.get(fullKey).forEach(validator -> validator.accept(fieldNode));
                } else {
                    // Si el campo tiene hijos, validar los hijos
                    validateNode(fieldNode, fullKey);
                }
            });
        } else {
            // Validar si el nodo actual es un campo esperado
            if (validationRules.containsKey(parentKey)) {
                validationRules.get(parentKey).forEach(validator -> validator.accept(node));
            }
        }
    }
}

