package com.lextiming.app.dto.request;

import com.lextiming.app.model.enums.CategoriaTiempo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistroTiempoRequest {

    @NotNull(message = "El ID del caso es obligatorio")
    private String casoId;

    private LocalDateTime fechaHoraInicio;

    private LocalDateTime fechaHoraFin;

    @NotNull(message = "La categoría es obligatoria")
    private CategoriaTiempo categoria;

    private String descripcion;

    private Boolean facturable;
}
