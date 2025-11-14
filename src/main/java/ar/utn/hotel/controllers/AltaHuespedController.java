package ar.utn.hotel.controllers;

import ar.utn.hotel.dto.DarAltaHuespedDTO;
import ar.utn.hotel.enums.PopUpType;
import ar.utn.hotel.gestor.GestorHuesped;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ar.utn.hotel.controllers.PopUpController.mostrarPopUp;
import static ar.utn.hotel.utils.TextManager.*;

public class AltaHuespedController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtOcupacion;
    @FXML private TextField txtCuit;
    @FXML private ComboBox<String> comboPosicionIva;
    @FXML private TextField txtNumDoc;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumeroCalle;
    @FXML private TextField txtDepto;
    @FXML private TextField txtPiso;
    @FXML private TextField txtCodigoPostal;
    @FXML private DatePicker dateNacimiento;
    @FXML private ComboBox<String> comboNacionalidad;
    @FXML private ComboBox<String> comboLocalidad;
    @FXML private ComboBox<String> comboProvincia;
    @FXML private ComboBox<String> comboTipoDoc;
    @FXML private ComboBox<String> comboPais;
    @FXML private Button btnCancelar;
    @FXML private Button btnSiguiente;


    // === CLIENTE HTTP (se reutiliza para todas las llamadas) ===
    private final HttpClient http = HttpClient.newHttpClient();

    @FXML
    public void initialize() {

        aplicarMayusculas(txtNombre,txtApellido,txtCalle, txtOcupacion, txtEmail);
        aplicarMascaraDNI(txtNumDoc);
        aplicarMascaraCUIT(txtCuit);
        limitarCaracteres(15, txtNombre, txtApellido, txtCalle, txtOcupacion);
        limitarCaracteres(4, txtNumeroCalle, txtDepto, txtPiso, txtCodigoPostal);
        soloLetras(txtNombre, txtApellido, txtCalle, txtOcupacion);
        soloNumeros(txtNumeroCalle, txtDepto, txtPiso, txtCodigoPostal);
        cargarProvincias();


        comboProvincia.setOnAction(e -> {
            String prov = comboProvincia.getValue();
            if (prov != null && !prov.isBlank()) {
                cargarLocalidades(prov);
            }
        });

        comboNacionalidad.getItems().addAll("Argentina", "Uruguaya", "Chilena", "Paraguaya", "Brasileña", "Otra");
        comboTipoDoc.getItems().addAll("DNI", "Pasaporte", "Cédula", "Otro");
        comboTipoDoc.setValue("DNI");
        comboPosicionIva.getItems().addAll("RESPONSABLE INSCRIPTO", "MONOTRIBUTISTA", "Otro");
        comboPais.getItems().addAll("Argentina");
        comboPais.setValue("Argentina");

    }

    // ===============================================================
    // CARGA DE PROVINCIAS DESDE API GEOREF
    // ===============================================================
    private void cargarProvincias() {
        comboProvincia.setDisable(true);
        comboProvincia.setPromptText("Cargando provincias...");

        String url = "https://apis.datos.gob.ar/georef/api/provincias?campos=nombre&max=100";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::procesarProvincias)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        comboProvincia.setPromptText("Error al cargar");
                        comboProvincia.setDisable(false);
                    });
                    return null;
                });
    }

    private void procesarProvincias(String json) {
        List<String> provincias = extraerNombres(json);
        Platform.runLater(() -> {
            comboProvincia.getItems().setAll(provincias);
            comboProvincia.setPromptText("Provincia");
            comboProvincia.setDisable(false);
        });
    }

    // ===============================================================
    // CARGA DE LOCALIDADES SEGÚN PROVINCIA
    // ===============================================================
    private void cargarLocalidades(String provinciaNombre) {
        comboLocalidad.setDisable(true);
        comboLocalidad.getItems().clear();
        comboLocalidad.setPromptText("Cargando localidades...");

        String provEncoded = URLEncoder.encode(provinciaNombre, StandardCharsets.UTF_8);
        String url = "https://apis.datos.gob.ar/georef/api/localidades?provincia=" + provEncoded
                + "&campos=nombre&max=5000";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(json -> {
                    List<String> localidades = extraerNombres(json);
                    Platform.runLater(() -> {
                        comboLocalidad.getItems().setAll(localidades);
                        comboLocalidad.setPromptText("Localidad");
                        comboLocalidad.setDisable(false);
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        comboLocalidad.setPromptText("Error al cargar");
                        comboLocalidad.setDisable(false);
                    });
                    return null;
                });
    }

    // ===============================================================
    // PARSER SIMPLE (sin dependencias externas)
    // ===============================================================
    private List<String> extraerNombres(String json) {
        List<String> nombres = new ArrayList<>();
        String[] partes = json.split("\"nombre\"\\s*:\\s*");
        for (int i = 1; i < partes.length; i++) {
            String resto = partes[i].trim();
            if (!resto.startsWith("\"")) continue;
            int fin = resto.indexOf('"', 1);
            if (fin > 1) {
                String nombre = resto.substring(1, fin).trim();
                if (!nombre.isBlank()) nombres.add(nombre);
            }
        }
        nombres.sort(String::compareToIgnoreCase);
        return nombres;
    }

    // ===============================================================
    // BOTÓN SIGUIENTE
    // ===============================================================
    @FXML
    private void onSiguienteClicked() {
        DarAltaHuespedDTO dto = new DarAltaHuespedDTO(
                txtNombre.getText(),
                txtApellido.getText(),
                txtTelefono.getText(),
                txtNumDoc.getText(),
                comboTipoDoc.getValue(),
                comboPosicionIva.getValue(),
                dateNacimiento.getValue().toString(),
                txtOcupacion.getText(),
                comboNacionalidad.getValue(),
                txtEmail.getText(),
                txtCuit.getText(),
                txtCalle.getText(),
                txtNumeroCalle.getText(),
                txtDepto.getText(),
                txtPiso.getText(),
                txtCodigoPostal.getText(),
                comboLocalidad.getValue(),
                comboProvincia.getValue(),
                comboPais.getValue()
        );

        GestorHuesped gestor = new GestorHuesped();
        gestor.cargar(dto);

        mostrarPopUp(PopUpType.SUCCESS, "Huésped guardado correctamente en la base de datos.");


        System.out.println("========================================");
        System.out.println("          DATOS DEL HUÉSPED");
        System.out.println("========================================");
        System.out.printf("%-22s%s%n", "Nombre:", txtNombre.getText());
        System.out.printf("%-22s%s%n", "Apellido:", txtApellido.getText());
        System.out.printf("%-22s%s%n", "Email:", txtEmail.getText());
        System.out.printf("%-22s%s%n", "País:", comboPais.getValue());
        System.out.printf("%-22s%s%n", "Provincia:", comboProvincia.getValue());
        System.out.printf("%-22s%s%n", "Localidad:", comboLocalidad.getValue());
        System.out.printf("%-22s%s%n", "Calle:", txtCalle.getText());
        System.out.println("----------------------------------------");

    }
}
