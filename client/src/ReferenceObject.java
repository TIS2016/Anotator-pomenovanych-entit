import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by michal on 12/1/16.
 */
public class ReferenceObject extends TreeObject<TreeObject<?>> {

    public ReferenceObject(String name) {
        super(name);
    }

    @Override
    public void add(TreeObject<?> item) throws IllegalStateException {
        throw new IllegalStateException("Annotation object can't have children");
    }

    @Override
    public void addAll(TreeObject<?>... items) throws IllegalStateException {
        throw new IllegalStateException("Annotation object can't have children");
    }

    @Override
    public ObservableList<TreeObject<?>> getChildren() {
        return FXCollections.emptyObservableList();
    }

    @Override
    protected final void clearChildren() {
        this.getChildren().clear();
    }
}
