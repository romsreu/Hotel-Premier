package ar.utn.hotel.dao.interfaces;

import ar.utn.hotel.model.Estadia;
import ar.utn.hotel.model.Reserva;
import java.time.LocalDate;
import java.util.List;

public interface EstadiaDAO {
    Estadia guardar(Estadia estadia);
    Estadia buscarPorId(Integer id);
    Estadia buscarPorReserva(Long idReserva);
    List<Estadia> listarTodas();
    List<Estadia> listarActivas();
    List<Estadia> listarPorHabitacion(Integer numeroHabitacion);
    List<Estadia> listarPorFechas(LocalDate fechaInicio, LocalDate fechaFin);
    void actualizar(Estadia estadia);
    void eliminar(Integer id);
    Estadia crearDesdeReserva(Reserva reserva);
}