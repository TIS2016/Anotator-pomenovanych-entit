import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by michal on 12/1/16.
 */
public class ReferenceObject extends DisplayedTreeObject<DisplayedTreeObject<?>> {

    public ReferenceObject(String name) {
        super(name);
    }

    @Override
    public void add(DisplayedTreeObject<?> item) throws IllegalStateException {
        throw new IllegalStateException("Annotation object can't have children");
    }

    @Override
    public void addAll(DisplayedTreeObject<?>... items) throws IllegalStateException {
        throw new IllegalStateException("Annotation object can't have children");
    }

    @Override
    public ObservableList<DisplayedTreeObject<?>> getChildren() {
        return FXCollections.emptyObservableList();
    }

    @Override
    protected final void clearChildren() {}
}
