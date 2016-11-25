
import java.io.File;

import javax.swing.JFileChooser;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class GroupCreateDialog extends Dialog {
	public GroupCreateDialog(Window owner){
		this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Create Group Formular");
	    this.getDialogPane().setContent(new CreatePane(this));
		
	}
	private class CreatePane extends GridPane{

		public CreatePane(GroupCreateDialog groupCreateDialog) {
			DialogPane crtPane = groupCreateDialog.getDialogPane();
			
			SimpleBooleanProperty invalidName = new SimpleBooleanProperty();
	        invalidName.set(true);
	        SimpleBooleanProperty invalidDoc = new SimpleBooleanProperty();
	        invalidDoc.set(true);
	        SimpleBooleanProperty invalidPrivilege = new SimpleBooleanProperty();
	        invalidPrivilege.set(true);
			
			final TextField name = new TextField("");
			name.textProperty().addListener((ov, oldV, newV) -> {
               invalidName.set(newV.trim().isEmpty());
			});
						
			final TextArea privileges = new TextArea ("user1:priv; user2:priv;");
			privileges.textProperty().addListener(e->{
				invalidPrivilege.set(privileges.getText().isEmpty());
			});
			
			final TextField defaultPriv = new TextField("");
			final TextField  password= new TextField("");
			
			final Button openButton = new Button("Upload File");
	 
	        openButton.setOnMouseClicked(e->{
	        	FileChooser fileChooser = new FileChooser();
	        	fileChooser.setTitle("Open Resource File");
	        	fileChooser.getExtensionFilters().addAll(
	                    new FileChooser.ExtensionFilter("Text Files *.txt", "*.txt")
	                );
	        	File file = fileChooser.showOpenDialog(((Node)e.getTarget()).getScene().getWindow());
                if (file != null) {
                	invalidDoc.set(file==null);
                	System.out.println(file.getName());
                
                }
	        });
			
			
			
			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    crtPane.getButtonTypes().setAll(okButton, cancelBtn);
		    Button okBtn = (Button) crtPane.lookupButton(okButton);
		    okBtn.disableProperty().bind(invalidName.or(invalidPrivilege).or(invalidDoc));
		    okBtn.addEventFilter(ActionEvent.ACTION, event -> {
		    	
		    	
		    	
		    	
		    	
            });
			
			
			
		    this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(new Label("Group Name: "), 0, 0);
            this.add(name, 1, 0);
            this.add(new Label("Upload Document: "),0,1);
            this.add(openButton, 1, 1);
            this.add(new Label("Set User Privileges: "), 0, 2);
            this.add(privileges, 1, 2);
            this.add(new Label("Default Privileges: "), 0, 3);
            this.add(defaultPriv, 1, 3);
            this.add(new Label("Group Password: "), 0, 4);
            this.add(password, 1, 4);
			
		}
		
	}

}
