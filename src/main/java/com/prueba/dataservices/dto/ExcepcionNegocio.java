package com.prueba.dataservices.dto;

import lombok.*;

@Getter
@NoArgsConstructor
public class ExcepcionNegocio extends RuntimeException {
    private Long codigo;
    private String mensaje;

    public ExcepcionNegocio(String message) {
        super(message);
    }

    public ExcepcionNegocio(Long codigo, String message) {
        this.codigo = codigo;
        this.mensaje = message;
    }
}
