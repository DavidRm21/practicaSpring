package com.prueba.dataservices.utils;

import com.google.gson.Gson;
import com.prueba.dataservices.entity.MiNombre;
import com.prueba.dataservices.repository.IProcedureExec;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.util.Base64;

@Log4j2
@Repository
@RequiredArgsConstructor
public class ProcedureExec implements IProcedureExec {

    private final EntityManager entityManager;


    @Override
    public String getStoredProcedureQuery(MiNombre miNombre) {
        log.info("Inicio del procdemiento");


        StoredProcedureQuery storedProcedureQuery = entityManager.createStoredProcedureQuery("PKG_TEST.PRO_TEST");
        log.info("Preparando entradas");
        // Registrar los parámetros de entrada y salida
        storedProcedureQuery.registerStoredProcedureParameter("PAR_CRISTIAN_IN", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_DAVID_IN", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_ROMERO_OUT", String.class, ParameterMode.OUT);
        storedProcedureQuery.registerStoredProcedureParameter("PAR_MELO_OUT", Integer.class, ParameterMode.OUT);

        log.info("Seteando valores.");
        // Configuramos el valor de entrada
        // Assuming the actual procedure parameter names are consistent with registration
        storedProcedureQuery.setParameter("PAR_CRISTIAN_IN", miNombre.getCristian());
        storedProcedureQuery.setParameter("PAR_DAVID_IN", miNombre.getDavid());
        log.info("Ejecutando ...");

        // If the procedure requires specific input for output parameters
//        storedProcedureQuery.setParameter("romero", miNombre.romero());
//        storedProcedureQuery.setParameter("melo", miNombre.melo());


        // Realizamos la llamada al procedimiento
        storedProcedureQuery.execute();

        // Obtenemos los valores de salida
        log.info("Se construye en objeto");
        MiNombre result = MiNombre.builder()
                .romero((String)storedProcedureQuery.getOutputParameterValue("PAR_ROMERO_OUT"))
                .melo((Integer)storedProcedureQuery.getOutputParameterValue("PAR_MELO_OUT")).build();
//        Map<String, Object> result = new HashMap<>();
//        result.put("romero", (String)storedProcedureQuery.getOutputParameterValue("PAR_ROMERO_OUT"));
//        result.put("melo", (Integer)storedProcedureQuery.getOutputParameterValue("PAR_MELO_OUT"));
        Gson gson = new Gson();
        return Base64.getEncoder().encodeToString((gson.toJson(result)).getBytes());
    }
}
