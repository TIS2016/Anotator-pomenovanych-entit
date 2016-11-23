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
			
			
			
			ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    annotPane.getButtonTypes().setAll(okButton, cancelBtn);
		    
		    
		    
		     
		     
		}
			
	}

	

}
