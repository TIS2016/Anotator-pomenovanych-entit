package org.ape.annotations.treeObjects;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import org.ape.control.ColorConverter;
import org.ape.control.Controller;
import org.ape.AppData;

public class CategoryObject extends TreeObject<TreeObject<?>> {

    private final SimpleObjectProperty<Color> colorProperty = new SimpleObjectProperty<>();
    private final SimpleIntegerProperty intColorProperty = new SimpleIntegerProperty();
    private final SimpleStringProperty tagProperty = new SimpleStringProperty();

    public CategoryObject(long id, @Nullable CategoryObject parent,
                          String name, String tag, Color color) {
        super(id, name);
        this.colorProperty.addListener(
                (observable, oldValue, newValue) -> this.intColorProperty.set(ColorConverter.colorToInt(newValue))
        );
        this.colorProperty.set(color);
        this.tagProperty.set(tag);
        if (parent != null) {
            parent.add(this);
        }
    }

    public void update(String name, String tag, Color color, @NotNull TreeObject<?> parent) {
        final String previous = this.toLogString();
        this.changeParent(parent);
        this.setName(name);
        this.setTag(tag);
        if (!this.getColor().equals(color)) {
            this.setColor(color);
            int caretPosition = AppData.textArea.getCaretPosition();
            AppData.textArea.selectAll(); //trick to force recolor
            AppData.textArea.deselect();
            AppData.textArea.selectRange(caretPosition, caretPosition);
        }
        Controller.logger.logUpdate(previous + " -> " + this.toLogString());
    }

    public final Color getColor() {
        return colorProperty.get();
    }

    public final void setColor(Color color) {
        this.colorProperty.set(color);
    }

    public final String getTag() {
        return tagProperty.get();
    }

    public final void setTag(String tag) {
        this.tagProperty.set(tag);
    }

    public final SimpleObjectProperty<Color> colorProperty() {
        return colorProperty;
    }

    public final int getIntColor() {
        return intColorProperty.get();
    }

    @Override
    public String toLogString() {
        final CategoryObject co = (CategoryObject) this.getParent();
        final String parentString = co.getParent() == null ? "None" : String.format("%s[%s]", co.getName(), co.getTag());
        return String.format(
                "Category - %s[%s] Subcategory: %s",
                this.getName(), this.getTag(), parentString);
    }

    @Override
    public void onDelete() {
        super.onDelete();
        if (AppData.defaultCategory.get() == this) {
            AppData.defaultCategory.set(null);
        }
    }
}
