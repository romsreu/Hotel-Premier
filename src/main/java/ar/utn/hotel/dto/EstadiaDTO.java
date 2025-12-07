package ar.utn.hotel.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadiaDTO {
    private Integer idEstadia;
    private Long idReserva;
    private Integer numeroHabitacion;
    private String nombreHuesped;
    private String apellidoHuesped;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDateTime horaCheckIn;
    private LocalDateTime horaCheckOut;
}