package com.lextiming.app.repository;

import com.lextiming.app.model.entity.Factura;
import com.lextiming.app.model.enums.EstadoFactura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FacturaRepositorio extends JpaRepository<Factura, String> {

    // Buscar facturas por caso
    List<Factura> findByCasoId(String casoId);
    Page<Factura> findByCasoId(String casoId, Pageable pageable);

    // Buscar facturas por estado
    List<Factura> findByEstado(EstadoFactura estado);

    // Buscar facturas por número de factura
    Optional<Factura> findByNumeroFactura(String numeroFactura);

    // Buscar facturas por rango de fechas
    List<Factura> findByFechaEmisionBetween(LocalDateTime inicio, LocalDateTime fin);

    // Contar facturas emitidas entre dos fechas (útil para generar el número correlativo anual)
    long countByFechaEmisionBetween(LocalDateTime start, LocalDateTime end);

    // Buscar facturas vencidas (fechaVencimiento antes de hoy y estado no pagado)
    List<Factura> findByFechaVencimientoBeforeAndEstadoNot(LocalDateTime fecha, EstadoFactura estado);

    // Buscar facturas de un abogado específico (a través del caso)
    List<Factura> findByCasoClienteUsuarioId(String usuarioId);

    // Sumar total de facturas pagadas de un caso - CORREGIDO usando @Query
    @Query("SELECT SUM(f.monto) FROM Factura f WHERE f.caso.id = :casoId AND f.estado = :estado")
    Double sumMontoByCasoIdAndEstado(@Param("casoId") String casoId, @Param("estado") EstadoFactura estado);
}