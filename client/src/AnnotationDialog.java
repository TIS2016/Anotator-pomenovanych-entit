import java.io.IOException;
import java.net.Socket;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class AnnotationDialog extends Dialog{
	public AnnotationDialog(Window owner) {
		  this.initOwner(owner);
	      this.setResizable(false);
	      this.setTitle("Annotation");
	      this.getDialogPane().setContent(new AnnotationPane(this));
	}
	
	private class AnnotationPane extends GridPane{

		public AnnotationPane(AnnotationDialog annotDialog) {
			DialogPane annotPane = annotDialog.getDialogPane();
			
			ComboBox categoryBox = new ComboBox();//FXCollections.observableArrayList("Category","Subcategoryxxxxx"));
			//categoryBox.getItems().addAll("...");  Add items to the list.
			final TextField links = new TextField("");
		    final TextArea description = new TextArea ("");
			
		    categoryBox.getSelectionModel().selectFirst();
		    
			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    annotPane.getButtonTypes().setAll(okButton, cancelBtn);
		    Button okBtn = (Button) annotPane.lookupButton(okButton);
		    okBtn.addEventFilter(ActionEvent.ACTION, event -> {
		    	if (categoryBox.getValue()==null){
			    	Alert alert = new Alert(Alert.AlertType.ERROR);
	                alert.setTitle("ERROR");
	                alert.setHeaderText("You need to create at least one category in order to Annotate !!!");
	                alert.showAndWait();
			    }
		    	
		    	
		    	
		    	
            });
		    
		    
		    
		    this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(new Label("Category: "), 0, 0);
            this.add(categoryBox, 1, 0);
            this.add(new Label("Links: "),0,1);
            this.add(links, 1, 1);
            this.add(new Label("Description: "), 0, 2);
            this.add(description, 1, 2);
		     
            if (categoryBox.getValue()==null){
		    	Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR");
                alert.setHeaderText("You haven't created any Categories yet !!!");
                alert.showAndWait();
		    }
		}
			
	}

	

}
