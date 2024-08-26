package com.prueba.dataservices.entity;


import jakarta.persistence.NamedStoredProcedureQuery;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureParameter;
import lombok.*;

@NamedStoredProcedureQuery(
        name = "Test",
        procedureName = "PRO_TEST",
        resultClasses = {MiNombre.class},
        parameters = {
                @StoredProcedureParameter(name = "PAR_CRISTIAN_IN", type = Integer.class, mode = ParameterMode.IN),
                @StoredProcedureParameter(name = "PAR_DAVID_IN", type = String.class, mode = ParameterMode.IN),
                @StoredProcedureParameter(name = "PAR_ROMERO_OUT", type = String.class, mode = ParameterMode.OUT),
                @StoredProcedureParameter(name = "PAR_MELO_OUT", type = Integer.class, mode = ParameterMode.OUT)
        }
)
@Builder
//public record MiNombre(Integer cristian, String david, String romero, Integer melo) {
//
//    public boolean isSuccess(){
//        return cristian.equals(1) || String.valueOf(cristian).equals("1");
//    }
//
//}
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MiNombre{
    private Integer cristian;
    private String david;
    private String romero;
    private Integer melo;

    public boolean isSuccess(){
        return this.cristian.equals(1) || String.valueOf(cristian).equals("1");
    }
}
