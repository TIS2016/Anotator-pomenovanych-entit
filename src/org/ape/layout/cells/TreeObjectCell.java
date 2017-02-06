package org.ape.layout.cells;

import org.ape.annotations.treeObjects.*;
import org.ape.control.ColorConverter;
import org.ape.control.Controller;
import org.ape.layout.AnnotationTree;
import org.ape.layout.dialogs.AnnotationDialog;
import org.ape.layout.dialogs.CategoryDialog;
import org.ape.layout.dialogs.CoreferenceDialog;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import org.ape.AppData;

public class TreeObjectCell<T extends TreeObject<?>> extends CheckBoxTreeCell<T> {

    public TreeObjectCell(AnnotationTree at) {
        super();
        this.styleProperty().bind(Bindings.createObjectBinding(() -> {
            T item = this.getItem();
            if (item != null && item.getParent() != null && item instanceof CategoryObject) {
                CategoryObject co = (CategoryObject) item;
                return String.format("-fx-background-color: %s; -fx-text-fill: %s;",
                        ColorConverter.intToStringColor(co.getIntColor()),
                        Double.compare(co.getColor().getBrightness(), 0.6) <= 0 ? "white" : "black");
            }
            return "";
        }, this.itemProperty(), this.selectedProperty())); //selected property necessary for color update

        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem update = new MenuItem("Update");
        update.setOnAction(actionEvent -> {
            T item = this.getItem();
            if (item instanceof CategoryObject) {
                new CategoryDialog(AppData.owner, (CategoryObject) item).showAndWait();
            } else if (item instanceof AnnotationObject) {
                new AnnotationDialog(AppData.owner, (AnnotationObject) item).showAndWait();
            } else if (item instanceof CoreferenceObject) {
                new CoreferenceDialog(AppData.owner, (CoreferenceObject) item).showAndWait();
            }
            this.getTreeView().getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        update.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> at.getRoot() != this.getTreeItem(), //TO
                this.selectedProperty(), at.rootProperty()));

        final MenuItem delete = new MenuItem();

        delete.setOnAction(actionEvent -> {
            Controller.deleteTreeObject(this.getTreeView().getSelectionModel().getSelectedItems()
                    .stream().map(TreeItem::getValue).toArray(size -> new TreeObject<?>[size]));
            this.getTreeView().getSelectionModel().clearSelection();
            actionEvent.consume();
        });
        delete.textProperty().bind(Bindings
                .when(this.treeItemProperty().isEqualTo(at.rootProperty()))
                .then("Delete All")
                .otherwise("Delete"));

        final MenuItem newCategory = new MenuItem("New Category");
        newCategory.setOnAction(actionEvent -> {
            new CategoryDialog(AppData.owner, null).showAndWait();
            this.getTreeView().getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        final SeparatorMenuItem separator1 = new SeparatorMenuItem();
        separator1.visibleProperty().bind(update.visibleProperty().or(delete.visibleProperty()));

        final MenuItem setDefault = new MenuItem();
        setDefault.textProperty().bind(
                Bindings.when(AppData.defaultCategory.isEqualTo(this.itemProperty()))
                        .then("Unset Default")
                        .otherwise("Set Default"));
        setDefault.setOnAction(actionEvent -> {
            if (this.getItem() != AppData.defaultCategory.get()) {
                AppData.defaultCategory.set((CategoryObject) this.getItem());
            } else {
                AppData.defaultCategory.set(null);
            }
            this.getTreeView().getSelectionModel().clearSelection();
            actionEvent.consume();
        });
        setDefault.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> this.getItem() instanceof CategoryObject && this.getItem().getParent() != null,
                this.itemProperty()));

        final MenuItem setAnchor = new MenuItem();
        setAnchor.textProperty().bind(
                Bindings.when(AppData.anchorAnnotation.isEqualTo(this.itemProperty()))
                .then("Unset Anchor")
                .otherwise("Set Anchor"));
        setAnchor.setOnAction(actionEvent -> {
            if (this.getItem() != AppData.anchorAnnotation.get()) {
                AppData.anchorAnnotation.set((AnnotationObject) this.getItem());
            } else {
                AppData.anchorAnnotation.set(null);
            }
            this.getTreeView().getSelectionModel().clearSelection();
            actionEvent.consume();
        });
        setAnchor.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> this.getItem() instanceof AnnotationObject,
                this.itemProperty()
        ));

        final SeparatorMenuItem separator2 = new SeparatorMenuItem();
        separator2.visibleProperty().bind(setDefault.visibleProperty().or(setAnchor.visibleProperty()));
        
        contextMenu.getItems().addAll(
                newCategory, separator1,
                update, delete, separator2,
                setDefault, setAnchor);

        this.contextMenuProperty().bind(Bindings.when(this.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));

        this.setPrefHeight(25);
        this.setWrapText(true);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(empty ? "" : item.getTreeName());
    }
}