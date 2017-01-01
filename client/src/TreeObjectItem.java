import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.util.Callback;

import java.lang.reflect.Field;

/**
 * Created by michal on 11/28/16.
 * BASED ON: https://bugs.openjdk.java.net/browse/JDK-8089158
 */
public class TreeObjectItem<T extends TreeObject<?>> extends CheckBoxTreeItem<T> {

    private final Callback<TreeObjectItem<T>, Observable[]> extractor = treeObjectItem ->
        new Observable[] {treeObjectItem.getValue().nameProperty()};

    private final ObservableList<TreeObjectItem<T>> unsortedChildren = FXCollections.observableArrayList(extractor);

    private final SortedList<TreeObjectItem<T>> sortedChildren = new SortedList<>(unsortedChildren, (i1, i2) -> {
        TreeObject<?> o1 = i1.getValue();
        TreeObject<?> o2 = i2.getValue();
        if (o1 instanceof CategoryObject) {
            if (o2 instanceof CategoryObject)
                return o1.getName().compareTo(o2.getName());
            return -1; //categories go first
        }
        if (o2 instanceof CategoryObject)
            return 1;
        return o1.getName().compareTo(o2.getName());
    });

    private final FilteredList<TreeObjectItem<T>> filteredChildren = new FilteredList<>(sortedChildren);
    private ObjectProperty<TreePredicate<T>> predicate = new SimpleObjectProperty<>();

    public TreeObjectItem(T treeObject) {
        super(treeObject);

        treeObject.selectedProperty().bind(this.selectedProperty());
        //TODO?: rewrite as following
        /*if (treeObject instanceof DisplayedTreeObject) {
            ((DisplayedTreeObject) treeObject).setItem(this);
            this.selectedProperty().addListener(((observable, oldValue, newValue) -> ((DisplayedTreeObject) treeObject).onSelect()));
        }*/

        this.setFilteredChildren();
        this.filteredChildren.predicateProperty().bind(Bindings.createObjectBinding(() -> child -> {
                if (child != null) {
                    child.predicate.set(this.predicate.get());
                }
                if (this.predicate.get() == null) {
                    return true;
                }
                if (child.getChildren().size() > 0) {
                    return true;
                }
                return this.predicate.get().test(this, child.getValue());
            }, this.predicate));
    }

    public final ObjectProperty<TreePredicate<T>> predicateProperty() {
        return this.predicate;
    }

    private void setHiddenFieldChildren(ObservableList<TreeObjectItem<T>> list) {
        try {
            Field childrenField = TreeItem.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            childrenField.set(this, list);
            Field declaredField = TreeItem.class.getDeclaredField("childrenListener");
            declaredField.setAccessible(true);
            list.addListener((ListChangeListener<? super TreeObjectItem>) declaredField.get(this));
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public final void setFilteredChildren() {
        setHiddenFieldChildren(filteredChildren);
    }

    public ObservableList<TreeObjectItem<T>> getInternalChildren() {
        return unsortedChildren;
    }
}