import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * Created by michal on 11/28/16.
 */

public abstract class TreeObject<T extends TreeObject<?>> {

    private final SimpleStringProperty nameProperty = new SimpleStringProperty();
    private final SimpleStringProperty fullNameProperty = new SimpleStringProperty();
    private final ObservableList<T> children = FXCollections.observableArrayList();
    private final SimpleObjectProperty<TreeObject<?>> parentProperty = new SimpleObjectProperty<>();
    private ListChangeListener<TreeObject<?>> listener;
    private final SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty();

    public static final int MAX_DISPLAYED_LEGTH = 100;

    public TreeObject(String name) {
        this.setFullName(name);

        this.nameProperty.bind(Bindings.createObjectBinding(() -> {
            String fullName = this.fullNameProperty.get();
            return fullName != null ? fullName.substring(0, Math.min(fullName.length(), MAX_DISPLAYED_LEGTH)) : "";
        }, this.fullNameProperty));

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

    public final String getName() {
        return this.nameProperty.get();
    }

    public final void setFullName(String name) {
        this.fullNameProperty.set(name);
    }

    public final String getFullName() {
        return this.fullNameProperty.get();
    }

    public final SimpleObjectProperty<TreeObject<?>> parentProperty() {
        return this.parentProperty;
    }

    public final void changeParent(TreeObject<TreeObject<?>> newParent) {
        TreeObject<? extends TreeObject<?>> oldParent = this.getParent();
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
        this.getChildren().forEach(child -> child.clearChildren());
        this.getChildren().clear();
    }

    public final StringProperty nameProperty() {
        return this.nameProperty;
    }
}
