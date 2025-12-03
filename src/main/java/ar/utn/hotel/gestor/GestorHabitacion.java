package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.TipoHabitacionDAO;
import ar.utn.hotel.dto.HabitacionDTO;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.TipoHabitacion;
import ar.utn.hotel.model.EstadoHabitacion;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GestorHabitacion {

    private final HabitacionDAO habitacionDAO;
    private final TipoHabitacionDAO tipoHabitacionDAO;

    public GestorHabitacion(HabitacionDAO habitacionDAO, TipoHabitacionDAO tipoHabitacionDAO) {
        this.habitacionDAO = habitacionDAO;
        this.tipoHabitacionDAO = tipoHabitacionDAO;
    }

    public void crearHabitacion(HabitacionDTO dto) {
        validarHabitacionDTO(dto);

        // Verificar que no exista ya
        Habitacion existente = habitacionDAO.buscarPorNumero(dto.getNumero());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe una habitación con el número " + dto.getNumero());
        }

        // Buscar el tipo de habitación por nombre o ID
        TipoHabitacion tipo = buscarTipoHabitacion(dto.getTipo());
        if (tipo == null) {
            throw new IllegalArgumentException("No existe el tipo de habitación: " + dto.getTipo());
        }

        Habitacion habitacion = Habitacion.builder()
                .numero(dto.getNumero())
                .tipo(tipo)
                .piso(dto.getPiso())
                .build();

        habitacionDAO.guardar(habitacion);
    }

    public List<HabitacionDTO> obtenerHabitacionesDisponibles(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin"
            );
        }

        List<Habitacion> habitaciones = habitacionDAO.listarPorRangoDeFechas(fechaInicio, fechaFin);

        return habitaciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Reserva una o más habitaciones (cambia su estado a RESERVADA)
    public void reservarHabitaciones(Set<Integer> numerosHabitaciones, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (numerosHabitaciones == null || numerosHabitaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe proporcionar al menos una habitación para reservar"
            );
        }

        if (fechaDesde == null || fechaHasta == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta"
            );
        }

        habitacionDAO.reservarHabitaciones(numerosHabitaciones, fechaDesde, fechaHasta);
    }

    // Ocupa una o más habitaciones (cambia su estado a OCUPADA)
    public void ocuparHabitaciones(Set<Integer> numerosHabitaciones, LocalDate fechaDesde, LocalDate fechaHasta) {
        if (numerosHabitaciones == null || numerosHabitaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe proporcionar al menos una habitación para ocupar"
            );
        }

        if (fechaDesde == null || fechaHasta == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaDesde.isAfter(fechaHasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta"
            );
        }

        habitacionDAO.ocuparHabitaciones(numerosHabitaciones, fechaDesde, fechaHasta);
    }

    // Obtiene una habitación por su número y la convierte a DTO
    public HabitacionDTO obtenerHabitacion(Integer numero) {
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numero);

        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + numero);
        }

        return toDTO(habitacion);
    }

    public List<HabitacionDTO> obtenerTodasHabitaciones() {
        List<Habitacion> habitaciones = habitacionDAO.listarTodas();

        return habitaciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void actualizarHabitacion(HabitacionDTO dto) {
        validarHabitacionDTO(dto);

        Habitacion habitacion = habitacionDAO.buscarPorNumero(dto.getNumero());
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + dto.getNumero());
        }

        // Buscar el tipo de habitación
        TipoHabitacion tipo = buscarTipoHabitacion(dto.getTipo());
        if (tipo == null) {
            throw new IllegalArgumentException("No existe el tipo de habitación: " + dto.getTipo());
        }

        habitacion.setTipo(tipo);
        habitacion.setPiso(dto.getPiso());

        habitacionDAO.actualizar(habitacion);
    }

    public void eliminarHabitacion(Integer numero) {
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numero);
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + numero);
        }

        habitacionDAO.eliminar(numero);
    }

    private HabitacionDTO toDTO(Habitacion habitacion) {
        //EstadoHabitacion estadoActual = habitacion.getEstadoActual();

        return HabitacionDTO.builder()
                .numero(habitacion.getNumero())
                .tipo(habitacion.getTipo() != null ? habitacion.getTipo().getNombre() : null)
                .idTipoHabitacion(habitacion.getTipo() != null ? habitacion.getTipo().getIdTipoHabitacion() : null)
                .costoNoche(habitacion.getCostoNoche())
                .piso(habitacion.getPiso())
                .capacidad(habitacion.getTipo() != null ? habitacion.getTipo().getCapacidad() : null)
                .descripcion(habitacion.getTipo() != null ? habitacion.getTipo().getDescripcion() : null)
                //.estado(estadoActual != null ? estadoActual.getEstado().name() : null)
                .build();
    }

    // Busca un tipo de habitación por nombre o ID
    private TipoHabitacion buscarTipoHabitacion(String tipoStr) {
        if (tipoStr == null || tipoStr.trim().isEmpty()) {
            return null;
        }

        // Intentar buscar por ID si es un número
        try {
            Integer id = Integer.parseInt(tipoStr);
            return tipoHabitacionDAO.buscarPorId(id);
        } catch (NumberFormatException e) {
            // Si no es un número, buscar por nombre
            return tipoHabitacionDAO.buscarPorNombre(tipoStr);
        }
    }

    // Valida los datos del DTO
    private void validarHabitacionDTO(HabitacionDTO dto) {
        if (dto.getNumero() == null || dto.getNumero() <= 0) {
            throw new IllegalArgumentException("El número de habitación es inválido");
        }

        if (dto.getTipo() == null || dto.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de habitación es obligatorio");
        }
    }
}