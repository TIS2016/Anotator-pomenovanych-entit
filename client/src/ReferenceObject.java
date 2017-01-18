import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

/**
 * Created by michal on 12/1/16.
 */
public class ReferenceObject extends DisplayedTreeObject<DisplayedTreeObject<?>> {

    public ReferenceObject(final AnnotationObject annotationObject) {
        super(annotationObject);
    }

    @Override
    public void add(DisplayedTreeObject<?> item) throws IllegalStateException {
        throw new IllegalStateException("Reference can't have children");
    }

    @Override
    public void addAll(DisplayedTreeObject<?>... items) throws IllegalStateException {
        throw new IllegalStateException("Reference can't have children");
    }

    @Override
    public ObservableList<DisplayedTreeObject<?>> getChildren() {
        return FXCollections.emptyObservableList();
    }

    @Override
    protected final void clearChildren() {}

    @Override
    public SimpleObjectProperty<Color> colorProperty() {
        return ((AnnotationObject) this.getParent()).colorProperty();
    }
}
