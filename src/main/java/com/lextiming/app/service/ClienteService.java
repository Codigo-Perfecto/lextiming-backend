package com.lextiming.app.service;

import com.lextiming.app.dto.request.ClienteRequest;
import com.lextiming.app.dto.response.ClienteResponse;
import com.lextiming.app.model.entity.Cliente;
import com.lextiming.app.model.entity.Usuario;
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
public class ClienteService {

    private final ClienteRepositorio clienteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    private Usuario getUsuarioActual() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    private ClienteResponse convertirAResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .documento(cliente.getDocumento())
                .direccion(cliente.getDireccion())
                .notas(cliente.getNotas())
                .fechaCreacion(cliente.getFechaCreacion())
                .build();
    }

    @Transactional
    public ClienteResponse crearCliente(ClienteRequest request) {
        Usuario usuarioActual = getUsuarioActual();

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .documento(request.getDocumento())
                .direccion(request.getDireccion())
                .notas(request.getNotas())
                .usuario(usuarioActual)
                .build();

        return convertirAResponse(clienteRepositorio.save(cliente));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponse> listarClientes(Pageable pageable) {
        Usuario usuarioActual = getUsuarioActual();
        return clienteRepositorio.findByUsuarioId(usuarioActual.getId(), pageable)
                .map(this::convertirAResponse);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodosClientes() {
        Usuario usuarioActual = getUsuarioActual();
        return clienteRepositorio.findByUsuarioId(usuarioActual.getId())
                .stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerCliente(String id) {
        Usuario usuarioActual = getUsuarioActual();
        Cliente cliente = clienteRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (!cliente.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este cliente");
        }

        return convertirAResponse(cliente);
    }

    @Transactional
    public ClienteResponse actualizarCliente(String id, ClienteRequest request) {
        Usuario usuarioActual = getUsuarioActual();
        Cliente cliente = clienteRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (!cliente.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este cliente");
        }

        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDocumento(request.getDocumento());
        cliente.setDireccion(request.getDireccion());
        cliente.setNotas(request.getNotas());

        return convertirAResponse(clienteRepositorio.save(cliente));
    }

    @Transactional
    public void eliminarCliente(String id) {
        Usuario usuarioActual = getUsuarioActual();
        Cliente cliente = clienteRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (!cliente.getUsuario().getId().equals(usuarioActual.getId())) {
            throw new RuntimeException("No tiene acceso a este cliente");
        }

        clienteRepositorio.delete(cliente);
    }
}
