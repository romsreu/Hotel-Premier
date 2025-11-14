package ar.utn.hotel.controllers;

import ar.utn.hotel.enums.PopUpType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PopUpController {

    @FXML private AnchorPane root;
    @FXML private Label lblIcon;
    @FXML private Label lblMensaje;
    @FXML private Button btnAceptar;

    /**
     * Método para configurar visualmente el PopUp
     */
    public void setPopUp(PopUpType tipo, String mensaje) {
        lblMensaje.setText(mensaje);

        switch (tipo) {
            case SUCCESS:
                lblIcon.setText("✔");
                root.setStyle("-fx-background-color: #D4EDDA;");
                lblMensaje.setStyle("-fx-text-fill: #155724;");
                break;

            case ERROR:
                lblIcon.setText("✖");
                root.setStyle("-fx-background-color: #F8D7DA;");
                lblMensaje.setStyle("-fx-text-fill: #721C24;");
                break;

            case WARNING:
                lblIcon.setText("⚠");
                root.setStyle("-fx-background-color: #FFF3CD;");
                lblMensaje.setStyle("-fx-text-fill: #856404;");
                break;
        }
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) btnAceptar.getScene().getWindow();
        stage.close();
    }

    public static void mostrarPopUp(PopUpType tipo, String mensaje) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    PopUpController.class.getResource("/views/componentes/PopUp.fxml")
            );

            Parent root = loader.load();

            PopUpController controller = loader.getController();
            controller.setPopUp(tipo, mensaje);

            Stage ventana = new Stage();
            ventana.initModality(Modality.APPLICATION_MODAL);
            ventana.setResizable(false);
            ventana.setTitle("Mensaje");
            ventana.setScene(new Scene(root));
            ventana.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
