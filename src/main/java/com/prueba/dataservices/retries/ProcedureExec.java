package com.prueba.dataservices.retries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.prueba.dataservices.dto.ClaseFall;
import com.prueba.dataservices.entity.MiNombre;
import com.prueba.dataservices.repository.IProcedureExec;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Repository
@RequiredArgsConstructor
public class ProcedureExec implements IProcedureExec {

    private final EntityManager entityManager;


    public String getStoredProcedureQuery(JsonNode jsonNode) throws SQLException, JsonProcessingException {
        log.info("Inicio del procdemiento");


        StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("PKG_TEST.PRO_RETRIES");
        log.info("Preparando entradas");
        // Registrar los parámetros de entrada y salida
        storedProcedureQuery.registerStoredProcedureParameter("PAR_CONTEXT_PATH_IN", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_METHOD_IN", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_BODY_IN", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_STATUS_CODE_IN", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_ERROR_MESSAGE_IN", String.class, ParameterMode.IN);

        storedProcedureQuery.registerStoredProcedureParameter("PAR_RESULT_OUT", String.class, ParameterMode.OUT);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_CODIGO_OUT", String.class, ParameterMode.OUT);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_CURSOR_OUT", Void.class, ParameterMode.REF_CURSOR);

        log.info("Seteando valores.");
        // Configuramos el valor de entrada
        // Assuming the actual procedure parameter names are consistent with registration
        storedProcedureQuery.setParameter("PAR_CONTEXT_PATH_IN", jsonNode.path("contextPath").asText());
        storedProcedureQuery.setParameter("PAR_METHOD_IN", jsonNode.path("method").asText());
        storedProcedureQuery.setParameter("PAR_BODY_IN", jsonNode.path("body").asText());
        storedProcedureQuery.setParameter("PAR_STATUS_CODE_IN", jsonNode.path("statusCode").asText());
        storedProcedureQuery.setParameter("PAR_ERROR_MESSAGE_IN", jsonNode.path("errorMessage").asText());
        log.info("Ejecutando ...");

        // If the procedure requires specific input for output parameters
//        storedProcedureQuery.setParameter("romero", miNombre.romero());
//        storedProcedureQuery.setParameter("melo", miNombre.melo());


        // Realizamos la llamada al procedimiento
        storedProcedureQuery.execute();

        // Obtenemos los valores de salida
        log.info("Se construye en objeto");
        Map<String, String> result = new HashMap<>();
                result.put("result",(String)storedProcedureQuery.getOutputParameterValue("PAR_RESULT_OUT"));
                result.put("codigo",(String)storedProcedureQuery.getOutputParameterValue("PAR_CODIGO_OUT"));
                log.info("result {}", result);
//        List<Object[]> results = storedProcedureQuery.getResultList();
//        log.info("REf cursor: {}", results);
        ResultSet resultSet = (ResultSet) storedProcedureQuery.getOutputParameterValue("PAR_CURSOR_OUT");

        // Procesar el resultado
        List<JsonNode> jsonNodes = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        while (resultSet.next()) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put( "contextPath", resultSet.getString(1));
            objectNode.put( "method", resultSet.getString(2));
            objectNode.put( "bodyResponse", resultSet.getString(3));
            objectNode.put( "statusCode", resultSet.getString(4).equals("404")?"BADREQUEST":"OK");
            objectNode.put( "errorMessage", resultSet.getString(5));
            jsonNodes.add(objectNode);
        }
        resultSet.close();
//
        log.info("Fin {}", jsonNodes);
        Gson gson = new Gson();
        return objectMapper.writeValueAsString(jsonNodes);
    }

}
