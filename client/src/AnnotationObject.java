import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * BASED ON: https://github.com/james-d/heterogeneous-tree-example
 */

/**
 * Created by michal on 11/28/16.
 */
public final class AnnotationObject extends TreeObject<TreeObject<?>> {

    public AnnotationObject(String name) {
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
}
