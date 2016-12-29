import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseButton;

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
            } else {
                throw new AssertionError(String.format("Unknown object type: %s", item.getClass()));
            }
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        updateMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> at.getRoot() != this.getTreeItem(),// && !(this.getItem() instanceof ReferenceObject),
                this.selectedProperty(), at.rootProperty()));

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            //?
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

        deleteMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> at.getRoot() != this.getTreeItem(),
                this.selectedProperty(), at.rootProperty()));

        MenuItem newCategoryMenuItem = new MenuItem("New category");
        newCategoryMenuItem.setOnAction(actionEvent -> {
            new CategoryDialog(at.parent, null).showAndWait();
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

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
        /*if (item instanceof ReferenceObject) {
            this.setGraphic(null); //no checkbox for references
        }*/
    }
}