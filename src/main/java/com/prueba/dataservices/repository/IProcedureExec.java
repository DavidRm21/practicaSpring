package com.prueba.dataservices.repository;

import com.prueba.dataservices.entity.MiNombre;

public interface IProcedureExec {

    String getStoredProcedureQuery(MiNombre miNombre);
}
