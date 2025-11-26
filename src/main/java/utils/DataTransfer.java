package utils;

import ar.utn.hotel.model.Huesped;
import enums.ContextoEstadoHabitaciones;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class DataTransfer {
    private static List<Huesped> huespedesEnBusqueda;
    @Getter
    private static LocalDate fechaDesdeEstadoHabitaciones;
    @Getter
    private static LocalDate fechaHastaEstadoHabitaciones;
    @Getter
    private static ContextoEstadoHabitaciones contextoEstadoHabitaciones;

    public static void setHuespedesEnBusqueda(List<Huesped> huespedes) {
        DataTransfer.huespedesEnBusqueda = huespedes;
    }

    public static List<Huesped> getHuespedesEnBusqueda() {
        return DataTransfer.huespedesEnBusqueda;
    }

    public static void setRangoFechasEstadoHabitaciones(LocalDate fechaDesde, LocalDate fechaHasta) {
        DataTransfer.fechaDesdeEstadoHabitaciones = fechaDesde;
        DataTransfer.fechaHastaEstadoHabitaciones = fechaHasta;
    }

    public static void setContextoEstadoHabitaciones(ContextoEstadoHabitaciones contexto) {
        DataTransfer.contextoEstadoHabitaciones = contexto;
    }

    public static void limpiar() {
        DataTransfer.huespedesEnBusqueda = null;
        DataTransfer.fechaDesdeEstadoHabitaciones = null;
        DataTransfer.fechaHastaEstadoHabitaciones = null;
        DataTransfer.contextoEstadoHabitaciones = null;
    }
}