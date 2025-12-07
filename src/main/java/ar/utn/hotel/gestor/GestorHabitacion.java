package ar.utn.hotel.gestor;

import ar.utn.hotel.dao.implement.*;
import ar.utn.hotel.dao.interfaces.*;
import ar.utn.hotel.dto.EstadiaDTO;
import ar.utn.hotel.dto.HabitacionDTO;
import ar.utn.hotel.model.*;
import enums.EstadoHab;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestor que maneja la lógica de negocio relacionada con las habitaciones.
 * Incluye gestión de estados, reservas, estadías y catálogos base del sistema.
 */
public class GestorHabitacion {

    private final HabitacionDAO habitacionDAO;
    private final TipoHabitacionDAO tipoHabitacionDAO;
    private final EstadoHabitacionDAO estadoHabitacionDAO;
    private final TipoEstadoDAO tipoEstadoDAO;
    private final EstadiaDAO estadiaDAO;
    private final ReservaDAO reservaDAO;
    private GestorReserva gestorReserva; // Referencia circular controlada

    public GestorHabitacion(HabitacionDAO habitacionDAO,
                            TipoHabitacionDAO tipoHabitacionDAO,
                            EstadoHabitacionDAO estadoHabitacionDAO,
                            TipoEstadoDAO tipoEstadoDAO,
                            EstadiaDAO estadiaDAO,
                            ReservaDAO reservaDAO) {
        this.habitacionDAO = habitacionDAO;
        this.tipoHabitacionDAO = tipoHabitacionDAO;
        this.estadoHabitacionDAO = estadoHabitacionDAO;
        this.tipoEstadoDAO = tipoEstadoDAO;
        this.estadiaDAO = estadiaDAO;
        this.reservaDAO = reservaDAO;
    }

    public GestorHabitacion() {
        this.tipoEstadoDAO = new TipoEstadoDAOImpl();
        this.estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        this.habitacionDAO = new HabitacionDAOImpl(tipoEstadoDAO);
        this.tipoHabitacionDAO = new TipoHabitacionDAOImpl();
        this.estadiaDAO = new EstadiaDAOImpl();
        this.reservaDAO = new ReservaDAOImpl(tipoEstadoDAO);
    }

    /**
     * Establece la referencia al gestor de reservas (para evitar dependencia circular)
     */
    public void setGestorReserva(GestorReserva gestorReserva) {
        this.gestorReserva = gestorReserva;
    }

    // ========== GESTIÓN DE CATÁLOGOS BASE ==========

    /**
     * Crea un tipo de estado en el catálogo
     */
    public TipoEstado crearTipoEstado(EstadoHab estado) {
        if (tipoEstadoDAO.existeEstado(estado)) {
            throw new IllegalArgumentException("Ya existe el tipo de estado: " + estado.name());
        }

        TipoEstado tipoEstado = TipoEstado.builder()
                .estado(estado)
                .build();

        return tipoEstadoDAO.guardar(tipoEstado);
    }

    /**
     * Verifica si existe un tipo de estado
     */
    public boolean existeTipoEstado(EstadoHab estado) {
        return tipoEstadoDAO.existeEstado(estado);
    }

    /**
     * Lista todos los tipos de estado del catálogo
     */
    public List<TipoEstado> listarTiposEstado() {
        return tipoEstadoDAO.listarTodos();
    }

    /**
     * Crea un tipo de habitación en el catálogo
     */
    public TipoHabitacion crearTipoHabitacion(String nombre, String descripcion,
                                              Integer capacidad, Double costoNoche) {
        if (tipoHabitacionDAO.existeNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe un tipo de habitación con el nombre: " + nombre);
        }

        TipoHabitacion tipo = TipoHabitacion.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .capacidad(capacidad)
                .costoNoche(costoNoche)
                .build();

        return tipoHabitacionDAO.guardar(tipo);
    }

    /**
     * Busca un tipo de habitación por nombre
     */
    public TipoHabitacion buscarTipoHabitacionPorNombre(String nombre) {
        return tipoHabitacionDAO.buscarPorNombre(nombre);
    }

    /**
     * Verifica si existe un tipo de habitación
     */
    public boolean existeTipoHabitacion(String nombre) {
        return tipoHabitacionDAO.existeNombre(nombre);
    }

    /**
     * Lista todos los tipos de habitación del catálogo
     */
    public List<TipoHabitacion> listarTiposHabitacion() {
        return tipoHabitacionDAO.listarTodos();
    }

    // ========== GESTIÓN DE HABITACIONES ==========

