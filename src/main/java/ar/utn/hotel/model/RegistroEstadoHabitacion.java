package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "registro_habitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroEstadoHabitacion // Asumo que este código es para RegistroEstadoHabitacion.java
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin; // Será NULL si es el estado actual

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_numero", nullable = false)
    private Habitacion habitacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_habitacion_id", nullable = false)
    private EstadoHabitacion estadoHabitacion;

    public void finalizarRegistro() {
        this.fechaFin = LocalDate.now();
    }
}