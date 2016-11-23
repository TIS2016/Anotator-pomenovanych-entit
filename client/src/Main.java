import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Created by michal on 11/17/16.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setScene(new Scene(new Pane()));
        primaryStage.show();
        Button connectTest = new Button("Connection test");
        connectTest.setOnAction(event -> {
            ConnectionDialog conDialog = new ConnectionDialog(primaryStage);
            conDialog.showAndWait();
        });
        Button annotationTest = new Button("Annotation test");
        annotationTest.setOnAction(event -> {
            AnnotationDialog annotDialog = new AnnotationDialog(primaryStage);
            annotDialog.showAndWait();
        });
        Button createCategoryTest = new Button("Add Annotation test");
        createCategoryTest.setOnAction(event -> {
            CreateCategoryDialog createCattegoryDialog = new CreateCategoryDialog(primaryStage);
            createCattegoryDialog.showAndWait();
        });
        Scene scene = new Scene(new Pane(new VBox(5,
        		connectTest,annotationTest,createCategoryTest
        		)), 400, 400);
        primaryStage.setTitle("Testing");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
