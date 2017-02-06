package org.ape.layout.dialogs;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.ape.control.Controller;

/**
 * FROM:
 * https://stackoverflow.com/questions/36949595/how-do-i-create-a-javafx-alert-with-a-check-box-for-do-not-ask-again
 */
public final class ConfirmationAlert extends Alert {

    public enum Type {
        APP_EXIT,
        TREE_DELETE
    }

    public ConfirmationAlert(Type type) {
        super(AlertType.CONFIRMATION);

        this.setResizable(false);
        this.getDialogPane().applyCss();
        Node graphic = this.getDialogPane().getGraphic();

        final CheckBox doNotAsk = new CheckBox("Do not ask again");
        this.setDialogPane(new DialogPane() {

            @Override
            protected Node createDetailsButton() {
                return doNotAsk;
            }
        });

        // Fool the dialog into thinking there is some expandable content
        // a Group won't take up any space if it has no children
        this.getDialogPane().getButtonTypes().setAll(
                new ButtonType("OK", ButtonBar.ButtonData.YES),
                new ButtonType("Cancel", ButtonBar.ButtonData.NO));
        this.getDialogPane().setExpandableContent(new Group());
        this.getDialogPane().setExpanded(true);
        // Reset the dialog graphic using the default style
        this.getDialogPane().setGraphic(graphic);
        switch (type) {
            case TREE_DELETE: {
                this.setTitle("Delete");
                this.setContentText("Are you sure you want to delete selected item/s?");
            }
            break;
            case APP_EXIT: {
                this.setTitle("Exit");
                this.setHeaderText("Are you sure you want to exit?");
            }
            break;
            default: {
                this.setTitle("Default alert");
                this.setHeaderText("Are you sure?");
            }
        }

        this.setOnHiding(dialogEvent -> {
            Controller.setShouldDisplayDialog(type, !doNotAsk.isSelected());
            dialogEvent.consume();
        });
    }
}