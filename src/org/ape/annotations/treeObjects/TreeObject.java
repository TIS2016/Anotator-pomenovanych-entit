package org.ape.annotations.treeObjects;

import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.ape.control.Controller;
import org.ape.AppData;

import java.util.Collection;

public abstract class TreeObject<T extends TreeObject<?>> {

    public enum Status {
        DEFAULT,
        UPDATE, //called when changing parent to prevent children deletion
        DELETE,
    }

    private final SimpleStringProperty nameProperty = new SimpleStringProperty();
    private final ObservableList<T> children = FXCollections.observableArrayList();
    private final SimpleObjectProperty<TreeObject<?>> parentProperty = new SimpleObjectProperty<>();
    private ListChangeListener<TreeObject<?>> listener;
    private final SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty(true);
    private long id;
    private Status status = Status.DEFAULT;

    public TreeObject(@NotNull String name) throws IllegalArgumentException {
        if (name.length() == 0) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.setName(name);
        this.children.addListener((ListChangeListener.Change<? extends TreeObject<?>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(to -> to.setParent(this));
                }
            }
        });
    }

    public TreeObject(long id, String name) {
        this(name);
        this.setId(id);
    }

    public final Status getStatus() {
        return status;
    }

    public final void setStatus(Status status) {
        this.status = status;
    }

    public final long getId() {
        return this.id;
    }

    public final void setId(long id) {
        this.id = id;
    }

    public final SimpleBooleanProperty selectedProperty() {
        return this.selectedProperty;
    }

    public final boolean isSelected() {
        return selectedProperty.get();
    }

    @Override
    public String toString() { //combobox prettyprint
        return getParent() == null ? "None" : this.getTreeName();
    }

    public final void setChildrenListener(@NotNull final ListChangeListener<TreeObject<?>> listener) {
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

    public String getTreeName() {
        return this.nameProperty.get();
    }

    public final String getName() {
        return this.nameProperty.get();
    }

    public final <S extends TreeObject<?>> void changeParent(TreeObject<S> newParent) {
        TreeObject<? extends TreeObject<?>> oldParent = this.getParent();
        if (oldParent != newParent) {
            this.setStatus(Status.UPDATE);
            this.children.removeListener(this.listener);
            this.listener = null;
            oldParent.getChildren().remove(this);
            newParent.getChildren().add((S) this);
            this.setStatus(Status.DEFAULT);
        }
    }

    public final void setParent(TreeObject<?> parent) {
        this.parentProperty.set(parent);
    }

    public final TreeObject<?> getParent() {
        return this.parentProperty.get();
    }

    public void add(T item) {
        children.add(item);
    }

    public void addAll(Collection<? extends T> items) {
        children.addAll(items);
    }

    public void remove(TreeObject<?> treeObject) {
        this.children.remove(treeObject);
    }

    public ObservableList<T> getChildren() {
        return children;
    }

    public abstract String toLogString();

    public synchronized void onDelete() {
        Controller.logger.logDelete(this.toLogString());
        this.setStatus(Status.DELETE);
        AppData.treeObjects.remove(this);
        this.children.stream()
                .filter(treeObject -> treeObject.getStatus() != Status.DELETE)
                .forEach(treeObject -> Platform.runLater(() -> this.getChildren().remove(treeObject)));
    }

    public final StringProperty nameProperty() {
        return this.nameProperty;
    }
}