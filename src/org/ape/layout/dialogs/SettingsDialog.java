package org.ape.layout.dialogs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.ape.control.Controller;
import org.ape.AppData;

import java.io.File;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SettingsDialog extends Dialog {

    public SettingsDialog(Window owner) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("Settings");
        this.getDialogPane().setContent(new SettingsPane(this));
    }

    private class SettingsPane extends GridPane {

        SettingsPane(SettingsDialog dialog) {
            final DialogPane settingsPane = dialog.getDialogPane();

            final SimpleObjectProperty<Pattern> pattern = new SimpleObjectProperty<>();
            SimpleObjectProperty<File> startFile = new SimpleObjectProperty<>(null);

            final ComboBox<String> fontFamily = new ComboBox<>();
            fontFamily.getItems().setAll(Font.getFamilies());
            fontFamily.getSelectionModel().select(Controller.getFontFamily());

            final ComboBox<Integer> fontSize = new ComboBox<>();
            fontSize.getItems().setAll(IntStream.range(AppData.MIN_FONT_SIZE, AppData.MAX_FONT_SIZE + 1).boxed().collect(Collectors.toList()));
            fontSize.getSelectionModel().select(new Integer(Controller.getFontSize()));

            final CheckBox wrapText = new CheckBox("Wrap text");
            wrapText.setSelected(Controller.isWrap());

            final ToggleGroup toggleGroup = new ToggleGroup();

            final RadioButton autoSelectAlways = new RadioButton("Always");
            autoSelectAlways.setToggleGroup(toggleGroup);
            final RadioButton autoSelectEmpty = new RadioButton("Empty selection");
            autoSelectEmpty.setToggleGroup(toggleGroup);

            toggleGroup.selectToggle(Controller.isAutoSelectAlways() ? autoSelectAlways : autoSelectEmpty);

            final HBox autoSelectBox = new HBox(autoSelectAlways, autoSelectEmpty);
            autoSelectBox.setSpacing(10);
            autoSelectBox.setAlignment(Pos.CENTER_LEFT);

            final CheckBox promptExit = new CheckBox("Show exit dialog");
            promptExit.setSelected(Controller.isPromptExit());
            final CheckBox promptDel = new CheckBox("Show delete dialog");
            promptDel.setSelected(Controller.isPromptDel());

            final TextField tokenField = new TextField();
            tokenField.setPromptText("Pattern");

            tokenField.textProperty().addListener(((observable, oldValue, newValue) -> {
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
            tokenField.setText(Controller.getTokenPattern().toString());

            final Label statusLabel = new Label("Invalid pattern");
            statusLabel.visibleProperty().bind(pattern.isNull());
            statusLabel.setTextFill(Color.RED);

            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Project");

            final CheckBox autoStart = new CheckBox("Open project on start");
            final Button selectFileButton = new Button("Select file");
            selectFileButton.setOnAction(actionEvent -> {
                final File file = fileChooser.showOpenDialog(((Node) actionEvent.getSource()).getScene().getWindow());
                if (file != null) {
                    startFile.set(file);
                }
                actionEvent.consume();
            });
            startFile.addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    if (!newValue.exists() || newValue.canWrite() || !newValue.setWritable(true)) {
                        selectFileButton.setText(startFile.get().getName());
                        return;
                    }
                }
                selectFileButton.setText("Select file");
            }));

            final String pathName = Controller.getStartFilePath();
            if (!pathName.isEmpty()) {
                startFile.set(new File(pathName));
                autoStart.setSelected(true);
            }

            final HBox selectFileBox = new HBox(selectFileButton, statusLabel);
            selectFileBox.setSpacing(10);
            selectFileBox.setAlignment(Pos.CENTER_LEFT);

            selectFileButton.disableProperty().bind(autoStart.selectedProperty().not());

            final ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            final ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            settingsPane.getButtonTypes().setAll(okButton, cancelBtn);

            final Button okBtn = (Button) settingsPane.lookupButton(okButton);
            okBtn.disableProperty().bind(pattern.isNull().or(autoStart.selectedProperty().and(startFile.isNull())));
            okBtn.setOnAction(actionEvent -> {
                Controller.updateProperties(
                        fontFamily.getValue(), fontSize.getValue(), pattern.get(),
                        autoStart.isSelected() ? startFile.get().getAbsolutePath() : "",
                        toggleGroup.getSelectedToggle() == autoSelectAlways ? "always" : "empty",
                        wrapText.isSelected(), promptExit.isSelected(), promptDel.isSelected());
                actionEvent.consume();
            });

            this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);

            this.add(new Label("Font family:"), 0, 0);
            this.add(fontFamily, 1, 0);
            this.add(new Label("Font size:"), 0, 1);
            this.add(fontSize, 1, 1);
            this.add(wrapText, 0, 2);
            this.add(new Label("Auto-select:"), 0, 3);
            this.add(autoSelectBox, 1, 3);
            this.add(new Label("Tokens:"), 0, 4);
            this.add(tokenField, 1, 4);
            this.add(autoStart, 0, 5);
            this.add(selectFileBox, 1, 5);
            this.add(promptDel, 0, 6);
            this.add(promptExit, 0, 7);
        }
    }
}