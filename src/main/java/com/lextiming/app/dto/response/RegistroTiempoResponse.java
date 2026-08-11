package com.lextiming.app.dto.response;

import com.lextiming.app.model.enums.CategoriaTiempo;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class RegistroTiempoResponse {
    private String id;
    private String casoId;
    private String casoTitulo;
    private String usuarioId;
    private String usuarioNombre;
    private String usuarioApellido;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraInicioOriginal;  // 👈 Agregar este campo
    private LocalDateTime fechaHoraFin;
    private Double horasTotales;
    private Double horasAcumuladas;
    private String estadoTimer;
    private CategoriaTiempo categoria;
    private String descripcion;
    private Boolean facturable;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}