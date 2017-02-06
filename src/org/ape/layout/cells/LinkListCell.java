package org.ape.layout.cells;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;
import org.ape.control.Controller;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class LinkListCell extends TextFieldListCell<String> {

    private final Label urlLabel = new Label();

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
        super();
        this.setConverter(identityConverter);

        final SimpleBooleanProperty invalidUrl = new SimpleBooleanProperty(true);

        itemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && (oldValue == null || newValue.compareTo(oldValue) != 0)) {
                urlLabel.setText(newValue);
                try {
                    new URL(this.getItem()).toURI();
                    invalidUrl.set(false);
                } catch (MalformedURLException | URISyntaxException e) {
                    invalidUrl.set(true);
                }
            }
        }));

        urlLabel.styleProperty().bind(Bindings.when(invalidUrl)
                .then("-fx-text-fill: #c11111;")
                .otherwise("-fx-text-fill: #28b712;"));

        this.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                this.startEdit();
            }
            mouseEvent.consume();
        });

        final MenuItem visitMenuItem = new MenuItem("Visit");
        visitMenuItem.setOnAction(actionEvent -> {
            Controller.getHostServices().showDocument(urlLabel.getText());
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

        contextMenu.getItems().addAll(updateMenuItem, deleteMenuItem, new SeparatorMenuItem(), visitMenuItem);
        this.contextMenuProperty().bind(Bindings.when(this.emptyProperty()).then((ContextMenu) null).otherwise(contextMenu));

        this.setGraphic(urlLabel);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        this.setGraphic(urlLabel);
    }

    @Override
    public void commitEdit(String item) {
        if (!item.trim().isEmpty()) {
            super.commitEdit(item.trim());
        } else {
            this.getListView().getItems().remove(this.getIndex()); //auto-delete empty links
        }
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty) {
            this.setGraphic(urlLabel);
        }
    }
}