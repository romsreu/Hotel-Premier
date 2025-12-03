package ar.utn.hotel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_habitacion") // Se mapea a una tabla
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoHabitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre; // e.g., "DISPONIBLE", "MANTENIMIENTO", "OCUPADA"

    @Column
    private String descripcion;
}