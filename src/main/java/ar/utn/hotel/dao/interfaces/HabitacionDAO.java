package ar.utn.hotel.dao.interfaces;

import ar.utn.hotel.model.TipoHabitacion;
import enums.EstadoHab;
import ar.utn.hotel.model.Habitacion;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface HabitacionDAO {

    Habitacion guardar(Habitacion habitacion);

    Habitacion buscarPorNumero(Integer numero);

    List<Habitacion> listarTodas();

    List<Habitacion> listarPorRangoDeFechas(LocalDate fechaInicio, LocalDate fechaFin);

    List<Habitacion> buscarPorTipo(TipoHabitacion tipo);

    List<Habitacion> buscarPorEstado(EstadoHab estado);

    List<Habitacion> buscarDisponibles();

    void actualizar(Habitacion habitacion);

    void eliminar(Integer id);

    void reservarHabitaciones(Set<Integer> numerosHabitaciones, LocalDate fechaDesde, LocalDate fechaHasta);

    void ocuparHabitaciones(Set<Integer> numerosHabitaciones, LocalDate fechaDesde, LocalDate fechaHasta);

    boolean existeNumero(Integer numero);

    Long contarPorTipo(TipoHabitacion tipo);
}