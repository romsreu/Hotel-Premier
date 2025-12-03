package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "reserva")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", nullable = false)
    private Persona persona;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "reserva_habitacion",
            joinColumns = @JoinColumn(name = "reserva_id"),
            inverseJoinColumns = @JoinColumn(name = "habitacion_numero")
    )
    @Builder.Default
    private Set<Habitacion> habitaciones = new HashSet<>();

    public void agregarHabitacion(Habitacion habitacion) {
        this.habitaciones.add(habitacion);
        habitacion.getReservas().add(this);
    }

    public void removerHabitacion(Habitacion habitacion) {
        this.habitaciones.remove(habitacion);
        habitacion.getReservas().remove(this);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id=" + id +
                ", persona=" + (persona != null ? persona.getNombre() + " " + persona.getApellido() : "null") +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", cantidadHabitaciones=" + habitaciones.size() +
                '}';
    }
}