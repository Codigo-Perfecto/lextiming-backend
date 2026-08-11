package com.lextiming.app.repository;

import com.lextiming.app.model.entity.RegistroTiempo;
import com.lextiming.app.model.enums.CategoriaTiempo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistroTiempoRepositorio extends JpaRepository<RegistroTiempo, String> {

    // Buscar registros por caso
    List<RegistroTiempo> findByCasoId(String casoId);

    List<RegistroTiempo> findByCasoIdAndEstadoTimerAndFacturaIsNull(String casoId, String estadoTimer);

    // Buscar registros por usuario (abogado)
    List<RegistroTiempo> findByUsuarioId(String usuarioId);

    // Buscar registros por caso y categoría
    List<RegistroTiempo> findByCasoIdAndCategoria(String casoId, CategoriaTiempo categoria);

    // Buscar registros por rango de fechas
    List<RegistroTiempo> findByFechaHoraInicioBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar registros activos (CORRIENDO o PAUSADO)
    List<RegistroTiempo> findByEstadoTimerIn(List<String> estados);

    @Query("SELECT r FROM RegistroTiempo r WHERE r.usuario.id = :usuarioId AND r.estadoTimer IN ('CORRIENDO', 'PAUSADO') AND r.fechaHoraFin IS NULL ORDER BY CASE r.estadoTimer WHEN 'CORRIENDO' THEN 0 ELSE 1 END, r.fechaCreacion DESC")
    List<RegistroTiempo> findActiveByUsuarioId(@Param("usuarioId") String usuarioId);

    // Buscar por usuario y estado específico
    Optional<RegistroTiempo> findByUsuarioIdAndEstadoTimer(String usuarioId, String estadoTimer);

    // Buscar timers PAUSADOS por usuario
    List<RegistroTiempo> findAllByUsuarioIdAndEstadoTimer(String usuarioId, String estadoTimer);

    // Buscar timers de un caso que no estén FINALIZADOS
    List<RegistroTiempo> findByCasoIdAndEstadoTimerNot(String casoId, String estadoTimer);

    // Calcular horas totales trabajadas en un caso
    @Query("SELECT SUM(rt.horasTotales) FROM RegistroTiempo rt WHERE rt.caso.id = :casoId")
    Double sumHorasTotalesByCasoId(@Param("casoId") String casoId);

    // Calcular horas por categoría en un caso
    @Query("SELECT rt.categoria, SUM(rt.horasTotales) FROM RegistroTiempo rt WHERE rt.caso.id = :casoId GROUP BY rt.categoria")
    List<Object[]> sumHorasPorCategoriaByCasoId(@Param("casoId") String casoId);

    @Query("SELECT r FROM RegistroTiempo r WHERE r.usuario.id = :usuarioId AND r.estadoTimer = :estado AND r.fechaHoraFin IS NULL")
    Optional<RegistroTiempo> findByUsuarioIdAndEstadoTimerAndFechaHoraFinIsNull(@Param("usuarioId") String usuarioId, @Param("estado") String estadoTimer);
}