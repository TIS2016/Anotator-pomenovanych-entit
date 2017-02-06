package org.ape.layout.dialogs;

import javafx.application.Platform;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.controlsfx.dialog.ExceptionDialog;

public class FixedExceptionDialog extends ExceptionDialog {

    public FixedExceptionDialog(Throwable exception,
                                String headerReason) {
        super(exception);
        this.setResizable(true);
        this.setTitle("Error");
        this.getDialogPane().setMaxWidth(600);
        this.setHeaderText("Error occured during: " + headerReason);
        final DialogPane dialogPane = getDialogPane();
        dialogPane.expandedProperty().addListener(invalidation ->
                Platform.runLater(() -> {
                    dialogPane.requestLayout();
                    Stage stage = (Stage) dialogPane.getScene().getWindow();
                    stage.sizeToScene();
                })
        );
    }
}