package controllers;

import enums.PopUpType;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

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

            // ============================
            // OWNER (VENTANA PRINCIPAL)
            // ============================
            Stage owner = null;

            for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                if (w instanceof Stage stage && stage.isFocused()) {
                    owner = stage;
                    break;
                }
            }

            if (owner == null) {
                for (javafx.stage.Window w : javafx.stage.Window.getWindows()) {
                    if (w instanceof Stage stage) {
                        owner = stage;
                        break;
                    }
                }
            }

            final Stage finalOwner = owner;

            // ============================
            // POPUP SIN MODALIDAD
            // ============================
            Stage ventana = new Stage();
            ventana.initStyle(javafx.stage.StageStyle.UNDECORATED);
            ventana.initModality(Modality.NONE);  // 🔥 permite mover/minimizar el fondo
            ventana.initOwner(finalOwner);

            ventana.setScene(new Scene(root));
            ventana.setResizable(false);

            // ============================
            // BLUR AL OWNER
            // ============================
            if (finalOwner != null) {
                finalOwner.getScene().getRoot().setEffect(new javafx.scene.effect.GaussianBlur(18));
            }

            ventana.setOnHidden(ev -> {
                if (finalOwner != null) {
                    finalOwner.getScene().getRoot().setEffect(null);
                    finalOwner.getScene().getRoot().setMouseTransparent(false);
                }
            });

            // ============================
            // BLOQUEAR CLICS AL FONDO (sin modal)
            // ============================
            if (finalOwner != null) {
                finalOwner.getScene().getRoot().setMouseTransparent(true);
            }

            // ============================
            // CENTRAR POPUP
            // ============================
            Runnable centrar = () -> {
                if (finalOwner == null) return;
                ventana.setX(finalOwner.getX() + (finalOwner.getWidth() - ventana.getWidth()) / 2);
                ventana.setY(finalOwner.getY() + (finalOwner.getHeight() - ventana.getHeight()) / 2);
            };

            ventana.setOnShown(e -> centrar.run());

            if (finalOwner != null) {
                finalOwner.xProperty().addListener((o, a, b) -> centrar.run());
                finalOwner.yProperty().addListener((o, a, b) -> centrar.run());
                finalOwner.widthProperty().addListener((o, a, b) -> centrar.run());
                finalOwner.heightProperty().addListener((o, a, b) -> centrar.run());
            }

            // ============================
            // ANIMACIÓN FADE-IN
            // ============================
            FadeTransition ft = new FadeTransition(Duration.millis(180), root);
            root.setOpacity(0);
            ft.setToValue(1);
            ft.play();

            ventana.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
