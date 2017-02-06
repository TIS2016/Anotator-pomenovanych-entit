package org.ape.annotations;

import org.ape.annotations.treeObjects.CategoryObject;
import org.ape.annotations.treeObjects.TreeObject;
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
 * * BASED ON: https://bugs.openjdk.java.net/browse/JDK-8089158
 */
public class TreeObjectItem<T extends TreeObject<?>> extends CheckBoxTreeItem<T> {

    private final Callback<TreeObjectItem<T>, Observable[]> extractor = treeObjectItem ->
        new Observable[] { treeObjectItem.getValue().nameProperty() };

    private final ObservableList<TreeObjectItem<T>> unsortedChildren = FXCollections.observableArrayList(extractor);

    private final SortedList<TreeObjectItem<T>> sortedChildren = new SortedList<>(unsortedChildren, (i1, i2) -> {
        TreeObject<?> o1 = i1.getValue();
        TreeObject<?> o2 = i2.getValue();
        if (o1 instanceof CategoryObject) {
            if (o2 instanceof CategoryObject)
                return o1.getTreeName().compareTo(o2.getTreeName());
            return -1;
        }
        if (o2 instanceof CategoryObject)
            return 1;
        return o1.getTreeName().compareTo(o2.getTreeName());
    });

    private final FilteredList<TreeObjectItem<T>> filteredChildren = new FilteredList<>(sortedChildren);
    private ObjectProperty<TreePredicate<T>> predicate = new SimpleObjectProperty<>();

    public TreeObjectItem(T treeObject) {
        super(treeObject);

        treeObject.selectedProperty().bindBidirectional(this.selectedProperty());
        this.setHiddenFieldChildren(this.filteredChildren);
        this.filteredChildren.predicateProperty().bind(Bindings.createObjectBinding(() -> child -> {
                if (child != null) {
                    child.predicate.set(this.predicate.get());
                }
                return this.predicate.get() == null || child == null ||
                       child.getChildren().size() > 0 ||
                       this.predicate.get().test(this, child.getValue());
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
            e.printStackTrace();
        }
    }

    public ObservableList<TreeObjectItem<T>> getInternalChildren() {
        return unsortedChildren;
    }
}