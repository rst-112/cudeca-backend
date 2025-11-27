package com.cudeca.model.aux;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {

    // Expresión Regular simple y de uso común para validar la estructura básica de un email.
    // Verifica: [uno o más caracteres] @ [uno o más caracteres] . [dos o más caracteres]
    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    /**
     * Verifica si el string proporcionado tiene el formato básico de un correo electrónico.
     * * @param email El string a validar.
     * @return true si el email coincide con el patrón de correo electrónico, false en caso contrario.
     */
    public static boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }

        // El objeto Matcher compara el string con el patrón compilado.
        Matcher matcher = EMAIL_PATTERN.matcher(email);

        // El método matches() devuelve true si toda la secuencia coincide.
        return matcher.matches();
    }

    /*
    // Ejemplo de uso:
    public static void main(String[] args) {
        System.out.println("test@example.com: " + isEmailValid("test@example.com")); // true
        System.out.println("invalid-email.com: " + isEmailValid("invalid-email.com")); // false
        System.out.println("user@sub.domain.co: " + isEmailValid("user@sub.domain.co")); // true
    }
    */
}