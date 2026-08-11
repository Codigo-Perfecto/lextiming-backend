package com.lextiming.app.controller;

import com.lextiming.app.dto.request.CasoRequest;
import com.lextiming.app.dto.response.CasoResponse;
import com.lextiming.app.model.enums.EstadoCaso;
import com.lextiming.app.service.CasoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/casos")
@RequiredArgsConstructor
public class CasoController {

    private final CasoService casoService;

    @PostMapping
    public ResponseEntity<CasoResponse> crearCaso(@Valid @RequestBody CasoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(casoService.crearCaso(request));
    }

    @GetMapping
    public ResponseEntity<Page<CasoResponse>> listarCasos(
            @PageableDefault(size = 10, sort = "fechaCreacion", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(casoService.listarCasos(pageable));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CasoResponse>> listarCasosPorCliente(@PathVariable String clienteId) {
        return ResponseEntity.ok(casoService.listarCasosPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CasoResponse> obtenerCaso(@PathVariable String id) {
        return ResponseEntity.ok(casoService.obtenerCaso(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CasoResponse> actualizarCaso(
            @PathVariable String id,
            @Valid @RequestBody CasoRequest request) {
        return ResponseEntity.ok(casoService.actualizarCaso(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCaso(@PathVariable String id) {
        casoService.eliminarCaso(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CasoResponse> cambiarEstado(
            @PathVariable String id,
            @RequestParam EstadoCaso estado) {
        return ResponseEntity.ok(casoService.cambiarEstado(id, estado));
    }
}
