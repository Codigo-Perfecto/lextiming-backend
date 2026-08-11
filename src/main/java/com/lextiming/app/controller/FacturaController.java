package com.lextiming.app.controller;

import com.lextiming.app.dto.request.FacturaRequest;
import com.lextiming.app.dto.response.FacturaResponse;
import com.lextiming.app.model.enums.EstadoFactura;
import com.lextiming.app.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lextiming.app.dto.request.PagoFacturaRequest;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    // Crear factura manual
    @PostMapping
    public ResponseEntity<FacturaResponse> crearFactura(@Valid @RequestBody FacturaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaService.crearFactura(request));
    }

    // Crear factura automática (calcula horas del caso)
    @PostMapping("/auto/{casoId}")
    public ResponseEntity<FacturaResponse> crearFacturaAutomatica(
            @PathVariable String casoId,
            @RequestParam String numeroFactura,
            @RequestParam Double tarifaPorHora,
            @RequestParam(required = false) Integer diasVencimiento) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facturaService.crearFacturaAutomatica(casoId, numeroFactura, tarifaPorHora, diasVencimiento));
    }

    // Listar mis facturas
    @GetMapping
    public ResponseEntity<List<FacturaResponse>> listarMisFacturas() {
        return ResponseEntity.ok(facturaService.listarFacturasPorUsuario());
    }

    // Listar facturas por caso
    @GetMapping("/caso/{casoId}")
    public ResponseEntity<List<FacturaResponse>> listarFacturasPorCaso(@PathVariable String casoId) {
        return ResponseEntity.ok(facturaService.listarFacturasPorCaso(casoId));
    }

    // Listar facturas pendientes
    @GetMapping("/pendientes")
    public ResponseEntity<List<FacturaResponse>> listarFacturasPendientes() {
        return ResponseEntity.ok(facturaService.listarFacturasPendientes());
    }

    // Obtener factura por ID
    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponse> obtenerFactura(@PathVariable String id) {
        return ResponseEntity.ok(facturaService.obtenerFactura(id));
    }

    /* Actualizar estado de factura (PENDIENTE, PAGADA, VENCIDA, CANCELADA)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<FacturaResponse> actualizarEstado(
            @PathVariable String id,
            @RequestParam EstadoFactura estado) {
        return ResponseEntity.ok(facturaService.actualizarEstado(id, estado));
    }*/

    // Eliminar factura
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable String id) {
        facturaService.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener total facturado por caso
    @GetMapping("/caso/{casoId}/total-facturado")
    public ResponseEntity<Double> getTotalFacturadoPorCaso(@PathVariable String casoId) {
        return ResponseEntity.ok(facturaService.getTotalFacturadoPorCaso(casoId));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<FacturaResponse> registrarPago(@PathVariable String id,
                                                         @Valid @RequestBody PagoFacturaRequest request) {
        return ResponseEntity.ok(facturaService.registrarPago(id, request));
    }

}
