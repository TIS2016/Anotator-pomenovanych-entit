package org.ape.layout.dialogs;

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
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.ape.annotations.treeObjects.CategoryObject;
import org.ape.annotations.treeObjects.TreeObject;
import org.ape.AppData;

public class CategoryDialog extends Dialog<CategoryObject> {

	public CategoryDialog(Window owner,
                          CategoryObject categoryObject) {
        super();
        this.initOwner(owner);
        this.setResizable(false);
        this.setTitle(categoryObject == null ? "New Category" : "Update Category");
        this.getDialogPane().setContent(new CategoryPane(this, categoryObject));
    }
	
	private class CategoryPane extends GridPane {

		public CategoryPane(CategoryDialog categoryDialog,
                            final CategoryObject categoryObject) {
            super();
            final DialogPane categoryPane = categoryDialog.getDialogPane();
            final SimpleBooleanProperty invalidName = new SimpleBooleanProperty(true);
            final SimpleBooleanProperty invalidTag = new SimpleBooleanProperty(true);
            final SimpleBooleanProperty invalidColor = new SimpleBooleanProperty(true);

            final ComboBox<TreeObject<?>> categoryBox = new ComboBox<>(new FilteredList<>(AppData.sortedCategories,
                    treeObject -> !treeObject.isOnPathToRoot(categoryObject)));
            categoryBox.setPrefSize(150, 25);
            categoryBox.setCellFactory(lv -> {
                ComboBoxListCell<TreeObject<?>> cell = new ComboBoxListCell<>();
                cell.setWrapText(true);
                cell.setPrefSize(150, 25);
                return cell;
            });

            String name_ = "", tag_ = "";
            Color color_ = Color.WHITE;

            if (categoryObject != null) {
                name_ = categoryObject.getTreeName();
                tag_ = categoryObject.getTag();
                color_ = categoryObject.getColor();
                categoryBox.getSelectionModel().select(categoryObject.getParent());
            } else {
                categoryBox.getSelectionModel().selectFirst();
            }

			final TextField name = new TextField();
			name.textProperty().addListener((ov, oldV, newV) -> invalidName.set(newV.trim().isEmpty()));
            name.setText(name_);
			
		    final TextField tag = new TextField();
		    tag.textProperty().addListener((ov, oldV, newV) -> {
                final String trimmedTag = newV.trim();
                invalidTag.set(
                        trimmedTag.isEmpty() ||
                                AppData.categories.stream()
                                .filter(co -> co != categoryObject &&
                                        ((CategoryObject) co).getTag().compareTo(trimmedTag) == 0)
                                .findFirst()
                                .isPresent());
            });
            tag.setText(tag_);

		    final ColorPicker colorPicker = new ColorPicker();
            colorPicker.setValue(color_);
            invalidColor.bind(colorPicker.valueProperty().isEqualTo(Color.WHITE));

			final ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            final ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		    categoryPane.getButtonTypes().setAll(okButton, cancelBtn);

            final Button okBtn = (Button) categoryPane.lookupButton(okButton);
		    okBtn.disableProperty().bind(invalidColor.or(invalidName.or(invalidTag)));

            categoryDialog.setResultConverter(buttonType -> {
                if (buttonType == okButton) {
                    if (categoryObject == null) {
                        return new CategoryObject(
                                AppData.id++,
                                (CategoryObject) categoryBox.getValue(),
                                name.getText().trim(),
                                tag.getText(),
                                colorPicker.getValue()
                        );
                    }
                    categoryObject.update(
                            name.getText().trim(),
                            tag.getText().trim(),
                            colorPicker.getValue(),
                            categoryBox.getValue()
                    );
                }
                return null;
            });

		    this.setPadding(new Insets(10));
            this.setHgap(10);
            this.setVgap(10);

            this.add(new Label("Name:"), 0, 0);
            this.add(name, 1, 0);
            this.add(new Label("Tag:"),0,1);
            this.add(tag, 1, 1);
            this.add(new Label("Color:"), 0, 2);
            this.add(colorPicker, 1, 2);
            this.add(new Label("Subcategory of:"), 0, 3);
            this.add(categoryBox, 1, 3);
		}
	}
}