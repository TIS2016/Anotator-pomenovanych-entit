
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class GroupCreateDialog extends Dialog {
	public GroupCreateDialog(Window owner){
		this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Add Annotation Type");
	    this.getDialogPane().setContent(new CreatePane(this));
		
	}
	private class CreatePane extends GridPane{

		public CreatePane(GroupCreateDialog groupCreateDialog) {
			
		}
		
		
		
		
	}
	
	

}
