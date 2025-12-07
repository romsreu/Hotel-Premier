package controllers.ReservarHabitacion;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.CrearReservaDTO;
import ar.utn.hotel.dto.HabitacionReservaDTO;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.gestor.GestorHuesped;
import ar.utn.hotel.gestor.GestorReserva;
import ar.utn.hotel.model.Huesped;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import utils.DataTransfer;
import utils.Validator;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static utils.TextManager.*;

public class ReservarHabitacionController2 {

    @FXML private TextField tfNombre;
    @FXML private TextField tfApellido;
    @FXML private TextField tfTelefono;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;
    @FXML private Button btnConfirmar;

    // Iconos de validación
    @FXML private ImageView nombreO;
    @FXML private ImageView apellidoO;
    @FXML private ImageView telefonoO;

    private Validator validator;
    private GestorHuesped gestorHuesped;
    private GestorReserva gestorReserva;
    private List<HabitacionReservaDTO> habitacionesSeleccionadas;

    @FXML
    public void initialize() {
        // Inicializar gestores
        gestorHuesped = new GestorHuesped();
        gestorReserva = new GestorReserva();

        // Obtener habitaciones seleccionadas
        habitacionesSeleccionadas = DataTransfer.getHabitacionesSeleccionadas();

        if (habitacionesSeleccionadas == null || habitacionesSeleccionadas.isEmpty()) {
            PopUpController.mostrarPopUp(PopUpType.ERROR,
                    "No hay habitaciones seleccionadas");
            HotelPremier.cambiarA("menu");
            return;
        }

        configurarFormatoDeTexto();
        configurarValidaciones();
        ocultarIconosValidacion();
    }

    // ==================== CONFIGURACIÓN INICIAL ====================

    private void configurarFormatoDeTexto() {
        aplicarMayusculas(tfNombre, tfApellido);
        aplicarFiltrosDeEntrada();
        limitarLongitudDeCampos();
    }

    private void limitarLongitudDeCampos() {
        limitarCaracteres(15, tfNombre, tfApellido, tfTelefono);
    }

    private void aplicarFiltrosDeEntrada() {
        soloLetras(tfNombre, tfApellido);
        soloNumeros(tfTelefono);
    }

    private void configurarValidaciones() {
        validator = new Validator();

        // Validar campos obligatorios
        validator.addRule(tfNombre, nombreO).required().minLength(2);
        validator.addRule(tfApellido, apellidoO).required().minLength(2);
        validator.addRule(tfTelefono, telefonoO).required().minLength(7);
    }

    private void ocultarIconosValidacion() {
        if (nombreO != null) nombreO.setVisible(false);
        if (apellidoO != null) apellidoO.setVisible(false);
        if (telefonoO != null) telefonoO.setVisible(false);
    }

    // ==================== EVENTOS DE BOTONES ====================

    @FXML
    public void onCancelarClicked(ActionEvent actionEvent) {
        PopUpController.mostrarPopUpConCallback(
                PopUpType.CONFIRMATION,
                "¿Está seguro de cancelar la reserva?\nSe perderán todos los datos.",
                confirmado -> {
                    if (confirmado) {
                        DataTransfer.limpiar();
                        HotelPremier.cambiarA("menu");
                    }
                }
        );
    }

    @FXML
    public void onVolverClicked(ActionEvent actionEvent) {
        HotelPremier.cambiarA("reservar_hab1");
    }

