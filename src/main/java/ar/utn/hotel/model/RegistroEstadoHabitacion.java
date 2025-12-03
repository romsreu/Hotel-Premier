package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "registro_estado_habitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroEstadoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numero_habitacion", nullable = false)
    private Habitacion habitacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoHabitacion estado;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta")
    private LocalDate fechaHasta;

    // Método auxiliar para verificar si el registro está activo
    public boolean isActivo() {
        return fechaHasta == null || fechaHasta.isAfter(LocalDate.now());
    }

    // Método auxiliar para verificar si el registro está vigente en una fecha
    public boolean isVigenteEn(LocalDate fecha) {
        boolean despuesDeInicio = !fecha.isBefore(fechaDesde);
        boolean antesDelFin = fechaHasta == null || !fecha.isAfter(fechaHasta);
        return despuesDeInicio && antesDelFin;
    }
}