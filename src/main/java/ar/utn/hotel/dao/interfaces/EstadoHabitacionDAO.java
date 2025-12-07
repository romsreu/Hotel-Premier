package ar.utn.hotel.dao.interfaces;

import ar.utn.hotel.model.EstadoHabitacion;
import enums.EstadoHab;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface EstadoHabitacionDAO {
    EstadoHabitacion guardar(EstadoHabitacion estado);
    EstadoHabitacion buscarPorId(Integer id);
    List<EstadoHabitacion> listarTodos();
    List<EstadoHabitacion> listarPorHabitacion(Integer numeroHabitacion);
    List<EstadoHabitacion> listarActivos();
    List<EstadoHabitacion> listarPorTipoEstado(EstadoHab estado);
    EstadoHabitacion obtenerEstadoActual(Integer numeroHabitacion);
    EstadoHabitacion obtenerEstadoEn(Integer numeroHabitacion, LocalDate fecha);
    void actualizar(EstadoHabitacion estadoHabitacion);
    void eliminar(Integer id);
    Map<String, EstadoHab> obtenerEstadosEnRango(List<Integer> numerosHabitaciones,
                                                 LocalDate fechaInicio,
                                                 LocalDate fechaFin);
}