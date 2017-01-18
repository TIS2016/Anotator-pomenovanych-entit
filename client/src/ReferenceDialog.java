import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/**
 * Created by michal on 12/28/16.
 */
public class ReferenceDialog extends Dialog {

    public ReferenceDialog(Window owner) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("New Reference");
        this.getDialogPane().setContent(new ReferencePane(this, null));
    }

    public ReferenceDialog(Window owner, ReferenceObject referenceObject) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle("Update Reference");
        this.getDialogPane().setContent(new ReferencePane(this, referenceObject));
    }

    private class ReferencePane extends GridPane {

        public ReferencePane(final ReferenceDialog referenceDialog,
                             final ReferenceObject referenceObject) {
            super();
            DialogPane categoryPane = referenceDialog.getDialogPane();
            ObservableList<TreeObject<?>> annotationObjects = new FilteredList<>(SessionData.treeObjects,
                    treeObject -> treeObject instanceof AnnotationObject);


            Label referenceText = new Label();
            referenceText.setWrapText(true);
            referenceText.setEllipsisString("...\"");
            referenceText.setMaxHeight(50);

            ComboBox<TreeObject<?>> annotationBox = new ComboBox<>(annotationObjects);
            if (referenceObject != null) {
                referenceText.setText(referenceObject.getName());
                annotationBox.getSelectionModel().select(referenceObject.getParent());
            } else {
                referenceText.setText(MainLayout.textArea.getSelectedText());
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

            ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            categoryPane.getButtonTypes().setAll(okButton, cancelBtn);

            Button okBtn = (Button) categoryPane.lookupButton(okButton);
            okBtn.disableProperty().bind(annotationBox.valueProperty().isNull());

            okBtn.setOnAction(actionEvent -> {
                if (referenceObject != null) { //update
                    referenceObject.setStatus(DisplayedTreeObject.Status.UPDATING);
                    referenceObject.changeParent(annotationBox.getValue());
                    referenceObject.setStatus(DisplayedTreeObject.Status.DEFAULT);
                } else { //create
                    AnnotationObject parent = (AnnotationObject) annotationBox.getValue();
                    ReferenceObject ro = new ReferenceObject(parent);
                    parent.add(ro);
                    MainLayout.textArea.deselect();
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