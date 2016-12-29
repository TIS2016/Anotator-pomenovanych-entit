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

    public static HostServices hostServices;

    @Override
    public void start(Stage primaryStage) {
        Main.hostServices = this.getHostServices();
        Scene scene = new Scene(new MainLayout(primaryStage), 800, 600);
        primaryStage.setTitle("Testing...");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
