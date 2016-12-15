import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class AnnotationDialog extends Dialog{
	public AnnotationDialog(Window owner) {
		  this.initOwner(owner);
	      this.setResizable(false);
	      this.setTitle("New annotation");
	      this.getDialogPane().setContent(new AnnotationPane(this));
	}
	
	private class AnnotationPane extends GridPane{

		public AnnotationPane(AnnotationDialog annotDialog) {
			DialogPane annotPane = annotDialog.getDialogPane();
			
			ComboBox<TreeObject<?>> categoryBox = new ComboBox<>(AnnotationTree.categories);
			final TextField links = new TextField("");
		    final TextArea description = new TextArea ("");
			
		    categoryBox.getSelectionModel().selectFirst();
		    
			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    annotPane.getButtonTypes().setAll(okButton, cancelBtn);
		    Button okBtn = (Button) annotPane.lookupButton(okButton);

			Button categoryButton = new Button("New category");
			categoryButton.visibleProperty().bind(new SimpleBooleanProperty(true)); //TODO: visible only if has edit rights
			categoryButton.setOnAction(actionEvent -> {
				new CategoryDialog(annotDialog.getOwner()).showAndWait();
				actionEvent.consume();
			});

			categoryBox.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
				if (newValue.getParent() == null) {
					new CategoryDialog(annotDialog.getOwner()).showAndWait();
				}
			}));
			VBox categoryVBox = new VBox(categoryBox, categoryButton);
			categoryVBox.setSpacing(10);

			okBtn.setOnAction(actionEvent -> {
				System.out.println("BIND ME -- ANNOTATE");
				actionEvent.consume();
			});

		    this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(new Label("Category: "), 0, 0);
            this.add(categoryVBox, 1, 0);
            this.add(new Label("Links: "),0,1);
            this.add(links, 1, 1);
            this.add(new Label("Description: "), 0, 2);
            this.add(description, 1, 2);

		}
			
	}

	

}
