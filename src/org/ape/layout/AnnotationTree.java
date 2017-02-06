package org.ape.layout;

import org.ape.annotations.TreeObjectItem;
import org.ape.annotations.treeObjects.TreeObject;
import org.ape.control.Controller;
import org.ape.AppData;
import org.ape.layout.cells.TreeObjectCell;
import org.ape.layout.dialogs.CategoryDialog;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.input.*;
import org.fxmisc.wellbehaved.event.Nodes;

import java.util.stream.Collectors;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

public class AnnotationTree extends TreeView<TreeObject<?>> {

    public static TreeObjectItem<TreeObject<?>> create(TreeObject<?> obj) {
        final TreeObjectItem<TreeObject<?>> item = new TreeObjectItem<>(obj);
        if (obj.getStatus() != TreeObject.Status.UPDATE) {
            AppData.treeObjects.add(obj);
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
                    c.getRemoved().parallelStream()
                            .filter(to -> to.getStatus() != TreeObject.Status.UPDATE)
                            .forEach(TreeObject::onDelete);
                    internalTreeItems.removeIf(treeItem -> c.getRemoved().contains(treeItem.getValue()));
                }
            }
        };
        if (obj.getParent() != null && obj.getStatus() != TreeObject.Status.UPDATE) {
            Controller.logger.logNew(obj.toLogString());
        }
        obj.setChildrenListener(childrenListener);
        return item;
    }

    public AnnotationTree() {
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        this.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.DELETE) {
                Controller.deleteTreeObject(this.getSelectionModel()
                        .getSelectedItems()
                        .stream()
                        .map(TreeItem::getValue)
                        .toArray(size -> new TreeObject<?>[size])
                );
                this.getSelectionModel().clearSelection();
            }
            keyEvent.consume();
        });

        final KeyCodeCombination selectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
        Nodes.addInputMap(this, consume(keyPressed(selectAll), keyEvent -> this.getSelectionModel().selectAll()));

        MenuItem newCategory = new MenuItem("New Category");
        newCategory.setOnAction(actionEvent -> {
            new CategoryDialog(AppData.owner, null).showAndWait();
            actionEvent.consume();
        });

        final Menu treePosition = new Menu("Tree Position");
        final ToggleGroup treeToggleGroup = new ToggleGroup();

        final RadioMenuItem treeDisplayLeft = new RadioMenuItem("Left");
        treeDisplayLeft.setToggleGroup(treeToggleGroup);
        final RadioMenuItem treeDisplayRight = new RadioMenuItem("Right");
        treeDisplayRight.setToggleGroup(treeToggleGroup);

        treePosition.getItems().setAll(treeDisplayLeft, treeDisplayRight);

        this.setContextMenu(new ContextMenu(newCategory, new SeparatorMenuItem(),
                                            treePosition));
        this.disableProperty().bind(AppData.isActiveProject.not());
        this.setCellFactory(c -> new TreeObjectCell(this));
    }
}