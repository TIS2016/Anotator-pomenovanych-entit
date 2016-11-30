import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.Window;
import org.fxmisc.wellbehaved.event.Nodes;

import java.util.stream.Collectors;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

/**
 * Created by michal on 11/25/16.
 */
public class AnnotationTree extends TreeView<TreeObject<?>> {

    public static TreeObjectItem<TreeObject<?>> create(TreeObject<?> obj) {
        TreeObjectItem<TreeObject<?>> item = new TreeObjectItem<>(obj);
        item.getInternalChildren().addAll(obj.getChildren().stream().map(AnnotationTree::create).collect(Collectors.toList()));
        item.setExpanded(true);

        obj.getChildren().addListener((Change<? extends TreeObject<?>> c) -> {
            //item.setSortedChildren(); //???
            while (c.next()) {
                if (c.wasAdded()) {
                    item.getInternalChildren().addAll(c.getAddedSubList().stream().map(AnnotationTree::create).collect(Collectors.toList()));
                }
                if (c.wasRemoved()) {
                    item.getInternalChildren().removeIf(treeItem -> c.getRemoved().contains(treeItem.getValue()));
                }
            }
            //item.setFilteredChildren();
        });
        return item;
    }

    protected Window parent;

    public AnnotationTree(Window parent) {
        this.parent = parent;
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //test
        CategoryObject root = new CategoryObject("Press space to test");
        CategoryObject bar = new CategoryObject("bcde");
        AnnotationObject t1 = new AnnotationObject("ab");
        AnnotationObject t2 = new AnnotationObject("abc");
        AnnotationObject t3 = new AnnotationObject("zx");
        AnnotationObject t4 = new AnnotationObject("yy");

        bar.getChildren().addAll(t1, t2, t3, t4);
        root.getChildren().addAll(bar);

        TreeObjectItem<TreeObject<?>> rootItem = create(root); //automatically constructs the whole tree

        ContextMenu categoryMenu = new ContextMenu();
        MenuItem categoryMenuItem = new MenuItem("Dummy category");
        categoryMenuItem.setOnAction(actionEvent -> {
            CategoryObject dummyText = new CategoryObject("dummy category");
            ((CategoryObject) this.getRoot().getValue()).getChildren().add(dummyText);
            actionEvent.consume();
        });

        categoryMenu.getItems().add(categoryMenuItem);

        this.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                categoryMenu.show(this.parent, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            }
            mouseEvent.consume();
        });

        this.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SPACE) { //remove me
                categoryMenuItem.fire();
            }
            if (keyEvent.getCode() == KeyCode.DELETE) {
                ObservableList<TreeItem<TreeObject<?>>> tmp = FXCollections.observableArrayList(this.getSelectionModel().getSelectedItems());
                //FXCollections.copy(foo, this.getSelectionModel().getSelectedItems());
                tmp.filtered(item -> item != null && item != this.getRoot()).forEach(item -> {
                    TreeObject<?> value = item.getValue();
                    item.getParent().getValue().getChildren().remove(value);
                    if (value instanceof CategoryObject) { //necessary?
                        ((CategoryObject) value).deleteChildrenRecursive();
                    }
                });
                this.getSelectionModel().clearSelection();
            }
            keyEvent.consume();
        });

        KeyCodeCombination selectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
        Nodes.addInputMap(this, consume(keyPressed(selectAll), keyEvent -> {
          this.getSelectionModel().selectAll();
        }));

        this.setRoot(rootItem);
        this.setCellFactory(c -> {
            return new TreeObjectCell(this);
        });
    }

}
