package com.lextiming.app.dto.request;

import com.lextiming.app.model.enums.EstadoCaso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CasoRequest {

    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    private String descripcion;

    private String numeroExpediente;

    @NotNull(message = "El ID del cliente es obligatorio")
    private String clienteId;

    private EstadoCaso estado;
}
