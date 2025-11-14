package ar.utn.hotel.utils;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Utilidades para manipular texto en campos JavaFX.
 * Incluye conversión automática a mayúsculas.
 */
public class TextManager {

    private static final Locale LOCALE_ES_AR = new Locale("es", "AR");

    public static void aplicarMayusculas(TextField... campos) {
        for (TextField campo : campos) {
            if (campo == null) continue;
            campo.setTextFormatter(new TextFormatter<>(cambio -> {
                String texto = cambio.getText();
                if (texto != null) {
                    cambio.setText(texto.toUpperCase(LOCALE_ES_AR));
                }
                return cambio;
            }));
        }
    }

    public static void aplicarMascaraDNI(TextField tf) {
        final Pattern maxDigits = Pattern.compile("\\d{0,8}");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String proposedFormatted = change.getControlNewText();
            String newDigits = proposedFormatted.replaceAll("\\D", "");
            if (!maxDigits.matcher(newDigits).matches()) return null;
            String formatted = formatDNI(newDigits);
            int digitIndex = countDigitsUpTo(proposedFormatted, change.getCaretPosition());
            int newCaret = caretPosForDigitIndex(formatted, digitIndex);
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(newCaret);
            change.setAnchor(newCaret);

            return change;
        };
        tf.setTextFormatter(new TextFormatter<>(filter));
    }

    private static String formatDNI(String digits) {
        if (digits.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(digits);
        if (digits.length() > 2) sb.insert(2, ".");
        if (digits.length() > 5) sb.insert(6, ".");
        return sb.toString();
    }

    /** Aplica máscara CUIT: ##-########-# (máx 11 dígitos) con caret correcto. */
    public static void aplicarMascaraCUIT(TextField tf) {
        final Pattern maxDigits = Pattern.compile("\\d{0,11}");

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String proposedFormatted = change.getControlNewText();
            String newDigits = proposedFormatted.replaceAll("\\D", "");
            if (!maxDigits.matcher(newDigits).matches()) return null;
            String formatted = formatCUIT(newDigits);
            int digitIndex = countDigitsUpTo(proposedFormatted, change.getCaretPosition());
            int newCaret = caretPosForDigitIndex(formatted, digitIndex);
            change.setText(formatted);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(newCaret);
            change.setAnchor(newCaret);
            return change;
        };
        tf.setTextFormatter(new TextFormatter<>(filter));
    }

    private static String formatCUIT(String digits) {
        if (digits.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(digits);
        if (digits.length() > 2) sb.insert(2, "-");
        if (digits.length() > 10) sb.insert(11, "-");
        return sb.toString();
    }

    /* ===================== Helpers comunes ===================== */

    private static int countDigitsUpTo(String s, int upTo) {
        int count = 0;
        int limit = Math.min(upTo, s.length());
        for (int i = 0; i < limit; i++) {
            if (Character.isDigit(s.charAt(i))) count++;
        }
        return count;
    }

    /**
     * Dado un texto formateado y un índice de dígito (0..N),
     * devuelve el índice de caret justo después de ese dígito en el string formateado.
     * Si digitIndex es 0, devuelve el primer índice válido (antes de todo).
     */
    private static int caretPosForDigitIndex(String formatted, int digitIndex) {
        if (digitIndex <= 0) return 0;
        int count = 0;
        for (int i = 0; i < formatted.length(); i++) {
            if (Character.isDigit(formatted.charAt(i))) {
                count++;
                if (count == digitIndex) {
                    return i + 1;
                }
            }
        }
        return formatted.length();
    }

    public static void soloAlfanumerico(TextInputControl... campos) {
        List<TextInputControl> lista = Arrays.asList(campos);

        for (TextInputControl campo : lista) {
            campo.textProperty().addListener((obs, oldText, newText) -> {
                if (newText == null) return;

                // Reemplaza todo lo que no sea letra, número o espacio
                String filtrado = newText.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]", "");
                if (!filtrado.equals(newText)) {
                    int caretPos = campo.getCaretPosition();
                    campo.setText(filtrado);
                    campo.positionCaret(Math.min(caretPos, filtrado.length()));
                }
            });
        }
    }

    public static void limitarCaracteres(int maxLength, TextInputControl... campos) {
        List<TextInputControl> lista = Arrays.asList(campos);
        for (TextInputControl campo : lista) {
            campo.textProperty().addListener((obs, oldText, newText) -> {
                if (newText != null && newText.length() > maxLength) {
                    campo.setText(newText.substring(0, maxLength));
                    campo.positionCaret(maxLength);
                }
            });
        }
    }

    public static void aplicarMascaraPasaporte(TextField tf) {
        tf.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;
            String clean = newText.toUpperCase().replaceAll("[^A-Z0-9]", "");

            if (clean.length() > 9)
                clean = clean.substring(0, 9);
            String letras = clean.replaceAll("[^A-Z]", "");
            String numeros = clean.replaceAll("[^0-9]", "");
            if (letras.length() > 3) letras = letras.substring(0, 3);
            if (numeros.length() > 6) numeros = numeros.substring(0, 6);
            String formatted = letras;
            if (!numeros.isEmpty()) {
                formatted += "-" + numeros;
            }

            if (!formatted.equals(newText)) {
                tf.setText(formatted);
                tf.positionCaret(formatted.length());
            }
        });
    }

    public static void soloLetras(TextInputControl... campos) {
        List<TextInputControl> lista = Arrays.asList(campos);

        for (TextInputControl campo : lista) {
            campo.textProperty().addListener((obs, oldText, newText) -> {
                if (newText == null) return;

                // Solo letras y espacios (admite tildes y ñ)
                String filtrado = newText.replaceAll("[^a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]", "");
                if (!filtrado.equals(newText)) {
                    int caretPos = campo.getCaretPosition();
                    campo.setText(filtrado);
                    campo.positionCaret(Math.min(caretPos, filtrado.length()));
                }
            });
        }
    }

    public static void soloNumeros(TextInputControl... campos) {
        List<TextInputControl> lista = Arrays.asList(campos);

        for (TextInputControl campo : lista) {
            campo.textProperty().addListener((obs, oldText, newText) -> {
                if (newText == null) return;

                // Permite solo dígitos (0-9)
                String filtrado = newText.replaceAll("[^0-9]", "");
                if (!filtrado.equals(newText)) {
                    int caretPos = campo.getCaretPosition();
                    campo.setText(filtrado);
                    campo.positionCaret(Math.min(caretPos, filtrado.length()));
                }
            });
        }
    }
}


