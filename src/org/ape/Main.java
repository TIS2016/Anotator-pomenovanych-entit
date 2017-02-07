package org.ape;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.ape.control.Controller;
import org.ape.layout.MainLayout;
import org.ape.layout.dialogs.FixedExceptionDialog;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            AppData.owner = primaryStage;
            Controller.init(this.getHostServices());
            Scene scene = new Scene(new MainLayout(), Controller.getWindowWidth(), Controller.getWindowHeight());
            primaryStage.setTitle("Ape v1.0.0");
            primaryStage.setScene(scene);
            scene.getWindow().setOnCloseRequest(windowEvent -> {
                if (Controller.canShutdown()) {
                    Controller.shutdown();
                } else {
                    windowEvent.consume();
                }
            });
            primaryStage.show();
        } catch (Exception e) {
            new FixedExceptionDialog(e, "This should not happen...").showAndWait();
        }
    }
}
