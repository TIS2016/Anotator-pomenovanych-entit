package org.ape.annotations.treeObjects.serializable;

import org.ape.annotations.treeObjects.AnnotationObject;
import org.ape.annotations.treeObjects.CategoryObject;
import org.ape.annotations.treeObjects.CoreferenceObject;
import org.ape.annotations.treeObjects.TreeObject;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class SerializableTreeObj<S extends TreeObject<?>> implements Serializable {

    private long id;
    List<SerializableTreeObj> children;
    transient boolean isSelected;

    <T extends TreeObject<?>>
    SerializableTreeObj(T treeObject,
                        final HashMap<Integer, HashSet<SerializableDisTreeObj>> reverseLookup) {
        this.id = treeObject.getId();
        Stream<? extends TreeObject<?>> stream = treeObject.getChildren().stream();
        this.children = stream.map(to -> {
            if (to instanceof CategoryObject) {
                return new SerializableCatObj((CategoryObject) to, reverseLookup);
            } else if (to instanceof AnnotationObject) {
                return new SerializableAnotObj((AnnotationObject) to, reverseLookup, this);
            } else if (to instanceof CoreferenceObject) {
                return new SerializableRefObj((CoreferenceObject) to, reverseLookup, this);
            }
            return null;
        }).collect(Collectors.toList());
    }

    public final boolean isSelected() {
        return this.isSelected;
    }

    public final long getId() {
        return this.id;
    }

    abstract TreeObject<?> deserialize(S parent);

}

