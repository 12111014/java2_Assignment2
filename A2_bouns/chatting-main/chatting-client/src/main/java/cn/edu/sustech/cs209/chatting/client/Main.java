package cn.edu.sustech.cs209.chatting.client;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Optional;


public class Main extends Application {
    final int servePort = 8765;
    final String host = "localhost";
    public static User user;
    public static Controller controller;
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        try{
            Socket socket = new Socket(host,servePort);
            Socket socketForAllOnlineClient = new Socket(host,servePort);
            Socket socketForMessage = new Socket(host,servePort);
            user = new User(socket,socketForAllOnlineClient,socketForMessage);
        }catch (IOException e){
            Alert serveOut = new Alert(Alert.AlertType.ERROR);
            serveOut.setTitle("Error");
            serveOut.setHeaderText(null);
            serveOut.setContentText("The Server has Log out");
            serveOut.showAndWait();

            Platform.exit();
            System.exit(0);
            throw new RuntimeException(e);
        }


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();

        controller = fxmlLoader.getController();
        user.setMainController(controller);

        Platform.setImplicitExit(false);

        stage.setOnCloseRequest(event -> {
            System.out.print("try Stage close");
            try {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Checking Exit");
                alert.setHeaderText(null);
                alert.setContentText("Do you want to Exit?");
                Optional<ButtonType> exitResult = alert.showAndWait();
                if(exitResult.get() == ButtonType.OK){
                    user.logout();
                    Platform.exit();
                    System.exit(0);
                }
                else {
                    event.consume();
                    stage.show();
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static User getUser(){
        return user;
    }
}

