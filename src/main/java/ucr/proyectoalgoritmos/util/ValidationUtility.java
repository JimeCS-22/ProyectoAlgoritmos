package ucr.proyectoalgoritmos.util;

/**
 * Utilidades para validación de parámetros.
 */
public class ValidationUtility {

    /**
     * Valida que un objeto no sea nulo.
     * @param object Objeto a validar
     * @param fieldName Nombre del campo para el mensaje de error
     * @throws IllegalArgumentException si el objeto es nulo
     */
    public static void validateNotNull(Object object, String fieldName) {
        if (object == null) {
            throw new IllegalArgumentException(fieldName + " no puede ser nulo");
        }
    }

    /**
     * Valida que una cadena no sea nula o vacía.
     * @param str Cadena a validar
     * @param fieldName Nombre del campo para el mensaje de error
     * @throws IllegalArgumentException si la cadena es nula o vacía
     */
    public static void validateNotEmpty(String str, String fieldName) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " no puede ser nulo o vacío");
        }
    }

    public static boolean isValidName(String name) {
        return name != null && name.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s'-]+");
    }
}