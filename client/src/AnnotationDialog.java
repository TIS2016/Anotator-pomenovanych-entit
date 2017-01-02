import javafx.beans.property.SimpleBooleanProperty;
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
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.wellbehaved.event.Nodes;

import java.util.stream.Collectors;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;

public class AnnotationDialog extends Dialog {


	public AnnotationDialog(Window owner, final StyledTextArea<Void, DisplayedTreeObject<?>> textArea) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
	    this.setTitle("New Annotation");
	    this.getDialogPane().setContent(new AnnotationPane(this, textArea, null));
	}

	public AnnotationDialog(Window owner, AnnotationObject annotationObject) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("Update Annotation");
        this.getDialogPane().setContent(new AnnotationPane(this, null, annotationObject));
    }
	
	private class AnnotationPane extends GridPane {

        private static final String DEFAULT_LINK_STRING = "...";

		public AnnotationPane(AnnotationDialog annotationDialog,
                              final StyledTextArea<Void, DisplayedTreeObject<?>> textArea,
							  final AnnotationObject annotationObject) {
            super();
			DialogPane annotPane = annotationDialog.getDialogPane();

            String text_, descriptionTextArea_ = "";
            ObservableList<String> links_ = FXCollections.observableArrayList();

            //final ChangeListener<? super String> listener = (observable, oldValue, newValue) -> annotationText.setText(newValue);

            if (annotationObject != null) {
                //annotationObject.nameProperty().addListener(listener); //??
                text_ = annotationObject.getName();
                links_ = FXCollections.observableList(annotationObject.getLinks());
                descriptionTextArea_ = annotationObject.getDescription();
            } else {
                text_ = textArea.getSelectedText();
            }

            ComboBox<TreeObject<?>> categoryBox = new ComboBox<>(new FilteredList<>(SessionData.sortedCategories,
                    treeObject -> treeObject instanceof CategoryObject && treeObject.getParent() != null));
            if (annotationObject != null) {
                categoryBox.getSelectionModel().select(annotationObject.getParent());
            } else {
                categoryBox.getSelectionModel().selectFirst();
            }

		    final TextArea descriptionTextArea = new TextArea();
            descriptionTextArea.setPrefRowCount(5);
            descriptionTextArea.setPrefColumnCount(12);
            descriptionTextArea.setText(descriptionTextArea_);

			final ListView<String> linkList = new ListView<>();
            linkList.setEditable(true);
            linkList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            linkList.setCellFactory(c -> new LinkListCell());
            linkList.setItems(links_);
            linkList.setPrefHeight(100);

            KeyCodeCombination selectAllCombination = new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN);
            Nodes.addInputMap(linkList, consume(keyPressed(selectAllCombination),
                              keyEvent -> linkList.getSelectionModel().selectAll()));

            linkList.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.DELETE) {
                    linkList.getItems().removeAll(linkList.getSelectionModel().getSelectedItems());
                    linkList.getSelectionModel().clearSelection();
                }
                keyEvent.consume();
            });

            final Label annotationText = new Label(text_);
            annotationText.setWrapText(true);
            annotationText.setMaxHeight(50);
            annotationText.prefWidthProperty().bind(linkList.widthProperty());

            final Button addLink = new Button("Add Link");
            addLink.setOnAction(actionEvent -> {
                linkList.getItems().addAll(AnnotationPane.DEFAULT_LINK_STRING);
                int last = linkList.getItems().size() - 1;
                linkList.layout();
                linkList.scrollTo(last);
                linkList.edit(last);
                linkList.getSelectionModel().clearSelection();
                actionEvent.consume();
            });

			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    annotPane.getButtonTypes().setAll(okButton, cancelBtn);
		    Button okBtn = (Button) annotPane.lookupButton(okButton);

			Button categoryButton = new Button("New Category");
			categoryButton.visibleProperty().bind(new SimpleBooleanProperty(true)); //TODO: visible only if has edit rights
			categoryButton.setOnAction(actionEvent -> {
				new CategoryDialog(annotationDialog.getOwner())
                        .showAndWait()
                        .filter(co -> co != null)
                        .ifPresent(co -> categoryBox.getSelectionModel().select(co));
				actionEvent.consume();
			});

			HBox categoryHBox = new HBox(categoryBox, categoryButton);
			categoryHBox.setSpacing(10);

            okBtn.disableProperty().bind(categoryBox.valueProperty().isNull());
			okBtn.setOnAction(actionEvent -> {
                if (annotationObject != null) { //update annotation
                    annotationObject.setStatus(DisplayedTreeObject.Status.UPDATING);
                    annotationObject.setLinks(linkList.getItems().stream().collect(Collectors.toList()));
                    annotationObject.setDescription(descriptionTextArea.getText());
                    annotationObject.changeParent(categoryBox.getValue());
                    annotationObject.setStatus(DisplayedTreeObject.Status.DEFAULT);
                } else { //create new annotation
                    CategoryObject parent = (CategoryObject) categoryBox.getValue();
                    AnnotationObject ao = new AnnotationObject(
                            textArea,
                            parent,
                            descriptionTextArea.getText(),
                            linkList.getItems().stream().collect(Collectors.toList()));
                    parent.add(ao);
                }
				actionEvent.consume();
			});

            VBox linksBox = new VBox(linkList, addLink);
            linksBox.setSpacing(10);

		    this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(new Label("Annotation Text: "), 0, 0);
            this.add(annotationText, 1, 0);
            this.add(new Label("Category: "), 0, 1);
			this.add(categoryHBox, 1, 1);
			this.add(new Label("Links: "), 0, 2);
            this.add(linksBox, 1, 2);
            this.add(new Label("Description: "), 0, 3);
            this.add(descriptionTextArea, 1, 3);

            if (annotationObject != null) {
                final ListView<ReferenceObject> referenceList = new ListView<>();
                referenceList.setEditable(true);
                referenceList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                referenceList.setCellFactory(lv -> new ReferenceCell(annotationDialog.getOwner()));
                referenceList.setPrefHeight(100);
                referenceList.setItems(annotationObject.getChildren());

                Nodes.addInputMap(referenceList, consume(keyPressed(selectAllCombination),
                        keyEvent -> referenceList.getSelectionModel().selectAll()));

                referenceList.setOnKeyPressed(keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.DELETE) {
                        FXCollections.observableArrayList(referenceList.getSelectionModel().getSelectedItems())
                                .forEach(ref -> ref.getParent().getChildren().remove(ref));
                        referenceList.getSelectionModel().clearSelection();
                    }
                    keyEvent.consume();
                });

                this.add(new Label("References: "), 0, 4);
                this.add(referenceList, 1, 4);
            }
		}
	}
}