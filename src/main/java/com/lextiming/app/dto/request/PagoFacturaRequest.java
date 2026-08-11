package com.lextiming.app.dto.request;

import com.lextiming.app.model.enums.MedioPago;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PagoFacturaRequest {

    @NotNull(message = "El medio de pago es obligatorio")
    private MedioPago medioPago;

    private LocalDateTime fechaPago; // si es null, se asigna la fecha actual
}