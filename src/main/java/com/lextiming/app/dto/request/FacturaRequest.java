package com.lextiming.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FacturaRequest {

    @NotBlank(message = "El ID del caso es obligatorio")
    private String casoId;

    @NotBlank(message = "El número de factura es obligatorio")
    private String numeroFactura;

    @Positive(message = "El monto debe ser mayor a 0")
    private Double monto;

    private Double horasTrabajadas;

    private Double tarifaPorHora;

    private LocalDateTime fechaVencimiento;

    private String descripcion;
}
