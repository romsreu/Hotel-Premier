package ar.utn.hotel.dao;

import ar.utn.hotel.model.TipoHabitacion;
import java.util.List;

public interface TipoHabitacionDAO {
    TipoHabitacion obtenerPorNombre(String nombre);
    TipoHabitacion obtenerPorId(Integer id);
    List<TipoHabitacion> listarTodos();
    TipoHabitacion guardar(TipoHabitacion tipo);
}