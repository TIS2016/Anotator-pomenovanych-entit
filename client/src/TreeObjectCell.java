import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

/**
 * Created by michal on 11/28/16.
 */
public class TreeObjectCell<T extends TreeObject<?>> extends CheckBoxTreeCell<T> {

    public TreeObjectCell(AnnotationTree at) {
        ContextMenu contextMenu = new ContextMenu();

        this.styleProperty().bind(Bindings.createObjectBinding(() -> {
            T item = this.getItem();
            if (item != null && item.getParent() != null && item instanceof CategoryObject) {
                CategoryObject c = (CategoryObject) item;
                return String.format("-fx-background-color: #%06X; -fx-text-fill: %s; %s",
                        0xFFFFFF & c.getIntColor(),
                        Double.compare(c.getColor().getBrightness(), 0.6) <= 0 ? "white" : "black",
                        this.isSelected() ? "-fx-font-weight: bold;": "");
            }
            else return "";
        }, this.itemProperty(), this.selectedProperty())); //selected property necessary for color update

        //TODO: check for privileges, use controller
        MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(actionEvent -> {
            T item = this.getItem();
            if (item instanceof CategoryObject)
                new CategoryDialog(at.parent, (CategoryObject) item, true).showAndWait();
            else if (item instanceof AnnotationObject) {
                System.out.println("TODO: ANNOT");
            } else
                throw new AssertionError("Unknown TreeObject type -- UPDATE");
            //((TreeObjectItem<T>) this.getTreeItem().getParent()).forceSort();
            this.updateItem(item, false);
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        //TODO: check for privileges, use controller
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            ObservableList<TreeItem<TreeObject<?>>> tmp = FXCollections.observableArrayList(at.getSelectionModel().getSelectedItems());
            tmp.filtered(item -> item != at.getRoot()).forEach(item -> {
                TreeObject<?> value = item.getValue();
                if (item.getParent() != null) {
                    item.getParent().getValue().getChildren().remove(value);
                }
                value.clearChildren();
            });
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        MenuItem newCategoryMenuItem = new MenuItem("New category");
        newCategoryMenuItem.setOnAction(actionEvent -> {
            T item = this.getItem();
            if (item instanceof CategoryObject)
                new CategoryDialog(at.parent, (CategoryObject) item, false).showAndWait();
            else if (item instanceof AnnotationObject)
                new CategoryDialog(at.parent, (CategoryObject) item.getParent(), false).showAndWait();
            else if (item instanceof ReferenceObject)
                new CategoryDialog(at.parent, (CategoryObject) item.getParent().getParent(), false).showAndWait();
            else
                throw new AssertionError("Unknow Object");
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        deleteMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            return at.getRoot() != this.getTreeItem();
        }, this.selectedProperty(), at.rootProperty()));

        updateMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> { //do not update references directly
            return at.getRoot() != this.getTreeItem() && !(this.getItem() instanceof ReferenceObject);
        }, this.selectedProperty(), at.rootProperty()));

        this.onMouseClickedProperty().bind(Bindings.createObjectBinding(() -> {
            T item = this.getItem();
            return item != null && this.isSelected() ? (mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(at.parent, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                }
                mouseEvent.consume();
            }) : null;
        }, this.selectedProperty(), this.itemProperty()));

        SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
        separatorMenuItem.visibleProperty().bind(updateMenuItem.visibleProperty().or(deleteMenuItem.visibleProperty()));
        contextMenu.getItems().addAll(newCategoryMenuItem, separatorMenuItem, updateMenuItem, deleteMenuItem);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(empty || item == null ? "" : item.getName());
        if (item instanceof ReferenceObject) {
            this.setGraphic(null); //no checkbox for references
        }
    }
}