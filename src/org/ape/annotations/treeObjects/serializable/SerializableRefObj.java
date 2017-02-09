package org.ape.annotations.treeObjects.serializable;

import com.sun.istack.internal.NotNull;
import org.ape.annotations.treeObjects.AnnotationObject;
import org.ape.annotations.treeObjects.CoreferenceObject;
import org.ape.AppData;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringJoiner;

class SerializableRefObj
extends SerializableDisTreeObj<AnnotationObject>
implements Serializable {

    SerializableRefObj(@NotNull final CoreferenceObject treeObject,
                       final HashMap<Integer, HashSet<SerializableDisTreeObj>> reverseLookup,
                       SerializableTreeObj parent) {
        super(treeObject, reverseLookup, parent);
    }

    @Override
    CoreferenceObject deserialize(@NotNull final AnnotationObject parent) {
        return new CoreferenceObject(this.getId(), parent, this.i, this.j);
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public String toRecord(int start, boolean outputId, boolean outputDescription) {
        final SerializableAnotObj parent = (SerializableAnotObj) this.getParent();
        final String categoryTag = ((SerializableCatObj) parent.getParent()).getTag();
        if (start != i) {
            return AppData.CONT_TAG + (outputId ? VALUE_DELIMItER + this.getId() : "") + FIELD_DELIMITER + categoryTag;
        }

        final StringJoiner fieldJoiner = new StringJoiner(FIELD_DELIMITER);
        StringJoiner valueJoiner = new StringJoiner(VALUE_DELIMItER);

        valueJoiner.add(AppData.BEGIN_TAG);
        if (outputId) {
            valueJoiner.add(String.valueOf(this.getId()));
        }
        fieldJoiner.add(valueJoiner.toString());

        valueJoiner = new StringJoiner(VALUE_DELIMItER);
        valueJoiner.add(categoryTag);
        valueJoiner.add(AppData.REF_TAG);

        fieldJoiner.add(valueJoiner.toString());

        valueJoiner = new StringJoiner(VALUE_DELIMItER);
        for (int lineNum: parent.getLineNumbers()) {
            valueJoiner.add(String.valueOf(lineNum));
        }

        fieldJoiner.add(valueJoiner.toString());
        fieldJoiner.add(""); //to be consistent description...

        return fieldJoiner.toString();
    }
}
