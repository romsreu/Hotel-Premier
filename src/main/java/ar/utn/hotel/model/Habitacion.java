package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "habitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {

    @Id
    @Column(nullable = false, unique = true)
    private Integer numero;

    @ManyToOne
    @JoinColumn(name = "id_tipo_habitacion", nullable = false)
    private TipoHabitacion tipo;

    @Column(nullable = false)
    private Integer piso;

    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EstadoHabitacion> estados = new HashSet<>();

    @OneToMany(mappedBy = "habitacion")
    @Builder.Default
    private Set<Reserva> reservas = new HashSet<>();

    @OneToMany(mappedBy = "habitacion")
    @Builder.Default
    private Set<Estadia> estadias = new HashSet<>();

    // Métodos helper
    public Double getCostoNoche() {
        return tipo != null ? tipo.getCostoNoche() : 0.0;
    }

    public EstadoHabitacion getEstadoActual() {
        return estados.stream()
                .filter(EstadoHabitacion::isActivo)
                .findFirst()
                .orElse(null);
    }

    public EstadoHabitacion getEstadoEn(LocalDate fecha) {
        return estados.stream()
                .filter(e -> e.isVigenteEn(fecha))
                .findFirst()
                .orElse(null);
    }
}