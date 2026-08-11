package com.lextiming.app.repository;

import com.lextiming.app.model.entity.Caso;
import com.lextiming.app.model.enums.EstadoCaso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface CasoRepositorio extends JpaRepository<Caso, String> {

    // Buscar casos por cliente
    Page<Caso> findByClienteId(String clienteId, Pageable pageable);
    List<Caso> findByClienteId(String clienteId);

    // Buscar casos por estado
    List<Caso> findByEstado(EstadoCaso estado);

    // Buscar casos por número de expediente
    Optional<Caso> findByNumeroExpediente(String numeroExpediente);

    // Buscar casos por cliente y estado
    List<Caso> findByClienteIdAndEstado(String clienteId, EstadoCaso estado);

    // Contar casos activos de un cliente
    long countByClienteIdAndEstado(String clienteId, EstadoCaso estado);

    // Buscar casos por título (búsqueda)
    List<Caso> findByTituloContainingIgnoreCase(String titulo);

}
