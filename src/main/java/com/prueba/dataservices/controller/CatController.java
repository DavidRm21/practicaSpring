package com.prueba.dataservices.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prueba.dataservices.utils.constant.Constantes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Collections;
import java.util.Map;


@Slf4j
@RestController
public class CatController {

    @GetMapping(value = "cat/{issuerId}/x/{consumerId}")
    public ResponseEntity<?> mao (@RequestBody String req) throws JsonProcessingException {
        log.info("controller mao: {}", req);
        log.info("controller mao: {}", Constantes.HOLA);

        Map<String, Boolean> a = Collections.singletonMap("exitoso", true);
        log.info("obje {}", a);

        return ResponseEntity.ok(req);
    }

    @GetMapping(value = "cat")
    public ResponseEntity<?> cat (@RequestBody String req) throws JsonProcessingException {
        log.info("controller cat : {}", req);

        return ResponseEntity.ok(req);
    }
}
