package com.lextiming.app.controller;

import com.lextiming.app.dto.request.RegistroTiempoRequest;
import com.lextiming.app.dto.response.RegistroTiempoResponse;
import com.lextiming.app.service.RegistroTiempoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/registros-tiempo")
@RequiredArgsConstructor
public class RegistroTiempoController {

    private final RegistroTiempoService registroTiempoService;

    // Iniciar un timer (crea registro sin hora fin)
    @PostMapping("/iniciar")
    public ResponseEntity<RegistroTiempoResponse> iniciarRegistro(@Valid @RequestBody RegistroTiempoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroTiempoService.iniciarRegistro(request));
    }

    // NUEVO: Pausar un timer activo
    @PatchMapping("/{id}/pausar")
    public ResponseEntity<RegistroTiempoResponse> pausarRegistro(@PathVariable String id) {
        return ResponseEntity.ok(registroTiempoService.pausarRegistro(id));
    }

    // NUEVO: Reanudar un timer pausado
    @PatchMapping("/{id}/reanudar")
    public ResponseEntity<RegistroTiempoResponse> reanudarRegistro(@PathVariable String id) {
        return ResponseEntity.ok(registroTiempoService.reanudarRegistro(id));
    }

    // Finalizar un timer activo
    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<RegistroTiempoResponse> finalizarRegistro(
            @PathVariable String id,
            @RequestParam(required = false) LocalDateTime fechaHoraFin) {
        return ResponseEntity.ok(registroTiempoService.finalizarRegistro(id, fechaHoraFin));
    }

    // Crear registro completo (con inicio y fin)
    @PostMapping
    public ResponseEntity<RegistroTiempoResponse> crearRegistroCompleto(@Valid @RequestBody RegistroTiempoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroTiempoService.crearRegistroCompleto(request));
    }

    // Listar todos mis registros
    @GetMapping
    public ResponseEntity<List<RegistroTiempoResponse>> listarMisRegistros() {
        return ResponseEntity.ok(registroTiempoService.listarRegistrosPorUsuario());
    }

    // Listar registros por caso
    @GetMapping("/caso/{casoId}")
    public ResponseEntity<List<RegistroTiempoResponse>> listarRegistrosPorCaso(@PathVariable String casoId) {
        return ResponseEntity.ok(registroTiempoService.listarRegistrosPorCaso(casoId));
    }

    // Obtener registro por ID
    @GetMapping("/{id}")
    public ResponseEntity<RegistroTiempoResponse> obtenerRegistro(@PathVariable String id) {
        return ResponseEntity.ok(registroTiempoService.obtenerRegistro(id));
    }

    // Obtener registro activo (timer corriendo o pausado)
    @GetMapping("/activo")
    public ResponseEntity<RegistroTiempoResponse> obtenerRegistroActivo() {
        System.out.println(">>> Llamada a /activo recibida");
        RegistroTiempoResponse registro = registroTiempoService.obtenerRegistroActivo();
        System.out.println(">>> Resultado: " + (registro == null ? "null" : registro.getId()));
        if (registro == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(registro);
    }

    // Eliminar registro
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRegistro(@PathVariable String id) {
        registroTiempoService.eliminarRegistro(id);
        return ResponseEntity.noContent().build();
    }

    // Obtener total de horas por caso
    @GetMapping("/caso/{casoId}/total-horas")
    public ResponseEntity<Double> getHorasTotalesPorCaso(@PathVariable String casoId) {
        return ResponseEntity.ok(registroTiempoService.getHorasTotalesPorCaso(casoId));
    }

    // 👇 NUEVOS ENDPOINTS

    // Crear timer en estado PAUSADO (sin iniciar)
    @PostMapping("/pausado")
    public ResponseEntity<RegistroTiempoResponse> crearTimerPausado(@Valid @RequestBody RegistroTiempoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registroTiempoService.crearTimerPausado(request));
    }

    // Listar todos los timers PAUSADOS del usuario
    @GetMapping("/pausados")
    public ResponseEntity<List<RegistroTiempoResponse>> listarTimersPausados() {
        return ResponseEntity.ok(registroTiempoService.listarTimersPausados());
    }

    // Obtener el timer que está CORRIENDO actualmente
    @GetMapping("/corriendo")
    public ResponseEntity<RegistroTiempoResponse> obtenerTimerCorriendo() {
        RegistroTiempoResponse timer = registroTiempoService.obtenerTimerCorriendo();
        if (timer == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(timer);
    }

    // Pausar el timer actualmente corriendo
    @PostMapping("/pausar-actual")
    public ResponseEntity<RegistroTiempoResponse> pausarTimerActual() {
        return ResponseEntity.ok(registroTiempoService.pausarTimerActual());
    }

    // Reanudar un timer específico (pausa cualquier otro que esté corriendo)
    @PostMapping("/{registroId}/reanudar-especifico")
    public ResponseEntity<RegistroTiempoResponse> reanudarTimerEspecifico(@PathVariable String registroId) {
        return ResponseEntity.ok(registroTiempoService.reanudarTimerEspecifico(registroId));
    }

    // Verificar si un caso tiene timer activo
    @GetMapping("/caso/{casoId}/tiene-activo")
    public ResponseEntity<Boolean> casoHasTimerActivo(@PathVariable String casoId) {
        return ResponseEntity.ok(registroTiempoService.casoHasTimerActivo(casoId));
    }

    // Verificar si un caso está finalizado
    @GetMapping("/caso/{casoId}/finalizado")
    public ResponseEntity<Boolean> casoEstaFinalizado(@PathVariable String casoId) {
        return ResponseEntity.ok(registroTiempoService.casoEstaFinalizado(casoId));
    }

    @GetMapping("/casos-activos")
    public ResponseEntity<List<String>> getCasosConTimerActivo() {
        List<String> casosActivos = registroTiempoService.obtenerCasosConTimerActivo();
        return ResponseEntity.ok(casosActivos);
    }
}