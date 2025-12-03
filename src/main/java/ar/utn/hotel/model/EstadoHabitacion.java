package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, unique = true)
    private enums.EstadoHabitacion estado;

    @OneToMany(mappedBy = "estado", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<RegistroEstadoHabitacion> registros = new HashSet<>();
}