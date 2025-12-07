package ar.utn.hotel.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {
    private Long id;
    private Long idHuesped;
    private String nombreHuesped;
    private String apellidoHuesped;
    private String telefonoHuesped;
    private Integer numeroHabitacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cantHuespedes;
    private Double descuento;
    private Boolean tieneEstadia;
}