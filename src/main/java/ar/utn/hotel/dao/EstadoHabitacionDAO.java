package ar.utn.hotel.dao;

import ar.utn.hotel.model.EstadoHabitacion;
import java.util.List;

public interface EstadoHabitacionDAO {

    EstadoHabitacion guardar(EstadoHabitacion estado);

    EstadoHabitacion buscarPorId(Integer id);

    EstadoHabitacion buscarPorEstado(enums.EstadoHabitacion estado);

    List<EstadoHabitacion> listarTodos();

    void actualizar(EstadoHabitacion estado);

    void eliminar(Integer id);

    boolean existeEstado(enums.EstadoHabitacion estado);
}