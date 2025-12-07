package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.interfaces.HuespedDAO;
import ar.utn.hotel.dao.interfaces.ReservaDAO;
import ar.utn.hotel.dao.implement.HuespedDAOImpl;
import ar.utn.hotel.dao.implement.ReservaDAOImpl;
import ar.utn.hotel.dao.implement.TipoEstadoDAOImpl;
import ar.utn.hotel.dto.CrearReservaDTO;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.model.Huesped;
import ar.utn.hotel.model.Reserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gestor que maneja la lógica de negocio relacionada con las reservas.
 * Coordina con GestorHabitacion para cambiar estados de habitaciones.
 */
public class GestorReserva {

    private final ReservaDAO reservaDAO;
    private final HuespedDAO huespedDAO;
    private GestorHabitacion gestorHabitacion; // Referencia circular controlada

    public GestorReserva(ReservaDAO reservaDAO, HuespedDAO huespedDAO) {
        this.reservaDAO = reservaDAO;
        this.huespedDAO = huespedDAO;
    }

    public GestorReserva() {
        this.huespedDAO = new HuespedDAOImpl();
        TipoEstadoDAOImpl tipoEstadoDAO = new TipoEstadoDAOImpl();
        this.reservaDAO = new ReservaDAOImpl(tipoEstadoDAO);
    }

    /**
     * Establece la referencia al gestor de habitaciones (para evitar dependencia circular en constructor)
     */
    public void setGestorHabitacion(GestorHabitacion gestorHabitacion) {
        this.gestorHabitacion = gestorHabitacion;
    }

    /**
     * Crea una reserva para un huésped registrado.
     * IMPORTANTE: Este método también cambia el estado de la habitación a RESERVADA.
     *
     * @param dto DTO con la información para crear la reserva
     * @return ReservaDTO con los datos de la reserva creada
     * @throws Exception si hay algún error en el proceso
     */
    public ReservaDTO crearReserva(CrearReservaDTO dto) throws Exception {
        validarCrearReservaDTO(dto);

        // Verificar que el huésped existe
        Huesped huesped = huespedDAO.obtenerPorId(dto.getIdHuesped());
        if (huesped == null) {
            throw new IllegalArgumentException("Error: El huésped no existe en el sistema.");
        }

        // Crear la reserva (el DAO ya maneja el cambio de estado de la habitación)
        Reserva reserva = reservaDAO.crearReserva(dto);

        // Cambiar estado de la habitación a RESERVADA usando el gestor
        if (gestorHabitacion != null) {
            gestorHabitacion.reservarHabitaciones(
                    Collections.singleton(dto.getNumeroHabitacion()),
                    dto.getFechaInicio(),
                    dto.getFechaFin()
            );
        }

        return toDTO(reserva);
    }


