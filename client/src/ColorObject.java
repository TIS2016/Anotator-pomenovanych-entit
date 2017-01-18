import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.paint.Color;
import javafx.util.Callback;

/**
 * Created by michal on 12/14/16.
 */
public class ColorObject extends Object {

    private final Callback<DisplayedTreeObject<?>, Observable[]> extractor = displayedTreeObject ->
            new Observable[] { displayedTreeObject.selectedProperty()};

    private static final SimpleObjectProperty<Color> defaultColorProperty = new SimpleObjectProperty<>(Color.WHITE);

    private final ObservableList<DisplayedTreeObject<?>> baseReferences = FXCollections.observableArrayList(extractor);
    private final ObservableList<DisplayedTreeObject<?>> selectedBackreferences = new FilteredList<>(baseReferences, br -> br.isSelected());
    private int index;

    public ColorObject(int index, DisplayedTreeObject<?> treeObject) {
        this.index = index;
        baseReferences.add(treeObject);
    }

    public final ObservableList<DisplayedTreeObject<?>> getBackreferences() {
        return this.baseReferences;
    }

    public final DisplayedTreeObject<?> getLastBackreference() {
        int size = this.baseReferences.size();
        return size == 0 ? null : this.baseReferences.get(size - 1);
    }

    public final SimpleObjectProperty<Color> colorProperty() {
        return this.getLastSelectedBackreference() == null ? defaultColorProperty : this.getLastSelectedBackreference().colorProperty();
    }

    public final boolean isOverlapping() {
        return this.selectedBackreferences.size() > 1;
    }

    public final DisplayedTreeObject<?> getLastSelectedBackreference() {
        int size = this.selectedBackreferences.size();
        return size == 0 ? null : this.selectedBackreferences.get(size - 1);
    }

    public final ObservableList<DisplayedTreeObject<?>> getSelectedBackreferences() {
        return this.selectedBackreferences;
    }

    public final int getIndex() {
        return this.index;
    }
}
