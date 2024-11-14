package com.prueba.dataservices.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class JsonDataExtractor {

    private final JsonObject json;

    public JsonDataExtractor(JsonObject json) {
        this.json = json != null ? json : new JsonObject();
    }

    /**
     * Clase para manejar valores opcionales con tipo
     */
    public static class OptionalValue<T> {
        private final T value;
        private final boolean present;
        private final String error;

        private OptionalValue(T value, boolean present, String error) {
            this.value = value;
            this.present = present;
            this.error = error;
        }

        public static <T> OptionalValue<T> of(T value) {
            return new OptionalValue<>(value, true, null);
        }

        public static <T> OptionalValue<T> empty() {
            return new OptionalValue<>(null, false, null);
        }

        public static <T> OptionalValue<T> error(String error) {
            return new OptionalValue<>(null, false, error);
        }

        public boolean isPresent() {
            return present;
        }

        public boolean hasError() {
            return error != null;
        }

        public T get() {
            return value;
        }

        public T orElse(T other) {
            return present ? value : other;
        }

        public String getError() {
            return error;
        }
    }

    /**
     * Obtener String con manejo seguro
     */
    public OptionalValue<String> getString(String path) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (element.isJsonPrimitive()) {
                return OptionalValue.of(element.getAsString());
            } else if (element.isJsonObject() || element.isJsonArray()) {
                return OptionalValue.of(element.toString());
            }

            return OptionalValue.error("Tipo de dato incompatible con String");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener String: " + e.getMessage());
        }
    }

    /**
     * Obtener Integer con manejo seguro y conversión flexible
     */
    public OptionalValue<Integer> getInteger(String path) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    return OptionalValue.of(element.getAsInt());
                } else if (element.getAsJsonPrimitive().isString()) {
                    // Intenta convertir string a integer
                    String value = element.getAsString().trim();
                    return OptionalValue.of(Integer.parseInt(value));
                }
            }

            return OptionalValue.error("Tipo de dato incompatible con Integer");
        } catch (NumberFormatException e) {
            return OptionalValue.error("Valor no puede ser convertido a Integer");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener Integer: " + e.getMessage());
        }
    }

    /**
     * Obtener Double con manejo seguro y conversión flexible
     */
    public OptionalValue<Double> getDouble(String path) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    return OptionalValue.of(element.getAsDouble());
                } else if (element.getAsJsonPrimitive().isString()) {
                    // Intenta convertir string a double
                    String value = element.getAsString().trim();
                    return OptionalValue.of(Double.parseDouble(value));
                }
            }

            return OptionalValue.error("Tipo de dato incompatible con Double");
        } catch (NumberFormatException e) {
            return OptionalValue.error("Valor no puede ser convertido a Double");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener Double: " + e.getMessage());
        }
    }

    /**
     * Obtener BigDecimal con manejo seguro y conversión flexible
     */
    public OptionalValue<BigDecimal> getBigDecimal(String path) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isNumber()) {
                    return OptionalValue.of(element.getAsBigDecimal());
                } else if (element.getAsJsonPrimitive().isString()) {
                    // Intenta convertir string a BigDecimal
                    String value = element.getAsString().trim();
                    return OptionalValue.of(new BigDecimal(value));
                }
            }

            return OptionalValue.error("Tipo de dato incompatible con BigDecimal");
        } catch (NumberFormatException e) {
            return OptionalValue.error("Valor no puede ser convertido a BigDecimal");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener BigDecimal: " + e.getMessage());
        }
    }

    /**
     * Obtener Boolean con manejo seguro y conversión flexible
     */
    public OptionalValue<Boolean> getBoolean(String path) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (element.isJsonPrimitive()) {
                if (element.getAsJsonPrimitive().isBoolean()) {
                    return OptionalValue.of(element.getAsBoolean());
                } else if (element.getAsJsonPrimitive().isString()) {
                    // Manejo flexible de strings booleanos
                    String value = element.getAsString().trim().toLowerCase();
                    if (value.equals("true") || value.equals("1") || value.equals("yes") || value.equals("si")) {
                        return OptionalValue.of(true);
                    } else if (value.equals("false") || value.equals("0") || value.equals("no")) {
                        return OptionalValue.of(false);
                    }
                } else if (element.getAsJsonPrimitive().isNumber()) {
                    // Consideramos 0 como false y cualquier otro número como true
                    return OptionalValue.of(element.getAsInt() != 0);
                }
            }

            return OptionalValue.error("Tipo de dato incompatible con Boolean");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener Boolean: " + e.getMessage());
        }
    }

    /**
     * Obtener LocalDate con manejo seguro y múltiples formatos
     */
    public OptionalValue<LocalDate> getDate(String path, String... formats) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                return OptionalValue.error("Tipo de dato incompatible con LocalDate");
            }

            String dateStr = element.getAsString().trim();
            List<String> dateFormats = formats.length > 0 ?
                    Arrays.asList(formats) :
                    Arrays.asList("yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy");

            for (String format : dateFormats) {
                try {
                    return OptionalValue.of(LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format)));
                } catch (DateTimeParseException ignored) {
                    // Intenta el siguiente formato
                }
            }

            return OptionalValue.error("No se pudo parsear la fecha con los formatos disponibles");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener LocalDate: " + e.getMessage());
        }
    }

    /**
     * Obtener LocalDateTime con manejo seguro y múltiples formatos
     */
    public OptionalValue<LocalDateTime> getDateTime(String path, String... formats) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                return OptionalValue.error("Tipo de dato incompatible con LocalDateTime");
            }

            String dateStr = element.getAsString().trim();
            List<String> dateFormats = formats.length > 0 ?
                    Arrays.asList(formats) :
                    Arrays.asList("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy HH:mm:ss");

            for (String format : dateFormats) {
                try {
                    return OptionalValue.of(LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(format)));
                } catch (DateTimeParseException ignored) {
                    // Intenta el siguiente formato
                }
            }

            return OptionalValue.error("No se pudo parsear la fecha y hora con los formatos disponibles");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener LocalDateTime: " + e.getMessage());
        }
    }

    /**
     * Obtener JsonArray con manejo seguro
     */
    public OptionalValue<JsonArray> getArray(String path) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (element.isJsonArray()) {
                return OptionalValue.of(element.getAsJsonArray());
            }

            return OptionalValue.error("El elemento no es un array");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener JsonArray: " + e.getMessage());
        }
    }

    /**
     * Obtener JsonObject con manejo seguro
     */
    public OptionalValue<JsonObject> getObject(String path) {
        try {
            JsonElement element = getElementByPath(path);
            if (element == null || element.isJsonNull()) {
                return OptionalValue.empty();
            }

            if (element.isJsonObject()) {
                return OptionalValue.of(element.getAsJsonObject());
            }

            return OptionalValue.error("El elemento no es un objeto");
        } catch (Exception e) {
            return OptionalValue.error("Error al obtener JsonObject: " + e.getMessage());
        }
    }

    /**
     * Obtener elemento por path con soporte para notación de punto
     */
    private JsonElement getElementByPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        JsonElement current = json;

        for (String part : parts) {
            if (current == null || current.isJsonNull()) {
                return null;
            }

            if (current.isJsonObject()) {
                current = current.getAsJsonObject().get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    public static void main(String[] args) {
        String jsonStr = """
        {
            "id": "123",
            "name": "John Doe",
            "age": "30",
            "active": "1",
            "salary": "1234.56",
            "birthDate": "1990-01-01",
            "lastLogin": "2024-01-01 14:30:00",
            "address": {
                "street": "Main St",
                "number": 123
            },
            "phones": ["123-456", "789-012"],
            "nullField": null,
            "emptyObject": {}
        }
        """;

        JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
        JsonDataExtractor extractor = new JsonDataExtractor(jsonObject);

        // String normal
        String name = extractor.getString("name").orElse("Unknown");
        System.out.println("Name: " + name);

        // Integer desde string
        OptionalValue<Integer> age = extractor.getInteger("age");
        if (age.isPresent()) {
            System.out.println("Age: " + age.get());
        }

        // Boolean desde string numérico
        Boolean isActive = extractor.getBoolean("active").orElse(false);
        System.out.println("Is active: " + isActive);

        // BigDecimal desde string
        BigDecimal salary = extractor.getBigDecimal("salary").orElse(BigDecimal.ZERO);
        System.out.println("Salary: " + salary);

        // Fecha
        LocalDate birthDate = extractor.getDate("birthDate").orElse(LocalDate.now());
        System.out.println("Birth date: " + birthDate);

        // Fecha y hora
        OptionalValue<LocalDateTime> lastLogin = extractor.getDateTime("lastLogin");
        if (!lastLogin.hasError()) {
            System.out.println("Last login: " + lastLogin.orElse(null));
        }

        // Objeto anidado
        OptionalValue<String> street = extractor.getString("address.street");
        System.out.println("Street: " + street.orElse("No street"));

        // Array
        OptionalValue<JsonArray> phones = extractor.getArray("phones");
        if (phones.isPresent()) {
            System.out.println("Phones: " + phones.get());
        }

        // Campo nulo
        OptionalValue<String> nullField = extractor.getString("nullField");
        System.out.println("Null field present: " + nullField.isPresent());

        // Campo inexistente
        OptionalValue<String> nonExistent = extractor.getString("nonExistent");
        System.out.println("Non-existent field present: " + nonExistent.isPresent());

        // Campo con tipo incorrecto
        OptionalValue<Integer> wrongType = extractor.getInteger("name");
        if (wrongType.hasError()) {
            System.out.println("Error: " + wrongType.getError());
        }

    }
}
