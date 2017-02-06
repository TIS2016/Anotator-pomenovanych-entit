package org.ape.annotations.treeObjects.serializable;

import org.ape.annotations.treeObjects.AnnotationObject;
import org.ape.annotations.treeObjects.CategoryObject;
import org.ape.annotations.treeObjects.CoreferenceObject;
import org.ape.AppData;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

class SerializableAnotObj
extends SerializableDisTreeObj<CategoryObject>
implements Serializable {

    private List<String> links;
    private String description;

    SerializableAnotObj(AnnotationObject treeObject,
                        HashMap<Integer, HashSet<SerializableDisTreeObj>> reverseLookup,
                        SerializableTreeObj parent) {
        super(treeObject, reverseLookup, parent);
        links = new ArrayList<>(treeObject.getUrls());
        description = treeObject.getDescription();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SerializableAnotObj &&
               super.equals(other) &&
               this.links.equals(((SerializableAnotObj) other).links) &&
               this.description.compareTo(((SerializableAnotObj) other).description) == 0;
    }

    @Override  //B(,Id optional):TAG:urls -- may be empty:(desc optional)
    public String toRecord(int start, boolean outputId, boolean outputDescription) {
        if (start != i) {
            return AppData.CONT_TAG + (outputId ? VALUE_DELIMItER + this.getId() : "");
        }
        final StringJoiner fieldJoiner = new StringJoiner(FIELD_DELIMITER);
        StringJoiner valueJoiner = new StringJoiner(VALUE_DELIMItER);

        valueJoiner.add(AppData.BEGIN_TAG);
        if (outputId) {
            valueJoiner.add(String.valueOf(this.getId()));
        }
        fieldJoiner.add(valueJoiner.toString());

        fieldJoiner.add(((SerializableCatObj) this.getParent()).getTag());

        valueJoiner = new StringJoiner(VALUE_DELIMItER);
        for (String link: links) {
            valueJoiner.add(link);
        }
        fieldJoiner.add(valueJoiner.toString());

        if (outputDescription) {
            fieldJoiner.add(this.description);
        }

        return fieldJoiner.toString();
    }

    @Override
    AnnotationObject deserialize(CategoryObject parent) {
        final AnnotationObject ao = new AnnotationObject(this.getId(), parent, this.i, this.j, this.description, links);
        ao.getChildren().setAll(this.children.stream().map(sto -> (CoreferenceObject) sto.deserialize(ao)).collect(Collectors.toList()));
        return ao;
    }
}
