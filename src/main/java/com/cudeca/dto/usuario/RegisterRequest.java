package com.cudeca.dto.usuario;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterRequest {
    private String nombre;
    private String email;
    private String password;
    // Regex que verifica la estructura básica: palabra@dominio.extension
    private static String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    // Constructor (Opcional, pero útil)
    public RegisterRequest(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }

    /**
     * Verifica si el string proporcionado cumple con el formato básico de un correo electrónico.
     * Esta función utiliza una expresión regular (Regex) precompilada para validar la estructura.
     * * @param email El string (email) que se desea validar.
     * @return true si el email coincide completamente con el patrón de formato válido; false en caso contrario.
     */
    public static boolean esEmailSeguro(String email) {

        // Comprobación inicial: Si el email es nulo, la validación falla inmediatamente.
        if (email == null) {
            return false;
        }

        // Crea un objeto Matcher que compara el email de entrada con el patrón precompilado.
        Matcher matcher = EMAIL_PATTERN.matcher(email);

        // El método matches() devuelve true solo si toda la cadena coincide con el patrón completo.
        return matcher.matches();
    }

    /**
     * Verifica la fortaleza de una contraseña usando un patrón Regex.
     * Debe tener 8 caracteres, al menos una mayúscula, una minúscula, un número y un carácter especial.
     *
     * @param password La contraseña en texto plano.
     * @return true si la contraseña cumple con la política de seguridad.
     */
    private boolean esContrasenaSegura(String password) {
        if (password == null) {
            return false;
        }

        // El patrón exige 4 criterios Lookahead: mayúscula, minúscula, número y símbolo, además de 8 caracteres.
        final String PASSWORD_REGEX =
                "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

        Pattern pattern = Pattern.compile(PASSWORD_REGEX);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }

    // Getters y Setters
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        if(esEmailSeguro(email)){
            this.email = email;
        }else{
            throw new RuntimeException("El email no tiene un formato valido");
        }
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        if(esContrasenaSegura(password)){
            this.password = password;
        }else{
            throw new RuntimeException("La contraseña no es segura");
        }

    }
}