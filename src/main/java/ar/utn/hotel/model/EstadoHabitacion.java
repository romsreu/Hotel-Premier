package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "estado_habitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoHabitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "numero_habitacion", nullable = false)
    private Habitacion habitacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_tipo_estado", nullable = false)
    private TipoEstado tipoEstado;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta")
    private LocalDate fechaHasta;

    // Método auxiliar para verificar si el estado está activo
    public boolean isActivo() {
        return fechaHasta == null || !fechaHasta.isBefore(LocalDate.now());
    }

    // Método auxiliar para verificar si el estado está vigente en una fecha
    public boolean isVigenteEn(LocalDate fecha) {
        boolean despuesDeInicio = !fecha.isBefore(fechaDesde);
        boolean antesDelFin = fechaHasta == null || !fecha.isAfter(fechaHasta);
        return despuesDeInicio && antesDelFin;
    }
}