    public List<ReservaDTO> crearReservasMultiples(List<CrearReservaDTO> dtos) throws Exception {
        List<ReservaDTO> reservasCreadas = new ArrayList<>();

        for (CrearReservaDTO dto : dtos) {
            ReservaDTO reserva = crearReserva(dto);
            reservasCreadas.add(reserva);

            // Pequeño delay entre operaciones
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return reservasCreadas;
    }

    /**
     * Obtiene una reserva por su ID
     */
    public ReservaDTO obtenerReserva(Long id) {
        Reserva reserva = reservaDAO.obtenerPorId(id);

        if (reserva == null) {
            throw new IllegalArgumentException("No existe reserva con el ID " + id);
        }

        return toDTO(reserva);
    }

    /**
     * Obtiene la entidad Reserva (no DTO) por ID
     * Útil para otros gestores que necesitan la entidad completa
     */
    public Reserva obtenerReservaEntidad(Long id) {
        return reservaDAO.obtenerPorId(id);
    }

    /**
     * Lista todas las reservas
     */
    public List<ReservaDTO> listarReservas() {
        List<Reserva> reservas = reservaDAO.obtenerTodas();

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas por huésped
     */
    public List<ReservaDTO> obtenerReservasPorHuesped(Long idHuesped) {
        List<Reserva> reservas = reservaDAO.obtenerPorHuesped(idHuesped);

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas en un rango de fechas
     */
    public List<ReservaDTO> obtenerReservasPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        List<Reserva> reservas = reservaDAO.obtenerPorFechas(fechaInicio, fechaFin);

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene reservas por habitación
     */
    public List<ReservaDTO> obtenerReservasPorHabitacion(Integer numeroHabitacion) {
        List<Reserva> reservas = reservaDAO.obtenerPorHabitacion(numeroHabitacion);

        return reservas.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cancela una reserva y libera la habitación (cambia estado a DISPONIBLE)
     */
    public void cancelarReserva(Long id) {
        Reserva reserva = reservaDAO.obtenerPorId(id);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe reserva con el ID " + id);
        }

        // Verificar que la reserva no tenga estadía asociada
        if (reserva.getEstadia() != null) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva que ya tiene una estadía asociada (check-in realizado)"
            );
        }

        // Liberar la habitación (cambiar estado a DISPONIBLE)
        if (gestorHabitacion != null) {
            gestorHabitacion.liberarHabitaciones(
                    Collections.singleton(reserva.getHabitacion().getNumero())
            );
        }

        // Eliminar la reserva (el DAO también maneja el cambio de estado)
        reservaDAO.eliminar(id);
    }

    /**
     * Busca una reserva por habitación y fecha
     * Útil para verificar si existe una reserva antes de crear una estadía
     */
    public Reserva buscarReservaPorHabitacionYFecha(Integer numeroHabitacion, LocalDate fecha) {
        try {
            List<Reserva> reservas = reservaDAO.obtenerPorHabitacion(numeroHabitacion);

            return reservas.stream()
                    .filter(r -> !fecha.isBefore(r.getFechaInicio()) &&
                            !fecha.isAfter(r.getFechaFin()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            System.err.println("Error al buscar reserva: " + e.getMessage());
            return null;
        }
    }

    /**
     * Busca reservas activas (sin estadía) por habitación
     */
    public List<ReservaDTO> buscarReservasActivasPorHabitacion(Integer numeroHabitacion) {
        List<Reserva> reservas = reservaDAO.obtenerPorHabitacion(numeroHabitacion);

        return reservas.stream()
                .filter(r -> r.getEstadia() == null) // Sin estadía = reserva activa
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una entidad Reserva a DTO.
     */
    private ReservaDTO toDTO(Reserva reserva) {
        return ReservaDTO.builder()
                .id(reserva.getId())
                .idHuesped(reserva.getHuesped().getId())
                .nombreHuesped(reserva.getHuesped().getNombre())
                .apellidoHuesped(reserva.getHuesped().getApellido())
                .telefonoHuesped(reserva.getHuesped().getTelefono())
                .fechaInicio(reserva.getFechaInicio())
                .fechaFin(reserva.getFechaFin())
                .cantHuespedes(reserva.getCantHuespedes())
                .descuento(reserva.getDescuento())
                .numeroHabitacion(reserva.getHabitacion().getNumero())
                .tieneEstadia(reserva.getEstadia() != null)
                .build();
    }

    /**
     * Valida los datos del DTO para crear reserva
     */
    private void validarCrearReservaDTO(CrearReservaDTO dto) {
        if (dto.getIdHuesped() == null) {
            throw new IllegalArgumentException("El ID del huésped es obligatorio");
        }

        if (dto.getNumeroHabitacion() == null) {
            throw new IllegalArgumentException("El número de habitación es obligatorio");
        }

        if (dto.getFechaInicio() == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }

        if (dto.getFechaFin() == null) {
            throw new IllegalArgumentException("La fecha de fin es obligatoria");
        }

        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin"
            );
        }

        if (dto.getFechaInicio().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser anterior a hoy"
            );
        }
    }
}