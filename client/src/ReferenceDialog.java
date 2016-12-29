import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.fxmisc.richtext.StyledTextArea;

/**
 * Created by michal on 12/28/16.
 */
public class ReferenceDialog extends Dialog {

    public ReferenceDialog(Window owner, StyledTextArea<Void, DisplayedTreeObject<?>> textArea) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("New Reference");
        this.getDialogPane().setContent(new ReferencePane(this, textArea, null));
    }

    public ReferenceDialog(Window owner, ReferenceObject referenceObject) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("Update Reference");
        this.getDialogPane().setContent(new ReferencePane(this, null, referenceObject));
    }

    private class ReferencePane extends GridPane {

        public ReferencePane(final ReferenceDialog referenceDialog,
                             final StyledTextArea<Void, DisplayedTreeObject<?>> textArea,
                             final ReferenceObject referenceObject) {
            super();
            DialogPane categoryPane = referenceDialog.getDialogPane();
            ObservableList<TreeObject<?>> annotationObjects = new FilteredList<>(SessionData.treeObjects,
                    treeObject -> treeObject instanceof AnnotationObject);

            String text_ = referenceObject != null ? referenceObject.getName() : textArea.getSelectedText();

            Label referenceText = new Label(String.format("\"%s\"",
                    text_.substring(0, Math.min(text_.length(), TreeObject.MAX_DISPLAYED_LEGTH))));

            ComboBox<TreeObject<?>> annotationBox = new ComboBox<>(annotationObjects);
            if (referenceObject != null)
                annotationBox.getSelectionModel().select(referenceObject.getParent());
            else
                annotationBox.getSelectionModel().selectFirst();

            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            categoryPane.getButtonTypes().setAll(okButton, cancelBtn);

            Button okBtn = (Button) categoryPane.lookupButton(okButton);
            okBtn.disableProperty().bind(annotationBox.valueProperty().isNull());

            okBtn.setOnAction(actionEvent -> {
                if (referenceObject != null) { //update
                    referenceObject.setStatus(DisplayedTreeObject.Status.UPDATING);
                    referenceObject.changeParent((AnnotationObject) annotationBox.getValue());
                    referenceObject.setStatus(DisplayedTreeObject.Status.DEFAULT);
                } else { //create
                    AnnotationObject parent = (AnnotationObject) annotationBox.getValue();
                    ReferenceObject ro = new ReferenceObject(textArea, parent);
                    parent.add(ro);
                }
                actionEvent.consume();
            });

            this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(new Label("Reference text: "), 0, 0);
            this.add(referenceText, 1, 0);
            this.add(new Label("Reference of: "), 0, 1);
            this.add(annotationBox, 1, 1);
        }
    }
}
