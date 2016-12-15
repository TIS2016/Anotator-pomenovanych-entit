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

    private final SimpleIntegerProperty depthProperty = new SimpleIntegerProperty(0); //TODO: remove me, I'm not used

    public TreeObject(String name) {
        this.setName(name);

        this.parentProperty.addListener(((observable, oldValue, newValue) -> {
            assert newValue == this.parentProperty.get();
            assert newValue != null;
            this.depthProperty().bind(newValue.depthProperty.add(1));
        }));
        this.depthProperty().addListener(((observable, oldValue, newValue) -> { //update all children depths
            children.forEach(c -> {
                c.depthProperty().bind(this.depthProperty.add(1));
            });
        }));

        this.children.addListener((ListChangeListener.Change<? extends TreeObject<?>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach((TreeObject<?> o) -> {
                        o.setParent(this);
                    });
                }
            }
        });
    }

    public String toString() { //combobox prettyprint
        return getParent() == null ? "None" : this.getName();
    }

    public final void addChildrenListener(ListChangeListener<TreeObject<?>> listener) {
        this.listener = listener;
        this.children.addListener(this.listener);
    }

    public final boolean isOnPathToRoot(TreeObject<?> treeObject) {
        TreeObject<?> current = this;
        while (current != null) {
            if (current == treeObject)
                return true;
            current = current.getParent();
        }
        return false;
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

    public final SimpleIntegerProperty depthProperty() {
        return this.depthProperty;
    }
    public final int getDepth() {
        return this.depthProperty.get();
    }

    public final void changeParent(CategoryObject newParent) {
        TreeObject<?> oldParent = this.getParent();
        if (oldParent != newParent) {
            this.children.removeListener(this.listener);
            this.listener = null;
            oldParent.getChildren().remove(this);
            newParent.getChildren().add(this);
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
        this.getChildren().forEach(child -> {
            child.clearChildren();
        });
        this.getChildren().clear();
    }
}