    /**
     * Crea una nueva habitación con estado inicial DISPONIBLE
     */
    public void crearHabitacion(HabitacionDTO dto) {
        validarHabitacionDTO(dto);

        // Verificar que no exista ya
        if (habitacionDAO.existeNumero(dto.getNumero())) {
            throw new IllegalArgumentException("Ya existe una habitación con el número " + dto.getNumero());
        }

        // Buscar el tipo de habitación
        TipoHabitacion tipo = buscarTipoHabitacion(dto.getTipo());
        if (tipo == null) {
            throw new IllegalArgumentException("No existe el tipo de habitación: " + dto.getTipo());
        }

        // Crear la habitación
        Habitacion habitacion = Habitacion.builder()
                .numero(dto.getNumero())
                .tipo(tipo)
                .piso(dto.getPiso())
                .build();

        habitacionDAO.guardar(habitacion);

        // Crear estado inicial DISPONIBLE
        crearEstadoInicial(habitacion);
    }

    /**
     * Crea el estado inicial DISPONIBLE para una habitación nueva
     */
    private void crearEstadoInicial(Habitacion habitacion) {
        TipoEstado tipoDisponible = tipoEstadoDAO.buscarPorEstado(EstadoHab.DISPONIBLE);
        if (tipoDisponible == null) {
            throw new IllegalStateException("No existe el tipo de estado DISPONIBLE en el sistema");
        }

        EstadoHabitacion estadoInicial = EstadoHabitacion.builder()
                .habitacion(habitacion)
                .tipoEstado(tipoDisponible)
                .fechaDesde(LocalDate.now())
                .fechaHasta(null) // Sin fecha fin = indefinido
                .build();

        estadoHabitacionDAO.guardar(estadoInicial);
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
     * Obtiene una habitación por su número
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
     * Obtiene habitaciones por tipo
     */
    public List<HabitacionDTO> obtenerHabitacionesPorTipo(TipoHabitacion tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de habitación no puede ser nulo");
        }

        List<Habitacion> habitaciones = habitacionDAO.buscarPorTipo(tipo);

        return habitaciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene habitaciones por estado
     */
    public List<HabitacionDTO> obtenerHabitacionesPorEstado(EstadoHab estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }

        List<Habitacion> habitaciones = habitacionDAO.buscarPorEstado(estado);

        return habitaciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza una habitación
     */
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

    /**
     * Elimina una habitación
     */
    public void eliminarHabitacion(Integer numero) {
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numero);
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con el número " + numero);
        }

        // Verificar que no tenga reservas o estadías activas
        EstadoHab estadoActual = obtenerEstadoActual(numero);
        if (estadoActual == EstadoHab.RESERVADA || estadoActual == EstadoHab.OCUPADA) {
            throw new IllegalStateException(
                    "No se puede eliminar una habitación que está reservada u ocupada"
            );
        }

