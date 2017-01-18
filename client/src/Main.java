import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by michal on 11/17/16.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    static HostServices hostServices;

    @Override
    public void start(Stage primaryStage) {
        Controller.init();
        Main.hostServices = this.getHostServices();

        Scene scene = new Scene(new MainLayout(primaryStage), 800, 600);
        primaryStage.setTitle("Ape v 0.0.1");
        primaryStage.setScene(scene);
        scene.getWindow().setOnCloseRequest(windowEvent -> {
            if (!Controller.shutdown()) {
                windowEvent.consume();
            }
        });
        primaryStage.show();
    }
}
