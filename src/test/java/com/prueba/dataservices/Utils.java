package com.prueba.dataservices;

import com.google.gson.Gson;
import com.prueba.dataservices.utils.EncoderUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

public class Utils {

    private static final Logger log = LogManager.getLogger(Utils.class);
    private EncoderUtils encoderUtils;

    @BeforeEach
    void setup(){
        Gson gson = new Gson();
        encoderUtils = new EncoderUtils(gson);
    }

    @ParameterizedTest
    @MethodSource("shouldEncodeArguments")
    void shouldEncode(String actual, String expected) {

        // WHEN
        log.info("String recibido para codificar en base 64: {}", actual);
        String act = encoderUtils.encodeBase64(actual);
        log.info("String codificado: {} -> {}", act, expected);
        // THEN
        assertEquals(expected, act);
    }

    @ParameterizedTest
    @MethodSource("shouldDecodeArguments")
    void shouldDecode(String actual, String expected) {

        // WHEN
        log.info("String recibido para decodificar: {}", actual);
        String dec = encoderUtils.decodeBase64(actual);
        log.info("String decodificado: {} -> {}", dec, expected);
        // THEN
        assertEquals(expected, dec);


    }



    public static Stream<Arguments> shouldEncodeArguments(){
        return Stream.of(
                Arguments.arguments("employees", "ZW1wbG95ZWVz"),
                Arguments.arguments("{\"id\": 3, \"name\":\"Cristian\"}", "eyJpZCI6IDMsICJuYW1lIjoiQ3Jpc3RpYW4ifQ==")
        );
    }

    public static Stream<Arguments> shouldDecodeArguments(){
        return Stream.of(
                Arguments.arguments("ZW1wbG95ZWVz", "employees"),
                Arguments.arguments("eyJpZCI6IDMsICJuYW1lIjoiQ3Jpc3RpYW4ifQ==", "{\"id\": 3, \"name\":\"Cristian\"}")
        );
    }


}
