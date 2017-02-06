package org.ape.layout.dialogs;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.ape.annotations.treeObjects.AnnotationObject;
import org.ape.annotations.treeObjects.CoreferenceObject;
import org.ape.annotations.treeObjects.DisplayedTreeObject;
import org.ape.annotations.treeObjects.TreeObject;
import org.ape.AppData;

public class CoreferenceDialog extends Dialog {

    public CoreferenceDialog(Window owner, CoreferenceObject coreference) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle(coreference == null ? "New Coreference" : "Update Coreference");
        this.getDialogPane().setContent(new ReferencePane(this, coreference));
    }

    private class ReferencePane extends GridPane {

        private ReferencePane(final CoreferenceDialog coreferenceDialog,
                              final CoreferenceObject coreference) {
            super();
            final DialogPane categoryPane = coreferenceDialog.getDialogPane();
            final ObservableList<TreeObject<?>> annotationObjects = new FilteredList<>(AppData.treeObjects,
                    treeObject -> treeObject instanceof AnnotationObject);


            final Label referenceText = new Label();
            referenceText.setWrapText(true);
            referenceText.setMaxHeight(50);

            final ComboBox<TreeObject<?>> annotationBox = new ComboBox<>(annotationObjects);
            if (coreference != null) {
                referenceText.setText(coreference.getTreeName());
                annotationBox.getSelectionModel().select(coreference.getParent());
            } else {
                referenceText.setText(AppData.textArea.getSelectedText());
                annotationBox.getSelectionModel().selectFirst();
            }
            referenceText.prefWidthProperty().bind(annotationBox.prefWidthProperty());

            annotationBox.setPrefWidth(150);
            annotationBox.setMaxHeight(25);
            annotationBox.setCellFactory(lv -> {
                ComboBoxListCell<TreeObject<?>> cell = new ComboBoxListCell<>();
                cell.setPrefSize(150, 25);
                return cell;
            });

            final ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            final ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            categoryPane.getButtonTypes().setAll(okButton, cancelBtn);

            Button okBtn = (Button) categoryPane.lookupButton(okButton);
            okBtn.disableProperty().bind(annotationBox.valueProperty().isNull());

            okBtn.setOnAction(actionEvent -> {
                if (coreference != null) {
                    coreference.setStatus(TreeObject.Status.UPDATE);
                    coreference.update(annotationBox.getValue());
                    coreference.setStatus(DisplayedTreeObject.Status.DEFAULT);
                } else {
                    new CoreferenceObject(AppData.id++, (AnnotationObject) annotationBox.getValue());
                    AppData.textArea.deselect();
                }
                actionEvent.consume();
            });

            this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(new Label("Text:"), 0, 0);
            this.add(referenceText, 1, 0);
            this.add(new Label("Refers to:"), 0, 1);
            this.add(annotationBox, 1, 1);
        }
    }
}