import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyledTextArea;

/**
 * Created by michal on 11/23/16.
 */
public class MainMenuBar extends MenuBar {

    public MainMenuBar(Stage primaryStage, SplitPane masterSplitPane, SplitPane slaveSplitPane) {
        final Menu fileMenu = new Menu("_File");

        final MenuItem fileExport = new MenuItem("_Export - NYI");
        fileExport.disableProperty().bind(SessionData.isConnected.not());
        fileExport.setOnAction(actionEvent -> {
            System.out.println("FILE EXPORT -- BIND ME");
            actionEvent.consume();
        });

        final MenuItem fileExit = new MenuItem("E_xit");
        fileExit.setOnAction(actionEvent -> {
            Platform.exit();
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

        final MenuItem connOptions = new MenuItem("_Options - NYI");
        connOptions.setOnAction(actionEvent -> {
            System.out.println("CONNECTION OPTIONS -- BIND ME");
            actionEvent.consume();
        });

        connMenu.getItems().setAll(connNew, connDisconnect, new SeparatorMenuItem(), connOptions);

        final Menu projectionMenu = new Menu("_Projects");
        projectionMenu.disableProperty().bind(SessionData.isConnected.not());

        final MenuItem projectNew = new MenuItem("_New Project");
        projectNew.setOnAction(actionEvent -> {
            ProjectDialog projectDialog= new ProjectDialog(primaryStage);
            projectDialog.showAndWait();
            actionEvent.consume();
            //System.out.println("NEW SESSION -- BIND ME");
            //actionEvent.consume();
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

        final RadioMenuItem logDisplayWindow = new RadioMenuItem("_Window - NYI");
        logDisplayWindow.setToggleGroup(logToggleGroup);

        final RadioMenuItem logDisplayNone = new RadioMenuItem("Non_e");
        logDisplayNone.setToggleGroup(logToggleGroup);

        logToggleGroup.selectToggle(logDisplayTab);
        logToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            //TODO: rewrite using listeners on logDisplay....
            if (oldValue != null) {
                if (oldValue == logDisplayTab) {
                    TextArea toRemove = (TextArea) slaveSplitPane.getItems().get(1);
                    logDisplayTab.setUserData(toRemove);
                    slaveSplitPane.getItems().remove(toRemove);
                } else if (oldValue == logDisplayWindow) {
                    //TODO
                } else {
                    //TODO
                }
            }
            if (newValue != null) {
                if (newValue == logDisplayTab) {
                    slaveSplitPane.getItems().add((TextArea) logDisplayTab.getUserData());
                    slaveSplitPane.setDividerPosition(0, 0.8);
                } else if (oldValue == logDisplayWindow) {
                    //TODO
                } else {
                    //TODO
                }
                System.out.println("LOG DISPLAY VALUE CHANGED -- BIND ME");
            }
        });

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
        //TODO: clean this
        final StyledTextArea<Void, DisplayedTreeObject<?>> textArea =
                ((VirtualizedScrollPane<StyledTextArea<Void, DisplayedTreeObject<?>>>)
                ((SplitPane) masterSplitPane.getItems().get(0)).getItems().get(0)).getContent();
        final ObservableValue<IndexRange> selectedProperty = textArea.selectionProperty();
        annotNew.disableProperty().bind(Bindings.createBooleanBinding(
                () -> selectedProperty.getValue().getLength() == 0,
                selectedProperty));
        annotNew.setOnAction(actionEvent -> {
            new AnnotationDialog(primaryStage, textArea).showAndWait();
            textArea.deselect();
            actionEvent.consume();
        });

        final MenuItem refNew = new MenuItem("New _Reference");
        refNew.disableProperty().bind(Bindings.createBooleanBinding(
                () -> selectedProperty.getValue().getLength() == 0,
                selectedProperty));
        refNew.setOnAction(actionEvent -> {
            new ReferenceDialog(primaryStage, textArea).showAndWait();
            textArea.deselect();
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

        annotMenu.getItems().setAll(cateNew, annotNew, refNew, new SeparatorMenuItem(), treeDisplayOptions);

        this.getMenus().setAll(fileMenu, connMenu, projectionMenu, annotMenu, logMenu);
    }
}
