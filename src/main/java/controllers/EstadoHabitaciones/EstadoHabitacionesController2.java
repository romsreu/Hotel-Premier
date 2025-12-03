package controllers.EstadoHabitaciones;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dao.impl.EstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dao.impl.RegistroEstadoHabitacionDAOImpl;
import ar.utn.hotel.dao.impl.TipoHabitacionDAOImpl;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.model.EstadoHabitacion;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.RegistroEstadoHabitacion;
import ar.utn.hotel.model.TipoHabitacion;
import enums.ContextoEstadoHabitaciones;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import utils.DataTransfer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class EstadoHabitacionesController2 {

    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    @FXML private Button btnConfirmar;
    @FXML private TabPane tabPane;
    @FXML private Label lbFechaDesde;
    @FXML private Label lbFechaHasta;
    @FXML private Label lblTitulo;

    // Referencias a los GridPanes de cada tab
    private GridPane gridTodasHabitaciones;
    private GridPane gridIndividualEstandar;
    private GridPane gridDobleEstandar;
    private GridPane gridDobleSuperior;
    private GridPane gridSuperiorFamily;
    private GridPane gridSuiteDoble;

    // Referencias a los ScrollPanes
    private ScrollPane scrollTodasHabitaciones;
    private ScrollPane scrollIndividualEstandar;
    private ScrollPane scrollDobleEstandar;
    private ScrollPane scrollDobleSuperior;
    private ScrollPane scrollSuperiorFamily;
    private ScrollPane scrollSuiteDoble;

    // Configuración de visualización
    private static final double ANCHO_CELDA = 140.0;
    private static final double ALTO_CELDA = 70.0;
    private static final int COLUMNAS_VISIBLES = 6;
    private static final int FILAS_VISIBLES = 6;

    // DAOs
    private HabitacionDAOImpl habitacionDAO;
    private TipoHabitacionDAOImpl tipoHabitacionDAO;
    private EstadoHabitacionDAOImpl estadoHabitacionDAO;
    private RegistroEstadoHabitacionDAOImpl registroEstadoHabitacionDAO;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private ContextoEstadoHabitaciones contexto;
    private List<Habitacion> todasLasHabitaciones;
    private Map<String, TipoHabitacion> tiposPorNombre;

    // Mapa para cachear estados de habitaciones por fecha
    private Map<Integer, Map<LocalDate, EstadoHabitacion>> estadosPorHabitacion;

    // Variable para optimización (evita buscar "DISPONIBLE" miles de veces)
    private EstadoHabitacion estadoDisponibleDefault;

    // Para selección múltiple
    private Set<CeldaSeleccionada> celdasSeleccionadas;

    @FXML
    public void initialize() {
        habitacionDAO = new HabitacionDAOImpl();
        tipoHabitacionDAO = new TipoHabitacionDAOImpl();
        estadoHabitacionDAO = new EstadoHabitacionDAOImpl();
        registroEstadoHabitacionDAO = new RegistroEstadoHabitacionDAOImpl();
        celdasSeleccionadas = new HashSet<>();
        estadosPorHabitacion = new HashMap<>();

        // Obtener datos del DataTransfer
        fechaInicio = DataTransfer.getFechaDesdeEstadoHabitaciones();
        fechaFin = DataTransfer.getFechaHastaEstadoHabitaciones();
        contexto = DataTransfer.getContextoEstadoHabitaciones();

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (fechaInicio != null) lbFechaDesde.setText(fechaInicio.format(formato));
        if (fechaFin != null) lbFechaHasta.setText(fechaFin.format(formato));

        // Validar que se recibieron los datos
        if (fechaInicio == null || fechaFin == null || contexto == null) {
            mostrarError("Error: No se recibieron los datos correctamente");
            return;
        }

        // Cargar estado por defecto para optimización
        try {
            estadoDisponibleDefault = estadoHabitacionDAO.obtenerPorNombre("DISPONIBLE");
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo cargar el estado DISPONIBLE por defecto.");
        }

        configurarSegunContexto();
        inicializarTabs();
        cargarDatosReales();
    }

    private void configurarSegunContexto() {
        switch (contexto) {
            case MOSTRAR:
                if (lblTitulo != null) {
                    lblTitulo.setText("Estado de Habitaciones");
                }
                btnConfirmar.setVisible(false);
                btnConfirmar.setManaged(false);
                break;

            case RESERVAR:
                if (lblTitulo != null) {
                    lblTitulo.setText("Seleccionar Habitaciones para Reservar");
                }
                btnConfirmar.setText("Confirmar Reserva");
                btnConfirmar.setVisible(true);
                btnConfirmar.setManaged(true);
                break;

            case OCUPAR:
                if (lblTitulo != null) {
                    lblTitulo.setText("Seleccionar Habitación para Ocupar");
                }
                btnConfirmar.setText("Confirmar Ocupación");
                btnConfirmar.setVisible(true);
                btnConfirmar.setManaged(true);
                break;
        }
    }

    private void inicializarTabs() {
        // Obtener los tabs
        Tab tabTodas = tabPane.getTabs().get(0);
        Tab tabIndividualEstandar = tabPane.getTabs().get(1);
        Tab tabDobleEstandar = tabPane.getTabs().get(2);
        Tab tabDobleSuperior = tabPane.getTabs().get(3);
        Tab tabSuperiorFamily = tabPane.getTabs().get(4);
        Tab tabSuiteDoble = tabPane.getTabs().get(5);

        // Crear ScrollPanes y GridPanes
        scrollTodasHabitaciones = crearScrollPane();
        gridTodasHabitaciones = crearGridPane();
        scrollTodasHabitaciones.setContent(gridTodasHabitaciones);
        tabTodas.setContent(scrollTodasHabitaciones);

        scrollIndividualEstandar = crearScrollPane();
        gridIndividualEstandar = crearGridPane();
        scrollIndividualEstandar.setContent(gridIndividualEstandar);
        tabIndividualEstandar.setContent(scrollIndividualEstandar);

        scrollDobleEstandar = crearScrollPane();
        gridDobleEstandar = crearGridPane();
        scrollDobleEstandar.setContent(gridDobleEstandar);
        tabDobleEstandar.setContent(scrollDobleEstandar);

        scrollDobleSuperior = crearScrollPane();
        gridDobleSuperior = crearGridPane();
        scrollDobleSuperior.setContent(gridDobleSuperior);
        tabDobleSuperior.setContent(scrollDobleSuperior);

        scrollSuperiorFamily = crearScrollPane();
        gridSuperiorFamily = crearGridPane();
        scrollSuperiorFamily.setContent(gridSuperiorFamily);
        tabSuperiorFamily.setContent(scrollSuperiorFamily);

        scrollSuiteDoble = crearScrollPane();
        gridSuiteDoble = crearGridPane();
        scrollSuiteDoble.setContent(gridSuiteDoble);
        tabSuiteDoble.setContent(scrollSuiteDoble);
    }

    private ScrollPane crearScrollPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefViewportWidth(ANCHO_CELDA * COLUMNAS_VISIBLES);
        scroll.setPrefViewportHeight(ALTO_CELDA * FILAS_VISIBLES);
        configurarScrollDiscreto(scroll);
        return scroll;
    }

    private void configurarScrollDiscreto(ScrollPane scroll) {
        scroll.hvalueProperty().addListener((obs, oldVal, newVal) -> {
            double contentWidth = scroll.getContent().getBoundsInLocal().getWidth();
            double viewportWidth = scroll.getViewportBounds().getWidth();
            double maxScroll = contentWidth - viewportWidth;
            if (maxScroll > 0) {
                double pixelPosition = newVal.doubleValue() * maxScroll;
                double snappedPosition = Math.round(pixelPosition / ANCHO_CELDA) * ANCHO_CELDA;
                double snappedValue = snappedPosition / maxScroll;
                if (Math.abs(newVal.doubleValue() - snappedValue) > 0.001) {
                    scroll.setHvalue(snappedValue);
                }
            }
        });
        scroll.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            double contentHeight = scroll.getContent().getBoundsInLocal().getHeight();
            double viewportHeight = scroll.getViewportBounds().getHeight();
            double maxScroll = contentHeight - viewportHeight;
            if (maxScroll > 0) {
                double pixelPosition = newVal.doubleValue() * maxScroll;
                double snappedPosition = Math.round(pixelPosition / ALTO_CELDA) * ALTO_CELDA;
                double snappedValue = snappedPosition / maxScroll;
                if (Math.abs(newVal.doubleValue() - snappedValue) > 0.001) {
                    scroll.setVvalue(snappedValue);
                }
            }
        });
    }

    private GridPane crearGridPane() {
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(true);
        grid.setSnapToPixel(true);
        return grid;
    }

    private void cargarDatosReales() {
        // Cargar todas las habitaciones desde la BD
        todasLasHabitaciones = habitacionDAO.listarTodas();

        if (todasLasHabitaciones.isEmpty()) {
            mostrarError("No hay habitaciones registradas en el sistema");
            return;
        }

        // Cargar tipos de habitación
        List<TipoHabitacion> todosLosTipos = tipoHabitacionDAO.listarTodos();
        tiposPorNombre = todosLosTipos.stream()
                .collect(Collectors.toMap(
                        TipoHabitacion::getNombre,
                        tipo -> tipo
                ));

        // Pre-cargar estados de forma optimizada
        cargarEstadosHabitaciones();

        // Cargar grillas
        cargarGrilla(gridTodasHabitaciones, fechaInicio, fechaFin, todasLasHabitaciones);
        cargarGrillaPorTipo(gridIndividualEstandar, tiposPorNombre.get("INDIVIDUAL_ESTANDAR"));
        cargarGrillaPorTipo(gridDobleEstandar, tiposPorNombre.get("DOBLE_ESTANDAR"));
        cargarGrillaPorTipo(gridDobleSuperior, tiposPorNombre.get("DOBLE_SUPERIOR"));
        cargarGrillaPorTipo(gridSuperiorFamily, tiposPorNombre.get("SUPERIOR_FAMILY_PLAN"));
        cargarGrillaPorTipo(gridSuiteDoble, tiposPorNombre.get("SUITE_DOBLE"));
    }

    /**
     * VERSIÓN OPTIMIZADA: Carga todos los estados en 1 sola consulta SQL.
     */
    private void cargarEstadosHabitaciones() {
        System.out.println(">> Iniciando carga masiva optimizada...");
        long inicio = System.currentTimeMillis();

        // 1. Traer TODOS los registros del rango en UNA sola consulta
        List<RegistroEstadoHabitacion> registrosRango = registroEstadoHabitacionDAO.buscarPorRangoFechas(fechaInicio, fechaFin);

        // 2. Organizar los registros en un Mapa para búsqueda instantánea en memoria
        Map<Integer, List<RegistroEstadoHabitacion>> mapaRegistros = registrosRango.stream()
                .collect(Collectors.groupingBy(r -> r.getHabitacion().getNumero()));

        // 3. Procesar en memoria (RAM)
        for (Habitacion habitacion : todasLasHabitaciones) {
            Map<LocalDate, EstadoHabitacion> estadosPorFecha = new HashMap<>();

            // Obtenemos solo los registros de esta habitación (o lista vacía si no hay)
            List<RegistroEstadoHabitacion> registrosDeEstaHab = mapaRegistros.getOrDefault(habitacion.getNumero(), Collections.emptyList());

            LocalDate fechaActual = fechaInicio;
            while (!fechaActual.isAfter(fechaFin)) {

                EstadoHabitacion estadoFinal = estadoDisponibleDefault; // Por defecto verde

                // Buscamos en la lista pequeña de memoria si hay un estado para hoy
                for (RegistroEstadoHabitacion registro : registrosDeEstaHab) {
                    boolean aplicaFecha = !fechaActual.isBefore(registro.getFechaInicio()) &&
                            (registro.getFechaFin() == null || !fechaActual.isAfter(registro.getFechaFin()));

                    if (aplicaFecha) {
                        estadoFinal = registro.getEstadoHabitacion();
                        break; // Encontramos estado, dejamos de buscar
                    }
                }

                estadosPorFecha.put(fechaActual, estadoFinal);
                fechaActual = fechaActual.plusDays(1);
            }

            estadosPorHabitacion.put(habitacion.getNumero(), estadosPorFecha);
        }

        long fin = System.currentTimeMillis();
        System.out.println(">> Carga finalizada en " + (fin - inicio) + " ms.");
    }

    private void cargarGrillaPorTipo(GridPane grid, TipoHabitacion tipo) {
        if (tipo == null) {
            Label lblError = new Label("Tipo no encontrado.");
            grid.add(lblError, 0, 0);
            return;
        }

        List<Habitacion> habitacionesTipo = todasLasHabitaciones.stream()
                .filter(h -> h.getTipoHabitacion().equals(tipo))
                .collect(Collectors.toList());

        cargarGrilla(grid, fechaInicio, fechaFin, habitacionesTipo);
    }

    private void cargarGrilla(GridPane grid, LocalDate inicio, LocalDate fin,
                              List<Habitacion> habitaciones) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        if (habitaciones.isEmpty()) {
            Label lblVacio = new Label("No hay habitaciones de este tipo");
            lblVacio.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            grid.add(lblVacio, 0, 0);
            return;
        }

        long cantidadDias = ChronoUnit.DAYS.between(inicio, fin) + 1;
        int numFilas = (int) cantidadDias + 1;
        int numColumnas = habitaciones.size() + 1;

        for (int i = 0; i < numColumnas; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(ANCHO_CELDA);
            col.setPrefWidth(ANCHO_CELDA);
            col.setMaxWidth(ANCHO_CELDA);
            col.setHgrow(Priority.NEVER);
            grid.getColumnConstraints().add(col);
        }

        for (int i = 0; i < numFilas; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(ALTO_CELDA);
            row.setPrefHeight(ALTO_CELDA);
            row.setMaxHeight(ALTO_CELDA);
            row.setVgrow(Priority.NEVER);
            grid.getRowConstraints().add(row);
        }

        StackPane esquina = crearCeldaEncabezado("Fecha");
        grid.add(esquina, 0, 0);

        for (int col = 0; col < habitaciones.size(); col++) {
            Habitacion hab = habitaciones.get(col);
            String texto = hab.getTipoHabitacion().getDescripcion() + " " + hab.getNumero();
            StackPane header = crearCeldaEncabezado(texto);
            grid.add(header, col + 1, 0);
        }

        LocalDate fechaActual = inicio;
        for (int fila = 0; fila < cantidadDias; fila++) {
            StackPane lblFecha = crearCeldaEncabezado(
                    fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            grid.add(lblFecha, 0, fila + 1);

            for (int col = 0; col < habitaciones.size(); col++) {
                Habitacion hab = habitaciones.get(col);
                // OBTENER ESTADO DEL MAPA EN MEMORIA (¡Rápido!)
                EstadoHabitacion estado = estadosPorHabitacion
                        .get(hab.getNumero())
                        .get(fechaActual);

                StackPane celda = crearCeldaEstado(fechaActual, hab, estado);
                grid.add(celda, col + 1, fila + 1);
            }
            fechaActual = fechaActual.plusDays(1);
        }
    }

    private StackPane crearCeldaEncabezado(String texto) {
        StackPane celda = new StackPane();
        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        celda.setStyle("-fx-background-color: #2d3748; -fx-border-color: #1a202c; -fx-border-width: 1;");
        Label label = new Label(texto);
        label.setStyle("-fx-text-fill: #f7fafc; -fx-font-weight: bold; -fx-font-size: 12px;");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setMaxWidth(ANCHO_CELDA - 10);
        celda.getChildren().add(label);
        return celda;
    }

    private StackPane crearCeldaEstado(LocalDate fecha, Habitacion habitacion, EstadoHabitacion estado) {
        StackPane celda = new StackPane();
        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        String colorFondo;
        String nombreEstado = estado != null ? estado.getNombre() : "DISPONIBLE";

        switch (nombreEstado) {
            case "DISPONIBLE": colorFondo = "#48bb78"; break;
            case "RESERVADA": colorFondo = "#f56565"; break;
            case "MANTENIMIENTO": colorFondo = "#4299e1"; break;
            case "OCUPADA": colorFondo = "#ed8936"; break;
            default: colorFondo = "#a0aec0";
        }

        celda.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 1;",
                colorFondo
        ));

        Label label = new Label(estado != null ? estado.getDescripcion() : "Disponible");
        label.setStyle("-fx-text-fill: #1a202c; -fx-font-weight: bold; -fx-font-size: 13px;");
        label.setAlignment(Pos.CENTER);
        celda.getChildren().add(label);

        if (contexto != ContextoEstadoHabitaciones.MOSTRAR) {
            if ("DISPONIBLE".equals(nombreEstado)) {
                celda.setCursor(Cursor.HAND);
                celda.setOnMouseClicked(e -> handleCeldaClickSeleccion(celda, fecha, habitacion, estado));
                celda.setOnMouseEntered(e -> celda.setOpacity(0.8));
                celda.setOnMouseExited(e -> celda.setOpacity(1.0));
            }
        } else {
            celda.setCursor(Cursor.HAND);
            celda.setOnMouseClicked(e -> handleCeldaClickInfo(fecha, habitacion, estado));
            celda.setOnMouseEntered(e -> celda.setOpacity(0.8));
            celda.setOnMouseExited(e -> celda.setOpacity(1.0));
        }
        return celda;
    }

    private void handleCeldaClickInfo(LocalDate fecha, Habitacion habitacion, EstadoHabitacion estado) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información de Habitación");
        alert.setHeaderText(habitacion.getTipoHabitacion().getDescripcion() + " " + habitacion.getNumero());
        alert.setContentText(String.format(
                "Fecha: %s\nEstado: %s\nPrecio por noche: $%.2f",
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                estado.getDescripcion(),
                habitacion.getTipoHabitacion().getCostoNoche()
        ));
        alert.showAndWait();
    }

    private void handleCeldaClickSeleccion(StackPane celda, LocalDate fecha,
                                           Habitacion habitacion, EstadoHabitacion estado) {
        CeldaSeleccionada celdaSel = new CeldaSeleccionada(fecha, habitacion.getNumero());

        if (celdasSeleccionadas.contains(celdaSel)) {
            celdasSeleccionadas.remove(celdaSel);
            celda.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 1;",
                    "#48bb78"
            ));
        } else {
            if (contexto == ContextoEstadoHabitaciones.OCUPAR && !celdasSeleccionadas.isEmpty()) {
                mostrarAdvertencia("Solo puede seleccionar una habitación para ocupar");
                return;
            }
            celdasSeleccionadas.add(celdaSel);
            celda.setStyle(String.format(
                    "-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 3;",
                    "#38a169"
            ));
        }
        System.out.println("Celdas seleccionadas: " + celdasSeleccionadas.size());
    }

    @FXML private void onCancelarClicked() {
        DataTransfer.limpiar();
        HotelPremier.cambiarA("menu");
    }

    @FXML private void onVovlerClicked() {
        HotelPremier.cambiarA("estado_habs1");
    }

    @FXML private void onConfirmarClicked() {
        if (celdasSeleccionadas.isEmpty()) {
            mostrarAdvertencia("Debe seleccionar al menos una habitación");
            return;
        }
        switch (contexto) {
            case RESERVAR: confirmarReserva(); break;
            case OCUPAR: confirmarOcupacion(); break;
            default: break;
        }
    }

    private void confirmarReserva() {
        if (celdasSeleccionadas.isEmpty()) {
            mostrarAdvertencia("Debe seleccionar al menos una habitación.");
            return;
        }

        LocalDate fechaMin = celdasSeleccionadas.stream()
                .map(c -> c.fecha).min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate fechaMax = celdasSeleccionadas.stream()
                .map(c -> c.fecha).max(LocalDate::compareTo).orElse(LocalDate.now());

        Set<Integer> habitacionesSet = celdasSeleccionadas.stream()
                .map(c -> c.numeroHabitacion)
                .collect(Collectors.toSet());

        ReservaDTO dtoParcial = ReservaDTO.builder()
                .fechaInicio(fechaMin)
                .fechaFin(fechaMax)
                .numerosHabitaciones(habitacionesSet)
                .build();

        DataTransfer.setReservaPendiente(dtoParcial);
        DataTransfer.setContextoEstadoHabitaciones(contexto);
        HotelPremier.cambiarA("ocupar_hab1");
    }

    private void confirmarOcupacion() {
        CeldaSeleccionada celda = celdasSeleccionadas.iterator().next();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Ocupación");
        alert.setHeaderText("¿Desea confirmar la ocupación?");
        alert.setContentText(String.format("Habitación %d\nFecha: %s",
                celda.numeroHabitacion,
                celda.fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Lógica de ocupación aquí
                mostrarExito("Ocupación confirmada (Stub)");
                HotelPremier.cambiarA("menu");
            }
        });
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private static class CeldaSeleccionada {
        LocalDate fecha;
        Integer numeroHabitacion;

        CeldaSeleccionada(LocalDate fecha, Integer numeroHabitacion) {
            this.fecha = fecha;
            this.numeroHabitacion = numeroHabitacion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CeldaSeleccionada that = (CeldaSeleccionada) o;
            return Objects.equals(fecha, that.fecha) &&
                    Objects.equals(numeroHabitacion, that.numeroHabitacion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fecha, numeroHabitacion);
        }
    }
}