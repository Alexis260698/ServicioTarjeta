package com.example.Proyecto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TarjetaConsulta {
    private String vencimiento;
    private String estado;
    private String numeroTarjeta;
    private Float limiteDeExtraccion;
}
