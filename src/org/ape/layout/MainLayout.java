package org.ape.layout;

import javafx.geometry.Insets;
import org.ape.annotations.TreeObjectItem;
import org.ape.annotations.TreePredicate;
import org.ape.annotations.treeObjects.AnnotationObject;
import org.ape.annotations.ColorObject;
import org.ape.annotations.treeObjects.CoreferenceObject;
import org.ape.annotations.treeObjects.DisplayedTreeObject;
import org.ape.annotations.treeObjects.TreeObject;
import org.ape.control.Controller;
import org.ape.AppData;
import org.ape.layout.dialogs.AnnotationDialog;
import org.ape.layout.dialogs.CategoryDialog;
import org.ape.layout.dialogs.CoreferenceDialog;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import org.fxmisc.richtext.*;
import org.fxmisc.richtext.model.TwoDimensional;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;


import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static java.time.Duration.ofMillis;

public class MainLayout extends VBox {

    public MainLayout() {
        super();

        final StyledTextArea<Void, DisplayedTreeObject<?>> textArea = AppData.textArea;

        final SplitPane masterSplitPane = new SplitPane();
        masterSplitPane.prefHeightProperty().bind(this.heightProperty());

        final SplitPane slaveSplitPane = new SplitPane();

        //see org.ape.layout.VirtualizedScrollPane2 source file for explanation
        VirtualizedScrollPane2<StyledTextArea> scrollPane = new VirtualizedScrollPane2<>(textArea);
        slaveSplitPane.setOrientation(Orientation.VERTICAL);
        slaveSplitPane.getItems().addAll(scrollPane);

        final VBox treeBox = new VBox();
        treeBox.disableProperty().bind(AppData.isActiveProject.not());

        final TextField treeFilter = new TextField();
        treeFilter.setPromptText("Search");

        AppData.tree.rootProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                treeFilter.setText("");
                ((TreeObjectItem<TreeObject<?>>) newValue)
                        .predicateProperty()
                        .bind(Bindings.createObjectBinding(
                                () -> treeFilter.getText().trim().isEmpty() ? null :
                                        TreePredicate.create(
                                                child -> child.getTreeName().contains(treeFilter.getText().trim())
                                        )
                                , treeFilter.textProperty())
                        );
            }
        }));

        VBox.setVgrow(AppData.tree, Priority.ALWAYS);

        treeBox.getChildren().addAll(treeFilter, AppData.tree);

        masterSplitPane.setOrientation(Orientation.HORIZONTAL);
        if (Controller.getTreePos().compareTo("left") == 0) {
            masterSplitPane.setDividerPositions(0.3);
            masterSplitPane.getItems().addAll(treeBox, slaveSplitPane);
        } else {
            masterSplitPane.setDividerPositions(0.7);
            masterSplitPane.getItems().addAll(slaveSplitPane, treeBox);
        }

        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(actionEvent -> {
            DisplayedTreeObject treeObject = (DisplayedTreeObject) contextMenu.getUserData();
            if (treeObject instanceof AnnotationObject) {
                new AnnotationDialog(AppData.owner, (AnnotationObject) treeObject).showAndWait();
            } else if (treeObject instanceof CoreferenceObject) {
                new CoreferenceDialog(AppData.owner, (CoreferenceObject) treeObject).showAndWait();
            }
            actionEvent.consume();
        });

        final MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            Controller.deleteTreeObject((DisplayedTreeObject) contextMenu.getUserData());
            actionEvent.consume();
        });

        final SeparatorMenuItem annotSeparator1 = new SeparatorMenuItem();
        MenuItem referToThisMenuItem = new MenuItem("Refer to this");

        referToThisMenuItem.setOnAction(actionEvent -> {
            AnnotationObject annotationObject = (AnnotationObject) contextMenu.getUserData();
            if (Controller.shouldAdjustSelection()) {
                Controller.adjustSelection();
                if (textArea.getSelection().getLength() == 0) {
                    actionEvent.consume();
                    return;
                }
            }
            new CoreferenceObject(AppData.id++, annotationObject);
            actionEvent.consume();
        });

        final SimpleObjectProperty<AnnotationObject> anchorProperty = new SimpleObjectProperty<>();

        final SeparatorMenuItem annotSeparator2 = new SeparatorMenuItem();
        final MenuItem anchorMenuItem = new MenuItem();
        anchorMenuItem.textProperty().bind(
                Bindings.when(AppData.anchorAnnotation.isEqualTo(anchorProperty))
                        .then("Unset Anchor")
                        .otherwise("Set Anchor"));
        anchorMenuItem.setOnAction(actionEvent -> {
            AppData.anchorAnnotation.set((AnnotationObject) contextMenu.getUserData());
            actionEvent.consume();
        });

        textArea.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                final CharacterHit hit = textArea.hit(mouseEvent.getX(), mouseEvent.getY());
                final ColorObject colorObject = AppData.colorObjects.get(hit.getCharacterIndex().orElse(-1));
                if (colorObject != null && colorObject.getLastVisibleDislayedTreeObject() != null) {
                    DisplayedTreeObject last = colorObject.getLastVisibleDislayedTreeObject();
                    contextMenu.setUserData(last);
                    if (last instanceof AnnotationObject) {
                        anchorProperty.set((AnnotationObject) last);
                        contextMenu.getItems().setAll(
                                referToThisMenuItem, annotSeparator1,
                                anchorMenuItem, annotSeparator2,
                                updateMenuItem, deleteMenuItem);
                    } else if (last instanceof CoreferenceObject) {
                        contextMenu.getItems().setAll(updateMenuItem, deleteMenuItem);
                    }
                    contextMenu.show(textArea, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
            }
            mouseEvent.consume();
        });

        Nodes.addInputMap(textArea, InputMap.consume(
                EventPattern.keyPressed(new KeyCodeCombination(KeyCode.A, KeyCombination.SHIFT_DOWN)),
                keyEvent -> {
                    if (Controller.shouldAdjustSelection()) {
                        Controller.adjustSelection();
                    }
                    if (textArea.getSelection().getLength() == 0) {
                        keyEvent.consume();
                        return;
                    }
                    new AnnotationDialog(AppData.owner, null).showAndWait();
                    keyEvent.consume();
                })
        );

        Nodes.addInputMap(textArea, InputMap.consume(
                EventPattern.keyPressed(new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN)),
                keyEvent -> {
                    new CategoryDialog(AppData.owner, null).showAndWait();
                    keyEvent.consume();
                })
        );

        Nodes.addInputMap(textArea, InputMap.consume(
                EventPattern.keyPressed(new KeyCodeCombination(KeyCode.R, KeyCombination.SHIFT_DOWN)),
                keyEvent -> {
                    if (Controller.shouldAdjustSelection()) {
                        Controller.adjustSelection();
                    }
                    if (textArea.getSelection().getLength() == 0) {
                        keyEvent.consume();
                        return;
                    }
                    new CoreferenceDialog(AppData.owner, null).showAndWait();
                    keyEvent.consume();
                })
        );

        //default annotate
        Nodes.addInputMap(textArea, InputMap.consume(
                EventPattern.keyPressed(new KeyCodeCombination(KeyCode.D, KeyCombination.SHIFT_DOWN)).
                        onlyIf(keyEvent -> AppData.defaultCategory.get() != null),
                keyEvent -> {
                    if (Controller.shouldAdjustSelection()) {
                        Controller.adjustSelection();
                    }
                    if (textArea.getSelection().getLength() == 0) {
                        keyEvent.consume();
                        return;
                    }
                    new AnnotationObject(AppData.id++, AppData.defaultCategory.get(), "", new ArrayList<>());
                    AppData.textArea.deselect();
                    keyEvent.consume();
                })
        );

        //refere to anchor
        Nodes.addInputMap(textArea, InputMap.consume(
                EventPattern.keyPressed(new KeyCodeCombination(KeyCode.F, KeyCombination.SHIFT_DOWN))
                        .onlyIf(keyEvent -> AppData.anchorAnnotation.get() != null),
                keyEvent -> {
                    if (Controller.shouldAdjustSelection()) {
                        Controller.adjustSelection();
                    }
                    if (textArea.getSelection().getLength() == 0) {
                        keyEvent.consume();
                        return;
                    }
                    new CoreferenceObject(AppData.id++, AppData.anchorAnnotation.get());
                    AppData.textArea.deselect();
                    keyEvent.consume();
                })
        );

        final Popup annotationPopup = new Popup();
        final Label annotNumber = new Label();
        annotNumber.setStyle("-fx-background-color: black; -fx-text-fill: white; -fx-padding: 5;");
        annotationPopup.getContent().add(annotNumber);
        annotationPopup.setAutoFix(true);

        textArea.setMouseOverTextDelay(ofMillis(666));
        textArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, mouseEvent -> {
            final Point2D position = mouseEvent.getScreenPosition();
            final ColorObject colorObject = AppData.colorObjects.get(mouseEvent.getCharacterIndex());
            if (colorObject != null) {
                AtomicIntegerArray counts = new AtomicIntegerArray(4);
                colorObject.getAllDisplayedTreeObjects().parallelStream().forEach(dto -> {
                    if (dto instanceof AnnotationObject) {
                        if (dto.isSelected())
                            counts.addAndGet(0, 1);
                        counts.addAndGet(1, 1);
                    } else {
                        if (dto.isSelected())
                            counts.addAndGet(2, 1);
                        counts.addAndGet(3, 1);
                    }
                });
                if (colorObject.getLastVisibleDislayedTreeObject() != null) {
                    annotNumber.setFont(new Font(Controller.getFontFamily(), Controller.getFontSize()));
                    annotNumber.setText(String.format("Annotations: [%d/%d]\nCoreferences: [%d/%d]", counts.get(0), counts.get(1), counts.get(2), counts.get(3)));
                    annotationPopup.show(textArea, position.getX(), position.getY());
                }
            }
            mouseEvent.consume();
        });
        textArea.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> {
            if (annotationPopup.isShowing()) {
                annotationPopup.hide();
            }
            mouseEvent.consume();
        });

        final Label caretPosition = new Label();
        caretPosition.setPadding(new Insets(2));
        caretPosition.setFont(new Font(13));
        textArea.selectionProperty().addListener(((observable, oldValue, newValue) -> {
            final TwoDimensional.Position anchorPos = textArea.offsetToPosition(newValue.getStart(), TwoDimensional.Bias.Forward);
            final TwoDimensional.Position caretPos = textArea.offsetToPosition(newValue.getEnd(), TwoDimensional.Bias.Forward);
            final int charsNum = newValue.getLength();
            final int linesNum = Math.abs(caretPos.getMajor() - anchorPos.getMajor()) + 1;
            caretPosition.setText(
                    (caretPos.getMajor() + 1) + ":" + caretPos.getMinor() +
                            (charsNum > 0 ? " " + charsNum + (charsNum == 1 ? " char" : " chars") : "") +
                            (linesNum > 1 ? ", " + linesNum + " lines" : ""));
        }));

        final StackPane progressPane = new StackPane();
        final ProgressBar progressBar = new ProgressBar();
        progressBar.setMinWidth(200);
        progressBar.progressProperty().bind(Controller.taskMonitor.currentTaskProgressProperty());
        progressBar.visibleProperty().bind(Controller.taskMonitor.currentTaskNameProperty().isNotEmpty());

        final Label progressLabel = new Label();
        progressLabel.setFont(new Font(13));
        progressLabel.setPadding(new Insets(2));
        progressLabel.setLabelFor(progressBar);
        progressLabel.textProperty().bind(Controller.taskMonitor.currentTaskNameProperty());

        progressPane.setAlignment(Pos.CENTER);
        progressPane.getChildren().addAll(progressBar, progressLabel);

        final Pane growingPane = new Pane();
        final HBox bottomBox = new HBox(caretPosition, growingPane, progressPane);
        bottomBox.setPadding(new Insets(2));
        bottomBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(growingPane, Priority.ALWAYS);

        final MainMenuBar mainMenuBar = new MainMenuBar(masterSplitPane, slaveSplitPane);
        VBox.setVgrow(mainMenuBar, Priority.ALWAYS);
        textArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ALT) {
                mainMenuBar.requestFocus();
                keyEvent.consume();
            }
        });

        this.getChildren().addAll(mainMenuBar, masterSplitPane, bottomBox);
    }
}