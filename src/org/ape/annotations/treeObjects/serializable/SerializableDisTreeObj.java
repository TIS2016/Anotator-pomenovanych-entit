package org.ape.annotations.treeObjects.serializable;

import org.ape.annotations.treeObjects.DisplayedTreeObject;
import org.ape.annotations.treeObjects.TreeObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public abstract class SerializableDisTreeObj<S extends TreeObject<?>>
extends SerializableTreeObj<S>
implements Serializable {

    static final transient String FIELD_DELIMITER = ":";
    static final transient String VALUE_DELIMItER = ",";

    int i, j;

    private final transient SerializableTreeObj parent;
    private final transient List<Integer> lineNumbers = new ArrayList<>();
    private final transient int lineNo, caretPos;
    private transient boolean isOkStart = false;
    private transient boolean isOkEnd = false;

    <T extends DisplayedTreeObject<?>>
    SerializableDisTreeObj(T treeObject,
                           HashMap<Integer, HashSet<SerializableDisTreeObj>> reverseLookup,
                           SerializableTreeObj parent) {
        super(treeObject, reverseLookup);
        this.isSelected = treeObject.isSelected();
        this.parent = parent;
        this.i = treeObject.getStart();
        this.j = treeObject.getEnd();
        this.lineNo = treeObject.getLineNum();
        this.caretPos = treeObject.getCaretCol();
        this.populateMap(reverseLookup);
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof SerializableDisTreeObj &&
               this.i == ((SerializableDisTreeObj) other).i &&
               this.j == ((SerializableDisTreeObj) other).j &&
               this.parent == ((SerializableDisTreeObj) other).parent;
    }

    private void populateMap(HashMap<Integer, HashSet<SerializableDisTreeObj>> reverseLookup) {
        if (reverseLookup != null) {
            for (int k = i; k <= j; k++) {
                HashSet<SerializableDisTreeObj> list = reverseLookup.getOrDefault(k, new HashSet<>());
                list.add(this);
                reverseLookup.putIfAbsent(k, list);
            }
        }
    }

    public final List<Integer> getLineNumbers() {
        return this.lineNumbers;
    }

    public final String toPosition() {
        return String.format("[%d:%d]", this.lineNo, this.caretPos);
    }

    public final boolean isInvalid() {
        return !this.isOkStart || !this.isOkEnd;
    }

    public final boolean isStart(int i) {
        return this.i == i;
    }

    public final boolean isEnd(int j) {
        return this.j == j;
    }

    public final SerializableTreeObj getParent() {
        return this.parent;
    }

    public final void setIsOkStart() {
        this.isOkStart = true;
    }

    public final void setIsOkEnd() {
        this.isOkEnd = true;
    }

    public final void addLineNumber(int lineNumber) {
        this.lineNumbers.add(lineNumber);
    }

    public abstract String toRecord(int start, boolean outputId, boolean outputDescription);
}
