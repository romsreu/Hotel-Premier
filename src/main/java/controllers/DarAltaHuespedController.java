package controllers;

import ar.utn.hotel.HotelPremier;
import ar.utn.hotel.dto.DarAltaHuespedDTO;
import ar.utn.hotel.dto.HuespedDTO;
import ar.utn.hotel.gestor.GestorHuesped;
import enums.PopUpType;
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
    @FXML private ComboBox<String> combPosicionIva;
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

    //Iconos de validación (campos vacios o mal formateados) de cada campo
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

    private Validator validator;
    @FXML
    public void initialize() {
        ocultarTodosLosIconos();
        aplicarMayusculas(txtNombre,txtApellido,txtCalle, txtOcupacion, txtEmail);
        aplicarMascaraDNI(txtNumDoc);
        aplicarMascaraCUIT(txtCuit);
        limitarCaracteres(40, txtEmail);
        limitarCaracteres(15, txtNombre, txtApellido, txtCalle, txtOcupacion);
        limitarCaracteres(4, txtNumeroCalle, txtDepto, txtPiso, txtCodigoPostal);
        soloLetras(txtNombre, txtApellido, txtCalle, txtOcupacion);
        soloNumeros(txtNumeroCalle, txtDepto, txtPiso, txtCodigoPostal);
        soloEmail(txtEmail);

        LocalDate fechaMinima = LocalDate.now().minusYears(18);

        // Mostrar por defecto hace 18 años (UX)
        dateNacimiento.setValue(fechaMinima);

        // Bloquear TODAS las fechas que te hagan menor de 18
        dateNacimiento.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Bloquear fechas posteriores a (hoy - 18 años)
                setDisable(empty || date.isAfter(fechaMinima));
            }
        });



        // ==========================================
        // CARGA DE PROVINCIAS DESDE CACHE
        // ==========================================
        if (GeorefCache.provincias != null) {
            comboProvincia.getItems().setAll(GeorefCache.provincias);
        }

        // ==========================================
        // CARGA DE LOCALIDADES DESDE CACHE
        // ==========================================
        comboProvincia.setOnAction(e -> {
            String prov = comboProvincia.getValue();
            if (prov != null) {
                comboLocalidad.getItems().setAll(
                        GeorefCache.localidadesPorProvincia.getOrDefault(prov, List.of())
                );
            }
        });

        comboProvincia.setOnAction(e -> {
            String prov = comboProvincia.getValue();
            if (prov != null && !prov.isBlank()) {
                comboLocalidad.getItems().setAll(
                        GeorefCache.localidadesPorProvincia.getOrDefault(prov, List.of())
                );
                comboLocalidad.setDisable(false);
            }
        });

        comboNacionalidad.getItems().addAll("Argentina", "Uruguaya", "Chilena", "Paraguaya", "Brasileña", "Otra");
        comboTipoDoc.getItems().addAll("DNI", "Pasaporte", "Cédula", "Otro");
        comboTipoDoc.setValue("DNI");
        combPosicionIva.getItems().addAll("RESPONSABLE INSCRIPTO", "MONOTRIBUTISTA", "Otro");
        comboPais.getItems().addAll("Argentina");
        comboPais.setValue("Argentina");

        validator = new Validator();
        validator.addRule(txtNombre, nombreO).required().minLength(3);
        validator.addRule(txtApellido, apellidoO).required().minLength(3);
        validator.addRule(txtEmail, emailO).required().email();
        validator.addRule(comboNacionalidad, nacionalidadO).required();
        validator.addRule(combPosicionIva, posivaO).required();
        validator.addRule(comboProvincia, provinciaO).required();
        validator.addRule(comboLocalidad, localidadO).required();
        validator.addRule(comboPais, paisO).required();
        validator.addRule(dateNacimiento, fechanacO).required().mayorDe18();
        validator.addRule(dateNacimiento, fechanacO).required();
        validator.addRule(txtCalle, calleO).required();
        validator.addRule(txtNumeroCalle, numCalleO).required();
        validator.addRule(txtCodigoPostal, codPostalO).required();
        validator.addRule(txtOcupacion, ocupacionO).required();
        validator.addRule(comboTipoDoc, tipodniO).required();
        validator.addRule(txtNumDoc, numdniO).required();
        validator.addRule(txtCuit, cuitO).required().cuitCoincideCon(txtNumDoc);
        validator.addRule(txtNumDoc, numdniO).required().dni();

    }

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
    }




    @FXML
    private void onSiguienteClicked() {

        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
//        String telefono = txtTelefono.getText();
        String email = txtEmail.getText();
        String tipoDoc = comboTipoDoc.getValue();
        String doc = txtNumDoc.getText();
        String ocupacion = txtOcupacion.getText();
        String posicionIVA = combPosicionIva.getValue();
        String fechaNac = (dateNacimiento.getValue() != null) ? dateNacimiento.getValue().toString() : "No especificada";
        String nacionalidad = comboNacionalidad.getValue();
        String pais = comboPais.getValue();
        String provincia = comboProvincia.getValue();
        String localidad = comboLocalidad.getValue();
        String calle = txtCalle.getText();
        String nroCalle = txtNumeroCalle.getText();
        String depto = txtDepto.getText();
        String piso = txtPiso.getText();
        String codPostal = txtCodigoPostal.getText();
        String cuit = txtCuit.getText();

        if (!validator.validateAll()) {
            PopUpController.mostrarPopUp(
                    PopUpType.WARNING,
                    "Hay campos incompletos o con errores."
            );
            return;
        }

        DarAltaHuespedDTO dto = new DarAltaHuespedDTO();
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setEmail(email);
        dto.setTipoDocumento(tipoDoc);
        dto.setNumeroDocumento(doc);
        dto.setOcupacion(ocupacion);
        dto.setTelefono("12345678");
        dto.setPosicionIVA(posicionIVA);
        dto.setFechaNacimiento(fechaNac);
        dto.setNacionalidad(nacionalidad);
        dto.setOcupacion(ocupacion);
        dto.setDepto(depto);
        dto.setPais(pais);
        dto.setProvincia(provincia);
        dto.setLocalidad(localidad);
        dto.setCalle(calle);
        dto.setNumero(nroCalle);
        dto.setDepto(depto);
        dto.setPiso(piso);
        dto.setCodPostal(codPostal);
        dto.setCuit(cuit);


        GestorHuesped gestor = new GestorHuesped();
        gestor.cargar(dto);


        System.out.println("========================================");
        System.out.println("          DATOS DEL HUÉSPED");
        System.out.println("========================================");
        System.out.printf("%-22s%s%n", "Nombre:", nombre);
        System.out.printf("%-22s%s%n", "Apellido:", apellido);
        System.out.printf("%-22s%s%n", "Email:", email);
        System.out.printf("%-22s%s%n", "Tipo de Documento:", tipoDoc);
        System.out.printf("%-22s%s%n", "Número de Documento:", doc);
        System.out.printf("%-22s%s%n", "CUIT:", cuit);
        System.out.printf("%-22s%s%n", "Ocupación:", ocupacion);
        System.out.printf("%-22s%s%n", "Posición frente al IVA:", posicionIVA);
        System.out.printf("%-22s%s%n", "Fecha de Nacimiento:", fechaNac);
        System.out.printf("%-22s%s%n", "Nacionalidad:", nacionalidad);
        System.out.printf("%-22s%s%n", "País:", pais);
        System.out.printf("%-22s%s%n", "Provincia:", provincia);
        System.out.printf("%-22s%s%n", "Localidad:", localidad);
        System.out.printf("%-22s%s%n", "Calle:", calle);
        System.out.printf("%-22s%s%n", "Número:", nroCalle);
        System.out.printf("%-22s%s%n", "Depto:", depto);
        System.out.printf("%-22s%s%n", "Piso:", piso);
        System.out.printf("%-22s%s%n", "Código Postal:", codPostal);
        System.out.println("----------------------------------------");
    }

    @FXML
    private void onCancelarClicked(){
        HotelPremier.cambiarA("menu");

    }
}
