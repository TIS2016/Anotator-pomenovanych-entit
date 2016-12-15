import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    private static final ObservableList<TreeObject<?>> internalItems = FXCollections.observableArrayList();
    public static final FilteredList<TreeObject<?>> categories = new FilteredList<>(internalItems, treeObject -> {
        return treeObject instanceof CategoryObject;
    });

    //TODO: rewrite as non static
    public static TreeObjectItem<TreeObject<?>> create(TreeObject<?> obj) {
        TreeObjectItem<TreeObject<?>> item = new TreeObjectItem<>(obj);
        if (!internalItems.contains(obj)) { //because of update/reorder
            internalItems.add(obj);
        }
        //ObservableList<TreeObjectItem<TreeObject<?>>> internalTreeItems = item.getInternalChildren();
        item.getInternalChildren().addAll(obj.getChildren().stream().map(AnnotationTree::create).collect(Collectors.toList()));
        if (item.getValue() instanceof CategoryObject)
            item.setExpanded(true);

        ListChangeListener<TreeObject<?>> listener = (Change<? extends TreeObject<?>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    item.getInternalChildren().addAll(c.getAddedSubList().stream().map(AnnotationTree::create).collect(Collectors.toList()));
                    if (item.getValue() instanceof CategoryObject)
                        item.setExpanded(true);
                } else if (c.wasRemoved()) {
                    internalItems.removeAll(c.getRemoved());
                    item.getInternalChildren().removeIf(treeItem -> c.getRemoved().contains(treeItem.getValue()));
                }
            }
        };
        obj.addChildrenListener(listener);
        return item;
    }

    public AnnotationTree(Window parent) {
        this.parent = parent;
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //test
        CategoryObject root = new CategoryObject("Categories", "xxx", Color.WHITE);

        CategoryObject bar = new CategoryObject("cate1", "cat1_Tag", Color.ROYALBLUE);
        CategoryObject bar2 = new CategoryObject("cate2", "cat2_Tag", Color.RED);

        AnnotationObject t1 = new AnnotationObject("anot1");
        AnnotationObject t2 = new AnnotationObject("anot2");
        ReferenceObject t3 = new ReferenceObject("ref1");
        ReferenceObject t4 = new ReferenceObject("ref2");

        bar.getChildren().addAll(t1, t2, bar2);
        t1.getChildren().add(t3);
        t2.getChildren().add(t4);
        root.getChildren().addAll(bar);

        TreeObjectItem<TreeObject<?>> rootItem = create(root); //automatically constructs the whole tree

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
        Nodes.addInputMap(this, consume(keyPressed(selectAll), keyEvent -> {
          this.getSelectionModel().selectAll();
        }));

        this.setRoot(rootItem);
        this.setCellFactory(c -> {
            return new TreeObjectCell(this);
        });
    }

}
