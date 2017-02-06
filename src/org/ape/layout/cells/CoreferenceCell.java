package org.ape.layout.cells;

import javafx.scene.Node;
import org.ape.annotations.treeObjects.CoreferenceObject;
import org.ape.layout.dialogs.CoreferenceDialog;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;

public class CoreferenceCell extends ListCell<CoreferenceObject> {

    public CoreferenceCell() {
        super();
        final ContextMenu contextMenu = new ContextMenu();

        final MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(actionEvent -> {
            new CoreferenceDialog(((Node) actionEvent.getSource()).getScene().getWindow(), this.getItem()).showAndWait();
            actionEvent.consume();
        });

        final MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            CoreferenceObject coreferenceObject = this.getItem();
            coreferenceObject.getParent().getChildren().remove(coreferenceObject);
            actionEvent.consume();
        });
        this.setPrefSize(150, 25);
        this.setWrapText(true);

        this.contextMenuProperty().bind(Bindings.when(this.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
        contextMenu.getItems().addAll(updateMenuItem, deleteMenuItem);
    }

    @Override
    public void updateItem(CoreferenceObject item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(item == null || empty ? "" : item.getTreeName());
    }
}