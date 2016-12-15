import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.util.stream.Collectors;

/**
 * Created by michal on 12/14/16.
 */
public class ColorObject { //TODO: ???

    private ObservableList<TreeObject<?>> referencedBy = FXCollections.observableArrayList();
    private SimpleObjectProperty<Color> colorProperty = new SimpleObjectProperty<>();
    private int index;

    public ColorObject(int index, TreeObject<?> treeObject) {
        this.index = index;
        referencedBy.addListener(((ListChangeListener.Change<? extends TreeObject<?>> c) -> {
            if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                int intColor = (int) Math.round(referencedBy.stream().
                        map(to -> { if (to instanceof CategoryObject) return (CategoryObject) to.getParent();
                                    else return (CategoryObject) to.getParent().getParent(); }).
                        collect(Collectors.averagingInt(CategoryObject::getIntColor)));
                this.colorProperty.set(Color.valueOf(String.format("#%06X", (0xFFFFFF & intColor))));
            }
            if (referencedBy.size() == 0) {
                //TODO:
            }
            System.out.println("foobar");
        }));
        referencedBy.add(treeObject);
    }

    public final ObservableList<TreeObject<?>> getReferencedBy() {
        return this.referencedBy;
    }

    public final int getIndex() {
        return index;
    }

    public final Color getColor() {
        return colorProperty.get();
    }

}
