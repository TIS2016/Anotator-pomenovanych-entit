package org.ape.annotations.treeObjects;

import com.sun.istack.internal.NotNull;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.paint.Color;
import org.ape.control.Controller;
import org.ape.AppData;

import java.util.ArrayList;
import java.util.List;

public class AnnotationObject extends DisplayedTreeObject<CoreferenceObject> {

    private List<String> urls = new ArrayList<>();
    private final SimpleStringProperty description = new SimpleStringProperty();

    public AnnotationObject(long id, @NotNull final CategoryObject parent,
                            String description,
                            List<String> urls) {
        super(id);
        this.description.set(description);
        this.urls = urls;
        parent.add(this);
    }

    public AnnotationObject(long id, @NotNull final CategoryObject parent,
                            int start, int end,
                            String description,
                            List<String> urls) {
        super(id, start, end);
        this.description.set(description);
        this.urls = urls;
        parent.add(this);
    }

    public final void setUrls(final List<String> urls) {
        this.urls = urls;
    }

    public final List<String> getUrls() {
        return urls;
    }

    public final String getDescription() {
        return this.description.get();
    }

    public final void setDescription(final String description) {
        this.description.set(description);
    }

    public void update(List<String> urls, String description, TreeObject<?> newParent) {
        final String previous = this.toLogString();
        this.setUrls(urls);
        this.setDescription(description);
        this.changeParent(newParent);
        Controller.logger.logUpdate(previous + " -> " + this.toLogString());
    }

    @Override
    public final SimpleObjectProperty<Color> colorProperty() {
        return ((CategoryObject) this.getParent()).colorProperty();
    }

    @Override
    public String toLogString() {
        final CategoryObject co = (CategoryObject) this.getParent();
        return String.format(
                "Annotation - Category: %s[%s] Position: [%d:%d]",
                co.getTreeName(), co.getTag(), this.getLineNum(),
                this.getCaretCol()
        );
    }

    @Override
    public void onDelete() {
        super.onDelete();
        if (AppData.anchorAnnotation.get() == this) {
            AppData.anchorAnnotation.set(null);
        }
    }
}
