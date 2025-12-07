package ar.utn.hotel.dao.interfaces;

import ar.utn.hotel.model.TipoHabitacion;
import java.util.List;

public interface TipoHabitacionDAO {
    TipoHabitacion guardar(TipoHabitacion tipo);
    TipoHabitacion buscarPorId(Integer id);
    TipoHabitacion buscarPorNombre(String nombre);
    List<TipoHabitacion> listarTodos();
    void actualizar(TipoHabitacion tipo);
    void eliminar(Integer id);
    boolean existeNombre(String nombre);
    List<TipoHabitacion> buscarPorCapacidad(Integer capacidad);
    List<TipoHabitacion> buscarPorRangoPrecio(Double precioMin, Double precioMax);
}