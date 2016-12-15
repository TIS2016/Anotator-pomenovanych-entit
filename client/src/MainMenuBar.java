import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by michal on 11/23/16.
 */
public class MainMenuBar extends MenuBar {

    public MainMenuBar(Stage primaryStage, SplitPane masterSplitPane, SplitPane slaveSplitPane) {
        final Menu fileMenu = new Menu("_File");

        final MenuItem fileExport = new MenuItem("_Export");
        fileExport.disableProperty().bind(User.isConnected.not());
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

        final MenuItem connNew = new MenuItem("_New connection");
        connNew.setOnAction(actionEvent -> {
            ConnectionDialog connectionDialog = new ConnectionDialog(primaryStage);
            actionEvent.consume();
            connectionDialog.showAndWait();
        });


        final MenuItem connDisconnect = new MenuItem("_Disconnect");
        connDisconnect.disableProperty().bind(User.isConnected.not());
        connDisconnect.setOnAction(actionEvent -> {
            System.out.println("CONNECTION DISCONNECT -- BIND ME");
            actionEvent.consume();
        });

        final MenuItem connOptions = new MenuItem("_Options");
        connOptions.setOnAction(actionEvent -> {
            System.out.println("CONNECTION OPTIONS -- BIND ME");
            actionEvent.consume();
        });

        connMenu.getItems().setAll(connNew, connDisconnect, new SeparatorMenuItem(), connOptions);

        final Menu sessionMenu = new Menu("_Session");
        sessionMenu.disableProperty().bind(User.isConnected.not());

        final MenuItem sessNew = new MenuItem("_New session");
        sessNew.setOnAction(actionEvent -> {
            GroupCreateDialog createGroupDialog= new GroupCreateDialog(primaryStage);
            createGroupDialog.showAndWait();
            actionEvent.consume();
            //System.out.println("NEW SESSION -- BIND ME");
            //actionEvent.consume();
        });
        final MenuItem sessJoin = new MenuItem("_Join session");
        sessJoin.setOnAction(actionEvent -> {
            GroupsDialog groupsDialog = new GroupsDialog(primaryStage);
            groupsDialog.showAndWait();


            //System.out.println("SESSION JOIN -- BIND ME");
            //actionEvent.consume();
        });
        final MenuItem sessActive = new MenuItem("_Active session");
        sessActive.disableProperty().bind(User.hasActiveSession.not());
        sessActive.setOnAction(actionEvent -> {
            System.out.println("ACTIVE INFO SESSION -- BIND ME");
            actionEvent.consume();
        });
        final MenuItem sessLeave = new MenuItem("L_eave");
        sessLeave.disableProperty().bind(User.hasActiveSession.not());
        sessLeave.setOnAction(actionEvent -> {
            System.out.println("LEAVE SESSION -- BIND ME");
            actionEvent.consume();
        });

        sessionMenu.getItems().setAll(sessNew, sessJoin,
                new SeparatorMenuItem(), sessActive, sessLeave);

        final Menu logMenu = new Menu("_Log");
        logMenu.disableProperty().bind(User.hasActiveSession.not());

        final MenuItem logExport = new MenuItem("_Export log");
        logExport.setOnAction(actionEvent -> {
            System.out.println("EXPORT LOG -- BIND ME");
            actionEvent.consume();
        });
        final MenuItem logClear = new MenuItem("_Clear log");
        logClear.setOnAction(actionEvent -> {
            System.out.println("CLEAR LOG -- BIND ME");
            actionEvent.consume();
        });
        final Menu logDisplayOptions = new Menu("P_osition");

        final ToggleGroup logToggleGroup = new ToggleGroup();

        final RadioMenuItem logDisplayTab = new RadioMenuItem("_Tab");
        logDisplayTab.setToggleGroup(logToggleGroup);

        final RadioMenuItem logDisplayWindow = new RadioMenuItem("_Window");
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
                    //lastTabDividerPosition = slaveSplitPane.getDividerPositions()[0];
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

        final Menu anotMenu = new Menu("_Anotations");
        anotMenu.disableProperty().bind(User.hasActiveSession.not());

        final MenuItem anotNew = new MenuItem("_New category");
        anotNew.setOnAction(actionEvent -> {
            CategoryDialog categoryDialog = new CategoryDialog(primaryStage);
            categoryDialog.showAndWait();
            actionEvent.consume();
        });

        final MenuItem anotTest = new MenuItem("_Add anotation");
        anotTest.setOnAction(actionEvent -> {
            AnnotationDialog annotationDialog = new AnnotationDialog(primaryStage);
            annotationDialog.showAndWait();
            actionEvent.consume();
        });

        final Menu treeDisplayOptions = new Menu("_Tree position");

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

        anotMenu.getItems().setAll(anotNew, anotTest, new SeparatorMenuItem(), treeDisplayOptions);

        this.getMenus().setAll(fileMenu, connMenu, sessionMenu, anotMenu, logMenu);
    }
}
