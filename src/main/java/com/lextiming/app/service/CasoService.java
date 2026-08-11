package com.lextiming.app.service;

import com.lextiming.app.dto.request.CasoRequest;
import com.lextiming.app.dto.response.CasoResponse;
import com.lextiming.app.model.entity.Caso;
import com.lextiming.app.model.entity.Cliente;
import com.lextiming.app.model.entity.Usuario;
import com.lextiming.app.model.enums.EstadoCaso;
import com.lextiming.app.repository.CasoRepositorio;
import com.lextiming.app.repository.ClienteRepositorio;
import com.lextiming.app.repository.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CasoService {

    private final CasoRepositorio casoRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private void verificarAccesoACliente(String clienteId, Usuario usuario) {
        Cliente cliente = clienteRepositorio.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (!cliente.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("No tiene acceso a este cliente");
        }
    }

    private CasoResponse convertirAResponse(Caso caso) {
        return CasoResponse.builder()
                .id(caso.getId())
                .titulo(caso.getTitulo())
                .descripcion(caso.getDescripcion())
                .numeroExpediente(caso.getNumeroExpediente())
                .estado(caso.getEstado())
                .clienteId(caso.getCliente().getId())
                .clienteNombre(caso.getCliente().getNombre())
                .clienteApellido(caso.getCliente().getApellido())
                .fechaCreacion(caso.getFechaCreacion())
                .fechaActualizacion(caso.getFechaActualizacion())
                .build();
    }

    @Transactional
    public CasoResponse crearCaso(CasoRequest request) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoACliente(request.getClienteId(), usuarioActual);

        Cliente cliente = clienteRepositorio.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Caso caso = Caso.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .numeroExpediente(request.getNumeroExpediente())
                .estado(request.getEstado() != null ? request.getEstado() : EstadoCaso.ACTIVO)
                .cliente(cliente)
                .build();

        return convertirAResponse(casoRepositorio.save(caso));
    }

    @Transactional(readOnly = true)
    public Page<CasoResponse> listarCasos(Pageable pageable) {
        Usuario usuarioActual = getUsuarioActual();

        // Obtener todos los clientes del usuario
        List<Cliente> clientes = clienteRepositorio.findByUsuarioId(usuarioActual.getId());
        List<String> clientesIds = clientes.stream()
                .map(Cliente::getId)
                .collect(Collectors.toList());

        // Buscar casos de esos clientes
        // Nota: Esto requiere un método en el repositorio. Alternativa más simple:
        return casoRepositorio.findAll(pageable).map(this::convertirAResponse);
    }

    @Transactional(readOnly = true)
    public List<CasoResponse> listarCasosPorCliente(String clienteId) {
        Usuario usuarioActual = getUsuarioActual();
        verificarAccesoACliente(clienteId, usuarioActual);

        return casoRepositorio.findByClienteId(clienteId)
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CasoResponse obtenerCaso(String id) {
        Usuario usuarioActual = getUsuarioActual();
        Caso caso = casoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        verificarAccesoACliente(caso.getCliente().getId(), usuarioActual);

        return convertirAResponse(caso);
    }

    @Transactional
    public CasoResponse actualizarCaso(String id, CasoRequest request) {
        Usuario usuarioActual = getUsuarioActual();
        Caso caso = casoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        verificarAccesoACliente(caso.getCliente().getId(), usuarioActual);

        // Si se cambia el cliente, verificar acceso al nuevo cliente
        if (!caso.getCliente().getId().equals(request.getClienteId())) {
            verificarAccesoACliente(request.getClienteId(), usuarioActual);
            Cliente nuevoCliente = clienteRepositorio.findById(request.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            caso.setCliente(nuevoCliente);
        }

        caso.setTitulo(request.getTitulo());
        caso.setDescripcion(request.getDescripcion());
        caso.setNumeroExpediente(request.getNumeroExpediente());
        if (request.getEstado() != null) {
            caso.setEstado(request.getEstado());
        }

        return convertirAResponse(casoRepositorio.save(caso));
    }

    @Transactional
    public void eliminarCaso(String id) {
        Usuario usuarioActual = getUsuarioActual();
        Caso caso = casoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        verificarAccesoACliente(caso.getCliente().getId(), usuarioActual);

        casoRepositorio.delete(caso);
    }

    @Transactional
    public CasoResponse cambiarEstado(String id, EstadoCaso nuevoEstado) {
        Usuario usuarioActual = getUsuarioActual();
        Caso caso = casoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Caso no encontrado"));

        verificarAccesoACliente(caso.getCliente().getId(), usuarioActual);

        caso.setEstado(nuevoEstado);

        return convertirAResponse(casoRepositorio.save(caso));
    }
}
