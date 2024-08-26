package com.prueba.dataservices.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "INCIDENCIAS")
@Builder @Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ErrorDatabase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "titulo")
    private String title;
    @Column(name = "descripcion")
    private String description;
    @Column(name = "causa")
    private String cause;
    @Column(name = "solucion")
    private String solution;

    @Override
    public String toString() {
        return "ErrorDatabase{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", cause='" + cause + '\'' +
                ", solution='" + solution + '\'' +
                '}';
    }
}

