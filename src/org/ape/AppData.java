package org.ape;

import javafx.stage.Window;
import org.ape.annotations.*;
import org.ape.annotations.treeObjects.AnnotationObject;
import org.ape.annotations.treeObjects.CategoryObject;
import org.ape.annotations.treeObjects.DisplayedTreeObject;
import org.ape.annotations.treeObjects.TreeObject;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.paint.Color;
import org.ape.control.Controller;
import org.ape.layout.AnnotationTree;
import org.ape.layout.LogListView;
import org.fxmisc.richtext.StyledTextArea;

import java.io.File;
import java.util.*;

public final class AppData {

    public static StyledTextArea<Void, DisplayedTreeObject<?>> textArea = new StyledTextArea<>(null, ((textFlow, s) -> {}),
            null, (textExt, treeObject) -> {
        if (treeObject != null) {
            textExt.fillProperty().bind(Bindings.createObjectBinding(
                    () -> Double.compare(treeObject.colorProperty()
                            .get().getBrightness(), 0.6) <= 0 ? Color.WHITE : Color.BLACK,
                    treeObject.colorProperty()));
            textExt.backgroundColorProperty().bind(treeObject.colorProperty());
        } else {
            textExt.backgroundColorProperty().unbind();
            textExt.setBackgroundColor(Color.WHITE);

            textExt.fillProperty().unbind();
            textExt.setFill(Color.BLACK);
        }
    });
    public static final String REF_TAG = "REF";
    public static final String BEGIN_TAG = "B";
    public static final String CONT_TAG = "I";

    public static final int MIN_FONT_SIZE = 10;
    public static final int MAX_FONT_SIZE = 100;

    public static final LogListView logListView = new LogListView(Controller.logger);

    public static Window owner;
    public static long id = 1;

    public static final HashMap<Integer, ColorObject> colorObjects = new HashMap<>();
    public static final SimpleObjectProperty<CategoryObject> defaultCategory = new SimpleObjectProperty<>();
    public static final SimpleObjectProperty<AnnotationObject> anchorAnnotation = new SimpleObjectProperty<>();
    public static final SimpleObjectProperty<File> saveFile = new SimpleObjectProperty<>();
    public static final SimpleStringProperty exportPath = new SimpleStringProperty("");
    public static final SimpleBooleanProperty isActiveProject = new SimpleBooleanProperty(false);

    public static final ObservableList<TreeObject<?>> treeObjects = FXCollections.observableArrayList();
    public static final FilteredList<TreeObject<?>> categories = new FilteredList<>(
            treeObjects, treeObject -> treeObject instanceof CategoryObject
    );
    public static final SortedList<TreeObject<?>> sortedCategories = new SortedList<>(
            categories, (to1, to2) -> to1.getParent() == null ? -1 : to1.getTreeName().compareTo(to2.getTreeName())
    );
    public static final AnnotationTree tree = new AnnotationTree();
}
