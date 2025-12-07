package ar.utn.hotel.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitacionDTO {
    private Integer numero;
    private String tipo;
    private Integer idTipoHabitacion;
    private Double costoNoche;
    private Integer piso;
    private Integer capacidad;
    private String descripcion;
    private String estadoActual; // DISPONIBLE, OCUPADA, RESERVADA, MANTENIMIENTO
}