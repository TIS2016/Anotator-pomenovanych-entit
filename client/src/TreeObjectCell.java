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

        //TODO: check for privileges or rewrite with controller
        MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(actionEvent -> {
            T item = this.getItem();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Under construction");
            alert.setContentText("...");
            alert.showAndWait();
            //TODO: open annotation dialog with data in treeobject
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        //TODO: check for privileges or rewrite with controller
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            ObservableList<TreeItem<TreeObject<?>>> tmp = FXCollections.observableArrayList(at.getSelectionModel().getSelectedItems());
            //FXCollections.copy(foo, at.getSelectionModel().getSelectedItems());
            tmp.filtered(item -> item != at.getRoot()).forEach(item -> { //item != null
                TreeObject<?> value = item.getValue();
                item.getParent().getValue().getChildren().remove(value);
                if (value instanceof CategoryObject) {
                    ((CategoryObject) value).deleteChildrenRecursive();
                }
            });
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        MenuItem newCategoryMenuItem = new MenuItem("Dummy category");
        newCategoryMenuItem.setOnAction(actionEvent -> {
            T item = this.getItem();
            CategoryObject dummyText = new CategoryObject("dummy category");
            if (item instanceof CategoryObject)
                ((CategoryObject) item).getChildren().add(dummyText);
            else
                ((CategoryObject) item.parentProperty().get()).getChildren().add(dummyText);
            at.getSelectionModel().clearSelection();
            actionEvent.consume();
        });

        deleteMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            return at.getRoot() != this.getTreeItem();
        }, this.selectedProperty(), at.rootProperty()));

        updateMenuItem.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            return at.getRoot() != this.getTreeItem();
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

        contextMenu.getItems().addAll(updateMenuItem, deleteMenuItem, newCategoryMenuItem);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(empty || item == null ? "" : item.getName());
        if (item instanceof AnnotationObject) {
            this.setGraphic(null); //no checkbox for annotations
        }
    }
}