        habitacionDAO.eliminar(numero);
    }

    /**
     * Lista todas las habitaciones (devuelve entidades, no DTOs)
     */
    public List<Habitacion> listarTodasHabitaciones() {
        return habitacionDAO.listarTodas();
    }

    // ========== GESTIÓN DE ESTADOS ==========

    /**
     * Reserva habitaciones (cambia estado a RESERVADA)
     * Este método solo cambia el estado, la reserva se crea en GestorReserva
     */
    public void reservarHabitaciones(Set<Integer> numerosHabitaciones,
                                     LocalDate fechaDesde,
                                     LocalDate fechaHasta) {
        validarParametrosReserva(numerosHabitaciones, fechaDesde, fechaHasta);

        for (Integer numeroHab : numerosHabitaciones) {
            cambiarEstadoHabitacion(numeroHab, EstadoHab.RESERVADA, fechaDesde, fechaHasta);
        }
    }

    /**
     * Ocupa habitaciones (cambia estado a OCUPADA)
     * Debe llamarse después de crear la estadía
     */
    public void ocuparHabitaciones(Set<Integer> numerosHabitaciones,
                                   LocalDate fechaDesde,
                                   LocalDate fechaHasta) {
        validarParametrosReserva(numerosHabitaciones, fechaDesde, fechaHasta);

        for (Integer numeroHab : numerosHabitaciones) {
            cambiarEstadoHabitacion(numeroHab, EstadoHab.OCUPADA, fechaDesde, fechaHasta);
        }
    }

    /**
     * Libera habitaciones (cambia estado a DISPONIBLE)
     */
    public void liberarHabitaciones(Set<Integer> numerosHabitaciones) {
        if (numerosHabitaciones == null || numerosHabitaciones.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos una habitación");
        }

        for (Integer numeroHab : numerosHabitaciones) {
            cambiarEstadoHabitacion(numeroHab, EstadoHab.DISPONIBLE, LocalDate.now(), null);
        }
    }

    /**
     * Pone habitaciones en mantenimiento
     */
    public void ponerEnMantenimiento(Set<Integer> numerosHabitaciones,
                                     LocalDate fechaDesde,
                                     LocalDate fechaHasta) {
        if (numerosHabitaciones == null || numerosHabitaciones.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos una habitación");
        }

        if (fechaDesde == null) {
            fechaDesde = LocalDate.now();
        }

        for (Integer numeroHab : numerosHabitaciones) {
            cambiarEstadoHabitacion(numeroHab, EstadoHab.MANTENIMIENTO, fechaDesde, fechaHasta);
        }
    }

    /**
     * Cambia el estado de una habitación
     * Cierra el estado anterior y crea uno nuevo
     */
    private void cambiarEstadoHabitacion(Integer numeroHabitacion,
                                         EstadoHab nuevoEstado,
                                         LocalDate fechaDesde,
                                         LocalDate fechaHasta) {
        // Verificar que la habitación exista
        Habitacion habitacion = habitacionDAO.buscarPorNumero(numeroHabitacion);
        if (habitacion == null) {
            throw new IllegalArgumentException("No existe habitación con número " + numeroHabitacion);
        }

        // Buscar el tipo de estado
        TipoEstado tipoEstado = tipoEstadoDAO.buscarPorEstado(nuevoEstado);
        if (tipoEstado == null) {
            throw new IllegalStateException("No existe el tipo de estado: " + nuevoEstado);
        }

        // Cerrar el estado actual si existe y está abierto
        EstadoHabitacion estadoActual = estadoHabitacionDAO.obtenerEstadoActual(numeroHabitacion);
        if (estadoActual != null && estadoActual.getFechaHasta() == null) {
            estadoActual.setFechaHasta(fechaDesde.minusDays(1));
            estadoHabitacionDAO.actualizar(estadoActual);
        }

        // Crear el nuevo estado
        EstadoHabitacion nuevoEstadoHab = EstadoHabitacion.builder()
                .habitacion(habitacion)
                .tipoEstado(tipoEstado)
                .fechaDesde(fechaDesde)
                .fechaHasta(fechaHasta)
                .build();

        estadoHabitacionDAO.guardar(nuevoEstadoHab);
    }

    /**
     * Obtiene el estado actual de una habitación
     */
    public EstadoHab obtenerEstadoActual(Integer numeroHabitacion) {
        EstadoHabitacion estado = estadoHabitacionDAO.obtenerEstadoActual(numeroHabitacion);
        return estado != null ? estado.getTipoEstado().getEstado() : null;
    }

    /**
     * Obtiene el estado de una habitación en una fecha específica
     */
    public EstadoHab obtenerEstadoEn(Integer numeroHabitacion, LocalDate fecha) {
        EstadoHabitacion estado = estadoHabitacionDAO.obtenerEstadoEn(numeroHabitacion, fecha);
        return estado != null ? estado.getTipoEstado().getEstado() : null;
    }

    // ========== GESTIÓN DE ESTADÍAS (OCUPACIONES) ==========

    /**
     * Realiza el check-in: convierte una reserva en una estadía y cambia el estado a OCUPADA
     *
     * @param idReserva ID de la reserva a convertir
     * @return EstadiaDTO con la información de la estadía creada
     */
    public EstadiaDTO realizarCheckIn(Long idReserva) {
        // Buscar la reserva
        Reserva reserva = reservaDAO.obtenerPorId(idReserva);
        if (reserva == null) {
            throw new IllegalArgumentException("No existe reserva con ID " + idReserva);
        }

        // Verificar que no tenga ya una estadía
        if (reserva.getEstadia() != null) {
            throw new IllegalArgumentException("La reserva ya tiene una estadía asociada");
        }

        // Crear la estadía usando el método del DAO
        Estadia estadia = estadiaDAO.crearDesdeReserva(reserva);

        // Cambiar estado de la habitación a OCUPADA
        Set<Integer> habitacion = Collections.singleton(reserva.getHabitacion().getNumero());
        ocuparHabitaciones(habitacion, reserva.getFechaInicio(), reserva.getFechaFin());

        return toEstadiaDTO(estadia);
    }

    /**
     * Realiza el check-out: marca la hora de salida y libera la habitación
     *
     * @param idEstadia ID de la estadía
     * @return EstadiaDTO actualizada
     */
    public EstadiaDTO realizarCheckOut(Integer idEstadia) {
        Estadia estadia = estadiaDAO.buscarPorId(idEstadia);
        if (estadia == null) {
            throw new IllegalArgumentException("No existe estadía con ID " + idEstadia);
        }

        if (estadia.getHoraCheckOut() != null) {
            throw new IllegalArgumentException("La estadía ya tiene check-out registrado");
        }

        // Registrar hora de check-out
        estadia.setHoraCheckOut(LocalDateTime.now());
        estadiaDAO.actualizar(estadia);

        // Cambiar estado de la habitación a DISPONIBLE
        Set<Integer> habitacion = Collections.singleton(estadia.getHabitacion().getNumero());
        liberarHabitaciones(habitacion);

        return toEstadiaDTO(estadia);
    }

    /**
     * Crea una estadía directamente desde una reserva (ocupación directa con check-in)
     *
     * @param reserva Reserva ya creada
     * @return EstadiaDTO de la estadía creada
     */
    public EstadiaDTO crearEstadiaDirecta(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }

        // Crear la estadía
        Estadia estadia = estadiaDAO.crearDesdeReserva(reserva);

        // Cambiar estado a OCUPADA
        Set<Integer> habitacion = Collections.singleton(reserva.getHabitacion().getNumero());
        ocuparHabitaciones(habitacion, reserva.getFechaInicio(), reserva.getFechaFin());

        return toEstadiaDTO(estadia);
    }

    /**
     * Obtiene todas las estadías activas (sin check-out)
     */
    public List<EstadiaDTO> listarEstadiasActivas() {
        return estadiaDAO.listarActivas().stream()
                .map(this::toEstadiaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadías por habitación
     */
    public List<EstadiaDTO> listarEstadiasPorHabitacion(Integer numeroHabitacion) {
        return estadiaDAO.listarPorHabitacion(numeroHabitacion).stream()
                .map(this::toEstadiaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadías en un rango de fechas
     */
    public List<EstadiaDTO> listarEstadiasPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.isAfter(fechaFin)) {
            throw new IllegalArgumentException(
                    "La fecha de inicio no puede ser posterior a la fecha de fin"
            );
        }

        return estadiaDAO.listarPorFechas(fechaInicio, fechaFin).stream()
                .map(this::toEstadiaDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca estadía por reserva
     */
    public EstadiaDTO buscarEstadiaPorReserva(Long idReserva) {
        Estadia estadia = estadiaDAO.buscarPorReserva(idReserva);
        return estadia != null ? toEstadiaDTO(estadia) : null;
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Convierte una entidad Habitacion a DTO
     */
    private HabitacionDTO toDTO(Habitacion habitacion) {
        EstadoHab estadoActual = obtenerEstadoActual(habitacion.getNumero());

        return HabitacionDTO.builder()
                .numero(habitacion.getNumero())
                .tipo(habitacion.getTipo() != null ? habitacion.getTipo().getNombre() : null)
                .idTipoHabitacion(habitacion.getTipo() != null ?
                        habitacion.getTipo().getIdTipoHabitacion() : null)
                .costoNoche(habitacion.getCostoNoche())
                .piso(habitacion.getPiso())
                .capacidad(habitacion.getTipo() != null ? habitacion.getTipo().getCapacidad() : null)
                .descripcion(habitacion.getTipo() != null ? habitacion.getTipo().getDescripcion() : null)
                .estadoActual(estadoActual != null ? estadoActual.name() : null)
                .build();
    }

    /**
     * Convierte una entidad Estadia a DTO
     */
    private EstadiaDTO toEstadiaDTO(Estadia estadia) {
        return EstadiaDTO.builder()
                .idEstadia(estadia.getIdEstadia())
                .idReserva(estadia.getReserva().getId())
                .numeroHabitacion(estadia.getHabitacion().getNumero())
                .nombreHuesped(estadia.getReserva().getHuesped().getNombre())
                .apellidoHuesped(estadia.getReserva().getHuesped().getApellido())
                .fechaInicio(estadia.getFechaInicio())
                .fechaFin(estadia.getFechaFin())
                .horaCheckIn(estadia.getHoraCheckIn())
                .horaCheckOut(estadia.getHoraCheckOut())
                .build();
    }

    /**
     * Busca un tipo de habitación por nombre o ID
     */
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

        if (dto.getPiso() == null || dto.getPiso() < 0) {
            throw new IllegalArgumentException("El piso es inválido");
        }
    }

    /**
     * Obtiene todos los estados de múltiples habitaciones en un rango de fechas
     * Optimizado para evitar N+1 queries
     */
    public Map<String, EstadoHab> obtenerEstadosEnRango(List<Integer> numerosHabitaciones,
                                                        LocalDate fechaInicio,
                                                        LocalDate fechaFin) {
        return estadoHabitacionDAO.obtenerEstadosEnRango(numerosHabitaciones, fechaInicio, fechaFin);
    }

    /**
     * Valida parámetros comunes de reserva/ocupación
     */
    private void validarParametrosReserva(Set<Integer> numerosHabitaciones,
                                          LocalDate fechaDesde,
                                          LocalDate fechaHasta) {
        if (numerosHabitaciones == null || numerosHabitaciones.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe proporcionar al menos una habitación"
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
    }
}