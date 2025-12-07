package ar.utn.hotel.services;

import ar.utn.hotel.dto.CrearReservaDTO;
import ar.utn.hotel.dto.DarAltaHuespedDTO;
import ar.utn.hotel.dto.HabitacionDTO;
import ar.utn.hotel.gestor.GestorHabitacion;
import ar.utn.hotel.gestor.GestorHuesped;
import ar.utn.hotel.gestor.GestorReserva;
import ar.utn.hotel.model.Huesped;
import enums.EstadoHab;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Inicializador de datos del sistema de hotel
 * Utiliza ÚNICAMENTE los gestores para todas las operaciones
 */
public class InicializadorDatos {

    private final GestorHabitacion gestorHabitacion;
    private final GestorHuesped gestorHuesped;
    private final GestorReserva gestorReserva;

    // Lista para guardar IDs de huéspedes creados
    private final List<Long> idsHuespedes = new ArrayList<>();

    // Configuración de tipos de habitación
    private static final List<TipoHabitacionConfig> TIPOS_CONFIG = Arrays.asList(
            new TipoHabitacionConfig("Individual Estándar",
                    "Habitación individual con cama simple", 1, 80.0, 10),
            new TipoHabitacionConfig("Doble Estándar",
                    "Habitación doble con dos camas individuales o una matrimonial", 2, 120.0, 18),
            new TipoHabitacionConfig("Doble Superior",
                    "Habitación doble amplia con amenities premium", 2, 150.0, 8),
            new TipoHabitacionConfig("Superior Family Plan",
                    "Habitación familiar con espacio adicional", 4, 200.0, 10),
            new TipoHabitacionConfig("Suite Doble",
                    "Suite de lujo con sala de estar separada", 2, 300.0, 2)
    );

    public InicializadorDatos() {
        // Inicializar solo gestores
        this.gestorHabitacion = new GestorHabitacion();
        this.gestorHuesped = new GestorHuesped();
        this.gestorReserva = new GestorReserva();

        // Establecer referencias circulares
        this.gestorReserva.setGestorHabitacion(gestorHabitacion);
        this.gestorHabitacion.setGestorReserva(gestorReserva);
    }

