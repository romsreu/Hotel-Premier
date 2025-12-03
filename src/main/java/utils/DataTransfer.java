package utils;

import ar.utn.hotel.dto.ReservaDTO; // Asegúrate de tener este import
import ar.utn.hotel.model.Huesped;
import enums.ContextoEstadoHabitaciones;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class DataTransfer {

    // 1. Variables existentes
    private static List<Huesped> huespedesEnBusqueda;

    @Getter
    private static LocalDate fechaDesdeEstadoHabitaciones;

    @Getter
    private static LocalDate fechaHastaEstadoHabitaciones;

    @Getter
    private static ContextoEstadoHabitaciones contextoEstadoHabitaciones;

    // 2. LA VARIABLE QUE FALTABA (Aquí es donde se guarda el DTO)
    @Getter
    private static ReservaDTO reservaPendiente;

    // --- Métodos Setters y Getters ---

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

    // Setter para la reserva pendiente
    public static void setReservaPendiente(ReservaDTO dto) {
        DataTransfer.reservaPendiente = dto;
    }

    // (El getter getReservaPendiente() lo genera Lombok automáticamente por la anotación @Getter)

    public static void limpiar() {
        DataTransfer.huespedesEnBusqueda = null;
        DataTransfer.fechaDesdeEstadoHabitaciones = null;
        DataTransfer.fechaHastaEstadoHabitaciones = null;
        DataTransfer.contextoEstadoHabitaciones = null;
        DataTransfer.reservaPendiente = null; // Limpiamos también el DTO
    }
}