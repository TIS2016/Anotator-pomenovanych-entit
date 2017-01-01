import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.fxmisc.wellbehaved.event.Nodes;

import java.util.stream.Collectors;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

/**
 * Created by michal on 11/25/16.
 */
public class AnnotationTree extends TreeView<TreeObject<?>> {

    protected Window parent;

    //TODO: rewrite as non static
    public static TreeObjectItem<TreeObject<?>> create(TreeObject<?> obj) {
        TreeObjectItem<TreeObject<?>> item = new TreeObjectItem<>(obj);
        if (!SessionData.treeObjects.contains(obj)) { //because of update
            SessionData.treeObjects.add(obj);
        }
        final ObservableList<TreeObjectItem<TreeObject<?>>> internalTreeItems = item.getInternalChildren();
        internalTreeItems.addAll(obj.getChildren().stream().map(AnnotationTree::create).collect(Collectors.toList()));
        item.setSelected(true);
        item.setExpanded(true);

        final ListChangeListener<TreeObject<?>> childrenListener = (Change<? extends TreeObject<?>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    internalTreeItems.addAll(c.getAddedSubList()
                            .stream()
                            .map(AnnotationTree::create)
                            .collect(Collectors.toList()));
                    item.setSelected(true);
                    item.setExpanded(true);
                } else if (c.wasRemoved()) {
                    c.getRemoved().stream()
                            .filter(to -> to instanceof DisplayedTreeObject &&
                                    ((DisplayedTreeObject) to).getStatus() == DisplayedTreeObject.Status.DEFAULT)
                            .map(to -> (DisplayedTreeObject) to)
                            .forEach(dto -> dto.onDelete());
                    SessionData.treeObjects.removeAll(c.getRemoved());
                    internalTreeItems.removeIf(treeItem -> c.getRemoved().contains(treeItem.getValue()));
                }
            }
        };
        obj.setChildrenListener(childrenListener);
        return item;
    }

    public AnnotationTree(Window parent) {
        this.parent = parent;
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        CategoryObject root = new CategoryObject("Categories", "ROOT", Color.WHITE);
        CategoryObject foo = new CategoryObject("dummy_category", "TAG1", Color.RED);

        root.getChildren().add(foo);

        TreeObjectItem<TreeObject<?>> rootItem = AnnotationTree.create(root); //automatically constructs the whole tree

        ContextMenu categoryMenu = new ContextMenu();
        MenuItem categoryMenuItem = new MenuItem("New category");
        categoryMenuItem.setOnAction(actionEvent -> {
            new CategoryDialog(parent).showAndWait();
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
            if (keyEvent.getCode() == KeyCode.DELETE) {
                ObservableList<TreeItem<TreeObject<?>>> tmp = FXCollections.observableArrayList(this.getSelectionModel().getSelectedItems());
                tmp.filtered(item -> item != this.getRoot()).forEach(item -> {
                    TreeObject<?> value = item.getValue();
                    if (item.getParent() != null) {
                        item.getParent().getValue().getChildren().remove(value);
                    }
                    value.clearChildren();
                });
                this.getSelectionModel().clearSelection();
            }
            keyEvent.consume();
        });

        KeyCodeCombination selectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
        Nodes.addInputMap(this, consume(keyPressed(selectAll), keyEvent -> this.getSelectionModel().selectAll()));

        this.setRoot(rootItem);
        this.setCellFactory(c -> new TreeObjectCell(this));
    }
}
