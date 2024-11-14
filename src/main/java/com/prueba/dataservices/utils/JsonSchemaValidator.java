package com.prueba.dataservices.utils;

import com.google.gson.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.owasp.encoder.Encode;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class JsonSchemaValidator {
    private static final int MAX_STRING_LENGTH = 10000; // Límite para strings
    private static final int MAX_ARRAY_SIZE = 1000;     // Límite para arrays
    private static final int MAX_OBJECT_SIZE = 100;     // Límite para objetos
    private static final int MAX_NESTING_DEPTH = 20;    // Límite de anidación
    private static final Pattern SAFE_STRING_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\p{P}\\s^+$]*$");//("^[\\p{L}\\p{N}\\p{P}\\s]*$");
    private final JsonObject schema;
    private final Map<String, Pattern> patternCache;

    public JsonSchemaValidator(JsonObject schema) {
        validateSchema(schema);
        this.schema = schema;
        this.patternCache = new HashMap<>();
    }

    public JsonSchemaValidator(JsonObject schema, Map<String, Pattern> patternCache) {
        this.schema = schema;
        this.patternCache = patternCache;
    }

    public static class ValidationResult {
        private final List<String> errors;
        private boolean securityViolation;

        public ValidationResult() {
            this.errors = new ArrayList<>();
            this.securityViolation = false;
        }

        public void addError(String path, String message) {
            // Sanitizar mensajes
            String safePath = sanitizeString(path);
            String safeMessage = sanitizeString(message);
            errors.add(String.format("%s: %s", safePath, safeMessage));
        }

        public void markAsSecurityViolation() {
            this.securityViolation = true;
        }

        public boolean isValid() {
            return errors.isEmpty() && !securityViolation;
        }

        public boolean hasSecurityViolation() {
            return securityViolation;
        }

        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }
    }

    public ValidationResult validate(JsonObject json) {
        ValidationResult result = new ValidationResult();
        try {
            validateSecurityConstraints(json, 0, result);
            if (!result.hasSecurityViolation()) {
                validateAgainstSchema(json, schema, "", result, 0);
            }
        } catch (SecurityException e) {
            result.markAsSecurityViolation();
            result.addError("security", "Violación de seguridad detectada: " + sanitizeString(e.getMessage()));
        }
        return result;
    }

    private void validateSecurityConstraints(JsonElement element, int depth, ValidationResult result) {
        if (depth > MAX_NESTING_DEPTH) {
            throw new SecurityException("Excedido el límite máximo de anidación");
        }

        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.size() > MAX_OBJECT_SIZE) {
                throw new SecurityException("Excedido el límite máximo de propiedades del objeto");
            }
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                validatePropertyName(entry.getKey());
                validateSecurityConstraints(entry.getValue(), depth + 1, result);
            }
        } else if (element.isJsonArray()) {
            JsonArray arr = element.getAsJsonArray();
            if (arr.size() > MAX_ARRAY_SIZE) {
                throw new SecurityException("Excedido el límite máximo de elementos del array");
            }
            for (JsonElement item : arr) {
                validateSecurityConstraints(item, depth + 1, result);
            }
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                validateString(primitive.getAsString());
            }
        }
    }

    private void validateString(String value) {
        if (value.length() > MAX_STRING_LENGTH) {
            throw new SecurityException("Excedido el límite máximo de longitud de string");
        }
        if (!SAFE_STRING_PATTERN.matcher(value).matches()) {
            throw new SecurityException("Caracteres no permitidos en el string: " + value);
        }
        // Verificar caracteres peligrosos
        if (containsDangerousCharacters(value)) {
            throw new SecurityException("Detectados caracteres potencialmente peligrosos");
        }
    }

    private boolean containsDangerousCharacters(String value) {
        // Patrones de ataques
        String[] dangerousPatterns = {
                "<script", "javascript:", "vbscript:",
                "onload=", "onerror=", "onclick=", "file:", "document.",
                "eval(", "setTimeout(", "setInterval(",
                "../../", "/../", "%00",
                "\\u0000", "\\x00"
        }; //"data:"

        String lowercaseValue = value.toLowerCase();
        for (String pattern : dangerousPatterns) {
            if (lowercaseValue.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private void validatePropertyName(String name) {
        if (name.length() > 256) { // Límite razonable para nombres de propiedades
            throw new SecurityException("Nombre de propiedad demasiado largo");
        }
        if (!SAFE_STRING_PATTERN.matcher(name).matches()) {
            throw new SecurityException("Caracteres no permitidos en el nombre de propiedad: "+ name);
        }
    }

    private void validateSchema(JsonObject schema) {
        // Validar que el esquema no contenga patrones maliciosos
        try {
            validateSecurityConstraints(schema, 0, new ValidationResult());
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Esquema no seguro: " + e.getMessage());
        }
    }

    private static String sanitizeString(String input) {
        if (input == null) {
            return "";
        }
        // Aplicar múltiples capas de sanitización
        String sanitized = input;
        sanitized = StringEscapeUtils.escapeHtml4(sanitized);  // Escapar HTML
        sanitized = StringEscapeUtils.escapeJava(sanitized);   // Escapar caracteres especiales
        sanitized = Encode.forHtml(sanitized);                 // Codificación OWASP
        return sanitized;
    }

    private void validateAgainstSchema(JsonElement json, JsonObject schema, String path,
                                       ValidationResult result, int depth) {
        if (depth > MAX_NESTING_DEPTH) {
            result.markAsSecurityViolation();
            return;
        }

        try {
            String type = getStringProperty(schema, "type");
            if (type != null && !validateType(json, type)) {
                result.addError(path, "tipo inválido, se esperaba: " + type);
                return;
            }

            if (json.isJsonObject()) {
                validateObject(json.getAsJsonObject(), schema, path, result);
            }

            String pattern = getStringProperty(schema, "pattern");
            if (pattern != null && json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                validatePattern(json.getAsString(), pattern, path, result);
            }

            JsonElement enumElement = schema.get("enum");
            if (enumElement != null && enumElement.isJsonArray()) {
                validateEnum(json, enumElement.getAsJsonArray(), path, result);
            }

            if (type != null && (type.equals("number") || type.equals("integer"))) {
                validateNumeric(json, schema, path, result);
            }
        } catch (Exception e) {
            result.addError(path, "Error de validación: " + sanitizeString(e.getMessage()));
        }
    }

    private void validateObject(JsonObject json, JsonObject schema, String path, ValidationResult result) {
        // Validar propiedades requeridas
        JsonElement requiredElement = schema.get("required");
        if (requiredElement != null && requiredElement.isJsonArray()) {
            for (JsonElement req : requiredElement.getAsJsonArray()) {
                String requiredField = req.getAsString();
                if (!json.has(requiredField) || json.get(requiredField).isJsonNull()) {
                    result.addError(path + (path.isEmpty() ? "" : ".") + requiredField, "campo requerido");
                }
            }
        }

        // Validar propiedades
        JsonObject properties = schema.getAsJsonObject("properties");
        if (properties != null) {
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String propertyName = entry.getKey();
                JsonElement propertyValue = entry.getValue();
                JsonElement propertySchema = properties.get(propertyName);

                if (propertySchema != null && propertySchema.isJsonObject()) {
                    String newPath = path + (path.isEmpty() ? "" : ".") + propertyName;
                    validateAgainstSchema(propertyValue, propertySchema.getAsJsonObject(), newPath, result, 0);
                }
            }
        }
    }

    private void validatePattern(String value, String pattern, String path, ValidationResult result) {
        try {
            Pattern compiledPattern = patternCache.computeIfAbsent(pattern, p -> {
                if (p.length() > 1000) { // Límite para patrones regex
                    throw new SecurityException("Patrón regex demasiado largo");
                }
                return Pattern.compile(p);
            });
            if (!compiledPattern.matcher(value).matches()) {
                result.addError(path, "no coincide con el patrón requerido");
            }
        } catch (PatternSyntaxException e) {
            result.addError(path, "patrón de validación inválido");
        } catch (SecurityException e) {
            result.markAsSecurityViolation();
        }
    }

    private void validateEnum(JsonElement value, JsonArray enumValues, String path, ValidationResult result) {
        boolean valid = false;
        for (JsonElement enumValue : enumValues) {
            if (value.equals(enumValue)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            result.addError(path, "valor no permitido en la enumeración");
        }
    }

    private void validateNumeric(JsonElement value, JsonObject schema, String path, ValidationResult result) {
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            result.addError(path, "se esperaba un valor numérico");
            return;
        }

        double numValue = value.getAsDouble();

        // Validar minimum
        JsonElement minimum = schema.get("minimum");
        if (minimum != null && numValue < minimum.getAsDouble()) {
            result.addError(path, "valor menor al mínimo permitido: " + minimum.getAsDouble());
        }

        // Validar maximum
        JsonElement maximum = schema.get("maximum");
        if (maximum != null && numValue > maximum.getAsDouble()) {
            result.addError(path, "valor mayor al máximo permitido: " + maximum.getAsDouble());
        }

        // Validar multipleOf
        JsonElement multipleOf = schema.get("multipleOf");
        if (multipleOf != null) {
            double factor = multipleOf.getAsDouble();
            if (numValue % factor != 0) {
                result.addError(path, "valor debe ser múltiplo de: " + factor);
            }
        }
    }

    private boolean validateType(JsonElement value, String expectedType) {
        switch (expectedType) {
            case "string":
                return value.isJsonPrimitive() && value.getAsJsonPrimitive().isString();
            case "number":
                return value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
            case "integer":
                return value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()
                        && value.getAsDouble() == value.getAsLong();
            case "boolean":
                return value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean();
            case "object":
                return value.isJsonObject();
            case "array":
                return value.isJsonArray();
            case "null":
                return value.isJsonNull();
            default:
                return false;
        }
    }

    private String getStringProperty(JsonObject obj, String property) {
        JsonElement element = obj.get(property);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
    }

    // ... [Resto de métodos de validación como en la versión anterior]
    // Los métodos validateObject, validateEnum, validateNumeric, validateType y getStringProperty
    // permanecen iguales pero deben incluir el parámetro depth para control de anidación
    public static void main(String[] args) {
        // Definir el esquema
        String schemaStr = """
        {
          "type": "object",
          "properties": {
            "name": { "type": "string" },
            "phone": { 
              "type": "string",
              "pattern": "^[\\\\+]?[(]?[0-9]{3}[)]?[-\\\\s\\\\.]?[0-9]{3}[-\\\\s\\\\.]?[0-9]{4,6}$"
            },
            "address": {
              "type": "object",
              "properties": {
                "address": { "type": "string" },
                "city": { "type": "string" },
                "postalCode": {
                  "type": "string",
                  "pattern": "^[0-9]{5}(?:-[0-9]{4})?$"
                },
                "state": {
                  "type": "string",
                  "enum": ["AL", "AK", "AZ", "AR", "CA", "CO", "NY"]
                }
              },
              "required": ["address", "city", "postalCode", "state"]
            }
          },
          "required": ["name", "phone", "address"]
        }
        """;

        // Crear el validador con el esquema
        JsonObject schema = JsonParser.parseString(schemaStr).getAsJsonObject();
        JsonSchemaValidator validator = new JsonSchemaValidator(schema);

        // JSON a validar
        String jsonStr = """
        {
            "name": "John Doe",
            "phone": "123-456-7890",
            "address": {
                "address": "123 Main St",
                "city": "New York",
                "postalCode": "10001",
                "state": "NY"
            }
        }
        """;

        // Validar
        JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
        JsonSchemaValidator.ValidationResult result = validator.validate(json);

        // Mostrar resultados
        if (result.isValid()) {
            System.out.println("El JSON es válido según el esquema");
        } else {
            System.out.println("Errores de validación encontrados:");
            for (String error : result.getErrors()) {
                System.out.println("- " + error);
            }
        }
    }

}
