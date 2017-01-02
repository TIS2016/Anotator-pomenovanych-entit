import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.*;
import org.fxmisc.wellbehaved.event.Nodes;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

/**
 * Created by michal on 11/23/16.
 */
public class MainLayout extends VBox {

    public MainLayout(Stage primaryStage) {
        super();

        final SplitPane masterSplitPane = new SplitPane();
        final SplitPane slaveSplitPane = new SplitPane();
        StyledTextArea<Void, DisplayedTreeObject<?>> textArea = new StyledTextArea<>(null, ((textFlow, s) -> {}),
                null, (textExt, treeObject) -> {
            if (treeObject != null) {
                textExt.fillProperty().bind(Bindings.createObjectBinding(
                        () -> Double.compare(treeObject.colorProperty()
                                .get().getBrightness(), 0.6) <= 0 ? Color.WHITE : Color.BLACK,
                        treeObject.colorProperty()));
                textExt.backgroundColorProperty().bind(treeObject.colorProperty());
            } else {
                textExt.backgroundColorProperty().unbind();
                textExt.setBackgroundColor(Color.WHITE);

                textExt.fillProperty().unbind();
                textExt.setFill(Color.BLACK);
            }
        });

        textArea.setShowCaret(StyledTextArea.CaretVisibility.ON);
        textArea.setUseInitialStyleForInsertion(false);
        textArea.setEditable(false);
        textArea.setParagraphGraphicFactory(LineNumberFactory.get(textArea));
        textArea.setStyle("-fx-font-size: 13;"); //TODO: options?
        textArea.insertText(0, "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog\n" +
                "The quick brown fox jumps over the lazy dog");

        textArea.setWrapText(true);
        VirtualizedScrollPane<StyledTextArea> scrollPane = new VirtualizedScrollPane<>(textArea);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); //because wrap is on
        //BUG -- horizontal scrollbar not working

        TextArea logArea = new TextArea("TODO: log");
        logArea.setEditable(false);

        slaveSplitPane.setOrientation(Orientation.VERTICAL);
        slaveSplitPane.setDividerPositions(0.8);
        slaveSplitPane.getItems().addAll(scrollPane, logArea);

        VBox treeBox = new VBox();

        TextField treeFilter = new TextField();
        treeFilter.setPromptText("Search");

        AnnotationTree annotationTree = new AnnotationTree(primaryStage);
        TreeObjectItem<TreeObject<?>> rootItem = (TreeObjectItem<TreeObject<?>>) annotationTree.getRoot();
        rootItem.predicateProperty().bind(Bindings.createObjectBinding(() -> treeFilter.getText().trim().isEmpty() ? null :
                TreePredicate.create(child -> child.getName().contains(treeFilter.getText().trim()))
        , treeFilter.textProperty()));

        VBox.setVgrow(annotationTree, Priority.ALWAYS);

        treeBox.getChildren().addAll(treeFilter, annotationTree);

        masterSplitPane.setOrientation(Orientation.HORIZONTAL);
        masterSplitPane.setDividerPositions(0.7);
        masterSplitPane.getItems().addAll(slaveSplitPane, treeBox);

        MainMenuBar mainMenuBar = new MainMenuBar(primaryStage, masterSplitPane, slaveSplitPane);
        VBox.setVgrow(mainMenuBar, Priority.ALWAYS);

        ContextMenu contextMenu = new ContextMenu();

        //TODO: check privileges

        MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(actionEvent -> {
            DisplayedTreeObject treeObject = (DisplayedTreeObject) contextMenu.getUserData();
            if (treeObject instanceof AnnotationObject)
                new AnnotationDialog(primaryStage, (AnnotationObject) treeObject).showAndWait();
            else if (treeObject instanceof ReferenceObject)
                new ReferenceDialog(primaryStage, (ReferenceObject) treeObject).showAndWait();
            actionEvent.consume();
        });

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            DisplayedTreeObject treeObject = (DisplayedTreeObject) contextMenu.getUserData();
            treeObject.getParent().getChildren().remove(treeObject);
            treeObject.clearChildren();
            actionEvent.consume();
        });

        SeparatorMenuItem annotSeparator = new SeparatorMenuItem();
        annotSeparator.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> textArea.selectionProperty().getValue().getLength() > 0,
                textArea.selectionProperty()));

        MenuItem referToThisMenuItem = new MenuItem("Refer to this");
        referToThisMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> textArea.selectionProperty().getValue().getLength() > 0,
                textArea.selectionProperty()));

        referToThisMenuItem.setOnAction(actionEvent -> {
            AnnotationObject annotationObject = (AnnotationObject) contextMenu.getUserData();
            annotationObject.getChildren().add(new ReferenceObject(textArea, annotationObject));
            textArea.deselect();
            actionEvent.consume();
        });

        textArea.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ALT) {
                mainMenuBar.requestFocus(); //MenuBar mnemonics
            } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                Platform.exit();
            }
            keyEvent.consume();
        });
        textArea.addEventHandler(MouseEvent.MOUSE_CLICKED, me -> {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
            if (me.getButton() == MouseButton.SECONDARY) {
                CharacterHit hit = textArea.hit(me.getX(), me.getY());
                ColorObject colorObject = SessionData.colorObjects.get(hit.getCharacterIndex().orElse(-1));
                if (colorObject != null && colorObject.getLastSelectedBackreference() != null) {
                    DisplayedTreeObject last = colorObject.getLastSelectedBackreference();
                    contextMenu.setUserData(last);
                    if (last instanceof AnnotationObject) {
                        contextMenu.getItems().setAll(updateMenuItem,
                                                      deleteMenuItem,
                                                      annotSeparator,
                                                      referToThisMenuItem);
                    } else if (last instanceof ReferenceObject) {
                        contextMenu.getItems().setAll(updateMenuItem, deleteMenuItem);
                    }
                    contextMenu.show(textArea, me.getScreenX(), me.getScreenY());
                }
            }
            me.consume();
        });

        /*KeyCodeCombination searchCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        Nodes.addInputMap(textArea, consume(keyPressed(searchCombination), keyEvent -> {
            System.out.println("CTRL-F SEARCH INVOKED -- BIND ME");
            keyEvent.consume();
        }));*/

        KeyCodeCombination annotCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.SHIFT_DOWN);

        Nodes.addInputMap(textArea, consume(keyPressed(annotCombination).onlyIf(keyEvent -> {
            return textArea.getSelection().getLength() > 0;  //TODO: add more control -- can annotate
        }), keyEvent -> {
            new AnnotationDialog(primaryStage, textArea).showAndWait();
            textArea.deselect();
            keyEvent.consume();
        }));

        this.heightProperty().addListener(
                (observable, oldValue, newValue) -> masterSplitPane.setPrefHeight(newValue.doubleValue()));

        this.getChildren().addAll(mainMenuBar, masterSplitPane);
    }
}