package com.example.Proyecto.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class TarjetaDto {
    private String marca;
    private String tipo;
    private String estado;
    private Float limiteDeExtraccion;
    private String vencimiento;

}
