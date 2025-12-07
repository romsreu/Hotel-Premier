package controllers.PopUp;

import enums.PopUpType;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.function.Consumer;

public class PopUpController {

    @FXML private AnchorPane root;
    @FXML private Label lblIcon;
    @FXML private Label lblMensaje;
    @FXML private Button btnAceptar;
    @FXML private Button btnCancelar;
    @FXML private HBox hboxBotones;

    private Consumer<Boolean> callback;
    private boolean resultado = false;

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
                btnAceptar.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-cursor: hand;");
                break;

            case ERROR:
                lblIcon.setText("✖");
                root.setStyle("-fx-background-color: #F8D7DA;");
                lblMensaje.setStyle("-fx-text-fill: #721C24;");
                btnAceptar.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-cursor: hand;");
                break;

            case WARNING:
                lblIcon.setText("⚠");
                root.setStyle("-fx-background-color: #FFF3CD;");
                lblMensaje.setStyle("-fx-text-fill: #856404;");
                btnAceptar.setStyle("-fx-background-color: #ffc107; -fx-text-fill: #212529; " +
                        "-fx-padding: 10 20; -fx-cursor: hand;");
                break;

            case INFO:
                lblIcon.setText("ℹ");
                root.setStyle("-fx-background-color: #D1ECF1;");
                lblMensaje.setStyle("-fx-text-fill: #0C5460;");
                btnAceptar.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-cursor: hand;");
                break;

            case CONFIRMATION:
                lblIcon.setText("❓");
                root.setStyle("-fx-background-color: #E7F3FF;");
                lblMensaje.setStyle("-fx-text-fill: #004085;");
                btnAceptar.setText("Confirmar");
                btnAceptar.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-cursor: hand;");

                // Mostrar botón cancelar
                btnCancelar.setVisible(true);
                btnCancelar.setManaged(true);
                btnCancelar.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-cursor: hand;");
                break;
        }
    }

    public void setCallback(Consumer<Boolean> callback) {
        this.callback = callback;
    }

    @FXML
    private void aceptar() {
        resultado = true;
        cerrar();
    }

    @FXML
    private void cancelar() {
        resultado = false;
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) btnAceptar.getScene().getWindow();

        // Ejecutar callback si existe
        if (callback != null) {
            callback.accept(resultado);
        }

        stage.close();
    }

    // ========================================
    // MÉTODO ESTÁTICO PARA MENSAJES SIMPLES
    // ========================================
    public static void mostrarPopUp(PopUpType tipo, String mensaje) {
        mostrarPopUpConCallback(tipo, mensaje, null);
    }

    // ========================================
    // MÉTODO ESTÁTICO PARA CONFIRMACIONES
    // ========================================
    public static void mostrarPopUpConCallback(PopUpType tipo, String mensaje, Consumer<Boolean> callback) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    PopUpController.class.getResource("/views/componentes/PopUp.fxml")
            );

            Parent root = loader.load();
            PopUpController controller = loader.getController();
            controller.setPopUp(tipo, mensaje);
            controller.setCallback(callback);

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
            ventana.initModality(Modality.NONE);
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
            // BLOQUEAR CLICS AL FONDO
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