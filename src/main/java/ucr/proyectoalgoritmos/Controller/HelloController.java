package ucr.proyectoalgoritmos.Controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import ucr.proyectoalgoritmos.Controller.UserController.UserMenuController;

public class HelloController implements Initializable, LoginListener {

    @FXML private VBox mainContentHost;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("HelloController inicializado correctamente.");
    }

    @FXML
    private void handleStartAction(ActionEvent event) {
        loadLoginScreen();
    }

    private void loadLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/Login.fxml"));
            VBox loginRoot = loader.load();
            LoginController loginController = loader.getController();
            loginController.setLoginListener(this);

            mainContentHost.getChildren().setAll(loginRoot);
            mainContentHost.setPrefHeight(loginRoot.getPrefHeight());
            mainContentHost.setPrefWidth(loginRoot.getPrefWidth());

        } catch (IOException e) {
            System.err.println("Error al cargar la pantalla de login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginSuccess(String userRole) {
        System.out.println("Login exitoso. Rol: " + userRole);
        try {
            FXMLLoader loader;
            if ("ADMINISTRADOR".equalsIgnoreCase(userRole)) {
                loader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/admin-menubar.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/user-menubar.fxml"));
            }
            VBox menuRoot = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AdminMenuController) {
                ((AdminMenuController) controller).setHelloController(this);
            } else if (controller instanceof UserMenuController) {
                ((UserMenuController) controller).setHelloController(this);
            }

            mainContentHost.getChildren().setAll(menuRoot);
            mainContentHost.setPrefHeight(menuRoot.getPrefHeight());
            mainContentHost.setPrefWidth(menuRoot.getPrefWidth());

        } catch (IOException e) {
            System.err.println("Error al cargar el menú después del login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onLoginCanceled() {
        System.out.println("Login cancelado. Regresando a la pantalla de bienvenida.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ucr/proyectoalgoritmos/hello-view.fxml"));
            AnchorPane fullRoot = loader.load();

            VBox welcomeContentVBox = (VBox) fullRoot.lookup("#mainContentHost");

            mainContentHost.getChildren().setAll(welcomeContentVBox.getChildren());

            Button btStart = (Button) mainContentHost.lookup("#btStart");

            if (btStart != null) {
                btStart.setOnAction(this::handleStartAction);
            }

            mainContentHost.setPrefHeight(welcomeContentVBox.getPrefHeight());
            mainContentHost.setPrefWidth(welcomeContentVBox.getPrefWidth());

        } catch (IOException e) {
            System.err.println("Error al regresar a la pantalla de bienvenida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void logout() {
        System.out.println("Cerrando sesión...");
        loadLoginScreen();
    }

    @FXML
    private void exitApplication(ActionEvent event) {
        System.out.println("Cerrando la aplicación por completo...");
        Platform.exit();
        System.exit(0);
    }

    public void loadContentIntoHost(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            mainContentHost.getChildren().setAll(content);

            if (content instanceof VBox) {
                mainContentHost.setPrefHeight(((VBox) content).getPrefHeight());
                mainContentHost.setPrefWidth(((VBox) content).getPrefWidth());
            } else if (content instanceof AnchorPane) {
                mainContentHost.setPrefHeight(((AnchorPane) content).getPrefHeight());
                mainContentHost.setPrefWidth(((AnchorPane) content).getPrefWidth());
            }

            Object controller = loader.getController();
            if (controller instanceof UserMenuController) {
                ((UserMenuController) controller).setHelloController(this);
            } else if (controller instanceof AdminMenuController) {
                ((AdminMenuController) controller).setHelloController(this);
            }

        } catch (IOException e) {
            System.err.println("Error al cargar contenido en el host desde: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadViewInNewStage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloController.class.getResource(fxmlPath));
            Parent view = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(view));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}