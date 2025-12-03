package ar.utn.hotel.dao;

import ar.utn.hotel.model.RegistroEstadoHabitacion;
import java.time.LocalDate;
import java.util.List; // <--- Importante: Agregado

public interface RegistroEstadoHabitacionDAO {
    void persist(RegistroEstadoHabitacion registro);
    void merge(RegistroEstadoHabitacion registro);
    RegistroEstadoHabitacion obtenerRegistroActual(Integer numeroHabitacion);
    RegistroEstadoHabitacion obtenerRegistroPorFecha(Integer numeroHabitacion, LocalDate fecha);

    // NUEVO MÉTODO PARA OPTIMIZACIÓN
    List<RegistroEstadoHabitacion> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);
}