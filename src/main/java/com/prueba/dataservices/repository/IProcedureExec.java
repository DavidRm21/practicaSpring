package com.prueba.dataservices.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.prueba.dataservices.entity.MiNombre;

import java.sql.SQLException;

public interface IProcedureExec {

    String getStoredProcedureQuery(JsonNode miNombre) throws SQLException, JsonProcessingException;
}
