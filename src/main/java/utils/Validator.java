package utils;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Validator {

    private final List<ValidationRule> rules = new ArrayList<>();

    // Métodos addRule para distintos tipos de campo
    public FieldValidator addRule(TextField campo, ImageView icono) {
        FieldValidator fv = new FieldValidator(campo, icono);
        rules.add(fv);
        return fv;
    }

    public FieldValidator addRule(ComboBox<?> combo, ImageView icono) {
        FieldValidator fv = new FieldValidator(combo, icono);
        rules.add(fv);
        return fv;
    }

    public FieldValidator addRule(DatePicker picker, ImageView icono) {
        FieldValidator fv = new FieldValidator(picker, icono);
        rules.add(fv);
        return fv;
    }

    // Validar todas las reglas
    public boolean validateAll() {
        boolean allOk = true;
        for (ValidationRule r : rules) {
            boolean ok = r.validate();
            if (!ok) allOk = false;
        }
        return allOk;
    }

    // ============================================================
    // CLASE BASE: LÓGICA DE VALIDACIÓN
    // ============================================================
    public static class ValidationRule implements ValidationRuleInterface {

        protected final Node campo;
        protected final ImageView icono;

        protected boolean isRequired = false;
        protected Integer minLength = null;
        protected boolean email = false;
        protected boolean dni = false;
        protected boolean cuit = false;
        protected boolean pasaporte = false;
        protected TextField dniRelacionado = null;


        public ValidationRule(Node campo, ImageView icono) {
            this.campo = campo;
            this.icono = icono;
        }
        protected String soloDigitos(String s) {
            return (s == null) ? "" : s.replaceAll("\\D", "");
        }


        @Override
        public boolean validate() {
            String value = extractValue();

            // LIMPIAR ESTADO
            icono.setVisible(false);
            Tooltip.uninstall(icono, null);

            // =============================
            // REQUIRED
            // =============================
            if (isRequired && (value == null || value.isBlank())) {
                showError("Campo obligatorio", "/images/advertencia.png");
                return false;
            }

            // Si no es obligatorio y está vacío, está OK
            if (!isRequired && (value == null || value.isBlank())) {
                return true;
            }

            // =============================
            // MIN LENGTH
            // =============================
            if (minLength != null && value.length() < minLength) {
                showError("Debe tener mínimo " + minLength + " caracteres",
                        "/images/incompleto.png");
                return false;
            }

            // =============================
            // EMAIL
            // =============================
            if (email) {
                String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
                if (!value.matches(regex)) {
                    showError("Formato de email inválido", "/images/incompleto.png");
                    return false;
                }
            }

            // =============================
            // DNI: 8 dígitos (solo números)
            // =============================
            if (dni) {
                String digitos = soloDigitos(value);
                if (digitos.length() != 8) {
                    showError("El DNI debe contener 8 dígitos",
                            "/images/incompleto.png");
                    return false;
                }
            }

            // =============================
            // CUIT: 11 dígitos (solo números)
            // =============================
            if (cuit) {

                String digitosCuit = soloDigitos(value);

                if (digitosCuit.length() != 11) {
                    showError("El CUIT debe tener 11 dígitos",
                            "/images/incompleto.png");
                    return false;
                }

                if (dniRelacionado != null) {
                    String digitosDni = soloDigitos(dniRelacionado.getText());

                    if (digitosDni.length() == 8) {
                        // los 8 del medio
                        String cuitMiddle = digitosCuit.substring(2, 10);

                        if (!cuitMiddle.equals(digitosDni)) {
                            showError("El CUIT no coincide con el DNI",
                                    "/images/incompleto.png");
                            return false;
                        }
                    }
                }
            }

            // =============================
            // PASAPORTE: 3 letras + 6 números
            // =============================
            if (pasaporte) {
                String limpio = value.replaceAll("[^A-Za-z0-9]", "");
                if (!limpio.matches("[A-Za-z]{3}\\d{6}")) {
                    showError("Formato pasaporte: 3 letras + 6 números",
                            "/images/incompleto.png");
                    return false;
                }
            }


            return true;
        }

        protected void showError(String mensaje, String iconoPath) {
            icono.setVisible(true);
            icono.setImage(new Image(getClass().getResource(iconoPath).toExternalForm()));

            Tooltip tooltip = new Tooltip(mensaje);
            tooltip.setShowDelay(Duration.millis(100));
            tooltip.setHideDelay(Duration.ZERO);
            tooltip.setStyle("-fx-font-size: 14;");
            Tooltip.install(icono, tooltip);
        }

        protected String extractValue() {
            if (campo instanceof TextField c) return c.getText();
            if (campo instanceof ComboBox<?> c) {
                Object o = c.getValue();
                return (o != null ? o.toString() : null);
            }
            if (campo instanceof DatePicker d) {
                return (d.getValue() != null ? d.getValue().toString() : null);
            }
            return null;
        }
    }

    public interface ValidationRuleInterface {
        boolean validate();
    }

    // ============================================================
    // CLASE PUBLICA: API FLUIDA PARA EL CONTROLADOR
    // ============================================================
    public static class FieldValidator extends ValidationRule {

        public FieldValidator(Node campo, ImageView icono) {
            super(campo, icono);
        }

        public FieldValidator required() {
            this.isRequired = true;
            return this;
        }

        public FieldValidator minLength(int len) {
            this.minLength = len;
            return this;
        }

        public FieldValidator email() {
            this.email = true;
            return this;
        }

        public FieldValidator dni() {
            this.dni = true;
            return this;
        }

        public FieldValidator cuit() {
            this.cuit = true;
            return this;
        }

        public FieldValidator cuitCoincideCon(TextField dniField) {
            this.cuit = true;          // activa validación de cuit
            this.dniRelacionado = dniField;
            return this;
        }




        public FieldValidator pasaporte() {
            this.pasaporte = true;
            return this;
        }
    }
}
