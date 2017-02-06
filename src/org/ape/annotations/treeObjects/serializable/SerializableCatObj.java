package org.ape.annotations.treeObjects.serializable;

import org.ape.annotations.treeObjects.CategoryObject;
import org.ape.annotations.treeObjects.TreeObject;
import org.ape.control.ColorConverter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

public class SerializableCatObj
extends SerializableTreeObj<CategoryObject>
implements Serializable {

    private int color;
    private String name, tag;

    public SerializableCatObj(CategoryObject treeObject, final HashMap<Integer, HashSet<SerializableDisTreeObj>> reverseLookup) {
        super(treeObject, reverseLookup);
        this.name = treeObject.getTreeName();
        this.tag = treeObject.getTag();
        this.color = treeObject.getIntColor();
    }

    public final String getTag() {
        return this.tag;
    }

    @Override
    public CategoryObject deserialize(CategoryObject parent) {
        final CategoryObject co = new CategoryObject(
                this.getId(), parent, this.name,
                this.tag, ColorConverter.intToColor(this.color)
        );
        co.getChildren().setAll(this.children.stream().map(sto -> (TreeObject<? extends TreeObject<?>>) sto.deserialize(co)).collect(Collectors.toList()));
        return co;
    }
}
