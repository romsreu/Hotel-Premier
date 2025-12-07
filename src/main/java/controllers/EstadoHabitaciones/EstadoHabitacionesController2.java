package controllers.EstadoHabitaciones;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.HabitacionReservaDTO;
import ar.utn.hotel.gestor.GestorHabitacion;
import ar.utn.hotel.gestor.GestorReserva;
import ar.utn.hotel.model.Habitacion;
import ar.utn.hotel.model.Reserva;
import controllers.PopUp.PopUpController;
import enums.ContextoEstadoHabitaciones;
import enums.EstadoHab;
import enums.PopUpType;
import enums.TipoHabitacion;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Cursor;
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
    private Map<String, EstadoHab> cacheEstados;

    private GridPane gridTodasHabitaciones, gridIndividualEstandar, gridDobleEstandar,
            gridDobleSuperior, gridSuperiorFamily, gridSuiteDoble;
    private ScrollPane scrollTodasHabitaciones, scrollIndividualEstandar, scrollDobleEstandar,
            scrollDobleSuperior, scrollSuperiorFamily, scrollSuiteDoble;

    private static final double ANCHO_CELDA = 140.0;
    private static final double ALTO_CELDA = 70.0;
    private static final int COLUMNAS_VISIBLES = 6;
    private static final int FILAS_VISIBLES = 6;

    private GestorHabitacion gestorHabitacion;
    private GestorReserva gestorReserva;
    private LocalDate fechaInicio, fechaFin;
    private ContextoEstadoHabitaciones contexto;
    private List<Habitacion> todasLasHabitaciones;
    private Set<CeldaSeleccionada> celdasSeleccionadas;
    private CeldaSeleccionada ultimaCeldaClickeada;
    private Map<String, StackPane> mapaCeldas;

    @FXML
    public void initialize() {
        mapaCeldas = new HashMap<>();
        celdasSeleccionadas = new HashSet<>();
        cacheEstados = new HashMap<>();
        gestorHabitacion = new GestorHabitacion();
        gestorReserva = new GestorReserva();
        gestorReserva.setGestorHabitacion(gestorHabitacion);
        gestorHabitacion.setGestorReserva(gestorReserva);

        fechaInicio = DataTransfer.getFechaDesdeEstadoHabitaciones();
        fechaFin = DataTransfer.getFechaHastaEstadoHabitaciones();
        contexto = DataTransfer.getContextoEstadoHabitaciones();

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lbFechaDesde.setText(fechaInicio.format(formato));
        lbFechaHasta.setText(fechaFin.format(formato));

        if (fechaInicio == null || fechaFin == null || contexto == null) {
            mostrarError("Error: No se recibieron los datos correctamente");
            return;
        }

        configurarSegunContexto();
        inicializarTabs();
        cargarDatosReales();
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) ->
                refrescarEstilosSeleccionados());
    }

    private void configurarSegunContexto() {
        switch (contexto) {
            case MOSTRAR:
                if (lblTitulo != null) lblTitulo.setText("Estado de Habitaciones");
                btnConfirmar.setVisible(false);
                btnConfirmar.setManaged(false);
                break;
            case RESERVAR:
                if (lblTitulo != null) lblTitulo.setText("Seleccionar Habitaciones para Reservar");
                btnConfirmar.setText("Confirmar Reserva");
                btnConfirmar.setVisible(true);
                btnConfirmar.setManaged(true);
                break;
            case OCUPAR:
                if (lblTitulo != null) lblTitulo.setText("Seleccionar Habitación para Ocupar");
                btnConfirmar.setText("Confirmar Ocupación");
                btnConfirmar.setVisible(true);
                btnConfirmar.setManaged(true);
                break;
        }
    }

    private void inicializarTabs() {
        Tab[] tabs = tabPane.getTabs().toArray(new Tab[0]);
        scrollTodasHabitaciones = crearScrollYGrid(tabs[0], gridTodasHabitaciones = crearGridPane());
        scrollIndividualEstandar = crearScrollYGrid(tabs[1], gridIndividualEstandar = crearGridPane());
        scrollDobleEstandar = crearScrollYGrid(tabs[2], gridDobleEstandar = crearGridPane());
        scrollDobleSuperior = crearScrollYGrid(tabs[3], gridDobleSuperior = crearGridPane());
        scrollSuperiorFamily = crearScrollYGrid(tabs[4], gridSuperiorFamily = crearGridPane());
        scrollSuiteDoble = crearScrollYGrid(tabs[5], gridSuiteDoble = crearGridPane());
    }

    private ScrollPane crearScrollYGrid(Tab tab, GridPane grid) {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(false);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefViewportWidth(ANCHO_CELDA * COLUMNAS_VISIBLES);
        scroll.setPrefViewportHeight(ALTO_CELDA * FILAS_VISIBLES);
        configurarScrollDiscreto(scroll);
        scroll.setContent(grid);
        tab.setContent(scroll);
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
        todasLasHabitaciones = gestorHabitacion.listarTodasHabitaciones();
        if (todasLasHabitaciones.isEmpty()) {
            mostrarError("No hay habitaciones registradas en el sistema");
            return;
        }

        List<Integer> numerosHab = todasLasHabitaciones.stream()
                .map(Habitacion::getNumero)
                .collect(Collectors.toList());

        cacheEstados = gestorHabitacion.obtenerEstadosEnRango(numerosHab, fechaInicio, fechaFin);

        // Ahora cargar las grillas (ya no hacen consultas individuales)
        cargarGrilla(gridTodasHabitaciones, fechaInicio, fechaFin, todasLasHabitaciones);
        cargarGrillaPorTipo(gridIndividualEstandar, TipoHabitacion.INDIVIDUAL_ESTANDAR);
        cargarGrillaPorTipo(gridDobleEstandar, TipoHabitacion.DOBLE_ESTANDAR);
        cargarGrillaPorTipo(gridDobleSuperior, TipoHabitacion.DOBLE_SUPERIOR);
        cargarGrillaPorTipo(gridSuperiorFamily, TipoHabitacion.SUPERIOR_FAMILY_PLAN);
        cargarGrillaPorTipo(gridSuiteDoble, TipoHabitacion.SUITE_DOBLE);
    }


    private void cargarGrillaPorTipo(GridPane grid, TipoHabitacion tipo) {
        // Obtener el nombre del enum y compararlo
        String nombreTipo = tipo.name().replace("_", " "); // Ej: "DOBLE_ESTANDAR" → "DOBLE ESTANDAR"

        List<Habitacion> habitacionesTipo = todasLasHabitaciones.stream()
                .filter(h -> h.getTipo() != null &&
                        h.getTipo().getNombre().toUpperCase().contains(nombreTipo))
                .collect(Collectors.toList());

        cargarGrilla(grid, fechaInicio, fechaFin, habitacionesTipo);
    }

    private void cargarGrilla(GridPane grid, LocalDate inicio, LocalDate fin, List<Habitacion> habitaciones) {
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

        grid.add(crearCeldaEncabezado("Fecha"), 0, 0);

        for (int col = 0; col < habitaciones.size(); col++) {
            Habitacion hab = habitaciones.get(col);
            String texto = hab.getTipo().getNombre() + " #" + hab.getNumero();
            grid.add(crearCeldaEncabezado(texto), col + 1, 0);
        }

        LocalDate fechaActual = inicio;
        for (int fila = 0; fila < cantidadDias; fila++) {
            grid.add(crearCeldaEncabezado(fechaActual.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 0, fila + 1);

            for (int col = 0; col < habitaciones.size(); col++) {
                Habitacion hab = habitaciones.get(col);
                EstadoHab estado = obtenerEstadoHabitacion(hab, fechaActual);
                StackPane celda = crearCeldaEstado(fechaActual, hab, estado);
                grid.add(celda, col + 1, fila + 1);

                String clave = generarClaveCelda(fechaActual, hab.getNumero());
                mapaCeldas.put(clave, celda);

                CeldaSeleccionada celdaSel = new CeldaSeleccionada(fechaActual, hab.getNumero(), estado);
                if (celdasSeleccionadas.contains(celdaSel)) {
                    aplicarEstiloSeleccionado(celda, estado);
                }
            }
            fechaActual = fechaActual.plusDays(1);
        }
    }

    /**
     * Obtiene el estado de una habitación en una fecha específica
     * usando el nuevo modelo EstadoHabitacion -> TipoEstado
     */
    private EstadoHab obtenerEstadoHabitacion(Habitacion habitacion, LocalDate fecha) {
        String clave = habitacion.getNumero() + "_" + fecha;
        return cacheEstados.getOrDefault(clave, EstadoHab.DISPONIBLE);
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

    private StackPane crearCeldaEstado(LocalDate fecha, Habitacion habitacion, EstadoHab estado) {
        StackPane celda = new StackPane();
        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        String colorFondo = switch (estado) {
            case DISPONIBLE -> "#48bb78";
            case RESERVADA -> "#f56565";
            case MANTENIMIENTO -> "#4299e1";
            case OCUPADA -> "#ed8936";
        };

        celda.setStyle(String.format("-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 1;", colorFondo));

        Label label = new Label(estado.name());
        label.setStyle("-fx-text-fill: #1a202c; -fx-font-weight: bold; -fx-font-size: 13px;");
        label.setAlignment(Pos.CENTER);
        celda.getChildren().add(label);

        if (contexto != ContextoEstadoHabitaciones.MOSTRAR) {
            boolean esSeleccionable = (contexto == ContextoEstadoHabitaciones.RESERVAR && estado == EstadoHab.DISPONIBLE) ||
                    (contexto == ContextoEstadoHabitaciones.OCUPAR && (estado == EstadoHab.DISPONIBLE || estado == EstadoHab.RESERVADA));

            if (esSeleccionable) {
                celda.setCursor(Cursor.HAND);
                celda.setOnMouseClicked(e -> handleCeldaClickSeleccion(celda, fecha, habitacion, estado, e));
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

    private void handleCeldaClickInfo(LocalDate fecha, Habitacion habitacion, EstadoHab estado) {
        String mensaje = String.format("%s #%d\n\nFecha: %s\nEstado: %s\nCapacidad: %d personas\nCosto por noche: $%.2f",
                habitacion.getTipo().getDescripcion(), habitacion.getNumero(),
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), estado.name(),
                habitacion.getTipo().getCapacidad(), habitacion.getTipo().getCostoNoche());
        mostrarInfo(mensaje);
    }

    private void handleCeldaClickSeleccion(StackPane celda, LocalDate fecha, Habitacion habitacion,
                                           EstadoHab estado, javafx.scene.input.MouseEvent event) {
        CeldaSeleccionada celdaActual = new CeldaSeleccionada(fecha, habitacion.getNumero(), estado);
        if (event.isShiftDown() && ultimaCeldaClickeada != null) {
            seleccionarRango(ultimaCeldaClickeada, celdaActual);
        } else {
            toggleSeleccion(celda, celdaActual);
            ultimaCeldaClickeada = celdaActual;
        }
    }

    @FXML private void onCancelarClicked() { DataTransfer.limpiar(); HotelPremier.cambiarA("menu"); }
    @FXML private void onVovlerClicked() { HotelPremier.cambiarA("estado_habs1"); }

    @FXML
    private void onConfirmarClicked() {
        if (celdasSeleccionadas.isEmpty()) {
            mostrarAdvertencia("Debe seleccionar al menos una habitación");
            return;
        }
        if (contexto == ContextoEstadoHabitaciones.RESERVAR) confirmarReserva();
        else if (contexto == ContextoEstadoHabitaciones.OCUPAR) confirmarOcupacion();
    }

    private void confirmarReserva() {
        Map<Integer, List<LocalDate>> habitacionPorFechas = new HashMap<>();
        for (CeldaSeleccionada celda : celdasSeleccionadas) {
            habitacionPorFechas.computeIfAbsent(celda.numeroHabitacion, k -> new ArrayList<>()).add(celda.fecha);
        }

        List<HabitacionReservaDTO> habitacionesDTO = new ArrayList<>();
        for (Map.Entry<Integer, List<LocalDate>> entry : habitacionPorFechas.entrySet()) {
            Integer numeroHab = entry.getKey();
            List<LocalDate> fechas = entry.getValue();
            fechas.sort(LocalDate::compareTo);
            LocalDate fechaIngreso = fechas.get(0);
            LocalDate fechaEgreso = fechas.get(fechas.size() - 1);

            Habitacion habitacion = todasLasHabitaciones.stream()
                    .filter(h -> h.getNumero().equals(numeroHab)).findFirst().orElse(null);

            if (habitacion != null) {
                long cantidadNoches = ChronoUnit.DAYS.between(fechaIngreso, fechaEgreso) + 1;
                double costoNoche = habitacion.getTipo().getCostoNoche();
                double costoTotal = cantidadNoches * costoNoche;

                habitacionesDTO.add(HabitacionReservaDTO.builder()
                        .numeroHabitacion(numeroHab)
                        .tipoHabitacion(habitacion.getTipo().getDescripcion())
                        .costoNoche(costoNoche)
                        .piso(habitacion.getPiso())
                        .capacidad(habitacion.getTipo().getCapacidad())
                        .fechaIngreso(fechaIngreso)
                        .fechaEgreso(fechaEgreso)
                        .cantidadNoches((int) cantidadNoches)
                        .costoTotal(costoTotal)
                        .build());
            }
        }

        habitacionesDTO.sort(Comparator.comparing(HabitacionReservaDTO::getNumeroHabitacion));
        DataTransfer.setHabitacionesSeleccionadas(habitacionesDTO);
        HotelPremier.cambiarA("reservar_hab1");
    }

    private void confirmarOcupacion() {
        List<CeldaSeleccionada> celdasReservadas = celdasSeleccionadas.stream()
                .filter(c -> c.estado == EstadoHab.RESERVADA).collect(Collectors.toList());

        if (!celdasReservadas.isEmpty()) {
            mostrarAdvertenciaReservasExistentes(celdasReservadas);
        } else {
            prepararYProcesarOcupacion();
        }
    }

    private void mostrarAdvertenciaReservasExistentes(List<CeldaSeleccionada> celdasReservadas) {
        StringBuilder mensaje = new StringBuilder("⚠ ADVERTENCIA: Habitaciones con reserva activa\n\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Map<Integer, List<CeldaSeleccionada>> porHabitacion = celdasReservadas.stream()
                .collect(Collectors.groupingBy(c -> c.numeroHabitacion));

        for (Map.Entry<Integer, List<CeldaSeleccionada>> entry : porHabitacion.entrySet()) {
            Integer numHab = entry.getKey();
            List<CeldaSeleccionada> celdas = entry.getValue();
            Reserva reserva = buscarReservaPorHabitacionYFecha(numHab, celdas.get(0).fecha);

            mensaje.append(String.format("🏨 HABITACIÓN %d\n", numHab));
            if (reserva != null) {
                // ✅ CAMBIO AQUÍ: .getPersona() → .getHuesped()
                mensaje.append(String.format("   Cliente: %s %s\n",
                        reserva.getHuesped().getNombre(),
                        reserva.getHuesped().getApellido()));
                mensaje.append(String.format("   Reserva: %s - %s\n",
                        reserva.getFechaInicio().format(formatter),
                        reserva.getFechaFin().format(formatter)));
            } else {
                mensaje.append("   (Reserva no encontrada en sistema)\n");
            }
            mensaje.append("\n");
        }

        mensaje.append("¿Desea ocupar las habitaciones de todos modos?");

        PopUpController.mostrarPopUpConCallback(PopUpType.CONFIRMATION, mensaje.toString(), confirmado -> {
            if (confirmado) prepararYProcesarOcupacion();
        });
    }

    private Reserva buscarReservaPorHabitacionYFecha(Integer numeroHabitacion, LocalDate fecha) {
        return gestorReserva.buscarReservaPorHabitacionYFecha(numeroHabitacion, fecha);
    }

    private void prepararYProcesarOcupacion() {
        Map<Integer, List<LocalDate>> habitacionPorFechas = new HashMap<>();
        for (CeldaSeleccionada celda : celdasSeleccionadas) {
            habitacionPorFechas.computeIfAbsent(celda.numeroHabitacion, k -> new ArrayList<>()).add(celda.fecha);
        }

        List<HabitacionReservaDTO> habitacionesDTO = new ArrayList<>();
        for (Map.Entry<Integer, List<LocalDate>> entry : habitacionPorFechas.entrySet()) {
            Integer numeroHab = entry.getKey();
            List<LocalDate> fechas = entry.getValue();
            fechas.sort(LocalDate::compareTo);
            LocalDate fechaIngreso = fechas.get(0);
            LocalDate fechaEgreso = fechas.get(fechas.size() - 1);
            Habitacion habitacion = buscarHabitacionPorNumero(numeroHab);

            if (habitacion != null) {
                long cantidadNoches = ChronoUnit.DAYS.between(fechaIngreso, fechaEgreso) + 1;
                double costoNoche = habitacion.getTipo().getCostoNoche();
                double costoTotal = cantidadNoches * costoNoche;

                habitacionesDTO.add(HabitacionReservaDTO.builder()
                        .numeroHabitacion(numeroHab)
                        .tipoHabitacion(habitacion.getTipo().getDescripcion())
                        .costoNoche(costoNoche)
                        .piso(habitacion.getPiso())
                        .capacidad(habitacion.getTipo().getCapacidad())
                        .fechaIngreso(fechaIngreso)
                        .fechaEgreso(fechaEgreso)
                        .cantidadNoches((int) cantidadNoches)
                        .costoTotal(costoTotal)
                        .build());
            }
        }

        habitacionesDTO.sort(Comparator.comparing(HabitacionReservaDTO::getNumeroHabitacion));
        DataTransfer.setHabitacionesSeleccionadas(habitacionesDTO);
        HotelPremier.cambiarA("ocupar_hab1");
    }

    private Habitacion buscarHabitacionPorNumero(Integer numero) {
        return todasLasHabitaciones.stream().filter(h -> h.getNumero().equals(numero)).findFirst().orElse(null);
    }

    private void refrescarEstilosSeleccionados() {
        for (CeldaSeleccionada celdaSel : celdasSeleccionadas) {
            String clave = generarClaveCelda(celdaSel.fecha, celdaSel.numeroHabitacion);
            StackPane celda = mapaCeldas.get(clave);
            if (celda != null) aplicarEstiloSeleccionado(celda, celdaSel.estado);
        }
    }

    private void mostrarError(String mensaje) { PopUpController.mostrarPopUp(PopUpType.ERROR, mensaje); }
    private void mostrarAdvertencia(String mensaje) { PopUpController.mostrarPopUp(PopUpType.WARNING, mensaje); }
    private void mostrarInfo(String mensaje) { PopUpController.mostrarPopUp(PopUpType.INFO, mensaje); }

    private static class CeldaSeleccionada {
        LocalDate fecha;
        Integer numeroHabitacion;
        EstadoHab estado;

        CeldaSeleccionada(LocalDate fecha, Integer numeroHabitacion, EstadoHab estado) {
            this.fecha = fecha;
            this.numeroHabitacion = numeroHabitacion;
            this.estado = estado;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CeldaSeleccionada that = (CeldaSeleccionada) o;
            return Objects.equals(fecha, that.fecha) && Objects.equals(numeroHabitacion, that.numeroHabitacion);
        }

        @Override
        public int hashCode() { return Objects.hash(fecha, numeroHabitacion); }
    }

    private String generarClaveCelda(LocalDate fecha, Integer numeroHab) { return fecha.toString() + "_" + numeroHab; }

    private void toggleSeleccion(StackPane celda, CeldaSeleccionada celdaSel) {
        if (celdasSeleccionadas.contains(celdaSel)) {
            celdasSeleccionadas.remove(celdaSel);
            aplicarEstiloDeseleccionado(celda, celdaSel.estado);
        } else {
            celdasSeleccionadas.add(celdaSel);
            aplicarEstiloSeleccionado(celda, celdaSel.estado);
        }
    }

    private void seleccionarRango(CeldaSeleccionada desde, CeldaSeleccionada hasta) {
        if (desde.numeroHabitacion.equals(hasta.numeroHabitacion)) {
            seleccionarRangoVertical(desde, hasta);
        } else if (desde.fecha.equals(hasta.fecha)) {
            seleccionarRangoHorizontal(desde, hasta);
        } else {
            String clave = generarClaveCelda(hasta.fecha, hasta.numeroHabitacion);
            StackPane celda = mapaCeldas.get(clave);
            if (celda != null) toggleSeleccion(celda, hasta);
        }
    }

    private void seleccionarRangoVertical(CeldaSeleccionada desde, CeldaSeleccionada hasta) {
        LocalDate fechaInicial = desde.fecha.isBefore(hasta.fecha) ? desde.fecha : hasta.fecha;
        LocalDate fechaFinal = desde.fecha.isAfter(hasta.fecha) ? desde.fecha : hasta.fecha;
        Integer numeroHab = desde.numeroHabitacion;

        LocalDate fechaActual = fechaInicial;
        while (!fechaActual.isAfter(fechaFinal)) {
            String clave = generarClaveCelda(fechaActual, numeroHab);
            StackPane celda = mapaCeldas.get(clave);
            if (celda != null) {
                Habitacion hab = buscarHabitacion(numeroHab);
                if (hab != null) {
                    EstadoHab estado = obtenerEstadoHabitacion(hab, fechaActual);
                    boolean esSeleccionable = (contexto == ContextoEstadoHabitaciones.RESERVAR && estado == EstadoHab.DISPONIBLE) ||
                            (contexto == ContextoEstadoHabitaciones.OCUPAR && (estado == EstadoHab.DISPONIBLE || estado == EstadoHab.RESERVADA));
                    if (esSeleccionable) {
                        CeldaSeleccionada celdaSel = new CeldaSeleccionada(fechaActual, numeroHab, estado);
                        if (!celdasSeleccionadas.contains(celdaSel)) {
                            celdasSeleccionadas.add(celdaSel);
                            aplicarEstiloSeleccionado(celda, estado);
                        }
                    }
                }
            }
            fechaActual = fechaActual.plusDays(1);
        }
    }

    private void seleccionarRangoHorizontal(CeldaSeleccionada desde, CeldaSeleccionada hasta) {
        Integer habInicial = Math.min(desde.numeroHabitacion, hasta.numeroHabitacion);
        Integer habFinal = Math.max(desde.numeroHabitacion, hasta.numeroHabitacion);
        LocalDate fecha = desde.fecha;

        for (Integer numHab = habInicial; numHab <= habFinal; numHab++) {
            String clave = generarClaveCelda(fecha, numHab);
            StackPane celda = mapaCeldas.get(clave);
            if (celda != null) {
                Habitacion hab = buscarHabitacion(numHab);
                if (hab != null) {
                    EstadoHab estado = obtenerEstadoHabitacion(hab, fecha);
                    boolean esSeleccionable = (contexto == ContextoEstadoHabitaciones.RESERVAR && estado == EstadoHab.DISPONIBLE) ||
                            (contexto == ContextoEstadoHabitaciones.OCUPAR && (estado == EstadoHab.DISPONIBLE || estado == EstadoHab.RESERVADA));
                    if (esSeleccionable) {
                        CeldaSeleccionada celdaSel = new CeldaSeleccionada(fecha, numHab, estado);
                        if (!celdasSeleccionadas.contains(celdaSel)) {
                            celdasSeleccionadas.add(celdaSel);
                            aplicarEstiloSeleccionado(celda, estado);
                        }
                    }
                }
            }
        }
    }

    private Habitacion buscarHabitacion(Integer numeroHab) {
        return todasLasHabitaciones.stream().filter(h -> h.getNumero().equals(numeroHab)).findFirst().orElse(null);
    }

    private void aplicarEstiloSeleccionado(StackPane celda, EstadoHab estado) {
        String colorSeleccion = switch (estado) {
            case DISPONIBLE -> "#38a169";
            case RESERVADA -> "#c53030";
            default -> "#38a169";
        };
        celda.setStyle(String.format("-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 3;", colorSeleccion));
    }

    private void aplicarEstiloDeseleccionado(StackPane celda, EstadoHab estado) {
        String colorFondo = switch (estado) {
            case DISPONIBLE -> "#48bb78";
            case RESERVADA -> "#f56565";
            case MANTENIMIENTO -> "#4299e1";
            case OCUPADA -> "#ed8936";
        };
        celda.setStyle(String.format("-fx-background-color: %s; -fx-border-color: #2d3748; -fx-border-width: 1;", colorFondo));
    }
}