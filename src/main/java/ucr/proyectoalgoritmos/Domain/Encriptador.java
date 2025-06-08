package ucr.proyectoalgoritmos.Domain;

public class Encriptador {

    //Metodo para encriptar usando el cifrado Cesar
    public static String encriptar(String text , int displacement){

        String encryptedtext = ""; //Se inicializa en una cadena vacia

        for (char caracter : text.toCharArray()){

            if (Character.isLetter(caracter)){

                char base = Character.isLowerCase(caracter) ? 'a' : 'A';
                //Calculamos el nuevo caracter y lo concatenamos
                caracter = (char) (base + (caracter - base + displacement) % 26);

            }

            encryptedtext = encryptedtext + caracter; //Concatenación directa

        }

        return encryptedtext;

    }

    //Metodo para desencriptar
    public static String desencriptar (String encryptedtext , int displacement){

        return encriptar(encryptedtext , -displacement);

    }

    //Para la verificaión de contraseñas
    public static String encriptarContrasenaParaComparar (String password){

        return encriptar(password , 3);
    }


}
