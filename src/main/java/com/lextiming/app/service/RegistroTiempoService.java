package com.lextiming.app.service;

import com.lextiming.app.dto.request.RegistroTiempoRequest;
import com.lextiming.app.dto.response.RegistroTiempoResponse;
import com.lextiming.app.model.entity.Caso;
import com.lextiming.app.model.entity.RegistroTiempo;
import com.lextiming.app.model.entity.Usuario;
import com.lextiming.app.repository.CasoRepositorio;
import com.lextiming.app.repository.RegistroTiempoRepositorio;
import com.lextiming.app.repository.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistroTiempoService {

    private final RegistroTiempoRepositorio registroTiempoRepositorio;
    private final CasoRepositorio casoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

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

    private double calcularHorasTotales(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && fin != null) {
            Duration duration = Duration.between(inicio, fin);
            return duration.toMinutes() / 60.0;
        }
        return 0.0;
    }

    private RegistroTiempoResponse convertirAResponse(RegistroTiempo registro) {
        return RegistroTiempoResponse.builder()
                .id(registro.getId())
                .casoId(registro.getCaso().getId())
                .casoTitulo(registro.getCaso().getTitulo())
                .usuarioId(registro.getUsuario().getId())
                .usuarioNombre(registro.getUsuario().getNombre())
                .usuarioApellido(registro.getUsuario().getApellido())
                .fechaHoraInicio(registro.getFechaHoraInicio())
                .fechaHoraInicioOriginal(registro.getFechaHoraInicioOriginal())
                .fechaHoraFin(registro.getFechaHoraFin())
                .horasTotales(registro.getHorasTotales())
                .horasAcumuladas(registro.getHorasAcumuladas())
                .estadoTimer(registro.getEstadoTimer())
                .categoria(registro.getCategoria())
                .descripcion(registro.getDescripcion())
                .facturable(registro.isFacturable())
                .fechaCreacion(registro.getFechaCreacion())
                .fechaActualizacion(registro.getFechaActualizacion())
                .build();
    }

    @Transactional
    public RegistroTiempoResponse iniciarRegistro(RegistroTiempoRequest request) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(request.getCasoId(), usuarioActual);

        // ✅ MODIFICADO: Solo verificar si hay un timer en estado CORRIENDO
        var timerCorriendo = registroTiempoRepositorio.findByUsuarioIdAndEstadoTimerAndFechaHoraFinIsNull(
                usuarioActual.getId(), "CORRIENDO");
        if (timerCorriendo.isPresent()) {
            throw new RuntimeException("Ya tiene un timer corriendo. Debe pausarlo o finalizarlo antes de iniciar otro.");
        }
        // Si hay timers pausados, se permite crear uno nuevo (empezará en CORRIENDO)

        Caso caso = casoRepositorio.findById(request.getCasoId())
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();

        RegistroTiempo registro = RegistroTiempo.builder()
                .caso(caso)
                .usuario(usuarioActual)
                .fechaHoraInicio(ahora)
                .fechaHoraInicioOriginal(ahora)
                .categoria(request.getCategoria())
                .descripcion(request.getDescripcion())
                .facturable(request.getFacturable() != null ? request.getFacturable() : true)
                .horasAcumuladas(0.0)
                .estadoTimer("CORRIENDO")  // El timer creado arranca directamente en CORRIENDO
                .build();

        return convertirAResponse(registroTiempoRepositorio.save(registro));
    }

    @Transactional
    public RegistroTiempoResponse pausarRegistro(String id) {
        Usuario usuarioActual = getUsuarioActual();

        RegistroTiempo registro = registroTiempoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de tiempo no encontrado"));

        if (!registro.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este registro");
        }

        if (!"CORRIENDO".equals(registro.getEstadoTimer())) {
            throw new RuntimeException("Solo se puede pausar un timer que esté corriendo");
        }

        registro.pausar();

        return convertirAResponse(registroTiempoRepositorio.save(registro));
    }

    @Transactional
    public RegistroTiempoResponse reanudarRegistro(String id) {
        Usuario usuarioActual = getUsuarioActual();

        RegistroTiempo registro = registroTiempoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de tiempo no encontrado"));

        if (!registro.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este registro");
        }

        if (!"PAUSADO".equals(registro.getEstadoTimer())) {
            throw new RuntimeException("Solo se puede reanudar un timer que esté pausado");
        }

        registro.reanudar();

        return convertirAResponse(registroTiempoRepositorio.save(registro));
    }

    @Transactional
    public RegistroTiempoResponse finalizarRegistro(String id, LocalDateTime fechaHoraFin) {
        Usuario usuarioActual = getUsuarioActual();

        RegistroTiempo registro = registroTiempoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de tiempo no encontrado"));

        if (!registro.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este registro");
        }

        if ("FINALIZADO".equals(registro.getEstadoTimer())) {
            throw new RuntimeException("Este registro ya fue finalizado");
        }

        registro.finalizar();

        return convertirAResponse(registroTiempoRepositorio.save(registro));
    }

    @Transactional
    public RegistroTiempoResponse crearRegistroCompleto(RegistroTiempoRequest request) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(request.getCasoId(), usuarioActual);

        // Validación: impedir registro manual si el caso tiene timer activo
        List<RegistroTiempo> timersActivos = registroTiempoRepositorio.findByCasoId(request.getCasoId())
                .stream()
                .filter(r -> "CORRIENDO".equals(r.getEstadoTimer()) || "PAUSADO".equals(r.getEstadoTimer()))
                .collect(Collectors.toList());

        if (!timersActivos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un timer activo para este caso. Finaliza o pausa el timer antes de agregar un registro manual.");
        }

        if (request.getFechaHoraInicio() == null) {
            throw new RuntimeException("La fecha/hora de inicio es obligatoria");
        }

        if (request.getFechaHoraFin() == null) {
            throw new RuntimeException("La fecha/hora de fin es obligatoria");
        }

        if (request.getFechaHoraFin().isBefore(request.getFechaHoraInicio())) {
            throw new RuntimeException("La fecha/hora de fin debe ser posterior a la de inicio");
        }

        Caso caso = casoRepositorio.findById(request.getCasoId())
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        double horasTotales = calcularHorasTotales(request.getFechaHoraInicio(), request.getFechaHoraFin());

        RegistroTiempo registro = RegistroTiempo.builder()
                .caso(caso)
                .usuario(usuarioActual)
                .fechaHoraInicio(request.getFechaHoraInicio())
                .fechaHoraInicioOriginal(request.getFechaHoraInicio())
                .fechaHoraFin(request.getFechaHoraFin())
                .horasTotales(horasTotales)
                .horasAcumuladas(horasTotales)
                .estadoTimer("FINALIZADO")
                .categoria(request.getCategoria())
                .descripcion(request.getDescripcion())
                .facturable(request.getFacturable() != null ? request.getFacturable() : true)
                .build();

        return convertirAResponse(registroTiempoRepositorio.save(registro));
    }

    @Transactional(readOnly = true)
    public List<RegistroTiempoResponse> listarRegistrosPorUsuario() {
        Usuario usuarioActual = getUsuarioActual();
        return registroTiempoRepositorio.findByUsuarioId(usuarioActual.getId())
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegistroTiempoResponse> listarRegistrosPorCaso(String casoId) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(casoId, usuarioActual);

        return registroTiempoRepositorio.findByCasoId(casoId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RegistroTiempoResponse obtenerRegistro(String id) {
        Usuario usuarioActual = getUsuarioActual();
        RegistroTiempo registro = registroTiempoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de tiempo no encontrado"));

        if (!registro.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este registro");
        }

        return convertirAResponse(registro);
    }

    @Transactional(readOnly = true)
    public RegistroTiempoResponse obtenerRegistroActivo() {
        Usuario usuarioActual = getUsuarioActual();
        List<RegistroTiempo> activos = registroTiempoRepositorio.findActiveByUsuarioId(usuarioActual.getId());
        return activos.stream().findFirst().map(this::convertirAResponse).orElse(null);
    }

    @Transactional
    public void eliminarRegistro(String id) {
        Usuario usuarioActual = getUsuarioActual();
        RegistroTiempo registro = registroTiempoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Registro de tiempo no encontrado"));

        if (!registro.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este registro");
        }

        registroTiempoRepositorio.delete(registro);
    }

    @Transactional(readOnly = true)
    public Double getHorasTotalesPorCaso(String casoId) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(casoId, usuarioActual);

        Double horas = registroTiempoRepositorio.sumHorasTotalesByCasoId(casoId);
        return horas != null ? horas : 0.0;
    }

    // NUEVO: Crear timer en estado CORRIENDO (sin iniciar formalmente)
    @Transactional
    public RegistroTiempoResponse crearTimerPausado(RegistroTiempoRequest request) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(request.getCasoId(), usuarioActual);

        // Verificar si el caso ya tiene un timer FINALIZADO
        List<RegistroTiempo> registrosCaso = registroTiempoRepositorio.findByCasoId(request.getCasoId());
        boolean tieneFinalizado = registrosCaso.stream()
                .anyMatch(r -> "FINALIZADO".equals(r.getEstadoTimer()));

        if (tieneFinalizado) {
            throw new RuntimeException("Este caso ya está finalizado. No se puede crear un nuevo timer.");
        }

        // Verificar si ya existe un timer PAUSADO o CORRIENDO para este caso
        boolean existeActivo = registrosCaso.stream()
                .anyMatch(r -> "PAUSADO".equals(r.getEstadoTimer()) || "CORRIENDO".equals(r.getEstadoTimer()));

        if (existeActivo) {
            throw new RuntimeException("Ya existe un timer para este caso. Use 'Reanudar' en lugar de crear uno nuevo.");
        }

        Caso caso = casoRepositorio.findById(request.getCasoId())
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        LocalDateTime ahora = LocalDateTime.now();

        RegistroTiempo registro = RegistroTiempo.builder()
                .caso(caso)
                .usuario(usuarioActual)
                .fechaHoraInicio(ahora)
                .fechaHoraInicioOriginal(ahora)
                .categoria(request.getCategoria())
                .descripcion(request.getDescripcion())
                .facturable(request.getFacturable() != null ? request.getFacturable() : true)
                .horasAcumuladas(0.0)
                .estadoTimer("CORRIENDO")
                .build();

        return convertirAResponse(registroTiempoRepositorio.save(registro));
    }

    // Listar todos los timers PAUSADOS del usuario
    @Transactional(readOnly = true)
    public List<RegistroTiempoResponse> listarTimersPausados() {
        Usuario usuarioActual = getUsuarioActual();
        return registroTiempoRepositorio.findAllByUsuarioIdAndEstadoTimer(usuarioActual.getId(), "PAUSADO")
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    // Obtener el timer que está CORRIENDO actualmente
    @Transactional(readOnly = true)
    public RegistroTiempoResponse obtenerTimerCorriendo() {
        Usuario usuarioActual = getUsuarioActual();
        return registroTiempoRepositorio.findByUsuarioIdAndEstadoTimer(usuarioActual.getId(), "CORRIENDO")
                .map(this::convertirAResponse)
                .orElse(null);
    }

    // Pausar el timer actualmente corriendo
    @Transactional
    public RegistroTiempoResponse pausarTimerActual() {
        Usuario usuarioActual = getUsuarioActual();

        RegistroTiempo timerCorriendo = registroTiempoRepositorio
                .findByUsuarioIdAndEstadoTimer(usuarioActual.getId(), "CORRIENDO")
                .orElseThrow(() -> new RuntimeException("No hay ningún timer corriendo para pausar."));

        timerCorriendo.pausar();

        return convertirAResponse(registroTiempoRepositorio.save(timerCorriendo));
    }

    // Reanudar un timer específico (pausa cualquier otro que esté corriendo)
    @Transactional
    public RegistroTiempoResponse reanudarTimerEspecifico(String registroId) {
        Usuario usuarioActual = getUsuarioActual();

        // Primero, pausar cualquier timer que esté corriendo actualmente
        registroTiempoRepositorio.findByUsuarioIdAndEstadoTimer(usuarioActual.getId(), "CORRIENDO")
                .ifPresent(timerCorriendo -> {
                    timerCorriendo.pausar();
                    registroTiempoRepositorio.save(timerCorriendo);
                });

        // Luego, reanudar el timer seleccionado
        RegistroTiempo registro = registroTiempoRepositorio.findById(registroId)
                .orElseThrow(() -> new RuntimeException("Registro no encontrado"));

        if (!registro.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este registro");
        }

        if ("FINALIZADO".equals(registro.getEstadoTimer())) {
            throw new RuntimeException("Este caso ya está finalizado. No se puede reanudar.");
        }

        if (!"PAUSADO".equals(registro.getEstadoTimer())) {
            throw new RuntimeException("Solo se puede reanudar un timer que esté pausado.");
        }

        registro.reanudar();

        return convertirAResponse(registroTiempoRepositorio.save(registro));
    }

    // Verificar si un caso tiene un timer activo (PAUSADO o CORRIENDO)
    @Transactional(readOnly = true)
    public boolean casoHasTimerActivo(String casoId) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(casoId, usuarioActual);

        List<RegistroTiempo> registros = registroTiempoRepositorio.findByCasoId(casoId);
        return registros.stream()
                .anyMatch(r -> "PAUSADO".equals(r.getEstadoTimer()) || "CORRIENDO".equals(r.getEstadoTimer()));
    }

    // Verificar si un caso está finalizado
    @Transactional(readOnly = true)
    public boolean casoEstaFinalizado(String casoId) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoAlCaso(casoId, usuarioActual);

        List<RegistroTiempo> registros = registroTiempoRepositorio.findByCasoId(casoId);
        return registros.stream()
                .anyMatch(r -> "FINALIZADO".equals(r.getEstadoTimer()));
    }

    public List<String> obtenerCasosConTimerActivo() {
        Usuario usuarioActual = getUsuarioActual();
        return registroTiempoRepositorio.findByUsuarioId(usuarioActual.getId())
                .stream()
                .filter(r -> "CORRIENDO".equals(r.getEstadoTimer()) || "PAUSADO".equals(r.getEstadoTimer()))
                .map(r -> r.getCaso().getId())
                .distinct()
                .collect(Collectors.toList());
    }
}