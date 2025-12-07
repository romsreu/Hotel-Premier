package ar.utn.hotel.dao.interfaces;

import ar.utn.hotel.dto.CrearReservaDTO;
import ar.utn.hotel.model.Reserva;
import java.time.LocalDate;
import java.util.List;

public interface ReservaDAO {

    // Crear reserva desde DTO
    Reserva crearReserva(CrearReservaDTO dto);

    // CRUD básico
    Reserva obtenerPorId(Long id);
    List<Reserva> obtenerTodas();
    List<Reserva> obtenerPorHuesped(Long idHuesped);
    List<Reserva> obtenerPorFechas(LocalDate fechaInicio, LocalDate fechaFin);
    List<Reserva> obtenerPorHabitacion(Integer numeroHabitacion);
    void actualizar(Reserva reserva);
    void eliminar(Long id);
}