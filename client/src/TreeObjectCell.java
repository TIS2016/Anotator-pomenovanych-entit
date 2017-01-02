import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseButton;

/**
 * Created by michal on 11/28/16.
 */
public class TreeObjectCell<T extends TreeObject<?>> extends CheckBoxTreeCell<T> {

    public TreeObjectCell(AnnotationTree at) {
        super();

        ContextMenu contextMenu = new ContextMenu();

        this.styleProperty().bind(Bindings.createObjectBinding(() -> {
            T item = this.getItem();
            if (item != null && item.getParent() != null && item instanceof CategoryObject) {
                CategoryObject c = (CategoryObject) item;
                return String.format("-fx-background-color: %s; -fx-text-fill: %s; %s",
                        ColorConverter.intToStringColor(c.getIntColor()),
                        Double.compare(c.getColor().getBrightness(), 0.6) <= 0 ? "white" : "black",
                        this.isSelected() ? "-fx-font-weight: bold;": "");
            }
            return "";
        }, this.itemProperty(), this.selectedProperty())); //selected property necessary for color update

        MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(actionEvent -> {
            T item = this.getItem();
            if (item instanceof CategoryObject) {
                new CategoryDialog(at.parent, (CategoryObject) item).showAndWait();
            } else if (item instanceof AnnotationObject) {
                new AnnotationDialog(at.parent, (AnnotationObject) item).showAndWait();
            } else if (item instanceof ReferenceObject) {
                new ReferenceDialog(at.parent, (ReferenceObject) item).showAndWait();
            }
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        updateMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> at.getRoot() != this.getTreeItem(),
                this.selectedProperty(), at.rootProperty()));

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            System.out.println("abc");
            FXCollections.observableArrayList(at.getSelectionModel().getSelectedItems()).forEach(item -> {
                TreeObject<?> value = item.getValue();
                if (item.getParent() != null) {
                    item.getParent().getValue().getChildren().remove(value);
                }
                value.clearChildren();
            });
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        deleteMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> at.getRoot() != this.getTreeItem(),
                this.selectedProperty(), at.rootProperty()));

        MenuItem newCategoryMenuItem = new MenuItem("New Category");
        newCategoryMenuItem.setOnAction(actionEvent -> {
            new CategoryDialog(at.parent, null).showAndWait();
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        this.setPrefHeight(25);
        this.setWrapText(true);

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        separatorMenuItem.visibleProperty().bind(updateMenuItem.visibleProperty().or(deleteMenuItem.visibleProperty()));
        contextMenu.getItems().addAll(newCategoryMenuItem, separatorMenuItem, updateMenuItem, deleteMenuItem);

        this.contextMenuProperty().bind(Bindings.when(this.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(empty ? "" : item.getName());
    }
}