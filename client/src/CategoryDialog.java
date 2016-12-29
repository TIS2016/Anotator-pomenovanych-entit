import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;


public class CategoryDialog extends Dialog<CategoryObject> {

	public CategoryDialog(Window owner) {
        super();
        this.initOwner(owner);
	    this.setResizable(false);
	    this.getDialogPane().setContent(new CategoryPane(this, null));
	}

	public CategoryDialog(Window owner, CategoryObject categoryObject) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.getDialogPane().setContent(new CategoryPane(this, categoryObject));
    }
	
	private class CategoryPane extends GridPane {

		public CategoryPane(CategoryDialog categoryDialog,
                            final CategoryObject categoryObject) {
            super();
            DialogPane categoryPane = categoryDialog.getDialogPane();
            SimpleBooleanProperty invalidName = new SimpleBooleanProperty(true);
            SimpleBooleanProperty invalidTag = new SimpleBooleanProperty(true);
            SimpleBooleanProperty invalidColor = new SimpleBooleanProperty(true);

            String name_ = "", tag_ = "";
            Color color_ = Color.WHITE;
            categoryDialog.setTitle("New Category");

            if (categoryObject != null) {
                name_ = categoryObject.getName();
                tag_ = categoryObject.getTag();
                color_ = categoryObject.getColor();
                categoryDialog.setTitle("Update Category");
            }

			final TextField name = new TextField();
			name.textProperty().addListener((ov, oldV, newV) -> invalidName.set(newV.trim().isEmpty()));
            name.setText(name_);
			
		    final TextField tag = new TextField();
		    tag.textProperty().addListener((ov, oldV, newV) -> invalidTag.set(newV.trim().isEmpty()));
            tag.setText(tag_);

		    final ColorPicker colorPicker = new ColorPicker();
            colorPicker.setValue(color_);

            invalidColor.bind(colorPicker.valueProperty().isEqualTo(Color.WHITE));

		    ComboBox<TreeObject<?>> categoryBox = new ComboBox<>(new FilteredList<>(SessionData.sortedCategories,
                    treeObject -> !treeObject.isOnPathToRoot(categoryObject)));
		    if (categoryObject == null)
		        categoryBox.getSelectionModel().selectFirst(); //creating by not clicking on node
            else
                categoryBox.getSelectionModel().select(categoryObject.getParent());

			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    categoryPane.getButtonTypes().setAll(okButton, cancelBtn);
		    
		    Button okBtn = (Button) categoryPane.lookupButton(okButton);
		    okBtn.disableProperty().bind(invalidColor.or(invalidName.or(invalidTag)));

            categoryDialog.setResultConverter(buttonType -> {
                if (buttonType == okButton) {
                    if (categoryObject == null) {
                        CategoryObject co = new CategoryObject(name.getText().trim(),
                                tag.getText(),
                                colorPicker.getValue());
                        ((CategoryObject) categoryBox.getValue()).getChildren().add(co);
                        return co;
                    }
                    categoryObject.setFullName(name.getText().trim());
                    categoryObject.setTag(tag.getText().trim());
                    categoryObject.setColor(colorPicker.getValue());
                    categoryObject.changeParent((CategoryObject) categoryBox.getValue());
                }
                return null;
            });

			/*okBtn.setOnAction(actionEvent -> {
                if (categoryObject != null) {
                    categoryObject.setFullName(name.getText().trim());
                    categoryObject.setTag(tag.getText().trim());
                    categoryObject.setColor(colorPicker.getValue());
                    categoryObject.changeParent((CategoryObject) categoryBox.getValue());
                } else {
                    ((CategoryObject) categoryBox.getValue()).
                            getChildren().add(new CategoryObject(name.getText().trim(),
                                                                 tag.getText(),
                                                                 colorPicker.getValue()));
                }
				actionEvent.consume();
			});*/

		    this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);
            this.add(new Label("Name: "), 0, 0);
            this.add(name, 1, 0);
            this.add(new Label("Tag: "),0,1);
            this.add(tag, 1, 1);
            this.add(new Label("Color: "), 0, 2);
            this.add(colorPicker, 1, 2);
            this.add(new Label("Subcategory of: "), 0, 3);
            this.add(categoryBox, 1, 3);
		}
	}
}