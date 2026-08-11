package com.lextiming.app.service;

import com.lextiming.app.dto.request.FacturaRequest;
import com.lextiming.app.dto.request.PagoFacturaRequest;
import com.lextiming.app.dto.response.FacturaResponse;
import com.lextiming.app.model.entity.Caso;
import com.lextiming.app.model.entity.Factura;
import com.lextiming.app.model.entity.RegistroTiempo;
import com.lextiming.app.model.entity.Usuario;
import com.lextiming.app.model.enums.EstadoFactura;
import com.lextiming.app.repository.CasoRepositorio;
import com.lextiming.app.repository.FacturaRepositorio;
import com.lextiming.app.repository.RegistroTiempoRepositorio;
import com.lextiming.app.repository.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepositorio facturaRepositorio;
    private final CasoRepositorio casoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final RegistroTiempoRepositorio registroTiempoRepositorio;

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private void verificarAccesoAlCaso(String casoId, Usuario usuario) {
        Caso caso = casoRepositorio.findById(casoId)
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));
        if (!caso.getCliente().getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tiene acceso a este caso");
        }
    }

    private FacturaResponse convertirAResponse(Factura factura) {
        return FacturaResponse.builder()
                .id(factura.getId())
                .numeroFactura(factura.getNumeroFactura())
                .casoId(factura.getCaso().getId())
                .casoTitulo(factura.getCaso().getTitulo())
                .clienteId(factura.getCaso().getCliente().getId())
                .clienteNombre(factura.getCaso().getCliente().getNombre())
                .clienteApellido(factura.getCaso().getCliente().getApellido())
                .monto(factura.getMonto())
                .horasTrabajadas(factura.getHorasTrabajadas())
                .tarifaPorHora(factura.getTarifaPorHora())
                .fechaEmision(factura.getFechaEmision())
                .fechaVencimiento(factura.getFechaVencimiento())
                .estado(factura.getEstado())
                .descripcion(factura.getDescripcion())
                .fechaCreacion(factura.getFechaCreacion())
                .fechaActualizacion(factura.getFechaActualizacion())
                .medioPago(factura.getMedioPago())
                .fechaPago(factura.getFechaPago())
                .build();
    }

    @Transactional
    public FacturaResponse crearFactura(FacturaRequest request) {
        // ... (sin cambios, igual que antes)
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(request.getCasoId(), usuarioActual);

        Caso caso = casoRepositorio.findById(request.getCasoId())
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        if (facturaRepositorio.findByNumeroFactura(request.getNumeroFactura()).isPresent()) {
            throw new RuntimeException("Ya existe una factura con el número: " + request.getNumeroFactura());
        }

        Double horasTrabajadas = request.getHorasTrabajadas();
        Double tarifaPorHora = request.getTarifaPorHora();
        Double monto = request.getMonto();

        if (monto == null && tarifaPorHora != null) {
            if (horasTrabajadas == null) {
                horasTrabajadas = registroTiempoRepositorio.sumHorasTotalesByCasoId(request.getCasoId());
                if (horasTrabajadas == null) horasTrabajadas = 0.0;
            }
            monto = horasTrabajadas * tarifaPorHora;
        }

        if (monto == null) {
            throw new RuntimeException("Debe especificar el monto o la tarifa por hora");
        }

        Factura factura = Factura.builder()
                .numeroFactura(request.getNumeroFactura())
                .caso(caso)
                .monto(monto)
                .horasTrabajadas(horasTrabajadas)
                .tarifaPorHora(tarifaPorHora)
                .fechaVencimiento(request.getFechaVencimiento())
                .estado(EstadoFactura.PENDIENTE)
                .descripcion(request.getDescripcion())
                .build();

        return convertirAResponse(facturaRepositorio.save(factura));
    }

    @Transactional
    public FacturaResponse crearFacturaAutomatica(String casoId, String numeroFactura, Double tarifaPorHora, Integer diasVencimiento) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(casoId, usuarioActual);

        Caso caso = casoRepositorio.findById(casoId)
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        // Evitar facturas duplicadas pendientes/pagadas
        List<Factura> facturasExistentes = facturaRepositorio.findByCasoId(casoId);
        boolean tieneFacturaReciente = facturasExistentes.stream()
                .anyMatch(f -> f.getEstado() == EstadoFactura.PENDIENTE || f.getEstado() == EstadoFactura.PAGADA);
        if (tieneFacturaReciente) {
            throw new RuntimeException("Ya existe una factura pendiente o pagada para este caso. No se puede generar otra automáticamente.");
        }

        // Obtener registros de tiempo FINALIZADOS y SIN facturar
        List<RegistroTiempo> registrosNoFacturados = registroTiempoRepositorio.findByCasoIdAndEstadoTimerAndFacturaIsNull(casoId, "FINALIZADO");
        if (registrosNoFacturados.isEmpty()) {
            throw new RuntimeException("No hay registros de tiempo no facturados para este caso.");
        }

        double horasTrabajadas = registrosNoFacturados.stream()
                .mapToDouble(rt -> rt.getHorasTotales() != null ? rt.getHorasTotales() : 0.0)
                .sum();
        double monto = horasTrabajadas * tarifaPorHora;

        // Generar número de factura
        if (numeroFactura == null || numeroFactura.trim().isEmpty()) {
            numeroFactura = generarNumeroFactura();
        } else {
            if (facturaRepositorio.findByNumeroFactura(numeroFactura).isPresent()) {
                throw new RuntimeException("Ya existe una factura con el número: " + numeroFactura);
            }
        }

        LocalDateTime fechaVencimiento = (diasVencimiento != null) ? LocalDateTime.now().plusDays(diasVencimiento) : null;

        Factura factura = Factura.builder()
                .numeroFactura(numeroFactura)
                .caso(caso)
                .monto(monto)
                .horasTrabajadas(horasTrabajadas)
                .tarifaPorHora(tarifaPorHora)
                .fechaVencimiento(fechaVencimiento)
                .estado(EstadoFactura.PENDIENTE)
                .descripcion("Factura automática por horas trabajadas (registros no facturados)")
                .build();

        factura = facturaRepositorio.save(factura);

        // Asociar los registros a la factura
        for (RegistroTiempo registro : registrosNoFacturados) {
            registro.setFactura(factura);
            registroTiempoRepositorio.save(registro);
        }

        return convertirAResponse(factura);
    }

    // Método auxiliar para generar el siguiente número de factura
    private String generarNumeroFactura() {
        int año = LocalDateTime.now().getYear();
        LocalDateTime inicioAño = LocalDateTime.of(año, 1, 1, 0, 0);
        LocalDateTime finAño = LocalDateTime.of(año, 12, 31, 23, 59, 59);
        long cantidad = facturaRepositorio.countByFechaEmisionBetween(inicioAño, finAño);
        int numero = (int) cantidad + 1;
        return String.format("FACT-%d-%04d", año, numero);
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarFacturasPorUsuario() {
        Usuario usuarioActual = getUsuarioActual();
        return facturaRepositorio.findByCasoClienteUsuarioId(usuarioActual.getId())
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarFacturasPorCaso(String casoId) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(casoId, usuarioActual);
        return facturaRepositorio.findByCasoId(casoId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtenerFactura(String id) {
        Factura factura = facturaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        Usuario usuarioActual = getUsuarioActual();
        if (!factura.getCaso().getCliente().getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a esta factura");
        }
        return convertirAResponse(factura);
    }

    @Transactional
    public void eliminarFactura(String id) {
        Factura factura = facturaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        Usuario usuarioActual = getUsuarioActual();
        if (!factura.getCaso().getCliente().getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a esta factura");
        }
        // No permitir eliminar facturas pagadas (solo pendientes o canceladas)
        if (factura.getEstado() == EstadoFactura.PAGADA) {
            throw new RuntimeException("No se puede eliminar una factura pagada. Utilice la opción 'Cancelar factura' para anularla.");
        }
        facturaRepositorio.delete(factura);
    }

    @Transactional
    public FacturaResponse cancelarFactura(String id) {
        Factura factura = facturaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        Usuario usuarioActual = getUsuarioActual();
        if (!factura.getCaso().getCliente().getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a esta factura");
        }
        // Solo se pueden cancelar facturas pendientes o pagadas (pagadas también, aunque sería extraño)
        if (factura.getEstado() == EstadoFactura.CANCELADA) {
            throw new RuntimeException("La factura ya está cancelada.");
        }

        // Liberar los registros de tiempo asociados a esta factura
        if (factura.getRegistrosTiempo() != null && !factura.getRegistrosTiempo().isEmpty()) {
            for (RegistroTiempo registro : factura.getRegistrosTiempo()) {
                registro.setFactura(null);
                registroTiempoRepositorio.save(registro);
            }
        }

        // Cambiar estado a CANCELADA
        factura.setEstado(EstadoFactura.CANCELADA);
        return convertirAResponse(facturaRepositorio.save(factura));
    }

    @Transactional(readOnly = true)
    public Double getTotalFacturadoPorCaso(String casoId) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(casoId, usuarioActual);
        Double total = facturaRepositorio.sumMontoByCasoIdAndEstado(casoId, EstadoFactura.PAGADA);
        return total != null ? total : 0.0;
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarFacturasPendientes() {
        Usuario usuarioActual = getUsuarioActual();
        return facturaRepositorio.findByEstado(EstadoFactura.PENDIENTE)
                .stream()
                .filter(f -> f.getCaso().getCliente().getUsuario().getId().equals(usuarioActual.getId()))
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FacturaResponse registrarPago(String id, PagoFacturaRequest request) {
        Usuario usuarioActual = getUsuarioActual();
        Factura factura = facturaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        if (!factura.getCaso().getCliente().getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a esta factura");
        }
        if (factura.getEstado() != EstadoFactura.PENDIENTE) {
            throw new RuntimeException("Solo se puede pagar una factura en estado PENDIENTE");
        }
        factura.setEstado(EstadoFactura.PAGADA);
        factura.setMedioPago(request.getMedioPago());
        factura.setFechaPago(request.getFechaPago() != null ? request.getFechaPago() : LocalDateTime.now());
        return convertirAResponse(facturaRepositorio.save(factura));
    }
}