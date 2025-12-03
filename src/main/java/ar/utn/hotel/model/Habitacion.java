package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "habitaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habitacion {

    @Id
    @Column(nullable = false, unique = true)
    private Integer numero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoHabitacion tipoHabitacion;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(length = 500)
    private String descripcion;

    @ManyToMany(mappedBy = "habitaciones") // "habitaciones" debe coincidir con el nombre del atributo en la clase Reserva
    @ToString.Exclude // IMPORTANTE: Evita bucles infinitos al imprimir
    @EqualsAndHashCode.Exclude // IMPORTANTE: Evita bucles infinitos en comparaciones
    @Builder.Default // Para que el builder inicialice el HashSet
    private Set<Reserva> reservas = new HashSet<>();
}