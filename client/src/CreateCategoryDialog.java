
import java.awt.Color;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

public class CreateCategoryDialog extends Dialog {
	public CreateCategoryDialog(Window owner) {
		this.initOwner(owner);
	    this.setResizable(false);
	    this.setTitle("Add Annotation Type");
	    this.getDialogPane().setContent(new CategoryPane(this));
	}
	
	private class CategoryPane extends GridPane{

		public CategoryPane(CreateCategoryDialog crtCatDialog) {
			DialogPane categoryPane = crtCatDialog.getDialogPane();
			
			 SimpleBooleanProperty invalidName = new SimpleBooleanProperty();
	         invalidName.set(true);
	         SimpleBooleanProperty invalidTag = new SimpleBooleanProperty();
	         invalidTag.set(true);
	         SimpleBooleanProperty invalidColor = new SimpleBooleanProperty();
	         invalidColor.set(true);
			
			final TextField name = new TextField("");
			name.textProperty().addListener((ov, oldV, newV) -> {
                invalidName.set(newV.trim().isEmpty());
			});
			
		    final TextField tag = new TextField ("");
		    tag.textProperty().addListener((ov, oldV, newV) -> {
                invalidTag.set(newV.trim().isEmpty());
			});
		    final ColorPicker colorPicker = new ColorPicker();
		    colorPicker.setOnAction(e -> {
		    	
		    	invalidColor.set(
				    	javafx.scene.paint.Color.WHITE.equals(colorPicker.getValue()));
		    });
		    
		    ComboBox categoryBox = new ComboBox(FXCollections.observableArrayList("<<W/O Parent Type>>"));
			
			//categoryBox.getItems().addAll("...");  Add items to the list.
		    categoryBox.getSelectionModel().selectFirst();
			
			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    categoryPane.getButtonTypes().setAll(okButton, cancelBtn);
		    
		    Button okBtn = (Button) categoryPane.lookupButton(okButton);
		    okBtn.disableProperty().bind(invalidColor.or(invalidName.or(invalidTag)));
		    okBtn.addEventFilter(ActionEvent.ACTION, event -> {
		    	
		    	
		    	
		    	
		    	
            });
		    
		    
		    this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(new Label("Name: "), 0, 0);
            this.add(name, 1, 0);
            this.add(new Label("Tag: "),0,1);
            this.add(tag, 1, 1);
            this.add(new Label("Color: "), 0, 2);
            this.add(colorPicker, 1, 2);
            this.add(new Label("Subtype of: "), 0, 3);
            this.add(categoryBox, 1, 3);
		     
		}
			
	}
}
