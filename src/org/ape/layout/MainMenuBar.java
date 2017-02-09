package org.ape.layout;

import javafx.collections.FXCollections;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.ape.control.Controller;
import org.ape.AppData;
import org.ape.layout.dialogs.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainMenuBar extends MenuBar {

    MainMenuBar(SplitPane masterSplitPane,
                       SplitPane slaveSplitPane) {
        final Menu fileMenu = new Menu("_File");

        final MenuItem fileNew = new MenuItem("_New Project");
        fileNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        fileNew.setOnAction(actionEvent -> {
            Controller.newProject();
            actionEvent.consume();
        });

        final MenuItem fileOpen = new MenuItem("_Open Project");
        fileOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        fileOpen.setOnAction(actionEvent -> {
            Controller.openProject();
            actionEvent.consume();
        });

        final MenuItem fileSave = new MenuItem("_Save Project");
        fileSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        fileSave.setOnAction(actionEvent -> {
            Controller.saveProject();
            actionEvent.consume();
        });
        fileSave.disableProperty().bind(AppData.isActiveProject.not());

        final MenuItem fileSaveAs = new MenuItem("S_ave Project As");
        fileSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
        fileSaveAs.setOnAction(actionEvent -> {
            Controller.saveProjectAs();
            actionEvent.consume();
        });
        fileSaveAs.disableProperty().bind(AppData.isActiveProject.not());

        final MenuItem fileExport = new MenuItem("_Export Project As");
        fileExport.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
        fileExport.setOnAction(actionEvent -> {
            new ExportDialog(AppData.owner).showAndWait();
            actionEvent.consume();
        });
        fileExport.disableProperty().bind(AppData.isActiveProject.not());

        final MenuItem fileExit = new MenuItem("E_xit");
        fileExit.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN));
        fileExit.setOnAction(actionEvent -> {
            if (Controller.canShutdown()) {
                Controller.shutdown();
            }
            actionEvent.consume();
        });

        fileMenu.getItems().setAll(fileNew, fileOpen, new SeparatorMenuItem(),
                                   fileSave, fileSaveAs, new SeparatorMenuItem(),
                                   fileExport, new SeparatorMenuItem(),
                                   fileExit);

        final LogListView logView = AppData.logListView;
        logView.setEditable(false);
        logView.disableProperty().bind(AppData.isActiveProject.not());

        final MenuItem exportLog = new MenuItem("Export log");
        exportLog.setOnAction(actionEvent -> {
            Controller.exportLog();
            actionEvent.consume();
        });

        final MenuItem clearLog = new MenuItem("Clear log");
        clearLog.setOnAction(actionEvent -> {
            AppData.logListView.clearLog();
            actionEvent.consume();
        });

        logView.setContextMenu(new ContextMenu(clearLog, exportLog));

        final Menu logMenu = new Menu("_Log");
        final MenuItem logClear = new MenuItem("_Clear Log");
        logClear.disableProperty().bind(AppData.isActiveProject.not());
        logClear.setOnAction(actionEvent -> {
            AppData.logListView.clearLog();
            actionEvent.consume();
        });
        final MenuItem logExport = new MenuItem("_Export Log");
        logExport.disableProperty().bind(AppData.isActiveProject.not());
        logExport.setOnAction(actionEvent -> {
            Controller.exportLog();
            actionEvent.consume();
        });

        final Menu logDisplayOptions = new Menu("Log Po_sition");
        final ToggleGroup logToggleGroup = new ToggleGroup();

        final RadioMenuItem logDisplayTab = new RadioMenuItem("_Tab");
        logDisplayTab.setToggleGroup(logToggleGroup);
        logDisplayTab.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                slaveSplitPane.getItems().add(logView);
                slaveSplitPane.setDividerPosition(0, 0.8);
            } else {
                slaveSplitPane.getItems().remove(logView);
            }
        }));

        final RadioMenuItem logDisplayNone = new RadioMenuItem("Non_e");
        logDisplayNone.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                AppData.logListView.pause();
            } else {
                AppData.logListView.play();
            }
        }));
        logDisplayNone.setToggleGroup(logToggleGroup);

        final RadioMenuItem logDisplayWindow = new RadioMenuItem("_Window");
        logDisplayWindow.setToggleGroup(logToggleGroup);

        final Stage logWindow = new Stage(StageStyle.UTILITY);
        logWindow.initOwner(AppData.owner);
        logWindow.setTitle("Action Log");
        logWindow.setOnCloseRequest(windowEvent -> {
            if (logToggleGroup.getSelectedToggle() == logDisplayWindow) {
                logToggleGroup.selectToggle(logDisplayNone);
            }
        });
        logView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE && logWindow.isShowing()) {
                logWindow.close();
            }
            keyEvent.consume();
        });

        logDisplayWindow.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                final Pane root = new Pane(logView);
                logView.prefWidthProperty().bind(root.widthProperty());
                logView.prefHeightProperty().bind(root.heightProperty());
                logWindow.setScene(new Scene(root, 600, 200));
                logWindow.show();
            } else {
                logView.prefWidthProperty().unbind();
                logView.prefHeightProperty().unbind();
                logWindow.close();
            }
        }));

        logToggleGroup.selectedToggleProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == logDisplayTab) {
                Controller.setLogPosProperty("tab");
            } else if (newValue == logDisplayWindow) {
                Controller.setLogPosProperty("window");
            } else if (newValue == logDisplayNone) {
                Controller.setLogPosProperty("none");
            }
        }));

        final String position = Controller.getLogPos();
        logToggleGroup.selectToggle(position.compareTo("tab") == 0 ? logDisplayTab :
                                    position.compareTo("window") == 0 ? logDisplayWindow :
                                    logDisplayNone);

        logDisplayOptions.getItems().setAll(logDisplayTab, logDisplayWindow, new SeparatorMenuItem(), logDisplayNone);
        logMenu.getItems().setAll(logClear, logExport, new SeparatorMenuItem(), logDisplayOptions);

        final Menu annotMenu = new Menu("A_nnotations");
        annotMenu.disableProperty().bind(AppData.isActiveProject.not());

        final MenuItem newCategory = new MenuItem("New _Category");
        newCategory.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN));
        newCategory.setOnAction(actionEvent -> {
            //to prevent alt+c when project in not active, bindigs were not working
            if (AppData.isActiveProject.get()) {
                new CategoryDialog(AppData.owner, null).showAndWait();
            }
            actionEvent.consume();
        });

        final MenuItem newAnnotation = new MenuItem("New _Annotation");
        newAnnotation.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN));
        newAnnotation.setOnAction(actionEvent -> {
            if (Controller.shouldAdjustSelection()) {
                Controller.adjustSelection();
            }
            if (AppData.textArea.getSelection().getLength() == 0) {
                actionEvent.consume();
                return;
            }
            new AnnotationDialog(AppData.owner, null).showAndWait();
            actionEvent.consume();
        });

        final MenuItem newCoreference = new MenuItem("New Co_reference");
        newCoreference.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN));
        newCoreference.setOnAction(actionEvent -> {
            if (Controller.shouldAdjustSelection()) {
                Controller.adjustSelection();
            }
            if (AppData.textArea.getSelection().getLength() == 0) {
                actionEvent.consume();
                return;
            }
            new CoreferenceDialog(AppData.owner, null).showAndWait();
            actionEvent.consume();
        });

        final Menu toolsMenu = new Menu("_Tools");

        final MenuItem optionsMenuItem = new MenuItem("_Options");
        optionsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN));
        optionsMenuItem.setOnAction(actionEvent -> {
            new SettingsDialog(AppData.owner).showAndWait();
            actionEvent.consume();
        });

        final Menu treeDisplayOptions = new Menu("Tree Po_sition");
        final ToggleGroup treeToggleGroup = new ToggleGroup();

        final RadioMenuItem treeDisplayLeftBar = new RadioMenuItem("L_eft");
        treeDisplayLeftBar.setToggleGroup(treeToggleGroup);
        final RadioMenuItem treeDisplayRightBar = new RadioMenuItem("_Right");
        treeDisplayRightBar.setToggleGroup(treeToggleGroup);

        treeToggleGroup.selectedToggleProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == null) {
                double[] positions = masterSplitPane.getDividerPositions();
                FXCollections.reverse(masterSplitPane.getItems());
                masterSplitPane.setDividerPosition(0, 1 - positions[0]);
                Controller.setTreePos(oldValue == treeDisplayLeftBar ? "right" : "left");
            }
        }));

        treeDisplayOptions.getItems().setAll(treeDisplayLeftBar, treeDisplayRightBar);
        treeToggleGroup.selectToggle(Controller.getTreePos().compareTo("left") == 0 ? treeDisplayLeftBar : treeDisplayRightBar);
        {
            final Menu treePosition = (Menu) AppData.tree.getContextMenu().getItems().get(2);
            final RadioMenuItem treeDisplayLeftAnnotTree = (RadioMenuItem) treePosition.getItems().get(0);
            final RadioMenuItem treeDislayRightAnnotTree = (RadioMenuItem) treePosition.getItems().get(1);

            treeDisplayLeftAnnotTree.selectedProperty().bindBidirectional(treeDisplayLeftBar.selectedProperty());
            treeDislayRightAnnotTree.selectedProperty().bindBidirectional(treeDisplayRightBar.selectedProperty());
        }

        toolsMenu.getItems().setAll(optionsMenuItem, new SeparatorMenuItem(), treeDisplayOptions);
        annotMenu.getItems().setAll(newCategory, newAnnotation, newCoreference);

        this.getMenus().setAll(fileMenu, annotMenu, logMenu, toolsMenu);
    }
}