import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by michal on 12/21/16.
 */
public final class SessionData {

    //Active project
    public static UserData userData = new UserData(0, "quux", (byte) 7);
    public static ProjectData projectData;

    //TextArea
    public static final HashMap<Integer, ColorObject> colorObjects = new HashMap<>();

    //Tree
    public static final ObservableList<TreeObject<?>> treeObjects = FXCollections.observableArrayList();
    public static final FilteredList<TreeObject<?>> categories = new FilteredList<>(treeObjects,
            treeObject -> treeObject instanceof CategoryObject);
    public static final SortedList<TreeObject<?>> sortedCategories = new SortedList<>(categories,
            (to1, to2) -> to1.getParent() == null ? -1 : to1.getName().compareTo(to2.getName()));

    //Project
    public static final ObservableList<BaseUserData> users = FXCollections.observableArrayList();
    public static final ObservableList<String> userNames = FXCollections.observableArrayList("foo", "bar", "baz");
    public static final ObservableList<ProjectData> projects = FXCollections.observableArrayList(new ProjectData(0, "myProject1",
            "doc1...", userData, false, (byte) 1, Arrays.asList()),
            new ProjectData(0, "notMyProject2",
                    "doc13", new UserData(10, "quas", (byte) 3), true, (byte) 1, Arrays.asList()));

    //Connection
    public static SimpleBooleanProperty isConnected = new SimpleBooleanProperty(true); //true for testing
    public static SimpleBooleanProperty hasActiveSession = new SimpleBooleanProperty(true);

}