    /**
     * Inicializa todos los datos del sistema
     */
    public void inicializar() {
        System.out.println("=== Iniciando carga de datos ===\n");

        try {
            // 1. Crear tipos de estado usando gestor
            inicializarCatalogoEstados();

            // 2. Crear tipos de habitación usando gestor
            inicializarTiposHabitacion();

            // 3. Crear habitaciones usando gestor
            inicializarHabitaciones();

            // 4. Crear huéspedes usando gestor
            crearHuespedes();

            // 5. Crear algunas reservas usando gestor
            crearReservas();

            // 6. Crear algunas estadías (check-in) usando gestor
            crearEstadias();

            System.out.println("\n=== Carga de datos completada exitosamente ===");
            mostrarResumen();

        } catch (Exception e) {
            System.err.println("Error durante la inicialización: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicializa el catálogo de estados usando GestorHabitacion
     */
    private void inicializarCatalogoEstados() {
        System.out.println("--- Inicializando catálogo de estados ---");

        for (EstadoHab estadoEnum : EstadoHab.values()) {
            if (!gestorHabitacion.existeTipoEstado(estadoEnum)) {
                gestorHabitacion.crearTipoEstado(estadoEnum);
                System.out.println("✓ Estado creado: " + estadoEnum.name());
            } else {
                System.out.println("○ Estado ya existe: " + estadoEnum.name());
            }
        }
    }

    /**
     * Inicializa los tipos de habitación usando GestorHabitacion
     */
    private void inicializarTiposHabitacion() {
        System.out.println("\n--- Inicializando tipos de habitación ---");

        for (TipoHabitacionConfig config : TIPOS_CONFIG) {
            if (!gestorHabitacion.existeTipoHabitacion(config.nombre)) {
                gestorHabitacion.crearTipoHabitacion(
                        config.nombre,
                        config.descripcion,
                        config.capacidad,
                        config.costoNoche
                );
                System.out.println("✓ Tipo creado: " + config.nombre + " - $" + config.costoNoche + "/noche");
            } else {
                System.out.println("○ Tipo ya existe: " + config.nombre);
            }
        }
    }

    /**
     * Inicializa las habitaciones usando GestorHabitacion
     */
    private void inicializarHabitaciones() {
        System.out.println("\n--- Inicializando habitaciones ---");

        int pisoActual = 1;
        int habitacionesPorPiso = 24;
        int habitacionesEnPisoActual = 0;

        for (TipoHabitacionConfig config : TIPOS_CONFIG) {
            System.out.println("\nCreando " + config.cantidad + " habitaciones de tipo: " + config.nombre);

            for (int i = 0; i < config.cantidad; i++) {
                // Generar número de habitación (formato: PISO + NÚMERO)
                Integer numeroHabitacion = Integer.valueOf(
                        String.format("%d%02d", pisoActual, (habitacionesEnPisoActual % habitacionesPorPiso) + 1)
                );

                try {
                    // Intentar obtener la habitación para ver si existe
                    gestorHabitacion.obtenerHabitacion(numeroHabitacion);
                    System.out.println("○ Habitación " + numeroHabitacion + " ya existe - omitida");
                } catch (IllegalArgumentException e) {
                    // No existe, crearla
                    HabitacionDTO dto = HabitacionDTO.builder()
                            .numero(numeroHabitacion)
                            .tipo(config.nombre)
                            .piso(pisoActual)
                            .build();

                    gestorHabitacion.crearHabitacion(dto);
                    System.out.println("✓ Habitación " + numeroHabitacion + " creada - " + config.nombre);
                }

                habitacionesEnPisoActual++;

                // Cambiar de piso cada 24 habitaciones
                if (habitacionesEnPisoActual >= habitacionesPorPiso) {
                    pisoActual++;
                    habitacionesEnPisoActual = 0;
                }
            }
        }
    }

    /**
     * Crea huéspedes de ejemplo usando GestorHuesped
     */
    private void crearHuespedes() {
        System.out.println("\n--- Creando huéspedes ---");

        // Huésped 1
        DarAltaHuespedDTO huesped1 = DarAltaHuespedDTO.builder()
                .nombre("JUAN CARLOS")
                .apellido("PÉREZ")
                .tipoDocumento("DNI")
                .numeroDocumento("42.567.890")
                .telefono("3511234567")
                .email("JUAN.PEREZ@EMAIL.COM")
                .cuit("20-42567890-3")
                .posicionIVA("CONSUMIDOR FINAL")
                .fechaNacimiento("15/03/1990")
                .ocupacion("INGENIERO")
                .nacionalidad("ARGENTINA")
                .calle("AV. COLÓN")
                .numero("1234")
                .piso("5")
                .depto("B")
                .localidad("CÓRDOBA")
                .provincia("CÓRDOBA")
                .pais("ARGENTINA")
                .codPostal("5000")
                .build();

        // Huésped 2
        DarAltaHuespedDTO huesped2 = DarAltaHuespedDTO.builder()
                .nombre("MARÍA LAURA")
                .apellido("GONZÁLEZ")
                .tipoDocumento("DNI")
                .numeroDocumento("38.123.456")
                .telefono("3512345678")
                .email("MARIA.GONZALEZ@EMAIL.COM")
                .cuit("27-38123456-8")
                .posicionIVA("MONOTRIBUTO")
                .fechaNacimiento("22/07/1985")
                .ocupacion("ARQUITECTA")
                .nacionalidad("ARGENTINA")
                .calle("BV. SAN JUAN")
                .numero("567")
                .piso("2")
                .depto("A")
                .localidad("CÓRDOBA")
                .provincia("CÓRDOBA")
                .pais("ARGENTINA")
                .codPostal("5000")
                .build();

        // Huésped 3
        DarAltaHuespedDTO huesped3 = DarAltaHuespedDTO.builder()
                .nombre("CARLOS ALBERTO")
                .apellido("RODRÍGUEZ")
                .tipoDocumento("DNI")
                .numeroDocumento("35.987.654")
                .telefono("3513456789")
                .email("CARLOS.RODRIGUEZ@EMAIL.COM")
                .cuit("20-35987654-1")
                .posicionIVA("RESP. INSCRIPTO")
                .fechaNacimiento("10/11/1982")
                .ocupacion("MÉDICO")
                .nacionalidad("ARGENTINA")
                .calle("AV. VÉLEZ SARSFIELD")
                .numero("890")
                .localidad("CÓRDOBA")
                .provincia("CÓRDOBA")
                .pais("ARGENTINA")
                .codPostal("5000")
                .build();

        List<DarAltaHuespedDTO> huespedes = Arrays.asList(huesped1, huesped2, huesped3);

        for (DarAltaHuespedDTO dto : huespedes) {
            try {
                Huesped huesped = gestorHuesped.cargar(dto);
                idsHuespedes.add(huesped.getId());
                System.out.println("✓ Huésped creado: " + huesped.getNombre() + " " +
                        huesped.getApellido() + " (DNI: " + huesped.getNumeroDocumento() +
                        ", ID: " + huesped.getId() + ")");
            } catch (IllegalArgumentException e) {
                // Ya existe
                List<Huesped> existentes = gestorHuesped.buscarPorDocumento(
                        dto.getNumeroDocumento(), dto.getTipoDocumento());
                if (!existentes.isEmpty()) {
                    idsHuespedes.add(existentes.get(0).getId());
                    System.out.println("○ Huésped ya existe: " + dto.getNombre() + " " +
                            dto.getApellido() + " (DNI: " + dto.getNumeroDocumento() + ")");
                }
            } catch (Exception e) {
                System.err.println("✗ Error al crear huésped " + dto.getNombre() +
                        " " + dto.getApellido() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Crea reservas de ejemplo usando GestorReserva
     * Reservas en el rango de 1 semana hacia adelante
     */
    /**
     * Crea reservas de ejemplo usando GestorReserva
     * Reservas en el rango de 1 semana hacia adelante
     */
    private void crearReservas() {
        System.out.println("\n--- Creando reservas ---");

        if (idsHuespedes.isEmpty()) {
            System.out.println("⚠ No hay huéspedes disponibles para crear reservas");
            return;
        }

        LocalDate hoy = LocalDate.now();

        try {
            // Reserva 1: Huésped 1, Habitación 101, mañana por 3 días
            crearReservaEjemplo(idsHuespedes.get(0), 101,
                    hoy.plusDays(1), hoy.plusDays(4), 1);

            // Reserva 2: Huésped 2, Habitación 102, pasado mañana por 2 días
            crearReservaEjemplo(idsHuespedes.get(1 % idsHuespedes.size()), 102,
                    hoy.plusDays(2), hoy.plusDays(4), 2);

            // Reserva 3: Huésped 3, Habitación 201, en 3 días por 2 días
            crearReservaEjemplo(idsHuespedes.get(2 % idsHuespedes.size()), 201,
                    hoy.plusDays(3), hoy.plusDays(5), 2);

            // Reserva 4: Huésped 1, Habitación 203, en 5 días por 3 días
            crearReservaEjemplo(idsHuespedes.get(0), 203,
                    hoy.plusDays(5), hoy.plusDays(8), 4);

            // Reserva 5: Huésped 2, Habitación 204, en 6 días por 2 días
            crearReservaEjemplo(idsHuespedes.get(1 % idsHuespedes.size()), 204,
                    hoy.plusDays(6), hoy.plusDays(8), 2);

        } catch (Exception e) {
            System.err.println("Error al crear reservas: " + e.getMessage());
        }
    }

    /**
     * Crea estadías de ejemplo (check-in) usando GestorHabitacion
     */
    private void crearEstadias() {
        System.out.println("\n--- Creando estadías (check-in) ---");

        if (idsHuespedes.isEmpty()) {
            System.out.println("⚠ No hay huéspedes disponibles para crear estadías");
            return;
        }

        LocalDate hoy = LocalDate.now();

        try {
            // Estadía 1: Huésped 3, Habitación 103, check-in hoy (salida en 3 días)
            crearReservaYCheckIn(
                    idsHuespedes.get(2 % idsHuespedes.size()),
                    103,
                    hoy,
                    hoy.plusDays(3),
                    1
            );

            // Estadía 2: Huésped 1, Habitación 202, check-in hoy (salida en 2 días)
            crearReservaYCheckIn(
                    idsHuespedes.get(0),
                    202,
                    hoy,
                    hoy.plusDays(2),
                    2
            );

        } catch (Exception e) {
            System.err.println("Error al crear estadías: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crea una reserva de ejemplo
     */
    private void crearReservaEjemplo(Long idHuesped, Integer numeroHab,
                                     LocalDate inicio, LocalDate fin, Integer cantHuespedes) {
        try {
            CrearReservaDTO dto = CrearReservaDTO.builder()
                    .idHuesped(idHuesped)
                    .numeroHabitacion(numeroHab)
                    .fechaInicio(inicio)
                    .fechaFin(fin)
                    .cantHuespedes(cantHuespedes)
                    .build();

            gestorReserva.crearReserva(dto);
            System.out.println("✓ Reserva creada: Habitación " + numeroHab +
                    " del " + inicio + " al " + fin);
        } catch (Exception e) {
            System.err.println("✗ Error al crear reserva para habitación " + numeroHab +
                    ": " + e.getMessage());
        }
    }

    /**
     * Crea una reserva y hace check-in inmediatamente
     */
    private Long crearReservaYCheckIn(Long idHuesped, Integer numeroHab,
                                      LocalDate inicio, LocalDate fin, Integer cantHuespedes) {
        try {
            // Crear reserva
            CrearReservaDTO dto = CrearReservaDTO.builder()
                    .idHuesped(idHuesped)
                    .numeroHabitacion(numeroHab)
                    .fechaInicio(inicio)
                    .fechaFin(fin)
                    .cantHuespedes(cantHuespedes)
                    .build();

            var reservaDTO = gestorReserva.crearReserva(dto);

            // Hacer check-in
            gestorHabitacion.realizarCheckIn(reservaDTO.getId());

            System.out.println("✓ Estadía creada: Habitación " + numeroHab +
                    " (Check-in realizado del " + inicio + " al " + fin + ")");

            return reservaDTO.getId();
        } catch (Exception e) {
            System.err.println("✗ Error al crear estadía para habitación " + numeroHab +
                    ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Muestra un resumen de los datos creados
     */
    private void mostrarResumen() {
        System.out.println("\n=== RESUMEN DEL SISTEMA ===");

        System.out.println("\n--- Habitaciones por Tipo ---");
        var tipos = gestorHabitacion.listarTiposHabitacion();
        int totalHabitaciones = 0;
        for (var tipo : tipos) {
            int cantidad = gestorHabitacion.obtenerHabitacionesPorTipo(tipo).size();
            totalHabitaciones += cantidad;
            System.out.printf("%-30s: %2d habitaciones - $%.2f/noche (Cap: %d)%n",
                    tipo.getDescripcion(),
                    cantidad,
                    tipo.getCostoNoche(),
                    tipo.getCapacidad());
        }

        System.out.println("\n--- Estadísticas Generales ---");
        System.out.println("Total tipos de habitación: " + tipos.size());
        System.out.println("Total de habitaciones: " + totalHabitaciones);
        System.out.println("Total de huéspedes: " + gestorHuesped.obtenerTodos().size());
        System.out.println("Total de reservas: " + gestorReserva.listarReservas().size());
        System.out.println("Total de estadías activas: " + gestorHabitacion.listarEstadiasActivas().size());

        System.out.println("\n--- Habitaciones por Estado ---");
        for (EstadoHab estado : EstadoHab.values()) {
            int count = gestorHabitacion.obtenerHabitacionesPorEstado(estado).size();
            System.out.println("  " + estado.name() + ": " + count);
        }
    }

    /**
     * Clase auxiliar para configuración de tipos de habitación
     */
    private static class TipoHabitacionConfig {
        String nombre;
        String descripcion;
        Integer capacidad;
        Double costoNoche;
        int cantidad;

        TipoHabitacionConfig(String nombre, String descripcion,
                             Integer capacidad, Double costoNoche, int cantidad) {
            this.nombre = nombre;
            this.descripcion = descripcion;
            this.capacidad = capacidad;
            this.costoNoche = costoNoche;
            this.cantidad = cantidad;
        }
    }

    /**
     * Método main para ejecutar la inicialización
     */
    public static void main(String[] args) {
        System.out.println("🏨 HOTEL PREMIER - Inicializador de Datos");
        System.out.println("==========================================\n");

        InicializadorDatos inicializador = new InicializadorDatos();
        inicializador.inicializar();

        System.out.println("\n==========================================");
        System.out.println("✅ Sistema listo para usar");
        System.out.println("==========================================");
    }
}