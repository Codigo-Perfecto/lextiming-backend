package com.lextiming.app.repository;

import com.lextiming.app.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface ClienteRepositorio extends JpaRepository<Cliente, String> {

    // Buscar clientes por usuario (abogado) - paginado
    Page<Cliente> findByUsuarioId(String usuarioId, Pageable pageable);

    // Buscar clientes por usuario (abogado) - lista completa
    List<Cliente> findByUsuarioId(String usuarioId);

    // Verificar si existe un cliente con ese email para un abogado específico
    boolean existsByEmailAndUsuarioId(String email, String usuarioId);

    // Buscar cliente por email (sin importar el abogado)
    Optional<Cliente> findByEmail(String email);

    // Buscar por documento
    Optional<Cliente> findByDocumento(String documento);

    // Buscar por nombre o apellido (para búsqueda rápida)
    List<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);

    // Buscar clientes por nombre del abogado
    List<Cliente> findByUsuarioNombreContainingIgnoreCase(String nombreAbogado);
}
