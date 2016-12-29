import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by michal on 12/22/16.
 */
public class LinkListCell extends TextFieldListCell<String> {

    private Label link = new Label();

    private static final StringConverter<String> identityConverter = new StringConverter<String>() {
        @Override
        public String toString(String object) {
            return object;
        }

        @Override
        public String fromString(String string) {
            return string;
        }
    };

    public LinkListCell() {
        this.setConverter(identityConverter);

        final SimpleBooleanProperty invalidUrl = new SimpleBooleanProperty(true);

        itemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && (oldValue == null || newValue.compareTo(oldValue) != 0)) {
                link.setText(newValue);
                HttpURLConnection conn = null;
                try {
                    new URL(this.getItem()).toURI();
                    conn = (HttpURLConnection) new URL(this.getItem()).openConnection();
                    conn.connect();
                    invalidUrl.set(false);
                } catch (URISyntaxException | IOException e) {
                    invalidUrl.set(true);
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }));

        link.styleProperty().bind(Bindings.when(invalidUrl)
                .then("-fx-text-fill: #8B0000;")
                .otherwise("-fx-text-fill: #228B22;"));

        this.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                this.startEdit();
            }
            mouseEvent.consume();
        });

        final MenuItem visitMenuItem = new MenuItem("Visit");
        visitMenuItem.setOnAction(actionEvent -> {
            Main.hostServices.showDocument(link.getText());
            actionEvent.consume();
        });

        final ContextMenu contextMenu = new ContextMenu();
        final MenuItem deleteMenuItem = new MenuItem("Delete");

        deleteMenuItem.setOnAction(actionEvent -> {
            this.getListView().getItems().remove(this.getItem());
            actionEvent.consume();
        });

        final MenuItem updateMenuItem = new MenuItem("Update");
        updateMenuItem.setOnAction(actionEvent -> {
            this.startEdit();
            actionEvent.consume();
        });

        this.contextMenuProperty().bind(Bindings.createObjectBinding(() -> this.isEmpty() ? null : contextMenu, this.emptyProperty()));

        contextMenu.getItems().addAll(updateMenuItem, deleteMenuItem, new SeparatorMenuItem(), visitMenuItem);

        this.setGraphic(link);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        this.setGraphic(link);
    }

    @Override
    public void commitEdit(String item) {
        if (!item.trim().isEmpty()) {
            super.commitEdit(item.trim());
        } else {
            this.getListView().getItems().remove(this.getIndex()); //delete empty links
        }
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            this.setGraphic(link);
        }
    }
}
