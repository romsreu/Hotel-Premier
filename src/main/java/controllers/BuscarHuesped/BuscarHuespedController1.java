package controllers.BuscarHuesped;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.BuscarHuespedDTO;
import ar.utn.hotel.gestor.GestorHuesped;
import ar.utn.hotel.model.Huesped;
import controllers.PopUpController;
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

    // ==================== CONFIGURACIÓN INICIAL ====================

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

    // ==================== EVENTOS DE BOTONES ====================

    @FXML
    private void onBuscarClicked() {
        // Validar que al menos un campo esté completo
        if (!tieneAlgunCampoLleno()) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Para continuar, debe completar al menos un campo"
            );
            return;
        }

        BuscarHuespedDTO dto = crearDTODesdeFormulario();
        realizarBusqueda(dto);
    }

    @FXML
    private void onCancelarClicked() {
        HotelPremier.cambiarA("menu");
    }

    // ==================== LÓGICA DE DATOS ====================

    private boolean tieneAlgunCampoLleno() {
        boolean nombreLleno = txtNombre.getText() != null && !txtNombre.getText().trim().isEmpty();
        boolean apellidoLleno = txtApellido.getText() != null && !txtApellido.getText().trim().isEmpty();
        boolean documentoLleno = txtNumeroDocumento.getText() != null && !txtNumeroDocumento.getText().trim().isEmpty();

        return nombreLleno || apellidoLleno || documentoLleno;
    }

    private BuscarHuespedDTO crearDTODesdeFormulario() {
        BuscarHuespedDTO dto = new BuscarHuespedDTO();

        dto.setNombre(txtNombre.getText().trim());
        dto.setApellido(txtApellido.getText().trim());
        dto.setNumeroDocumento(txtNumeroDocumento.getText().trim());

        return dto;
    }

    private void realizarBusqueda(BuscarHuespedDTO dto) {
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
            System.out.printf("pantalla1", resultados);
            HotelPremier.cambiarA("buscar_huesped2");
            mostrarPopUpExito(resultados.size());

        } catch (IllegalArgumentException e) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Ningún huésped se ajusta a los criterios de búsqueda"
            );
        } catch (Exception e) {
            PopUpController.mostrarPopUp(PopUpType.ERROR, "Error al buscar huésped: " + e.getMessage());
        }
    }

    private void mostrarPopUpExito(int cantidad) {
        String mensaje = String.format(
                "Se encontraron %d huésped(es)",
                cantidad
        );
        PopUpController.mostrarPopUp(PopUpType.SUCCESS, mensaje);
    }

}