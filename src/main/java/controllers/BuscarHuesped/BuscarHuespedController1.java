package controllers.BuscarHuesped;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.HuespedDTO;
import ar.utn.hotel.gestor.GestorHuesped;
import ar.utn.hotel.model.Huesped;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import utils.DataTransfer;

import static utils.TextManager.*;

import java.util.List;

public class BuscarHuespedController1 {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellido;
    @FXML
    private TextField txtNumeroDocumento;

    public void initialize() {
        configurarFormatoDeTexto();
    }

    private void configurarFormatoDeTexto() {
        aplicarMayusculas(txtNombre, txtApellido);
        aplicarMascaras();
        limitarLongitudDeCampos();
        aplicarFiltrosDeEntrada();
    }

    private void aplicarMascaras() {
        aplicarMascaraDNI(txtNumeroDocumento);
    }

    private void limitarLongitudDeCampos() {
        limitarCaracteres(15, txtNombre, txtApellido);
        limitarCaracteres(20, txtNumeroDocumento);
    }

    private void aplicarFiltrosDeEntrada() {
        soloLetras(txtNombre, txtApellido);
        soloNumeros(txtNumeroDocumento);
    }

    @FXML
    private void onBuscarClicked() {
        if (!tieneAlgunCampoLleno()) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Para continuar, debe completar al menos un campo"
            );
            return;
        }

        HuespedDTO dto = crearDTODesdeFormulario();
        realizarBusqueda(dto);
    }

    @FXML
    private void onCancelarClicked() {
        HotelPremier.cambiarA("menu");
    }

    private boolean tieneAlgunCampoLleno() {
        return (txtNombre.getText() != null && !txtNombre.getText().trim().isEmpty()) ||
                (txtApellido.getText() != null && !txtApellido.getText().trim().isEmpty()) ||
                (txtNumeroDocumento.getText() != null && !txtNumeroDocumento.getText().trim().isEmpty());
    }

    private HuespedDTO crearDTODesdeFormulario() {
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        String numeroDocumento = txtNumeroDocumento.getText();

        // Usar el builder de HuespedDTO
        return HuespedDTO.builder()
                .nombre((nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : null)
                .apellido((apellido != null && !apellido.trim().isEmpty()) ? apellido.trim() : null)
                .numeroDocumento((numeroDocumento != null && !numeroDocumento.trim().isEmpty()) ?
                        numeroDocumento.trim() : null)
                .build();
    }

    private void realizarBusqueda(HuespedDTO dto) {
        try {
            GestorHuesped gestor = new GestorHuesped();
            List<Huesped> resultados = gestor.buscarHuesped(dto);

            if (resultados == null || resultados.isEmpty()) {
                PopUpController.mostrarPopUp(
                        PopUpType.WARNING,
                        "Ningún huésped se ajusta a los criterios de búsqueda"
                );
                return;
            }

            DataTransfer.setHuespedesEnBusqueda(resultados);
            HotelPremier.cambiarA("buscar_huesped2");
            mostrarPopUpExito(resultados.size());

        } catch (Exception e) {
            PopUpController.mostrarPopUp(
                    PopUpType.ERROR,
                    "Error al buscar huésped: " + e.getMessage()
            );
        }
    }

    private void mostrarPopUpExito(int cantidad) {
        PopUpController.mostrarPopUp(
                PopUpType.SUCCESS,
                String.format("Se encontraron %d huésped(es)", cantidad)
        );
    }
}