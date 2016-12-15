import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

import java.util.HashSet;

/**
 * Created by michal on 11/28/16.
 */
public class CategoryObject extends TreeObject<TreeObject<?>> {

    private SimpleObjectProperty<Color> colorProperty = new SimpleObjectProperty<>();
    private SimpleIntegerProperty intColorProperty = new SimpleIntegerProperty();
    private SimpleStringProperty tagProperty = new SimpleStringProperty();

    public CategoryObject(String name, String tag, Color color) {
        super(name);
        colorProperty.addListener(((observable, oldValue, newValue) -> {
            intColorProperty.set(colorToInt(newValue));
        }));
        colorProperty.set(color);
        tagProperty.set(tag);
    }

    public final Color getColor() {
        return colorProperty.get();
    }

    public final void setColor(Color color) {
        this.colorProperty.set(color);
    }

    public final SimpleStringProperty tagProperty() {
        return tagProperty;
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

    private int colorToInt(Color color) {
        int red = Math.round(255 * (float) color.getRed());
        int green = Math.round(255 * (float) color.getGreen());
        int blue = Math.round(255 * (float) color.getBlue());
        red = (red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        green = (green << 8) & 0x0000FF00; //Shift green 8-bits and mask out other stuff
        blue = blue & 0x000000FF; //Mask out anything not blue.
        return 0xFF000000 | red | green | blue;
    }
}
