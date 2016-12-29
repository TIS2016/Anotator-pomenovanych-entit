import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyledTextArea;

/**
 * Created by michal on 12/1/16.
 */
public class ReferenceObject extends DisplayedTreeObject<DisplayedTreeObject<?>> {

    public ReferenceObject(StyledTextArea<Void, DisplayedTreeObject<?>> textArea,
                            AnnotationObject annotationObject) {
        super(textArea, annotationObject);
    }

    @Override
    public void add(TreeObject<?> item) throws IllegalStateException {
        throw new IllegalStateException("References can't have children");
    }

    @Override
    public void addAll(TreeObject<?>... items) throws IllegalStateException {
        throw new IllegalStateException("References can't have children");
    }

    @Override
    public ObservableList<TreeObject<?>> getChildren() {
        return FXCollections.emptyObservableList();
    }

    @Override
    protected final void clearChildren() {}

    @Override
    public SimpleObjectProperty<Color> colorProperty() {
        return ((AnnotationObject) this.getParent()).colorProperty();
    }
}
