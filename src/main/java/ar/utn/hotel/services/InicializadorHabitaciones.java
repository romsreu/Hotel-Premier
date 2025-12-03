package ar.utn.hotel.services;

import ar.utn.hotel.dao.EstadoHabitacionDAO;
import ar.utn.hotel.dao.HabitacionDAO;
import ar.utn.hotel.dao.RegistroEstadoHabitacionDAO;
import ar.utn.hotel.dao.TipoHabitacionDAO;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dao.impl.RegistroEstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.TipoHabitacionDAOImpl;
import ar.utn.hotel.model.EstadoHabitacion;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.RegistroEstadoHabitacion;
import ar.utn.hotel.model.TipoHabitacion;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InicializadorHabitaciones {

    private final HabitacionDAO habitacionDAO;
    private final TipoHabitacionDAO tipoHabitacionDAO;
    private final EstadoHabitacionDAO estadoHabitacionDAO;
    private final RegistroEstadoHabitacionDAO registroEstadoHabitacionDAO;

    // Configuración según la tabla - ahora usando nombres de tipos
    private static final Map<String, Integer> CANTIDAD_POR_TIPO = new HashMap<>();

    static {
        CANTIDAD_POR_TIPO.put("INDIVIDUAL_ESTANDAR", 10);
        CANTIDAD_POR_TIPO.put("DOBLE_ESTANDAR", 18);
        CANTIDAD_POR_TIPO.put("DOBLE_SUPERIOR", 8);
        CANTIDAD_POR_TIPO.put("SUPERIOR_FAMILY_PLAN", 10);
        CANTIDAD_POR_TIPO.put("SUITE_DOBLE", 2);
    }

    public InicializadorHabitaciones() {
        this.habitacionDAO = new HabitacionDAOImpl();
        this.tipoHabitacionDAO = new TipoHabitacionDAOImpl();
        this.estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        this.registroEstadoHabitacionDAO = new RegistroEstadoHabitacionDAOImpl();
    }

    public InicializadorHabitaciones(HabitacionDAO habitacionDAO,
                                     TipoHabitacionDAO tipoHabitacionDAO,
                                     EstadoHabitacionDAO estadoHabitacionDAO,
                                     RegistroEstadoHabitacionDAO registroEstadoHabitacionDAO) {
        this.habitacionDAO = habitacionDAO;
        this.tipoHabitacionDAO = tipoHabitacionDAO;
        this.estadoHabitacionDAO = estadoHabitacionDAO;
        this.registroEstadoHabitacionDAO = registroEstadoHabitacionDAO;
    }

    /**
     * Inicializa los tipos de habitación en la base de datos
     * DEBE ejecutarse ANTES de inicializar las habitaciones
     */
    public void inicializarTiposHabitacion() {
        System.out.println("=== Inicializando tipos de habitación ===");

        Map<String, TipoHabitacionConfig> configuraciones = new HashMap<>();
        configuraciones.put("INDIVIDUAL_ESTANDAR", new TipoHabitacionConfig("Individual Estándar", 50800.0));
        configuraciones.put("DOBLE_ESTANDAR", new TipoHabitacionConfig("Doble Estándar", 70230.0));
        configuraciones.put("DOBLE_SUPERIOR", new TipoHabitacionConfig("Doble Superior", 90560.0));
        configuraciones.put("SUPERIOR_FAMILY_PLAN", new TipoHabitacionConfig("Superior Family Plan", 110500.0));
        configuraciones.put("SUITE_DOBLE", new TipoHabitacionConfig("Suite Doble", 128600.0));

        for (Map.Entry<String, TipoHabitacionConfig> entry : configuraciones.entrySet()) {
            String nombre = entry.getKey();
            TipoHabitacionConfig config = entry.getValue();

            // Verificar si ya existe
            TipoHabitacion existente = tipoHabitacionDAO.obtenerPorNombre(nombre);
            if (existente == null) {
                TipoHabitacion tipo = TipoHabitacion.builder()
                        .nombre(nombre)
                        .descripcion(config.descripcion)
                        .costoNoche(config.costoNoche)
                        .build();

                tipoHabitacionDAO.guardar(tipo);
                System.out.println("✓ Tipo de habitación creado: " + config.descripcion);
            } else {
                System.out.println("○ Tipo de habitación ya existe: " + config.descripcion);
            }
        }

        System.out.println("=== Tipos de habitación inicializados ===\n");
    }

    /**
     * Inicializa los estados de habitación en la base de datos
     * DEBE ejecutarse ANTES de inicializar las habitaciones
     */
    public void inicializarEstadosHabitacion() {
        System.out.println("=== Inicializando estados de habitación ===");

        String[] estados = {"DISPONIBLE", "RESERVADA", "OCUPADA", "MANTENIMIENTO"};

        for (String nombreEstado : estados) {
            EstadoHabitacion existente = estadoHabitacionDAO.obtenerPorNombre(nombreEstado);
            if (existente == null) {
                EstadoHabitacion estado = EstadoHabitacion.builder()
                        .nombre(nombreEstado)
                        .build();

                estadoHabitacionDAO.guardar(estado);
                System.out.println("✓ Estado creado: " + nombreEstado);
            } else {
                System.out.println("○ Estado ya existe: " + nombreEstado);
            }
        }

        System.out.println("=== Estados de habitación inicializados ===\n");
    }

    /**
     * Inicializa todas las habitaciones en la base de datos
     */
    public void inicializar() {
        System.out.println("=== Iniciando carga de habitaciones ===");

        // Primero inicializar tipos y estados si no existen
        inicializarTiposHabitacion();
        inicializarEstadosHabitacion();

        // Obtener el estado DISPONIBLE para asignarlo a nuevas habitaciones
        EstadoHabitacion estadoDisponible = estadoHabitacionDAO.obtenerPorNombre("DISPONIBLE");
        if (estadoDisponible == null) {
            throw new IllegalStateException("El estado DISPONIBLE no existe en la base de datos");
        }

        // Cargar todos los tipos de habitación desde la BD
        List<TipoHabitacion> tiposDisponibles = tipoHabitacionDAO.listarTodos();
        Map<String, TipoHabitacion> tiposPorNombre = new HashMap<>();
        for (TipoHabitacion tipo : tiposDisponibles) {
            tiposPorNombre.put(tipo.getNombre(), tipo);
        }

        int contador = 1;
        int pisoActual = 1;
        int habitacionesPorPiso = 24;
        int habitacionesEnPisoActual = 0;

        for (Map.Entry<String, Integer> entry : CANTIDAD_POR_TIPO.entrySet()) {
            String nombreTipo = entry.getKey();
            int cantidad = entry.getValue();

            TipoHabitacion tipo = tiposPorNombre.get(nombreTipo);
            if (tipo == null) {
                System.err.println("ERROR: No se encontró el tipo de habitación: " + nombreTipo);
                continue;
            }

            System.out.println("\nCreando " + cantidad + " habitaciones de tipo: " + tipo.getDescripcion());

            for (int i = 0; i < cantidad; i++) {
                // Generar número de habitación (formato: PISO + NÚMERO)
                String numeroHabitacion = String.format("%d%02d", pisoActual, (habitacionesEnPisoActual % habitacionesPorPiso) + 1);

                // Verificar si ya existe
                if (!habitacionDAO.existeNumero(numeroHabitacion)) {
                    Habitacion habitacion = Habitacion.builder()
                            .numero(Integer.valueOf(numeroHabitacion))
                            .tipoHabitacion(tipo)
                            .capacidad(calcularCapacidad(tipo.getNombre()))
                            .descripcion("Habitación " + tipo.getDescripcion())
                            .build();

                    habitacionDAO.guardar(habitacion);

                    // Crear el registro inicial de estado (DISPONIBLE)
                    RegistroEstadoHabitacion registroInicial = RegistroEstadoHabitacion.builder()
                            .habitacion(habitacion)
                            .estadoHabitacion(estadoDisponible)
                            .fechaInicio(LocalDate.now())
                            .fechaFin(null) // null = estado actual
                            .build();

                    registroEstadoHabitacionDAO.persist(registroInicial);

                    System.out.println("✓ Habitación " + numeroHabitacion + " creada - " + tipo.getDescripcion());
                } else {
                    System.out.println("○ Habitación " + numeroHabitacion + " ya existe - omitida");
                }

                contador++;
                habitacionesEnPisoActual++;

                // Cambiar de piso cada 24 habitaciones
                if (habitacionesEnPisoActual >= habitacionesPorPiso) {
                    pisoActual++;
                    habitacionesEnPisoActual = 0;
                }
            }
        }

        System.out.println("\n=== Carga de habitaciones completada ===");
        mostrarResumen();
    }

    /**
     * Calcula la capacidad según el tipo de habitación
     */
    private int calcularCapacidad(String nombreTipo) {
        return switch (nombreTipo) {
            case "INDIVIDUAL_ESTANDAR" -> 1;
            case "DOBLE_ESTANDAR", "DOBLE_SUPERIOR" -> 2;
            case "SUPERIOR_FAMILY_PLAN" -> 4;
            case "SUITE_DOBLE" -> 2;
            default -> 2;
        };
    }

    /**
     * Elimina todas las habitaciones y vuelve a inicializar
     */
    public void reinicializar() {
        System.out.println("Eliminando todas las habitaciones...");

        for (Habitacion habitacion : habitacionDAO.listarTodas()) {
            habitacionDAO.eliminar(habitacion.getNumero());
        }

        System.out.println("Todas las habitaciones eliminadas");
        inicializar();
    }

    /**
     * Muestra un resumen de las habitaciones cargadas
     */
    public void mostrarResumen() {
        System.out.println("\n=== RESUMEN DE HABITACIONES ===");

        List<TipoHabitacion> tipos = tipoHabitacionDAO.listarTodos();
        for (TipoHabitacion tipo : tipos) {
            Long cantidad = habitacionDAO.contarPorTipo(tipo);
            System.out.printf("%-25s: %2d habitaciones - $%.2f/noche%n",
                    tipo.getDescripcion(),
                    cantidad,
                    tipo.getCostoNoche());
        }

        System.out.println("\nTotal de habitaciones: " + habitacionDAO.listarTodas().size());

        // Contar habitaciones disponibles usando el estado actual
        List<Habitacion> disponibles = habitacionDAO.buscarDisponibles();
        System.out.println("Habitaciones disponibles: " + disponibles.size());
    }

    /**
     * Clase auxiliar para configuración de tipos
     */
    private static class TipoHabitacionConfig {
        String descripcion;
        Double costoNoche;

        TipoHabitacionConfig(String descripcion, Double costoNoche) {
            this.descripcion = descripcion;
            this.costoNoche = costoNoche;
        }
    }
}