package controllers.estado_habitaciones;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Cursor;
import javafx.beans.value.ChangeListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EstadoHabitacionesController2 {

    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    @FXML private Button btnConfirmar;
    @FXML private TabPane tabPane;

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

    // Datos de prueba
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Map<String, Integer> cantidadHabitacionesPorTipo;

    @FXML
    public void initialize() {
        // Datos de prueba
        fechaInicio = LocalDate.of(2024, 11, 1);
        fechaFin = LocalDate.of(2024, 11, 30); // 30 días

        cantidadHabitacionesPorTipo = new LinkedHashMap<>(); // LinkedHashMap para mantener orden
        cantidadHabitacionesPorTipo.put("Individual Estándar", 10);
        cantidadHabitacionesPorTipo.put("Doble Estándar", 18);
        cantidadHabitacionesPorTipo.put("Doble Superior", 8);
        cantidadHabitacionesPorTipo.put("Superior Family Plan", 10);
        cantidadHabitacionesPorTipo.put("Suite Doble", 2);

        inicializarTabs();
        cargarDatosPrueba();
    }

    private void inicializarTabs() {
        // Obtener los tabs
        Tab tabTodas = tabPane.getTabs().get(0);
        Tab tabIndividualEstandar = tabPane.getTabs().get(1);
        Tab tabDobleEstandar = tabPane.getTabs().get(2);
        Tab tabDobleSuperior = tabPane.getTabs().get(3);
        Tab tabSuperiorFamily = tabPane.getTabs().get(4);
        Tab tabSuiteDoble = tabPane.getTabs().get(5);

        // Crear ScrollPanes y GridPanes para cada tab
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

        // Tamaño del viewport (área visible)
        scroll.setPrefViewportWidth(ANCHO_CELDA * COLUMNAS_VISIBLES);
        scroll.setPrefViewportHeight(ALTO_CELDA * FILAS_VISIBLES);

        // Configurar scroll discreto (snap)
        configurarScrollDiscreto(scroll);

        return scroll;
    }

    private void configurarScrollDiscreto(ScrollPane scroll) {
        // Listener para scroll horizontal discreto
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

        // Listener para scroll vertical discreto
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
        grid.setSnapToPixel(true); // Importante para alineación perfecta
        return grid;
    }

    private void cargarDatosPrueba() {
        // Cargar "Todas las Habitaciones" con todas las 48 habitaciones
        List<String> todasHabitaciones = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cantidadHabitacionesPorTipo.entrySet()) {
            String tipo = entry.getKey();
            int cantidad = entry.getValue();
            for (int i = 1; i <= cantidad; i++) {
                todasHabitaciones.add(tipo + " " + i);
            }
        }
        cargarGrilla(gridTodasHabitaciones, fechaInicio, fechaFin, todasHabitaciones);

        // Cargar cada tab específico
        cargarGrillaPorTipo(gridIndividualEstandar, fechaInicio, fechaFin, "Individual Estándar", 10);
        cargarGrillaPorTipo(gridDobleEstandar, fechaInicio, fechaFin, "Doble Estándar", 18);
        cargarGrillaPorTipo(gridDobleSuperior, fechaInicio, fechaFin, "Doble Superior", 8);
        cargarGrillaPorTipo(gridSuperiorFamily, fechaInicio, fechaFin, "Superior Family Plan", 10);
        cargarGrillaPorTipo(gridSuiteDoble, fechaInicio, fechaFin, "Suite Doble", 2);
    }

    private void cargarGrillaPorTipo(GridPane grid, LocalDate inicio, LocalDate fin,
                                     String tipoHabitacion, int cantidad) {
        List<String> habitaciones = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            habitaciones.add(tipoHabitacion + " " + i);
        }
        cargarGrilla(grid, inicio, fin, habitaciones);
    }

    private void cargarGrilla(GridPane grid, LocalDate inicio, LocalDate fin,
                              List<String> habitaciones) {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        long cantidadDias = ChronoUnit.DAYS.between(inicio, fin) + 1;
        int numFilas = (int) cantidadDias + 1; // +1 para encabezado
        int numColumnas = habitaciones.size() + 1; // +1 para columna de fechas

        // Configurar constraints de columnas
        for (int i = 0; i < numColumnas; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(ANCHO_CELDA);
            col.setPrefWidth(ANCHO_CELDA);
            col.setMaxWidth(ANCHO_CELDA);
            col.setHgrow(Priority.NEVER);
            grid.getColumnConstraints().add(col);
        }

        // Configurar constraints de filas
        for (int i = 0; i < numFilas; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(ALTO_CELDA);
            row.setPrefHeight(ALTO_CELDA);
            row.setMaxHeight(ALTO_CELDA);
            row.setVgrow(Priority.NEVER);
            grid.getRowConstraints().add(row);
        }

        // Celda vacía superior izquierda
        StackPane esquina = crearCeldaEncabezado("Fecha");
        grid.add(esquina, 0, 0);

        // Encabezados de columnas (habitaciones)
        for (int col = 0; col < habitaciones.size(); col++) {
            StackPane header = crearCeldaEncabezado(habitaciones.get(col));
            grid.add(header, col + 1, 0);
        }

        // Contenido de la grilla
        LocalDate fechaActual = inicio;
        Random random = new Random();

        for (int fila = 0; fila < cantidadDias; fila++) {
            // Encabezado de fila (fecha)
            StackPane lblFecha = crearCeldaEncabezado(
                    fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            grid.add(lblFecha, 0, fila + 1);

            // Celdas de datos - Estados aleatorios para prueba
            for (int col = 0; col < habitaciones.size(); col++) {
                EstadoHabitacion estado = generarEstadoAleatorio(random);
                StackPane celda = crearCeldaEstado(fechaActual, habitaciones.get(col), estado);
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

    private StackPane crearCeldaEstado(LocalDate fecha, String habitacion, EstadoHabitacion estado) {
        StackPane celda = new StackPane();
        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Color según estado
        String colorFondo = switch (estado) {
            case DISPONIBLE -> "#48bb78"; // Verde
            case RESERVADA -> "#f56565";  // Rojo
            case MANTENIMIENTO -> "#4299e1"; // Azul
            case OCUPADA -> "#ed8936"; // Naranja
        };

        celda.setStyle(String.format(
                "-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 1;",
                colorFondo
        ));

        Label label = new Label(estado.toString());
        label.setStyle("-fx-text-fill: #1a202c; -fx-font-weight: bold; -fx-font-size: 13px;");
        label.setAlignment(Pos.CENTER);

        celda.getChildren().add(label);

        // Agregar interactividad
        celda.setCursor(Cursor.HAND);
        celda.setOnMouseClicked(e -> handleCeldaClick(fecha, habitacion, estado));
        celda.setOnMouseEntered(e -> celda.setOpacity(0.8));
        celda.setOnMouseExited(e -> celda.setOpacity(1.0));

        return celda;
    }

    private EstadoHabitacion generarEstadoAleatorio(Random random) {
        int valor = random.nextInt(100);
        if (valor < 50) return EstadoHabitacion.DISPONIBLE;
        if (valor < 80) return EstadoHabitacion.RESERVADA;
        if (valor < 90) return EstadoHabitacion.OCUPADA;
        return EstadoHabitacion.MANTENIMIENTO;
    }

    private void handleCeldaClick(LocalDate fecha, String habitacion, EstadoHabitacion estado) {
        System.out.println(String.format(
                "Click en: %s - %s - Estado: %s",
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                habitacion,
                estado
        ));

        // Aquí puedes abrir un diálogo, cambiar estado, etc.
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información de Habitación");
        alert.setHeaderText(habitacion);
        alert.setContentText(String.format(
                "Fecha: %s\nEstado: %s",
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                estado
        ));
        alert.showAndWait();
    }

    @FXML
    private void onCancelarClicked() {
        System.out.println("Cancelar clicked");
        // Limpiar y cerrar
    }

    @FXML
    private void onVovlerClicked() {
        System.out.println("Volver clicked");
        // Volver a la pantalla anterior
    }

    @FXML
    private void onConfirmarClicked() {
        System.out.println("Confirmar clicked");
        // Guardar cambios
    }

    // Enum para estados
    enum EstadoHabitacion {
        DISPONIBLE("Disponible"),
        RESERVADA("Reservada"),
        OCUPADA("Ocupada"),
        MANTENIMIENTO("Mantenimiento");

        private final String texto;

        EstadoHabitacion(String texto) {
            this.texto = texto;
        }

        @Override
        public String toString() {
            return texto;
        }
    }
}