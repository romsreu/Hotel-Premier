package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.RegistroEstadoHabitacionDAO;
import ar.utn.hotel.dao.TipoHabitacionDAO;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dao.impl.RegistroEstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.TipoHabitacionDAOImpl;
import ar.utn.hotel.dto.HabitacionDTO;
import ar.utn.hotel.model.EstadoHabitacion;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.RegistroEstadoHabitacion;
import ar.utn.hotel.model.TipoHabitacion;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GestorHabitacion {

    private final HabitacionDAO habitacionDAO;
    private final TipoHabitacionDAO tipoHabitacionDAO;
    private final EstadoHabitacionDAO estadoHabitacionDAO;
    private final RegistroEstadoHabitacionDAO registroEstadoHabitacionDAO;

    public GestorHabitacion() {
        this.habitacionDAO = new HabitacionDAOImpl();
        this.tipoHabitacionDAO = new TipoHabitacionDAOImpl();
        this.estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        this.registroEstadoHabitacionDAO = new RegistroEstadoHabitacionDAOImpl();
    }

    public GestorHabitacion(HabitacionDAO habitacionDAO,
                            TipoHabitacionDAO tipoHabitacionDAO,
                            EstadoHabitacionDAO estadoHabitacionDAO,
                            RegistroEstadoHabitacionDAO registroEstadoHabitacionDAO) {
        this.habitacionDAO = habitacionDAO;
        this.tipoHabitacionDAO = tipoHabitacionDAO;
        this.estadoHabitacionDAO = estadoHabitacionDAO;
        this.registroEstadoHabitacionDAO = registroEstadoHabitacionDAO;
    }

    /**
     * Crea una nueva habitación
     */
    public void crearHabitacion(HabitacionDTO dto) {
        validarHabitacionDTO(dto);

        // Verificar que no exista ya
        Habitacion existente = habitacionDAO.buscarPorNumero(dto.getNumero());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe una habitación con el número " + dto.getNumero());
        }

        // Obtener el tipo de habitación desde la BD
        TipoHabitacion tipo = tipoHabitacionDAO.obtenerPorNombre(dto.getTipo());
        if (tipo == null) {
            throw new IllegalArgumentException("No existe el tipo de habitación: " + dto.getTipo());
        }

        // Crear la habitación
        Habitacion habitacion = Habitacion.builder()
                .numero(dto.getNumero())
                .tipoHabitacion(tipo)
                .capacidad(dto.getCapacidad() != null ? dto.getCapacidad() : calcularCapacidadPorTipo(tipo))
                .descripcion(dto.getDescripcion())
                .build();

        habitacionDAO.guardar(habitacion);

        // Crear el registro inicial de estado (DISPONIBLE)
        EstadoHabitacion estadoDisponible = estadoHabitacionDAO.obtenerPorNombre("DISPONIBLE");
        if (estadoDisponible == null) {
            throw new IllegalStateException("El estado DISPONIBLE no existe en la base de datos");
        }

        RegistroEstadoHabitacion registroInicial = RegistroEstadoHabitacion.builder()
                .habitacion(habitacion)
                .estadoHabitacion(estadoDisponible)
                .fechaInicio(LocalDate.now())
                .fechaFin(null)
                .build();

        registroEstadoHabitacionDAO.persist(registroInicial);
    }

    /**
     * Obtiene habitaciones disponibles en un rango de fechas
     */
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

    /**
     * Reserva una o más habitaciones (cambia su estado a RESERVADA)
     */
    public void reservarHabitaciones(Set<Integer> numerosHabitaciones) {
        if (numerosHabitaciones == null || numerosHabitaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe proporcionar al menos una habitación para reservar"
            );
        }

        habitacionDAO.reservarHabitaciones(numerosHabitaciones);
    }

    /**
     * Obtiene una habitación por su número y la convierte a DTO
     */
    public HabitacionDTO obtenerHabitacion(Integer numero) {
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numero);

        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + numero);
        }

        return toDTO(habitacion);
    }

    /**
     * Obtiene todas las habitaciones
     */
    public List<HabitacionDTO> obtenerTodasHabitaciones() {
        List<Habitacion> habitaciones = habitacionDAO.listarTodas();

        return habitaciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza una habitación (solo descripción y capacidad)
     */
    public void actualizarHabitacion(HabitacionDTO dto) {
        validarHabitacionDTO(dto);

        Habitacion habitacion = habitacionDAO.buscarPorNumero(dto.getNumero());
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + dto.getNumero());
        }

        // Solo se puede actualizar descripción y capacidad, no el tipo ni el costo
        if (dto.getDescripcion() != null) {
            habitacion.setDescripcion(dto.getDescripcion());
        }

        if (dto.getCapacidad() != null && dto.getCapacidad() > 0) {
            habitacion.setCapacidad(dto.getCapacidad());
        }

        habitacionDAO.actualizar(habitacion);
    }

    /**
     * Elimina una habitación
     */
    public void eliminarHabitacion(Integer numero) {
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numero);
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + numero);
        }

        habitacionDAO.eliminar(numero);
    }

    /**
     * Obtiene el estado actual de una habitación
     */
    public String obtenerEstadoActual(Integer numeroHabitacion) {
        RegistroEstadoHabitacion registroActual = registroEstadoHabitacionDAO.obtenerRegistroActual(numeroHabitacion);

        if (registroActual == null) {
            return "SIN ESTADO";
        }

        return registroActual.getEstadoHabitacion().getNombre();
    }

    /**
     * Cambia el estado de una habitación
     */
    public void cambiarEstadoHabitacion(Integer numeroHabitacion, String nuevoEstadoNombre) {
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numeroHabitacion);
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + numeroHabitacion);
        }

        EstadoHabitacion nuevoEstado = estadoHabitacionDAO.obtenerPorNombre(nuevoEstadoNombre);
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("No existe el estado: " + nuevoEstadoNombre);
        }

        // Finalizar el registro actual
        RegistroEstadoHabitacion registroActual = registroEstadoHabitacionDAO.obtenerRegistroActual(numeroHabitacion);
        if (registroActual != null) {
            registroActual.finalizarRegistro();
            registroEstadoHabitacionDAO.merge(registroActual);
        }

        // Crear nuevo registro de estado
        RegistroEstadoHabitacion nuevoRegistro = RegistroEstadoHabitacion.builder()
                .habitacion(habitacion)
                .estadoHabitacion(nuevoEstado)
                .fechaInicio(LocalDate.now())
                .fechaFin(null)
                .build();

        registroEstadoHabitacionDAO.persist(nuevoRegistro);
    }

    /**
     * Convierte una entidad Habitacion a DTO
     */
    private HabitacionDTO toDTO(Habitacion habitacion) {
        // Obtener el estado actual
        RegistroEstadoHabitacion registroActual = registroEstadoHabitacionDAO.obtenerRegistroActual(habitacion.getNumero());
        String estadoActual = (registroActual != null) ? registroActual.getEstadoHabitacion().getNombre() : "SIN ESTADO";

        return HabitacionDTO.builder()
                .numero(habitacion.getNumero())
                .tipo(habitacion.getTipoHabitacion().getNombre())
                .tipoDescripcion(habitacion.getTipoHabitacion().getDescripcion())
                .costoNoche(habitacion.getTipoHabitacion().getCostoNoche())
                .capacidad(habitacion.getCapacidad())
                .descripcion(habitacion.getDescripcion())
                .build();
    }

    /**
     * Valida los datos del DTO
     */
    private void validarHabitacionDTO(HabitacionDTO dto) {
        if (dto.getNumero() == null || dto.getNumero() <= 0) {
            throw new IllegalArgumentException("El número de habitación es inválido");
        }

        if (dto.getTipo() == null || dto.getTipo().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de habitación es obligatorio");
        }
    }

    /**
     * Calcula la capacidad por defecto según el tipo de habitación
     */
    private int calcularCapacidadPorTipo(TipoHabitacion tipo) {
        return switch (tipo.getNombre()) {
            case "INDIVIDUAL_ESTANDAR" -> 1;
            case "DOBLE_ESTANDAR", "DOBLE_SUPERIOR", "SUITE_DOBLE" -> 2;
            case "SUPERIOR_FAMILY_PLAN" -> 4;
            default -> 2;
        };
    }
}