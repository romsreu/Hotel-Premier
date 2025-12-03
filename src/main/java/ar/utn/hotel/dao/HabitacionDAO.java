package ar.utn.hotel.dao;

import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.TipoHabitacion; // ⬅️ Cambio aquí

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface HabitacionDAO {
    Habitacion guardar(Habitacion habitacion);
    Habitacion buscarPorNumero(Integer numero);
    List<Habitacion> listarTodas();
    List<Habitacion> listarPorRangoDeFechas(LocalDate fechaInicio, LocalDate fechaFin);
    void reservarHabitaciones(Set<Integer> numerosHabitaciones);
    List<Habitacion> buscarPorTipo(TipoHabitacion tipo); // ⬅️ Cambiado de enum a entidad
    List<Habitacion> buscarDisponibles();
    void actualizar(Habitacion habitacion);
    void eliminar(Integer numero);
    boolean existeNumero(String numero);
    Long contarPorTipo(TipoHabitacion tipo); // ⬅️ Cambiado de enum a entidad
}