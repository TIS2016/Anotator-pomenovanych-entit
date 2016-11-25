
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class GroupsDialog extends Dialog {
	public GroupsDialog(Window owner){
		this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Add Annotation Type");
	    this.getDialogPane().setContent(new JoinPane(this));
	}
	private class JoinPane extends GridPane{
		public JoinPane(GroupsDialog groupsDialog) {
			/* TODO Auto-generated constructor stub
			 * GroupList
			 * RightClick :		Join
			 * 					Info
			 * 					Delete
			 * searchBar
			 * CancelButton
			 * 
			 * Join With Password Dialog
			 * 
			 */
		}
	}

}
