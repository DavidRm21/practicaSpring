package com.prueba.dataservices.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.prueba.dataservices.dto.ExcepcionNegocio;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;

@Builder
@Log4j2
public record BasicResponseDTO(
        @JsonProperty("codigoRespuesta")
        Long code,
        @JsonProperty("mensajeRespuesta")
        String message,
        Object data
) {

    public void validate () {
        log.info("{} == 1", code);
        if(this.code != 1 ){
            log.info("Excepcion");
            throw new ExcepcionNegocio(99L,"Algo sucedio");
        }
    }
    @Override
    public String toString() {
        return "BasicResponseDTO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
