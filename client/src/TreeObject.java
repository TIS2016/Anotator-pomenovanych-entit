import com.sun.istack.internal.NotNull;
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
    private ListChangeListener<TreeObject<?>> listener;
    private final SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty();

    public TreeObject(@NotNull String name) {
        this.setName(name);//.replace("\n", "\\n"));

        this.children.addListener((ListChangeListener.Change<? extends TreeObject<?>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().stream().filter(o -> o.getParent() != this).forEach((TreeObject<?> o) -> o.setParent(this));
                }
            }
        });
    }

    public final SimpleBooleanProperty selectedProperty() {
        return this.selectedProperty;
    }

    public final boolean isSelected() {
        return selectedProperty.get();
    }

    public String toString() { //combobox prettyprint
        return getParent() == null ? "None" : this.getName();
    }

    public final void setChildrenListener(ListChangeListener<TreeObject<?>> listener) {
        this.listener = listener;
        this.children.addListener(this.listener);
    }

    public final boolean isOnPathToRoot(TreeObject<?> treeObject) {
        if (treeObject == null) {
            return false;
        }
        TreeObject<?> current = this;
        while (current != null) {
            if (current == treeObject)
                return true;
            current = current.getParent();
        }
        return false;
    }

    public final void setName(String name) {
        this.nameProperty.set(name);
    }

    public String getName() {
        return this.nameProperty.get();
    }

    public final SimpleObjectProperty<TreeObject<?>> parentProperty() {
        return this.parentProperty;
    }

    public final <S extends TreeObject<?>> void changeParent(TreeObject<S> newParent) {
        TreeObject<? extends TreeObject<?>> oldParent = this.getParent();
        if (oldParent != newParent) {
            this.children.removeListener(this.listener);
            this.listener = null;
            oldParent.getChildren().remove(this);
            newParent.getChildren().add((S) this);
        }
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
        this.getChildren().forEach(child -> child.clearChildren());
        this.getChildren().clear();
    }

    public final StringProperty nameProperty() {
        return this.nameProperty;
    }
}