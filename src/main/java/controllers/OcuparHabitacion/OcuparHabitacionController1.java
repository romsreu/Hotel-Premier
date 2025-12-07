package controllers.OcuparHabitacion;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.HuespedDTO;
import ar.utn.hotel.gestor.GestorHuesped;
import ar.utn.hotel.model.Huesped;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.DataTransfer;

import java.util.List;

import static utils.TextManager.*;

public class OcuparHabitacionController1 {

    @FXML private TextField tfNombre;
    @FXML private TextField tfApellido;
    @FXML private TextField tfNumeroDocumento;
    @FXML private Button btnCancelar;
    @FXML private Button btnAceptar;

    private GestorHuesped gestorHuesped;

    @FXML
    public void initialize() {
        gestorHuesped = new GestorHuesped();

        // Verificar que haya habitaciones seleccionadas
        if (DataTransfer.getHabitacionesSeleccionadas() == null ||
                DataTransfer.getHabitacionesSeleccionadas().isEmpty()) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "No hay habitaciones seleccionadas"
            );
            HotelPremier.cambiarA("menu");
            return;
        }

        configurarCampos();
    }

    private void configurarCampos() {
        aplicarMayusculas(tfNombre, tfApellido);
        soloLetras(tfNombre, tfApellido);
        soloNumeros(tfNumeroDocumento);
        limitarCaracteres(15, tfNombre, tfApellido);
        aplicarMascaraDNI(tfNumeroDocumento);
    }

    @FXML
    public void onCancelarClicked(ActionEvent actionEvent) {
        DataTransfer.limpiar();
        HotelPremier.cambiarA("menu");
    }

    @FXML
    public void onAceptarClicked(ActionEvent actionEvent) {
        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        String numeroDocumento = tfNumeroDocumento.getText().trim();

        // Validar que al menos tenga nombre y apellido
        if (nombre.isEmpty() || apellido.isEmpty()) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Debe ingresar al menos nombre y apellido del huésped"
            );
            return;
        }

        buscarHuespedes(nombre, apellido, numeroDocumento);
    }

    private void buscarHuespedes(String nombre, String apellido, String numeroDocumento) {
        try {
            // Crear DTO con los criterios de búsqueda
            HuespedDTO.HuespedDTOBuilder builder = HuespedDTO.builder()
                    .nombre(nombre)
                    .apellido(apellido);

            // Agregar número de documento solo si se ingresó
            if (!numeroDocumento.isEmpty()) {
                builder.numeroDocumento(numeroDocumento);
            }

            HuespedDTO dto = builder.build();

            // Buscar huéspedes usando el gestor
            List<Huesped> huespedes = gestorHuesped.buscarHuesped(dto);

            if (huespedes == null || huespedes.isEmpty()) {
                PopUpController.mostrarPopUp(
                        PopUpType.WARNING,
                        "No se encontraron huéspedes con esos datos.\n\n" +
                                "Asegúrese de que el huésped esté registrado en el sistema."
                );
                return;
            }

            // Guardar resultados y pasar a la siguiente pantalla
            DataTransfer.setHuespedesEnBusqueda(huespedes);
            HotelPremier.cambiarA("ocupar_hab2");

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al buscar huéspedes:\n" + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}