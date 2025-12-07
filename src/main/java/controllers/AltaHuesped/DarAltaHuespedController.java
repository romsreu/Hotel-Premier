package controllers.AltaHuesped;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.DarAltaHuespedDTO;
import ar.utn.hotel.gestor.GestorHuesped;
import controllers.PopUp.PopUpController;
import enums.PopUpType;
import enums.TipoDocumento;
import enums.TipoIVA;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

import static utils.TextManager.*;

import utils.GeorefCache;
import utils.Validator;

import java.time.LocalDate;
import java.util.List;

public class DarAltaHuespedController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtOcupacion;
    @FXML private TextField txtCuit;
    @FXML private ComboBox<TipoIVA> combPosicionIva;
    @FXML private TextField txtNumDoc;
    @FXML private TextField txtCalle;
    @FXML private TextField txtNumeroCalle;
    @FXML private TextField txtDepto;
    @FXML private TextField txtPiso;
    @FXML private TextField txtCodigoPostal;
    @FXML private TextField txtTel;
    @FXML private DatePicker dateNacimiento;
    @FXML private ComboBox<String> comboNacionalidad;
    @FXML private ComboBox<String> comboLocalidad;
    @FXML private ComboBox<String> comboProvincia;
    @FXML private ComboBox<TipoDocumento> comboTipoDoc;
    @FXML private ComboBox<String> comboPais;

    @FXML private ImageView nombreO;
    @FXML private ImageView apellidoO;
    @FXML private ImageView nacionalidadO;
    @FXML private ImageView posivaO;
    @FXML private ImageView emailO;
    @FXML private ImageView paisO;
    @FXML private ImageView provinciaO;
    @FXML private ImageView localidadO;
    @FXML private ImageView tipodniO;
    @FXML private ImageView numdniO;
    @FXML private ImageView fechanacO;
    @FXML private ImageView ocupacionO;
    @FXML private ImageView calleO;
    @FXML private ImageView numCalleO;
    @FXML private ImageView codPostalO;
    @FXML private ImageView cuitO;
    @FXML private ImageView nrotelO;

    private Validator validator;

    public void initialize() {
        ocultarTodosLosIconos();
        configurarFormatoDeTexto();
        configurarDatePicker();
        cargarDatosEnComboBoxes();
        agregarValidacionesDeCampos();
    }

    // ==================== CONFIGURACIÓN INICIAL ====================

    private void configurarFormatoDeTexto() {
        aplicarMayusculas(txtNombre, txtApellido, txtCalle, txtOcupacion, txtEmail);
        aplicarMascaras();
        limitarLongitudDeCampos();
        aplicarFiltrosDeEntrada();
    }

    private void aplicarMascaras() {
        aplicarMascaraDNI(txtNumDoc);
        aplicarMascaraCUIT(txtCuit);
    }

    private void limitarLongitudDeCampos() {
        limitarCaracteres(40, txtEmail);
        limitarCaracteres(15, txtNombre, txtApellido, txtCalle, txtOcupacion, txtTel);
        limitarCaracteres(4, txtNumeroCalle, txtDepto, txtPiso, txtCodigoPostal);
    }

    private void aplicarFiltrosDeEntrada() {
        soloLetras(txtNombre, txtApellido, txtCalle, txtOcupacion);
        soloNumeros(txtNumeroCalle, txtDepto, txtPiso, txtCodigoPostal, txtTel);
        soloEmail(txtEmail);
    }

    private void configurarDatePicker() {
        LocalDate fechaMinima = LocalDate.now().minusYears(18);
        dateNacimiento.setValue(fechaMinima);
        bloquearFechasInvalidasEnDatePicker(fechaMinima);
    }

    private void bloquearFechasInvalidasEnDatePicker(LocalDate fechaMinima) {
        dateNacimiento.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(fechaMinima));
            }
        });
    }

    private void cargarDatosEnComboBoxes() {
        cargarProvincias();
        configurarComboBoxProvincia();
        cargarNacionalidades();
        cargarTiposDocumento();
        cargarTiposIVA();
        cargarPaises();
    }

    private void cargarProvincias() {
        if (GeorefCache.provincias != null) {
            comboProvincia.getItems().setAll(GeorefCache.provincias);
        }
    }

    private void configurarComboBoxProvincia() {
        comboProvincia.setOnAction(e -> {
            String prov = comboProvincia.getValue();
            if (prov != null && !prov.isBlank()) {
                comboLocalidad.getItems().setAll(
                        GeorefCache.localidadesPorProvincia.getOrDefault(prov, List.of())
                );
                comboLocalidad.setDisable(false);
            }
        });
    }

    private void cargarNacionalidades() {
        comboNacionalidad.getItems().addAll(
                "Argentina", "Uruguaya", "Chilena", "Paraguaya", "Brasileña", "Otra"
        );
    }

    private void cargarTiposDocumento() {
        comboTipoDoc.getItems().addAll(TipoDocumento.values());
        comboTipoDoc.setValue(TipoDocumento.DNI);
    }

    private void cargarTiposIVA() {
        combPosicionIva.getItems().addAll(TipoIVA.values());
    }

    private void cargarPaises() {
        comboPais.getItems().addAll("Argentina");
        comboPais.setValue("Argentina");
    }

    // ==================== EVENTOS DE BOTONES ====================

    @FXML
    private void onSiguienteClicked() {
        if (!validator.validateAll()) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Hay campos incompletos o con errores."
            );
            return;
        }

        DarAltaHuespedDTO dto = crearDTODesdeFormulario();
        guardarYMostrarDatos(dto);
    }

    @FXML
    private void onCancelarClicked() {
        HotelPremier.cambiarA("menu");
    }

    // ==================== LÓGICA DE DATOS ====================

    private DarAltaHuespedDTO crearDTODesdeFormulario() {
        DarAltaHuespedDTO dto = new DarAltaHuespedDTO();

        // Datos personales
        dto.setNombre(txtNombre.getText());
        dto.setApellido(txtApellido.getText());
        dto.setEmail(txtEmail.getText());
        dto.setTelefono(txtTel.getText());
        dto.setOcupacion(txtOcupacion.getText());

        // Datos de identificación
        dto.setTipoDocumento(comboTipoDoc.getValue().name());
        dto.setNumeroDocumento(txtNumDoc.getText());
        dto.setCuit(txtCuit.getText());
        dto.setPosicionIVA(combPosicionIva.getValue().name());

        // Datos de nacimiento
        dto.setFechaNacimiento(obtenerFechaNacimiento());
        dto.setNacionalidad(comboNacionalidad.getValue());

        // Datos de dirección
        dto.setPais(comboPais.getValue());
        dto.setProvincia(comboProvincia.getValue());
        dto.setLocalidad(comboLocalidad.getValue());
        dto.setCalle(txtCalle.getText());
        dto.setNumero(txtNumeroCalle.getText());
        dto.setDepto(txtDepto.getText());
        dto.setPiso(txtPiso.getText());
        dto.setCodPostal(txtCodigoPostal.getText());

        return dto;
    }

    private String obtenerFechaNacimiento() {
        return (dateNacimiento.getValue() != null) ?
                dateNacimiento.getValue().toString() : "No especificada";
    }

    private void guardarYMostrarDatos(DarAltaHuespedDTO dto) {
        try {
            GestorHuesped gestor = new GestorHuesped();
            gestor.cargar(dto);

            mostrarPopUpExito(dto);
            mostrarResumenHuesped(dto);

        } catch (IllegalArgumentException e) {
            mostrarPopUpDNIDuplicado(dto.getNumeroDocumento());
        } catch (Exception e) {
            mostrarPopUpError(e.getMessage());
        }
    }

    private void mostrarPopUpExito(DarAltaHuespedDTO dto) {
        String mensaje = String.format(
                "El huésped '%s' '%s' '%s' ha sido cargado satisfactoriamente al sistema",
                dto.getNombre(),
                dto.getApellido(),
                formatearDNI(dto.getNumeroDocumento())
        );
        PopUpController.mostrarPopUp(PopUpType.SUCCESS, mensaje);
    }

    private void mostrarPopUpDNIDuplicado(String numeroDocumento) {
        String mensaje = String.format(
                "El DNI %s ya se encuentra en el sistema",
                formatearDNI(numeroDocumento)
        );
        PopUpController.mostrarPopUp(PopUpType.WARNING, mensaje);
    }

    private void mostrarPopUpError(String mensaje) {
        PopUpController.mostrarPopUp(PopUpType.ERROR, "Error al cargar el huésped: " + mensaje);
    }

    private String formatearDNI(String dni) {
        if (dni == null || dni.isEmpty()) return dni;

        dni = dni.replaceAll("[^0-9]", "");

        if (dni.length() == 8) {
            return dni.substring(0, 2) + "." + dni.substring(2, 5) + "." + dni.substring(5);
        }

        return dni;
    }

    private void mostrarResumenHuesped(DarAltaHuespedDTO dto) {
        System.out.println("========================================");
        System.out.println("          DATOS DEL HUÉSPED");
        System.out.println("========================================");
        System.out.printf("%-22s%s%n", "Nombre:", dto.getNombre());
        System.out.printf("%-22s%s%n", "Apellido:", dto.getApellido());
        System.out.printf("%-22s%s%n", "Email:", dto.getEmail());
        System.out.printf("%-22s%s%n", "Tipo de Documento:", dto.getTipoDocumento());
        System.out.printf("%-22s%s%n", "Número de Documento:", dto.getNumeroDocumento());
        System.out.printf("%-22s%s%n", "CUIT:", dto.getCuit());
        System.out.printf("%-22s%s%n", "Ocupación:", dto.getOcupacion());
        System.out.printf("%-22s%s%n", "Posición frente al IVA:", dto.getPosicionIVA());
        System.out.printf("%-22s%s%n", "Fecha de Nacimiento:", dto.getFechaNacimiento());
        System.out.printf("%-22s%s%n", "Nacionalidad:", dto.getNacionalidad());
        System.out.printf("%-22s%s%n", "País:", dto.getPais());
        System.out.printf("%-22s%s%n", "Provincia:", dto.getProvincia());
        System.out.printf("%-22s%s%n", "Localidad:", dto.getLocalidad());
        System.out.printf("%-22s%s%n", "Calle:", dto.getCalle());
        System.out.printf("%-22s%s%n", "Número:", dto.getNumero());
        System.out.printf("%-22s%s%n", "Depto:", dto.getDepto());
        System.out.printf("%-22s%s%n", "Piso:", dto.getPiso());
        System.out.printf("%-22s%s%n", "Código Postal:", dto.getCodPostal());
        System.out.println("----------------------------------------");
    }

    // ==================== VALIDACIÓN ====================

    private void agregarValidacionesDeCampos() {
        validator = new Validator();

        // Validaciones de texto
        validarCamposTexto();

        // Validaciones de selección
        validarCamposSeleccion();

        // Validaciones de documentos
        validarDocumentos();
    }

    private void validarCamposTexto() {
        validator.addRule(txtNombre, nombreO).required().minLength(3);
        validator.addRule(txtApellido, apellidoO).required().minLength(3);
        validator.addRule(txtTel, nrotelO).required();
        validator.addRule(txtEmail, emailO).required().email();
        validator.addRule(txtCalle, calleO).required();
        validator.addRule(txtNumeroCalle, numCalleO).required();
        validator.addRule(txtCodigoPostal, codPostalO).required();
        validator.addRule(txtOcupacion, ocupacionO).required();
    }

    private void validarCamposSeleccion() {
        validator.addRule(comboNacionalidad, nacionalidadO).required();
        validator.addRule(combPosicionIva, posivaO).required();
        validator.addRule(comboProvincia, provinciaO).required();
        validator.addRule(comboLocalidad, localidadO).required();
        validator.addRule(comboPais, paisO).required();
        validator.addRule(comboTipoDoc, tipodniO).required();
    }

    private void validarDocumentos() {
        validator.addRule(dateNacimiento, fechanacO).required().mayorDe18();
        validator.addRule(txtNumDoc, numdniO).required().dni();
        validator.addRule(txtCuit, cuitO).required().cuitCoincideCon(txtNumDoc);
    }

    // ==================== UTILIDADES ====================

    private void ocultarTodosLosIconos() {
        calleO.setVisible(false);
        numCalleO.setVisible(false);
        codPostalO.setVisible(false);
        tipodniO.setVisible(false);
        numdniO.setVisible(false);
        fechanacO.setVisible(false);
        ocupacionO.setVisible(false);
        localidadO.setVisible(false);
        provinciaO.setVisible(false);
        paisO.setVisible(false);
        posivaO.setVisible(false);
        apellidoO.setVisible(false);
        nombreO.setVisible(false);
        cuitO.setVisible(false);
        nacionalidadO.setVisible(false);
        emailO.setVisible(false);
        nrotelO.setVisible(false);
    }
}