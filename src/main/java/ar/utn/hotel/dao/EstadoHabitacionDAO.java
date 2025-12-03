package ar.utn.hotel.dao;

import ar.utn.hotel.model.EstadoHabitacion;

public interface EstadoHabitacionDAO {
    EstadoHabitacion obtenerPorNombre(String nombre);
    EstadoHabitacion guardar(EstadoHabitacion estado);
}