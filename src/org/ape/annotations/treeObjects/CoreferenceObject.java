package org.ape.annotations.treeObjects;

import com.sun.istack.internal.NotNull;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.ape.control.Controller;

import java.util.Collection;

public class CoreferenceObject extends DisplayedTreeObject<DisplayedTreeObject<?>> {

    public CoreferenceObject(long id, @NotNull AnnotationObject parent) {
        super(id);
        parent.add(this);
    }

    public CoreferenceObject(long id, @NotNull AnnotationObject parent, int start, int end) {
        super(id, start, end);
        parent.add(this);
    }

    @Override
    public final void add(DisplayedTreeObject<?> item) throws IllegalStateException {
        throw new IllegalStateException("Reference can't have children");
    }

    @Override
    public final void addAll(Collection<? extends DisplayedTreeObject<?>> items) throws IllegalStateException {
        throw new IllegalStateException("Reference can't have children");
    }

    public void update(TreeObject<?> parent) {
        final String previous = this.toLogString();
        this.changeParent(parent);
        Controller.logger.logUpdate(previous + " -> " + this.toLogString());
    }

    @Override
    public final ObservableList<DisplayedTreeObject<?>> getChildren() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public String toLogString() {
        final AnnotationObject parent = (AnnotationObject) this.getParent();
        final CategoryObject grandParent = (CategoryObject) parent.getParent();
        return String.format(
                "Coreference - Category: %s[%s] Position: [%d:%d] Refers to: [%d:%d]",
                grandParent.getName(), grandParent.getTag(),
                this.getLineNum(), this.getCaretCol(),
                parent.getLineNum(), parent.getCaretCol()
        );
    }

    @Override
    public final SimpleObjectProperty<Color> colorProperty() {
        return ((AnnotationObject) this.getParent()).colorProperty();
    }
}
