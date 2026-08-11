package com.lextiming.app.service;

import com.lextiming.app.config.JwtService;
import com.lextiming.app.dto.request.LoginRequest;
import com.lextiming.app.dto.request.RegisterRequest;
import com.lextiming.app.dto.response.AuthResponse;
import com.lextiming.app.model.entity.Usuario;
import com.lextiming.app.model.enums.RolUsuario;
import com.lextiming.app.repository.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepositorio.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.getFirstName())
                .apellido(request.getLastName())
                .email(request.getEmail())
                .contrasena(passwordEncoder.encode(request.getPassword()))
                .matricula(request.getBarNumber())
                .telefono(request.getPhone())
                .estudioJuridico(request.getLawFirm())
                .rol(RolUsuario.ABOGADO)
                .activo(true)
                .build();

        usuarioRepositorio.save(usuario);

        String token = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .firstName(usuario.getNombre())
                .lastName(usuario.getApellido())
                .role(usuario.getRol().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepositorio.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .firstName(usuario.getNombre())
                .lastName(usuario.getApellido())
                .role(usuario.getRol().name())
                .build();
    }
}