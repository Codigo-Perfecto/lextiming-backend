package com.lextiming.app.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ClienteResponse {
    private String id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;
    private String documento;
    private String direccion;
    private String notas;
    private LocalDateTime fechaCreacion;
}
