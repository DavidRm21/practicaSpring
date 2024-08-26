package com.prueba.dataservices.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.prueba.dataservices.entity.ErrorDatabase;
import com.prueba.dataservices.entity.MiNombre;
import com.prueba.dataservices.entity.Validaciones;
import com.prueba.dataservices.repository.IErroreDatabaseRepo;
import com.prueba.dataservices.repository.IProcedureExec;
import com.prueba.dataservices.utils.EncoderUtils;
import com.prueba.dataservices.utils.JsonNodeValidator;
import com.prueba.dataservices.utils.JsonNodeValidator2;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@RestController
@Validated
@RequestMapping("/error")
public class ErrorDatabaseController {

    private final IErroreDatabaseRepo _erroreDatabaseRepo;
    private final EncoderUtils encoderUtils;
    private final IProcedureExec procedureExec;
    private final ObjectMapper objectMapper;
    private int codifoRes;

    public ErrorDatabaseController(IErroreDatabaseRepo erroreDatabaseRepo, EncoderUtils encoderUtils, IProcedureExec procedureExec, ObjectMapper objectMapper) {
        _erroreDatabaseRepo = erroreDatabaseRepo;
        this.encoderUtils = encoderUtils;
        this.procedureExec = procedureExec;
        this.objectMapper = objectMapper;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    @PostMapping("/test/hoy")
    public ResponseEntity<String> ola(@RequestBody String o){
        log.info("Inicio de test_ {}", o);
        log.info(2+8);
        log.info("fin Test");
        return new ResponseEntity<>(o, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<String> findAll(){
        List<ErrorDatabase> listaErrores = _erroreDatabaseRepo.findAll();

        String response = encoderUtils.serializedJson(listaErrores);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> post(@RequestBody String request){
        log.info("Se recibe la peticion: {}", request);
        String errorDesearizable = encoderUtils.deserializedJson(request);
        log.info("Se decodifica: {}", errorDesearizable);
        return new ResponseEntity<>(errorDesearizable.toString(), HttpStatus.CREATED);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validate (@RequestBody String requestEncoded) throws JsonProcessingException {

        log.info("Request: {}", requestEncoded);
        JsonNode jsonNode = objectMapper.readTree(requestEncoded);
        log.info("Request en JsonNode: {}", jsonNode);

        log.info("Buscando valores nulos de :{}", jsonNode);
        JsonNodeValidator2 validator = new JsonNodeValidator2();
        validator.validate(jsonNode);

        log.info("Final todo Ok");


        return ResponseEntity.ok().build();



    }

    @PostMapping("/procedure")
    public ResponseEntity<String> procedure (@RequestBody String requestEncoded) throws JsonProcessingException {
        log.info("Ingreso de ejecucion del procedimiento: {}", requestEncoded);
        String resultDecoded = new String(Base64.getDecoder().decode(requestEncoded));
        log.info("Resultado de decofificacion: ", resultDecoded);
        MiNombre miNombre = objectMapper.readValue(resultDecoded, MiNombre.class);
        log.info("Objeto: {}", miNombre);
        String json= objectMapper.writeValueAsString(miNombre);
        log.info(Base64.getEncoder().encodeToString(json.getBytes()));
        //String result = procedureExec.getStoredProcedureQuery(miNombre);
        log.info("fin del metodo: {}", json);
        return ResponseEntity.ok(json);

    }
    @PostMapping("/test")
    public ResponseEntity<String> test(@RequestBody String requestEncoded) throws JsonProcessingException {
        if (StringUtils.isBlank(requestEncoded)){
            return ResponseEntity.badRequest().build();
        }


        String decodedRequest = new String(Base64.getDecoder().decode(requestEncoded));
        Map<?, ?> responseBody = objectMapper.readValue(decodedRequest, Map.class);
        log.info("Validando el codigo de respuesta : {}", (responseBody).get("codigoRespuesta"));

        codifoRes = (int)(responseBody).get("codigoRespuesta");
        if (codifoRes == 200) {
            log.info("Validando Vardedare");

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("nombre", "David");
            respuesta.put("edad", 20);
            respuesta.put("soltero", true);

            Optional.of(respuesta).map(item -> {
                log.info("item: {}", item);
                return ResponseEntity.accepted().build();
            });

            BasicResponseDTO basicResponseDTO =
                    new BasicResponseDTO(
                            1L,
                            "Transaccion Exitosa",
                            respuesta
                    );
            String basicEncoded = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(basicResponseDTO));
            log.info("basicResponse: {}", basicEncoded);

            log.info("--------------------------------------------------------------");

            res(basicEncoded);
            log.info("repuest:");



            return ResponseEntity.ok(Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(respuesta)));
        }
        log.info("Validando Falsa");

        return ResponseEntity.ok(Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(responseBody)));
    }

    private void res (String basicEncoded) throws JsonProcessingException {
        BasicResponseDTO responseTest;
        try{
            String basicDecod = new String(Base64.getDecoder().decode(basicEncoded));
            responseTest = objectMapper.readValue(basicDecod, BasicResponseDTO.class);
            log.info("El basic response es valido");
//            if (!responseTest.validate()) throw new RuntimeException();
        }catch (Exception e){
            log.info("ocurrio una excepcion");
        }


    }



}




