package com.lextiming.app.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lextiming.app.model.enums.CategoriaTiempo;

import java.time.LocalDateTime;
import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "registros_tiempo")
public class RegistroTiempo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caso_id", nullable = false)
    @JsonIgnore
    private Caso caso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fecha_hora_inicio_original")
    private LocalDateTime fechaHoraInicioOriginal;

    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    @Column(name = "horas_totales")
    private Double horasTotales;

    @Column(name = "horas_acumuladas")
    private Double horasAcumuladas;

    @Column(name = "estado_timer")
    private String estadoTimer;

    @Column(name = "ultima_pausa")
    private LocalDateTime ultimaPausa;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaTiempo categoria;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "facturable")
    @Builder.Default
    private boolean facturable = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        estadoTimer = "CORRIENDO";
        horasAcumuladas = 0.0;
        this.fechaHoraInicioOriginal = this.fechaHoraInicio;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public void pausar() {
        if ("CORRIENDO".equals(estadoTimer)) {
            Duration duration = Duration.between(fechaHoraInicio, LocalDateTime.now());
            long segundosTranscurridos = duration.getSeconds();
            double horasTranscurridas = segundosTranscurridos / 3600.0;
            this.horasAcumuladas = (horasAcumuladas != null ? horasAcumuladas : 0) + horasTranscurridas;
            this.estadoTimer = "PAUSADO";
            this.ultimaPausa = LocalDateTime.now();
        }
        System.out.println("PAUSAR - Inicio: " + fechaHoraInicio + ", Ahora: " + LocalDateTime.now() + ", Acumulado antes: " + horasAcumuladas);
    }

    public void reanudar() {
        if ("PAUSADO".equals(estadoTimer)) {
            this.fechaHoraInicio = LocalDateTime.now();
            this.estadoTimer = "CORRIENDO";
        }
        System.out.println("PAUSAR - Inicio: " + fechaHoraInicio + ", Ahora: " + LocalDateTime.now() + ", Acumulado antes: " + horasAcumuladas);
    }

    public void finalizar() {
        if ("CORRIENDO".equals(estadoTimer)) {
            Duration duration = Duration.between(fechaHoraInicio, LocalDateTime.now());
            long segundosTranscurridos = duration.getSeconds();
            long acumuladoSegundos = (long) ((horasAcumuladas != null ? horasAcumuladas : 0) * 3600);
            long totalSegundos = acumuladoSegundos + segundosTranscurridos;
            this.horasTotales = totalSegundos / 3600.0;
            this.estadoTimer = "FINALIZADO";
            this.fechaHoraFin = LocalDateTime.now();
        } else if ("PAUSADO".equals(estadoTimer)) {
            this.horasTotales = horasAcumuladas != null ? horasAcumuladas : 0;
            this.estadoTimer = "FINALIZADO";
            this.fechaHoraFin = LocalDateTime.now();
        }
        System.out.println("PAUSAR - Inicio: " + fechaHoraInicio + ", Ahora: " + LocalDateTime.now() + ", Acumulado antes: " + horasAcumuladas);
    }

    public boolean isCasoFinalizado() {
        return "FINALIZADO".equals(this.estadoTimer);
    }
}