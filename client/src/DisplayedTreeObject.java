import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Created by michal on 12/14/16.
 */
public abstract class DisplayedTreeObject<T extends DisplayedTreeObject<?>> extends TreeObject<DisplayedTreeObject<?>> {

    //protected final ObservableList<ColorObject> colors = FXCollections.emptyObservableList();

    public DisplayedTreeObject(String name) {
        super(name);
    }
        //TODO: create/find colorobjects for annotated text ???
}
