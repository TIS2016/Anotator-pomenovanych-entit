import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Created by michal on 11/28/16.
 */

public abstract class TreeObject<T extends TreeObject<?>> {

    private final SimpleStringProperty nameProperty = new SimpleStringProperty();
    private final ObservableList<T> children = FXCollections.observableArrayList();
    private final SimpleObjectProperty<TreeObject<?>> parentProperty = new SimpleObjectProperty<>();

    public TreeObject(String name) {
        this.setName(name);
        this.children.addListener((ListChangeListener.Change<? extends TreeObject<?>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().stream().forEach((TreeObject<?> o) -> {
                        o.parentProperty().set(this);
                    });
                }
            }
        });
    }

    public final SimpleStringProperty nameProperty() {
        return this.nameProperty;
    }

    public final void setName(String name) {
        this.nameProperty.set(name);
    }

    public final String getName() {
        return this.nameProperty.get();
    }

    public final SimpleObjectProperty<TreeObject<?>> parentProperty() {
        return this.parentProperty;
    }

    public final void setParent(TreeObject<?> parent) {
        this.parentProperty.set(parent);
    }

    public final TreeObject<?> getParent() {
        return this.parentProperty.get();
    }

    protected void add(T item) {
        children.add(item);
    }

    protected void addAll(T... items) {
        children.addAll(items);
    }

    protected ObservableList<T> getChildren() {
        return children;
    }

    protected void clearChildren() {
        this.getChildren().forEach(child -> {
            child.clearChildren();
        });
        this.getChildren().clear();
    }
}
