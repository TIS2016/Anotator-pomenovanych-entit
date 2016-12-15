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

import java.util.HashSet;
import java.util.logging.Filter;

public class CategoryDialog extends Dialog {

	public CategoryDialog(Window owner) {
		this.initOwner(owner);
	    this.setResizable(false);
	    this.getDialogPane().setContent(new CategoryPane(this, null, false));
	}

	public CategoryDialog(Window owner, CategoryObject category, boolean updating) {
        this.initOwner(owner);
        this.setResizable(false);
        this.getDialogPane().setContent(new CategoryPane(this, category, updating));
    }
	
	private class CategoryPane extends GridPane {

		public CategoryPane(CategoryDialog catDialog,
                            CategoryObject category,
                            boolean updating)  {
            DialogPane categoryPane = catDialog.getDialogPane();
            String name_ = "", tag_ = "";
            Color color_ = Color.WHITE;
            catDialog.setTitle("New category");

            if (category != null && updating) {
                name_ = category.getName();
                tag_ = category.getTag();
                color_ = category.getColor();
                catDialog.setTitle("Update category");
            }

            SimpleBooleanProperty invalidName = new SimpleBooleanProperty();
	        invalidName.set(true);
	        SimpleBooleanProperty invalidTag = new SimpleBooleanProperty();
	        invalidTag.set(true);
	        SimpleBooleanProperty invalidColor = new SimpleBooleanProperty();
	        invalidColor.set(Color.WHITE.equals(color_));
			
			final TextField name = new TextField();
			name.textProperty().addListener((ov, oldV, newV) -> {
                invalidName.set(newV.trim().isEmpty());
			});
            name.setText(name_);
			
		    final TextField tag = new TextField();
		    tag.textProperty().addListener((ov, oldV, newV) -> {
                invalidTag.set(newV.trim().isEmpty());
			});
            tag.setText(tag_);

		    final ColorPicker colorPicker = new ColorPicker();
            if (color_ != null)
                colorPicker.setValue(color_);

		    colorPicker.setOnAction(e -> {
		    	invalidColor.set(Color.WHITE.equals(colorPicker.getValue()));
		    });
            
			/*TextField categoryFilter = new TextField();
			categoryFilter.setPromptText("Filter");

			FilteredList<TreeObject<?>> filteredList = new FilteredList<>(AnnotationTree.categories);
			filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> {
				return categoryFilter.getText() == null || categoryFilter.getText().trim().isEmpty() ? null :
						(co -> {
							return co.getParent() == null || co.getName().contains(categoryFilter.getText().trim());
						});
			}, categoryFilter.textProperty()));
			*/
		    ComboBox<TreeObject<?>> categoryBox = new ComboBox<>(category == null ? AnnotationTree.categories :
		            new FilteredList<>(AnnotationTree.categories, treeObject -> {
                        return !treeObject.isOnPathToRoot(category);
                    })); //always shows root

		    if (category == null)
		        categoryBox.getSelectionModel().selectFirst();
            else if (updating)
                categoryBox.getSelectionModel().select(category.getParent());
            else
                categoryBox.getSelectionModel().select(category);

			//HBox categoryHBox = new HBox(categoryBox, categoryFilter);
			//categoryHBox.setSpacing(10);

			ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		    ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    categoryPane.getButtonTypes().setAll(okButton, cancelBtn);
		    
		    Button okBtn = (Button) categoryPane.lookupButton(okButton);
		    okBtn.disableProperty().bind(invalidColor.or(invalidName.or(invalidTag)));

			okBtn.setOnAction(actionEvent -> {
                if (category != null && updating) {
                    category.setName(name.getText().trim());
                    category.setTag(tag.getText().trim());
                    category.setColor(colorPicker.getValue());
                    category.changeParent((CategoryObject) categoryBox.getSelectionModel().getSelectedItem());
                } else {
                    ((CategoryObject) categoryBox.getSelectionModel().
                            getSelectedItem()).
                            getChildren().add(new CategoryObject(name.getText().trim(),
                                                                 tag.getText(), colorPicker.getValue()));
                }
				actionEvent.consume();
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
            this.add(new Label("Subcategory of: "), 0, 3);
            this.add(categoryBox, 1, 3);
		}
	}
}
