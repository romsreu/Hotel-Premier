package controllers.OcuparHabitacion;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dao.impl.HabitacionDAOImpl;
import ar.utn.hotel.dao.impl.PersonaDAOImpl;
import ar.utn.hotel.dao.impl.ReservaDAOImpl;
import ar.utn.hotel.dto.ReservaDTO;
import ar.utn.hotel.gestor.GestorReserva;
import ar.utn.hotel.model.Persona;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DataTransfer;

public class OcuparHabitacionController1 {

    // Referencias a los componentes de tu FXML
    @FXML private TextField tfNombre;
    @FXML private TextField tfApellido;
    @FXML private TextField tfNumeroDocumento;
    @FXML private ComboBox<String> cbTipoDocumento;
    @FXML private RadioButton rbHuesped;
    @FXML private RadioButton rbAcompanante;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancelar;

    // Lógica de negocio
    private GestorReserva gestorReserva;
    private PersonaDAOImpl personaDAO;

    @FXML
    public void initialize() {
        // 1. Inicializar las herramientas de base de datos
        personaDAO = new PersonaDAOImpl();
        gestorReserva = new GestorReserva(
                new ReservaDAOImpl(personaDAO, new HabitacionDAOImpl()),
                personaDAO
        );

        // 2. Llenar el combo de documentos
        if (cbTipoDocumento != null) {
            cbTipoDocumento.getItems().addAll("DNI", "PASAPORTE", "LC", "LE");
            cbTipoDocumento.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onAceptarClicked() {
        System.out.println(">> Botón ACEPTAR presionado.");

        // 1. Obtener datos de los campos de texto
        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        // String dni = tfNumeroDocumento.getText().trim(); // Úsalo si quieres buscar por DNI

        // 2. Validar que no estén vacíos
        if (nombre.isEmpty() || apellido.isEmpty()) {
            mostrarError("Debe escribir Nombre y Apellido.");
            return;
        }

        try {
            // 3. Recuperar el 'paquete' de datos que viene de la pantalla anterior
            ReservaDTO dto = DataTransfer.getReservaPendiente();

            if (dto == null) {
                mostrarError("Error: Se perdieron los datos de las habitaciones seleccionadas.");
                HotelPremier.cambiarA("menu");
                return;
            }

            // 4. Buscar a la persona en la Base de Datos
            System.out.println("Buscando persona: " + nombre + " " + apellido);
            Persona persona = personaDAO.buscarPorNombreApellido(nombre, apellido);

            if (persona == null) {
                mostrarError("La persona no existe en el sistema. Debe ir a 'Alta Huésped' primero.");
                return;
            }

            // 5. Completar los datos que faltaban en el DTO
            dto.setIdPersona(persona.getId());
            dto.setNombrePersona(persona.getNombre());
            dto.setApellidoPersona(persona.getApellido());

            // 6. Guardar la reserva
            gestorReserva.crearReserva(dto);

            // 7. Mostrar éxito y salir
            mostrarExito("¡Reserva guardada correctamente!");

            DataTransfer.limpiar();
            HotelPremier.cambiarA("menu");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Ocurrió un error: " + e.getMessage());
        }
    }

    @FXML
    private void onCancelarClicked() {
        // Si cancela, volvemos a la grilla de habitaciones
        HotelPremier.cambiarA("estado_habs2");
    }

    // --- Alertas ---

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}