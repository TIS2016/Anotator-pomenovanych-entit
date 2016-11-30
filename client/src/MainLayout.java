import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.*;
import org.fxmisc.wellbehaved.event.Nodes;

import java.time.Duration;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

/**
 * Created by michal on 11/23/16.
 */
public class MainLayout extends VBox {

    public MainLayout(Stage primaryStage) {
        super();

        SplitPane masterSplitPane = new SplitPane();
        SplitPane slaveSplitPane = new SplitPane();

        MainMenuBar mainMenuBar = new MainMenuBar(primaryStage, masterSplitPane, slaveSplitPane);
        VBox.setVgrow(mainMenuBar, Priority.ALWAYS);

        StyleClassedTextArea textArea = new StyleClassedTextArea();
        textArea.insertText(0, "Hover for 1 sec over the text or" +
                "\n right click the text");
        textArea.setEditable(false);
        textArea.setParagraphGraphicFactory(LineNumberFactory.get(textArea));  //TODO: custom line numbers

        Popup anotationPopup = new Popup();
        Label changeMe = new Label("TODO: display anotation info");
        changeMe.setStyle(
                "-fx-background-color: black;" +
                        "-fx-text-fill: yellow;" +
                        "-fx-padding: 5;");
        anotationPopup.getContent().add(changeMe);
        anotationPopup.setAutoFix(true);

        textArea.setMouseOverTextDelay(Duration.ofSeconds(1));
        textArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
            Point2D position = e.getScreenPosition();
            //TODO: find anotation/s based on index
            //int index = e.getCharacterIndex();
            anotationPopup.show(textArea, position.getX(), position.getY());
            e.consume();
        });
        textArea.addEventHandler(MouseEvent.MOUSE_MOVED, e -> {
            if (anotationPopup.isShowing()) {
                anotationPopup.hide();
            }
            e.consume();
        });

        VirtualizedScrollPane<StyleClassedTextArea> scrollPane = new VirtualizedScrollPane<>(textArea);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox mainArea = new VBox();
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        TextArea logArea = new TextArea("TODO: log");
        logArea.setEditable(false);
        textArea.setShowCaret(StyledTextArea.CaretVisibility.ON);

        mainArea.getChildren().add(scrollPane);

        slaveSplitPane.setOrientation(Orientation.VERTICAL);
        slaveSplitPane.setDividerPositions(0.8);
        slaveSplitPane.getItems().addAll(mainArea, logArea);

        VBox treeBox = new VBox();

        TextField treeSearch = new TextField();
        treeSearch.setPromptText("Search");

        AnnotationTree annotationTree = new AnnotationTree(primaryStage);
        TreeObjectItem<TreeObject<?>> rootItem = (TreeObjectItem<TreeObject<?>>) annotationTree.getRoot();
        rootItem.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            if (treeSearch.getText() == null || treeSearch.getText().trim().isEmpty()) {
                return null;
            }
            return TreePredicate.create(child -> child.getName().contains(treeSearch.getText()));
        }, treeSearch.textProperty()));

        VBox.setVgrow(annotationTree, Priority.ALWAYS);

        treeBox.getChildren().addAll(treeSearch, annotationTree);

        masterSplitPane.setOrientation(Orientation.HORIZONTAL);
        masterSplitPane.setDividerPositions(0.7);
        masterSplitPane.getItems().addAll(slaveSplitPane, treeBox);

        ContextMenu contextMenu = new ContextMenu();

        textArea.setOnKeyPressed(keyEvent -> { //for mnemonics
            System.out.println(keyEvent.getCode());
            if (keyEvent.getCode() == KeyCode.ALT) {
                mainMenuBar.requestFocus();
            }
            keyEvent.consume();
        });
        contextMenu.getItems().setAll(new MenuItem("Update"), new MenuItem("Delete"),
                new SeparatorMenuItem(), new MenuItem("Comments")); //TODO: replace with real context menu

        textArea.addEventHandler(MouseEvent.MOUSE_CLICKED, me -> {
            System.out.println("Click in text area -- CONTEXT MENU TEST");
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
            if (me.getButton() == MouseButton.SECONDARY) {
                CharacterHit hit = textArea.hit(me.getX(), me.getY());
                int index = hit.getCharacterIndex().orElse(-1);
                if (index != -1) { //TODO: find annotation at INDEX
                    contextMenu.show(textArea, me.getScreenX(), me.getScreenY());
                }
            }
            me.consume();
        });


        KeyCodeCombination searchCombination = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
        Nodes.addInputMap(textArea, consume(keyPressed(searchCombination), keyEvent -> {
            System.out.println("CTRL-F SEARCH INVOKED -- BIND ME");
        }));

        this.heightProperty().addListener((observable, oldValue, newValue) -> {
            masterSplitPane.setPrefHeight(newValue.doubleValue());
        });

        this.getChildren().addAll(mainMenuBar, masterSplitPane);
    }
}
