package org.ape.layout.dialogs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import org.ape.annotations.treeObjects.*;
import org.ape.control.Controller;
import org.ape.AppData;
import org.ape.layout.cells.CoreferenceCell;
import org.ape.layout.cells.LinkListCell;
import org.fxmisc.wellbehaved.event.Nodes;

import java.util.stream.Collectors;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

public class AnnotationDialog extends Dialog {

	public AnnotationDialog(Window owner, AnnotationObject annotationObject) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle(annotationObject == null ? "New Annotation" : "Update Annotation");
        this.getDialogPane().setContent(new AnnotationPane(this, annotationObject));
    }
	
	private class AnnotationPane extends GridPane {

        private static final String NEW_URL = "...";

		AnnotationPane(AnnotationDialog annotationDialog,
							  final AnnotationObject annotationObject) {
            super();
			final DialogPane annotPane = annotationDialog.getDialogPane();
            final ComboBox<TreeObject<?>> categoryBox = new ComboBox<>(new FilteredList<>(AppData.sortedCategories,
                    treeObject -> treeObject instanceof CategoryObject && treeObject.getParent() != null));

            String text_, descriptionText_ = "";
            ObservableList<String> links_ = FXCollections.observableArrayList();

            if (annotationObject != null) {
                text_ = annotationObject.getTreeName();
                links_ = FXCollections.observableList(annotationObject.getUrls());
                descriptionText_ = annotationObject.getDescription();
                categoryBox.getSelectionModel().select(annotationObject.getParent());
            } else {
                text_ = AppData.textArea.getSelectedText();
                categoryBox.getSelectionModel().selectFirst();
            }

		    final TextArea description = new TextArea();
            description.setPrefRowCount(5);
            description.setPrefColumnCount(10);
            description.setText(descriptionText_);

			final ListView<String> urlsList = new ListView<>(links_);
            urlsList.setEditable(true);
            urlsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            urlsList.setCellFactory(c -> new LinkListCell());
            urlsList.setPrefHeight(100);

            final KeyCodeCombination selectAll = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
            Nodes.addInputMap(urlsList, consume(keyPressed(selectAll),
                              keyEvent -> urlsList.getSelectionModel().selectAll()));

            urlsList.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.DELETE) {
                    urlsList.getItems().removeAll(urlsList.getSelectionModel().getSelectedItems());
                    urlsList.getSelectionModel().clearSelection();
                }
                keyEvent.consume();
            });

            final Label annotationText = new Label(text_);
            annotationText.setWrapText(true);
            annotationText.setMaxHeight(50);
            annotationText.prefWidthProperty().bind(urlsList.widthProperty());

            final Button addUrl = new Button("Add URL");
            addUrl.setOnAction(actionEvent -> {
                urlsList.getItems().addAll(AnnotationPane.NEW_URL);
                int last = urlsList.getItems().size() - 1;
                urlsList.layout();
                urlsList.scrollTo(last);
                urlsList.edit(last);
                urlsList.getSelectionModel().clearSelection();
                actionEvent.consume();
            });

			final ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    final ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    annotPane.getButtonTypes().setAll(okButton, cancelBtn);
		    final Button okBtn = (Button) annotPane.lookupButton(okButton);

			final Button categoryButton = new Button("New Category");
			categoryButton.setOnAction(actionEvent -> {
				new CategoryDialog(annotationDialog.getOwner(), null)
                        .showAndWait()
                        .filter(co -> co != null)
                        .ifPresent(co -> categoryBox.getSelectionModel().select((co)));
				actionEvent.consume();
			});

			final HBox categoryHBox = new HBox(categoryBox, categoryButton);
			categoryHBox.setSpacing(10);

            okBtn.disableProperty().bind(categoryBox.valueProperty().isNull());
			okBtn.setOnAction(actionEvent -> {
                if (annotationObject != null) {
                    annotationObject.update(
                            urlsList.getItems().stream().collect(Collectors.toList()),
                            description.getText(),
                            categoryBox.getValue());
                } else {
                    new AnnotationObject(
                            AppData.id++,
                            (CategoryObject) categoryBox.getValue(),
                            description.getText(),
                            urlsList.getItems().stream().collect(Collectors.toList())
                    );
                    AppData.textArea.deselect();
                }
				actionEvent.consume();
			});

            final VBox linksBox = new VBox(urlsList, addUrl);
            linksBox.setSpacing(10);

		    this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);

            this.add(new Label("Text:"), 0, 0);
            this.add(annotationText, 1, 0);
            this.add(new Label("Category:"), 0, 1);
			this.add(categoryHBox, 1, 1);
			this.add(new Label("Urls:"), 0, 2);
            this.add(linksBox, 1, 2);
            this.add(new Label("Description:"), 0, 3);
            this.add(description, 1, 3);

            if (annotationObject != null) {
                final ListView<CoreferenceObject> referenceList = new ListView<>(annotationObject.getChildren());
                referenceList.setEditable(true);
                referenceList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                referenceList.setCellFactory(lv -> new CoreferenceCell());
                referenceList.setPrefHeight(100);

                Nodes.addInputMap(referenceList, consume(keyPressed(selectAll),
                        keyEvent -> referenceList.getSelectionModel().selectAll()));

                referenceList.setOnKeyPressed(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.DELETE) {
                        Controller.deleteTreeObject(referenceList.getItems().toArray(new TreeObject<?>[]{}));
                        referenceList.getItems().removeAll(referenceList.getSelectionModel().getSelectedItems());
                        referenceList.getSelectionModel().clearSelection();
                        referenceList.refresh();
                    }
                    keyEvent.consume();
                });

                this.add(new Label("Coreferences:"), 0, 4);
                this.add(referenceList, 1, 4);
            }
		}
	}
}