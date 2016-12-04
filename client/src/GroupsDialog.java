
import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import javax.swing.text.AbstractDocument;

/*
*Used code from: http://stackoverflow.com/questions/28264907/javafx-listview-contextmenu
*/

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
			 * Bonus:: searchBar
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
	            MenuItem joinItem = new MenuItem();
	            joinItem.textProperty().bind(Bindings.format("Join", cell.itemProperty()));
	            joinItem.setOnAction(event -> {
	                String item = cell.getItem();

	                if (true) { // IF password protected THEN open password dialog ELSE set active group

						JoinPassDialog joinpass = new JoinPassDialog(groupsDialog.getOwner(), item.toString());
						joinpass.showAndWait();
					}
					else{

						}
	                System.out.println("Bind Join_Item");
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
	            contextMenu.getItems().addAll(joinItem,infoItem, deleteItem);

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
	private class JoinPassDialog extends Dialog{
		public JoinPassDialog(Window owner, String groupName){
			this.initOwner(owner);
			this.setResizable(false);
			this.setTitle("Join "+ groupName);

			DialogPane dialogPane = this.getDialogPane();
			GridPane root = new GridPane();

			CheckBox showPassword = new CheckBox("Unmask");
			showPassword.setSelected(false);
			PasswordField password = new PasswordField();
			password.setSkin(new PasswordFieldSkin(password, showPassword));
			showPassword.selectedProperty().addListener((observable, oldValue, newValue) -> {
				password.setText(password.getText());
			});

			SimpleBooleanProperty passNotOk = new SimpleBooleanProperty(true);
			passNotOk.bind(password.textProperty().isEmpty());

			Label passwordLabel = new Label("Password:");

			root.setAlignment(Pos.CENTER);
			root.setPadding(new Insets(10));
			root.setHgap(5);
			root.setVgap(5);
			root.add(passwordLabel, 0, 0);
			root.add(password, 1, 0);
			root.add(showPassword, 0, 1);
			dialogPane.setContent(root);

			ButtonType okButton = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
			ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
			dialogPane.getButtonTypes().setAll(okButton, cancelBtn);

			Button okBtn = (Button) dialogPane.lookupButton(okButton);
			okBtn.disableProperty().bind(passNotOk);
			okBtn.addEventFilter(ActionEvent.ACTION, event -> {
				if (true) { //we failed to log in or register, consume this event
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("ERROR");
					alert.setHeaderText("TODO: send data to server...");
					alert.showAndWait();
					event.consume();
				}
			});
		}

		private class PasswordFieldSkin extends TextFieldSkin {

			private SimpleBooleanProperty isSkinSet = new SimpleBooleanProperty();
			public static final char BULLET = '\u2022';

			public PasswordFieldSkin(TextField textField, CheckBox noSkinSet) {
				super(textField);
				isSkinSet.bind(noSkinSet.selectedProperty().not());
			}

			@Override
			protected String maskText(String txt) {

				TextField textField = this.getSkinnable();
				if (isSkinSet != null && this.isSkinSet.get() && textField instanceof PasswordField) {
					int n = textField.getLength();
					StringBuilder pwBuilder = new StringBuilder(n);
					for (int i = 0; i < n; i++)
						pwBuilder.append(BULLET);
					return pwBuilder.toString();
				}
				return txt;
			}
		}
	}
}
