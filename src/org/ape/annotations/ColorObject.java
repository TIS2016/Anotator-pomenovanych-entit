package org.ape.annotations;

import org.ape.annotations.treeObjects.DisplayedTreeObject;
import org.ape.annotations.treeObjects.TreeObject;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.util.Callback;

public class ColorObject {

    private static final Callback<DisplayedTreeObject<?>, Observable[]> extractor = displayedTreeObject ->
            new Observable[] { displayedTreeObject.selectedProperty()};

    private final ObservableList<DisplayedTreeObject<?>> displayedTreeObjects = FXCollections.observableArrayList(ColorObject.extractor);
    private final ObservableList<DisplayedTreeObject<?>> visibleDisplayedTreeObjects = new FilteredList<>(displayedTreeObjects, TreeObject::isSelected);

    public ColorObject(DisplayedTreeObject<?> treeObject) {
        displayedTreeObjects.add(treeObject);
    }

    public final ObservableList<DisplayedTreeObject<?>> getAllDisplayedTreeObjects() {
        return this.displayedTreeObjects;
    }

    public final synchronized DisplayedTreeObject<?> getLastVisibleDislayedTreeObject() {
        int size = this.visibleDisplayedTreeObjects.size();
        return size == 0 ? null : this.visibleDisplayedTreeObjects.get(size - 1);
    }

    public final synchronized DisplayedTreeObject<?> removeDislayedTreeObject(DisplayedTreeObject<?> displayedTreeObject) {
        this.displayedTreeObjects.remove(displayedTreeObject);
        return this.getLastVisibleDislayedTreeObject();
    }
}
