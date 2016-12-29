import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;

/**
 * Created by michal on 11/28/16.
 */
public class CategoryObject extends TreeObject<TreeObject<?>> {

    private SimpleObjectProperty<Color> colorProperty = new SimpleObjectProperty<>();
    private SimpleIntegerProperty intColorProperty = new SimpleIntegerProperty();
    private SimpleStringProperty tagProperty = new SimpleStringProperty();

    public CategoryObject(String name, String tag, Color color) {
        super(name);
        colorProperty.addListener((
                (observable, oldValue, newValue) -> intColorProperty.set(ColorConverter.colorToInt(newValue))));
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
}