    @FXML
    public void onConfirmarClicked(ActionEvent actionEvent) {
        // Validar campos
        if (!validator.validateAll()) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Complete los campos obligatorios correctamente:\n\n" +
                            "• Nombre (mínimo 2 caracteres)\n" +
                            "• Apellido (mínimo 2 caracteres)\n" +
                            "• Teléfono (mínimo 7 dígitos)"
            );
            return;
        }

        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        String telefono = tfTelefono.getText().trim();

        // Buscar si el huésped existe
        verificarYConfirmarReserva(nombre, apellido, telefono);
    }

    // ==================== LÓGICA DE VERIFICACIÓN Y RESERVA ====================

    private void verificarYConfirmarReserva(String nombre, String apellido, String telefono) {
        try {
            // Buscar si el huésped existe en BD
            List<Huesped> huespedesEncontrados = gestorHuesped.buscarPorNombreApellido(nombre, apellido);

            // Hacer la variable final para poder usarla en el lambda
            final Huesped huespedExistente;
            if (!huespedesEncontrados.isEmpty()) {
                // Si hay múltiples con el mismo nombre, tomar el primero
                // (en una aplicación real, deberías mostrar una lista para que el usuario elija)
                huespedExistente = huespedesEncontrados.get(0);
            } else {
                huespedExistente = null;
            }

            boolean existe = (huespedExistente != null);

            // Construir mensaje de confirmación
            String mensaje = construirMensajeConfirmacion(nombre, apellido, telefono, existe);

            // Mostrar confirmación
            PopUpController.mostrarPopUpConCallback(
                    PopUpType.CONFIRMATION,
                    mensaje,
                    confirmado -> {
                        if (confirmado) {
                            procesarReserva(nombre, apellido, telefono, huespedExistente);
                        }
                    }
            );

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al verificar el huésped:\n" + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private String construirMensajeConfirmacion(String nombre, String apellido,
                                                String telefono, boolean existe) {
        double costoTotal = calcularCostoTotal();
        int totalNoches = calcularTotalNoches();

        String estadoHuesped = existe ? "✓ HUÉSPED EXISTENTE" : "⚠ NUEVO HUÉSPED (se usará huésped existente o debe registrarse)";

        return String.format(
                "¿Confirmar reserva?\n\n" +
                        "Nombre: %s %s\n" +
                        "Teléfono: %s\n\n" +
                        "RESUMEN DE RESERVA:\n" +
                        "• Habitaciones: %d\n" +
                        "• Total noches: %d\n" +
                        "• Costo total: $%.2f\n\n" +
                        "%s",
                nombre,
                apellido,
                telefono.isEmpty() ? "No especificado" : telefono,
                habitacionesSeleccionadas.size(),
                totalNoches,
                costoTotal,
                estadoHuesped
        );
    }

    private void procesarReserva(String nombre, String apellido, String telefono, Huesped huespedExistente) {
        try {
            // Validar que el huésped exista (ya debe estar registrado en el sistema)
            if (huespedExistente == null) {
                PopUpController.mostrarPopUp(
                        PopUpType.WARNING,
                        "El huésped no está registrado en el sistema.\n\n" +
                                "Debe dar de alta al huésped antes de realizar una reserva.\n" +
                                "Use la opción 'Dar de alta huésped' del menú principal."
                );
                return;
            }

            // Crear lista de DTOs
            List<CrearReservaDTO> dtosReservas = new ArrayList<>();
            for (HabitacionReservaDTO hab : habitacionesSeleccionadas) {
                dtosReservas.add(CrearReservaDTO.builder()
                        .idHuesped(huespedExistente.getId())
                        .numeroHabitacion(hab.getNumeroHabitacion())
                        .fechaInicio(hab.getFechaIngreso())
                        .fechaFin(hab.getFechaEgreso())
                        .cantHuespedes(1)
                        .descuento(0.0)
                        .build());
            }

            // Crear todas las reservas de una vez
            List<ReservaDTO> reservasCreadas = gestorReserva.crearReservasMultiples(dtosReservas);

            // Mostrar confirmación exitosa
            mostrarConfirmacionExitosa(reservasCreadas, nombre, apellido, telefono, true);

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al procesar la reserva:\n" + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    private void mostrarConfirmacionExitosa(List<ReservaDTO> reservasCreadas, String nombre,
                                            String apellido, String telefono, boolean existia) {
        double costoTotal = calcularCostoTotal();
        int totalNoches = calcularTotalNoches();

        String estadoHuesped = existia ? "EXISTENTE" : "NUEVO";

        StringBuilder mensaje = new StringBuilder();
        mensaje.append("✓ RESERVA CONFIRMADA EXITOSAMENTE\n\n");
        mensaje.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        mensaje.append(String.format("Cliente: %s %s (%s)\n", nombre, apellido, estadoHuesped));
        mensaje.append(String.format("Teléfono: %s\n\n",
                telefono.isEmpty() ? "No especificado" : telefono));
        mensaje.append("RESUMEN:\n");
        mensaje.append(String.format("• Reservas creadas: %d\n", reservasCreadas.size()));
        mensaje.append(String.format("• Total noches: %d\n", totalNoches));
        mensaje.append(String.format("• Costo total: $%.2f\n\n", costoTotal));

        // Detalle por habitación
        mensaje.append("DETALLE:\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (HabitacionReservaDTO hab : habitacionesSeleccionadas) {
            mensaje.append(String.format("• Hab %d (%s): %s - %s\n",
                    hab.getNumeroHabitacion(),
                    hab.getTipoHabitacion(),
                    hab.getFechaIngreso().format(formatter),
                    hab.getFechaEgreso().format(formatter)));
        }

        PopUpController.mostrarPopUpConCallback(
                PopUpType.SUCCESS,
                mensaje.toString(),
                confirmado -> {
                    // Limpiar DataTransfer
                    DataTransfer.limpiar();

                    // Mostrar resumen en consola
                    mostrarResumenEnConsola(reservasCreadas, nombre, apellido, telefono, estadoHuesped);

                    // Volver al menú
                    HotelPremier.cambiarA("menu");
                }
        );
    }

    private double calcularCostoTotal() {
        return habitacionesSeleccionadas.stream()
                .mapToDouble(HabitacionReservaDTO::getCostoTotal)
                .sum();
    }

    private int calcularTotalNoches() {
        return habitacionesSeleccionadas.stream()
                .mapToInt(HabitacionReservaDTO::getCantidadNoches)
                .sum();
    }

    private void mostrarResumenEnConsola(List<ReservaDTO> reservasCreadas, String nombre,
                                         String apellido, String telefono, String estadoHuesped) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("          RESERVA CONFIRMADA - RESUMEN");
        System.out.println("=".repeat(60));
        System.out.printf("Cliente: %s %s (%s)%n", nombre, apellido, estadoHuesped);
        System.out.printf("Teléfono: %s%n", telefono.isEmpty() ? "No especificado" : telefono);
        System.out.println("-".repeat(60));
        System.out.printf("Total de reservas creadas: %d%n", reservasCreadas.size());
        System.out.printf("Total de noches: %d%n", calcularTotalNoches());
        System.out.printf("Costo total: $%.2f%n", calcularCostoTotal());
        System.out.println("-".repeat(60));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (int i = 0; i < habitacionesSeleccionadas.size() && i < reservasCreadas.size(); i++) {
            HabitacionReservaDTO hab = habitacionesSeleccionadas.get(i);
            ReservaDTO reserva = reservasCreadas.get(i);

            System.out.printf("\nReserva ID: %d%n", reserva.getId());
            System.out.printf("  Habitación: %d (%s)%n",
                    hab.getNumeroHabitacion(),
                    hab.getTipoHabitacion());
            System.out.printf("  Check-in:  %s%n", hab.getFechaIngreso().format(formatter));
            System.out.printf("  Check-out: %s%n", hab.getFechaEgreso().format(formatter));
            System.out.printf("  Noches:    %d%n", hab.getCantidadNoches());
            System.out.printf("  Costo:     $%.2f%n", hab.getCostoTotal());
        }

        System.out.println("=".repeat(60) + "\n");
    }
}