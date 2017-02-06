package org.ape.layout.dialogs;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.ape.control.Controller;
import org.ape.AppData;

import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ExportDialog extends Dialog {

    public ExportDialog(Window owner) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("Export Project");
        this.getDialogPane().setContent(new ExportPane(this));
    }

    private class ExportPane extends GridPane {

        ExportPane(final ExportDialog dialog) {
            final DialogPane exportPane = dialog.getDialogPane();

            final SimpleObjectProperty<File> exportFile = new SimpleObjectProperty<>();
            SimpleObjectProperty<Pattern> pattern = new SimpleObjectProperty<>();

            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Project");

            final Button selectFile = new Button("Select file");
            selectFile.setOnAction(actionEvent -> {
                final File file = fileChooser.showSaveDialog(((Node) actionEvent.getSource()).getScene().getWindow());
                if (file != null) {
                    exportFile.set(file);
                }
                actionEvent.consume();
            });
            exportFile.addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (!newValue.exists() || newValue.canWrite() || !newValue.setWritable(true)) {
                        selectFile.setText(exportFile.get().getName());
                        return;
                    }
                }
                selectFile.setText("Select file");
            }));
            exportFile.set(AppData.exportPath.get().isEmpty() ? null : new File(AppData.exportPath.get()));

            final CheckBox onlySelected = new CheckBox("Only selected");

            final TextField delimiterField = new TextField();
            delimiterField.setPromptText("Pattern");
            delimiterField.textProperty().addListener(((observable, oldValue, newValue) -> {
                try {
                    if (newValue.isEmpty()) {
                        pattern.set(null);
                    } else {
                        pattern.set(Pattern.compile(newValue));
                    }
                } catch (PatternSyntaxException e) {
                    pattern.set(null);
                }
            }));
            delimiterField.setText(Controller.getDelimiterPattern().pattern());

            final CheckBox outputId = new CheckBox("Output id");

            final CheckBox outputDescription = new CheckBox("Output description");

            final SimpleBooleanProperty invalidInt = new SimpleBooleanProperty();
            final TextField startFromLine = new TextField("1");
            startFromLine.textProperty().addListener(((observable, oldValue, newValue) -> {
                try {
                    Integer.valueOf(startFromLine.getText().trim());
                    invalidInt.set(false);
                } catch (IllegalArgumentException e) {
                    invalidInt.set(true);
                }
            }));

            final Label statusLabel = new Label("");
            statusLabel.textProperty().bind(Bindings
                    .when(pattern.isNull())
                    .then("Invalid pattern")
                    .otherwise(Bindings
                            .when(invalidInt)
                            .then("Not a number")
                            .otherwise("")));
            statusLabel.visibleProperty().bind(pattern.isNull().or(invalidInt));
            statusLabel.setTextFill(Color.RED);

            final ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            final ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            exportPane.getButtonTypes().setAll(okButton, cancelBtn);

            final Button okBtn = (Button) exportPane.lookupButton(okButton);
            okBtn.disableProperty().bind(exportFile.isNull().or(pattern.isNull().or(invalidInt)));
            okBtn.setOnAction(actionEvent -> {
                Controller.setDelimitersPattern(pattern.get());
                Controller.exportProject(
                        exportFile.get(), onlySelected.isSelected(),
                        outputId.isSelected(), outputDescription.isSelected(),
                        Integer.valueOf(startFromLine.getText()));
                actionEvent.consume();
            });

            this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);

            this.add(new Label("Export file:"), 0, 0);
            this.add(selectFile, 1, 0);
            this.add(onlySelected, 1, 1);
            this.add(new Label("Delimiters:"), 0, 2);
            this.add(delimiterField, 1, 2);
            this.add(startFromLine, 1, 3);
            this.add(outputId, 0, 4);
            this.add(outputDescription, 1, 4);
            this.add(statusLabel, 1, 5);
        }
    }
}
