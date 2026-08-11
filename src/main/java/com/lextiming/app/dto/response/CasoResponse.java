package com.lextiming.app.dto.response;

import com.lextiming.app.model.enums.EstadoCaso;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CasoResponse {
    private String id;
    private String titulo;
    private String descripcion;
    private String numeroExpediente;
    private EstadoCaso estado;
    private String clienteId;
    private String clienteNombre;
    private String clienteApellido;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
