package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ucr.proyectoalgoritmos.Domain.Circular.CircularLinkedList;
import ucr.proyectoalgoritmos.Domain.Circular.ListException;
import ucr.proyectoalgoritmos.Domain.Circular.Node;
import ucr.proyectoalgoritmos.Domain.Encriptador;
import ucr.proyectoalgoritmos.Domain.User;
import ucr.proyectoalgoritmos.util.FXUtility;

public class LoginController {
    @javafx.fxml.FXML
    private TextField UserName;
    @javafx.fxml.FXML
    private Button Enter;

    private CircularLinkedList userList ;
    @javafx.fxml.FXML
    private PasswordField Password;

    public LoginController(){

        userList = new CircularLinkedList();

        try {
            userList.add(new User( "ADMIN" , Encriptador.encriptar("admin123" , 3) , "ADMINISTRADOR"));
            userList.add(new User("User1" , Encriptador.encriptar("user123" , 3) , "USUARIO"));


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @javafx.fxml.FXML
    public void enterOnAction(ActionEvent actionEvent) {

        String enteredUsername = UserName.getText();
        String enteredPassword = Password.getText();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()){
            FXUtility.alert("Empty Fields" , "Please enter your username and password.");
            return;
        }

        try {
            User fondUser = null;
            Node aux = (Node) userList.getFirstNode();

            if (aux == null){

                FXUtility.alert("Login Error" ,"There are no registered users in the system." );

            }

            do{

                User currentUser = (User) aux.getData();

                if (currentUser.getUsername().equals(enteredUsername)){

                    fondUser =  currentUser;
                    break;
                }

                aux=aux.next;

            }while (aux != userList.getFirstNode());

            if (fondUser != null){

                String encryptedEnteredPassword = Encriptador.encriptar(enteredPassword , 3);

                if (fondUser.getPassword().equals(encryptedEnteredPassword)){

                    String userRole = fondUser.getRole();
                    FXUtility.alert("Successful Login" , "Welcome , " + enteredUsername + "!");

                    //Mientras tanto se muestra una alerta despues se muestra la pantalla
                    if (userRole.equalsIgnoreCase("ADMIN")){
                        FXUtility.alert("Welcome" , "Access granted to the administrator.");
                    } else if (userRole.equalsIgnoreCase("User1")) {
                        FXUtility.alert("Welcome" , "Access granted to the user.");

                    }

                    UserName.clear();
                    Password.clear();
                }else {

                    FXUtility.alert("Authentication Error" ,"Incorrect password. Please try again." );

                }
            }else {
                FXUtility.alert("Authentication Error" , "User not found. Please check your username.");
            }

        } catch (Exception e) {

            FXUtility.alert("System Error" , "An unexpected error occurred " + e.getMessage());
        }
    }

}
