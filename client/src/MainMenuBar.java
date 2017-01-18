import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by michal on 11/23/16.
 */
public class MainMenuBar extends MenuBar {

    public MainMenuBar(Stage primaryStage,
                       SplitPane masterSplitPane,
                       SplitPane slaveSplitPane) {
        final Menu fileMenu = new Menu("_File");

        final MenuItem fileExport = new MenuItem("_Export - NYI");
        fileExport.disableProperty().bind(SessionData.isConnected.not());
        fileExport.setOnAction(actionEvent -> {
            System.out.println("FILE EXPORT -- BIND ME");
            actionEvent.consume();
        });

        final MenuItem fileExit = new MenuItem("E_xit");
        fileExit.setOnAction(actionEvent -> {
            if (Controller.shutdown()) {
                Platform.exit();
            }
            actionEvent.consume();
        });

        fileMenu.getItems().setAll(fileExport, new SeparatorMenuItem(), fileExit);

        final Menu connMenu = new Menu("_Connection");

        final MenuItem connNew = new MenuItem("_New Connection");
        connNew.setOnAction(actionEvent -> {
            ConnectionDialog connectionDialog = new ConnectionDialog(primaryStage);
            actionEvent.consume();
            connectionDialog.showAndWait();
        });


        final MenuItem connDisconnect = new MenuItem("_Disconnect - NYI");
        connDisconnect.disableProperty().bind(SessionData.isConnected.not());
        connDisconnect.setOnAction(actionEvent -> {
            System.out.println("CONNECTION DISCONNECT -- BIND ME");
            actionEvent.consume();
        });

        connMenu.getItems().setAll(connNew, connDisconnect);

        final Menu projectionMenu = new Menu("_Projects");
        projectionMenu.disableProperty().bind(SessionData.isConnected.not());

        final MenuItem projectNew = new MenuItem("_New Project");
        projectNew.setOnAction(actionEvent -> {
            ProjectDialog projectDialog= new ProjectDialog(primaryStage);
            projectDialog.showAndWait();
            actionEvent.consume();
        });
        final MenuItem projectJoin = new MenuItem("_Search Projects");
        projectJoin.setOnAction(actionEvent -> {
            new ProjectSearchDialog(primaryStage).showAndWait();
            actionEvent.consume();
        });
        final MenuItem projectActive = new MenuItem("_Active Project - NYI");
        projectActive.disableProperty().bind(SessionData.hasActiveSession.not());
        projectActive.setOnAction(actionEvent -> {
            System.out.println("ACTIVE INFO SESSION -- BIND ME");
            actionEvent.consume();
        });
        final MenuItem projectLeave = new MenuItem("L_eave Project - NYI");
        projectLeave.disableProperty().bind(SessionData.hasActiveSession.not());
        projectLeave.setOnAction(actionEvent -> {
            System.out.println("LEAVE SESSION -- BIND ME");
            actionEvent.consume();
        });

        projectionMenu.getItems().setAll(projectNew, projectJoin,
                new SeparatorMenuItem(), projectActive, projectLeave);

        final TextArea logArea = new TextArea("TODO: log");
        logArea.setEditable(false);
        final MenuItem exportLog = new MenuItem("Export - NYI");
        exportLog.setOnAction(actionEvent -> {
            System.out.println("EXPORT LOG -- BIND ME");
            actionEvent.consume();
        });
        final MenuItem clearLog = new MenuItem("Clear - NYI");
        clearLog.setOnAction(actionEvent -> {
            System.out.println("CLEAR LOG -- BIND ME");
            actionEvent.consume();
        });
        logArea.setContextMenu(new ContextMenu(clearLog, new SeparatorMenuItem(), exportLog));

        final Menu logMenu = new Menu("_Log");
        logMenu.disableProperty().bind(SessionData.hasActiveSession.not());

        final MenuItem logExport = new MenuItem("_Export Log - NYI");
        logExport.setOnAction(actionEvent -> {
            System.out.println("EXPORT LOG -- BIND ME");
            actionEvent.consume();
        });
        final MenuItem logClear = new MenuItem("_Clear Log - NYI");
        logClear.setOnAction(actionEvent -> {
            System.out.println("CLEAR LOG -- BIND ME");
            actionEvent.consume();
        });

        final Menu logDisplayOptions = new Menu("Log Po_sition");
        final ToggleGroup logToggleGroup = new ToggleGroup();

        final RadioMenuItem logDisplayTab = new RadioMenuItem("_Tab");
        logDisplayTab.setToggleGroup(logToggleGroup);
        logDisplayTab.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                slaveSplitPane.getItems().add(logArea);
                slaveSplitPane.setDividerPosition(0, 0.8);
            } else {
                slaveSplitPane.getItems().remove(logArea);
            }
        }));

        final RadioMenuItem logDisplayNone = new RadioMenuItem("Non_e");
        logDisplayNone.setToggleGroup(logToggleGroup);

        final RadioMenuItem logDisplayWindow = new RadioMenuItem("_Window");
        logDisplayWindow.setToggleGroup(logToggleGroup);

        final Stage logWindow = new Stage();
        logWindow.setTitle("Log");
        logWindow.setOnCloseRequest(windowEvent -> {
            if (logToggleGroup.getSelectedToggle() == logDisplayWindow) {
                logToggleGroup.selectToggle(logDisplayNone);
            }
        });
        logArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE && logWindow.isShowing()) {
                logWindow.close();
            }
            keyEvent.consume();
        });

        logDisplayWindow.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                Pane root = new Pane(logArea);
                logArea.prefWidthProperty().bind(root.widthProperty());
                logArea.prefHeightProperty().bind(root.heightProperty());
                logWindow.setScene(new Scene(root, 600, 200));
                logWindow.show();
            } else {
                logArea.prefWidthProperty().unbind();
                logArea.prefHeightProperty().unbind();
                logWindow.close();
            }
        }));

        logToggleGroup.selectToggle(logDisplayTab);

        logDisplayOptions.getItems().setAll(logDisplayTab, logDisplayWindow, new SeparatorMenuItem(), logDisplayNone);

        logMenu.getItems().setAll(logExport, logClear, new SeparatorMenuItem(), logDisplayOptions);

        final Menu annotMenu = new Menu("_Annotations");
        annotMenu.disableProperty().bind(SessionData.hasActiveSession.not());

        final MenuItem cateNew = new MenuItem("New _Category");
        cateNew.setOnAction(actionEvent -> {
            CategoryDialog categoryDialog = new CategoryDialog(primaryStage);
            categoryDialog.showAndWait();
            actionEvent.consume();
        });

        final MenuItem annotNew = new MenuItem("New _Annotation");
        final ObservableValue<IndexRange> selectedProperty = MainLayout.textArea.selectionProperty();
        annotNew.disableProperty().bind(Bindings.createBooleanBinding(
                () -> selectedProperty.getValue().getLength() == 0,
                selectedProperty));
        annotNew.setOnAction(actionEvent -> {
            new AnnotationDialog(primaryStage).showAndWait();
            actionEvent.consume();
        });

        final MenuItem refNew = new MenuItem("New _Reference");
        refNew.disableProperty().bind(Bindings.createBooleanBinding(
                () -> selectedProperty.getValue().getLength() == 0,
                selectedProperty));
        refNew.setOnAction(actionEvent -> {
            new ReferenceDialog(primaryStage).showAndWait();
            actionEvent.consume();
        });

        final Menu treeDisplayOptions = new Menu("Tree Po_sition");

        final ToggleGroup treeToggleGroup = new ToggleGroup();

        final RadioMenuItem treeDisplayLeft = new RadioMenuItem("L_eft");
        treeDisplayLeft.setToggleGroup(treeToggleGroup);

        final RadioMenuItem treeDisplayRight = new RadioMenuItem("_Right");
        treeDisplayRight.setToggleGroup(treeToggleGroup);

        treeDisplayOptions.getItems().addAll(treeDisplayLeft, treeDisplayRight);
        treeToggleGroup.selectToggle(treeDisplayRight);

        treeDisplayLeft.setOnAction(actionEvent -> {
            VBox treeBox = (VBox) masterSplitPane.getItems().get(1);
            masterSplitPane.getItems().remove(treeBox);
            masterSplitPane.getItems().add(0, treeBox);
            masterSplitPane.setDividerPosition(0, 0.3);
        });

        treeDisplayRight.setOnAction(actionEvent -> {
            VBox treeBox = (VBox) masterSplitPane.getItems().get(0);
            masterSplitPane.getItems().remove(treeBox);
            masterSplitPane.getItems().add(treeBox);
            masterSplitPane.setDividerPosition(0, 0.7);
        });

        final Menu toolsMenu = new Menu("_Tools");
        final MenuItem settingsMenuItem = new MenuItem("_Settings");
        settingsMenuItem.setOnAction(actionEvent -> {
            Alert nyi = new Alert(Alert.AlertType.WARNING);
            nyi.setContentText("TODO: options");
            nyi.showAndWait();
            //new SettingsDialog(primaryStage).showAndWait();
            actionEvent.consume();
        });
        toolsMenu.getItems().addAll(settingsMenuItem);

        annotMenu.getItems().setAll(cateNew, annotNew, refNew, new SeparatorMenuItem(), treeDisplayOptions);

        this.getMenus().setAll(fileMenu, connMenu, projectionMenu, annotMenu, logMenu, toolsMenu);
    }
}
