package com.prueba.dataservices.retries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@Slf4j
@RestController
@RequestMapping("/custom")
@RequiredArgsConstructor
public class CustomerController {

    private final ProcedureExec procedureExec;
    @GetMapping
    public ResponseEntity<String> get(@RequestBody String request) throws JsonProcessingException, SQLException {

        JsonNode jsonNode = new ObjectMapper().readTree(request);
        String storedProcedureQuery = procedureExec.getStoredProcedureQuery(jsonNode);
        if (true)throw  new RuntimeException();
        return ResponseEntity.ok(storedProcedureQuery);
    }

    @PostMapping
    public ResponseEntity<String> post(@RequestBody String request){

        log.info("Post Request - <<Fin>>");
        return ResponseEntity.ok().build();
    }
}
