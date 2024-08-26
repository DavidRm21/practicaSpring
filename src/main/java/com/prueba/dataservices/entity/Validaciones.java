package com.prueba.dataservices.entity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Validaciones {

            @NotBlank(message = "No pude estar vacio")
            @NotNull(message = "no puede ser nulo")
            private String firstName;
            @Size(min = 2, max = 50, message = "Dimension incorrcta")
            private String lastName;
            @Positive(message = "no puede ser negativo")
            Integer age;
            @Size(max = 50)
            private String address;
            @Size(max = 12)
            private String phoneNumber;
            private String status;
}
