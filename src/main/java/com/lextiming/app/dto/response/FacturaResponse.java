package com.lextiming.app.dto.response;

import com.lextiming.app.model.enums.EstadoFactura;
import com.lextiming.app.model.enums.MedioPago;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FacturaResponse {
    private String id;
    private String numeroFactura;
    private String casoId;
    private String casoTitulo;
    private String clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private Double monto;
    private Double horasTrabajadas;
    private Double tarifaPorHora;
    private LocalDateTime fechaEmision;
    private LocalDateTime fechaVencimiento;
    private EstadoFactura estado;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private MedioPago medioPago;
    private LocalDateTime fechaPago;
}
