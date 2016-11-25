
import javafx.beans.binding.Bindings;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class GroupsDialog extends Dialog {
	public GroupsDialog(Window owner){
		this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Group List");
	    this.getDialogPane().setContent(new JoinPane(this));
	}
	private class JoinPane extends GridPane{
		public JoinPane(GroupsDialog groupsDialog) {
			DialogPane joinPane = groupsDialog.getDialogPane();
			
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
			
			ListView<String> listView = new ListView<>();
	        listView.getItems().addAll("One", "Two", "Three");

	        listView.setCellFactory(lv -> {

	            ListCell<String> cell = new ListCell<>();

	            ContextMenu contextMenu = new ContextMenu();

	            //       \"%s\"  == nazov Itemu
	            MenuItem editItem = new MenuItem();
	            editItem.textProperty().bind(Bindings.format("Join", cell.itemProperty()));
	            editItem.setOnAction(event -> {
	                String item = cell.getItem();
	                System.out.println("Bind Edit_Item");
	                event.consume();
	            });
	            MenuItem infoItem = new MenuItem();
	            infoItem.textProperty().bind(Bindings.format("Info", cell.itemProperty()));
	            infoItem.setOnAction(event -> {
	                String item = cell.getItem();
	                System.out.println("Bind Info_Item");
	            });
	            MenuItem deleteItem = new MenuItem();
	            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
	            deleteItem.setOnAction(event -> listView.getItems().remove(cell.getItem()));
	            contextMenu.getItems().addAll(editItem,infoItem, deleteItem);

	            cell.textProperty().bind(cell.itemProperty());

	            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
	                if (isNowEmpty) {
	                    cell.setContextMenu(null);
	                } else {
	                    cell.setContextMenu(contextMenu);
	                }
	            });
	            return cell ;
	        });

	        this.add(listView, 0, 0);
			
			
			
			ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    joinPane.getButtonTypes().setAll(cancelBtn);
		   
		}
	}

}
