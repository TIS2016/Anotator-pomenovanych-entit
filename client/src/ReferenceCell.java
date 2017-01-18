import javafx.beans.binding.Bindings;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;

/**
 * Created by michal on 1/1/17.
 */
public class ReferenceCell extends ListCell<ReferenceObject> {

    public ReferenceCell(final Window owner) {
        super();
        final ContextMenu contextMenu = new ContextMenu();

        final MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(actionEvent -> {
            new ReferenceDialog(owner, this.getItem()).showAndWait();
            actionEvent.consume();
        });

        final MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            ReferenceObject referenceObject = this.getItem();
            referenceObject.getParent().getChildren().remove(referenceObject);
            actionEvent.consume();
        });
        this.setPrefSize(150, 25);
        this.setWrapText(true);

        this.contextMenuProperty().bind(Bindings.when(this.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));
        contextMenu.getItems().addAll(updateMenuItem, deleteMenuItem);
    }

    @Override
    public void updateItem(ReferenceObject item, boolean empty) {
        super.updateItem(item, empty);
        this.setText(item == null || empty ? "" : item.getName());
    }
}