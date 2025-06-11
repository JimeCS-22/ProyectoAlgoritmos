package ucr.proyectoalgoritmos.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import ucr.proyectoalgoritmos.Domain.Circular.CircularLinkedList;
import ucr.proyectoalgoritmos.Domain.Circular.Node;
import ucr.proyectoalgoritmos.Domain.Encriptador;
import ucr.proyectoalgoritmos.Domain.User;
import ucr.proyectoalgoritmos.util.FXUtility;

public class LoginController {
    @FXML
    private TextField UserName;
    @FXML
    private Button Enter;
    @FXML
    private PasswordField Password;

    private CircularLinkedList userList;

    public LoginController() {
        userList = new CircularLinkedList();

        try {
            userList.add(new User("ADMIN", Encriptador.encriptar("admin123", 3), "ADMINISTRADOR"));
            userList.add(new User("User1", Encriptador.encriptar("user123", 3), "USUARIO"));
        } catch (Exception e) {
            System.err.println("Error initializing user list: " + e.getMessage());
            FXUtility.alert("Initialization Error", "There was a problem loading system users.");
        }
    }

    @FXML
    public void enterOnAction(ActionEvent actionEvent) {
        String enteredUsername = UserName.getText().trim();
        String enteredPassword = Password.getText().trim();

        if (enteredUsername.isEmpty() || enteredPassword.isEmpty()) {
            FXUtility.alert("Empty Fields", "Please enter your username and password.");
            return;
        }

        try {
            User foundUser = null;
            Node aux = (Node) userList.getFirstNode();

            if (aux == null) {
                FXUtility.alert("Login Error", "There are no registered users in the system.");
                return;
            }

            do {
                User currentUser = (User) aux.getData();

                if (currentUser.getUsername().equals(enteredUsername)) {
                    foundUser = currentUser;
                    break;
                }
                aux = aux.next;
            } while (aux != userList.getFirstNode());

            if (foundUser != null) {
                String encryptedEnteredPassword = Encriptador.encriptar(enteredPassword, 3);

                if (foundUser.getPassword().equals(encryptedEnteredPassword)) {

                    String userRole = foundUser.getRole();
                    FXUtility.alert("Successful Login", "Welcome, " + enteredUsername + "!");

                    if (userRole.equalsIgnoreCase("ADMINISTRADOR")) {
                        FXUtility.alert("Welcome", "Access granted to the administrator.");

                    } else if (userRole.equalsIgnoreCase("USUARIO")) {
                        FXUtility.alert("Welcome", "Access granted to the user.");
                        
                    }

                    UserName.clear();
                    Password.clear();

                } else {
                    FXUtility.alert("Authentication Error", "Incorrect password. Please try again.");
                }
            } else {
                FXUtility.alert("Authentication Error", "User not found. Please check your username.");
            }

        } catch (Exception e) {
            FXUtility.alert("System Error", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

}