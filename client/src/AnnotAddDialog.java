import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class AnnotAddDialog extends Dialog {
	public AnnotAddDialog(Window owner) {
		this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Annotation");
	    this.getDialogPane().setContent(new AnnotAddPane(this));
	}
	
	private class AnnotAddPane extends GridPane{

		public AnnotAddPane(AnnotAddDialog annotAddDialog) {
			DialogPane annotAddPane = annotAddDialog.getDialogPane();
			
			
			
			ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    annotAddPane.getButtonTypes().setAll(okButton, cancelBtn);
		    
		    
		    
		     
		     
		}
			
	}
}
