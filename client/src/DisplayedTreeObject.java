import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyledTextArea;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by michal on 12/14/16.
 */
public abstract class DisplayedTreeObject<T extends DisplayedTreeObject<?>> extends TreeObject<TreeObject<?>> {

    public enum Status {
        DEFAULT,
        UPDATING,
    }
    private final ArrayList<ColorObject> colorObjects = new ArrayList<>();
    private StyledTextArea<Void, DisplayedTreeObject<?>> textArea;
    private int start, end;
    private Status status = Status.DEFAULT;

    public DisplayedTreeObject(final StyledTextArea<Void, DisplayedTreeObject<?>> textArea,
                               final TreeObject<?> parent) {
        super(textArea.getSelectedText());
        this.setParent(parent);
        this.start = textArea.getSelection().getStart();
        this.end = textArea.getSelection().getEnd();
        this.textArea = textArea;
        this.selectedProperty().addListener((observable, oldValue, newValue) -> this.onSelect());
        this.init();
    }

    private final void init() {
        for (int i = this.start; i < this.end; i++) {
            ColorObject colorObject = SessionData.colorObjects.get(i);
            if (colorObject != null) {
                colorObject.getBackreferences().add(this);
            } else {
                colorObject = new ColorObject(i, this);
                SessionData.colorObjects.put(i, colorObject);
            }
            colorObjects.add(colorObject);
        }
        textArea.setStyle(this.start, this.end, this);
    }

    public final Status getStatus() {
        return status;
    }

    public final void setStatus(Status status) {
        this.status = status;
    }

    public final void onSelect() {
        Iterator<ColorObject> it = colorObjects.iterator();
        DisplayedTreeObject<?> color1 = it.next().getLastSelectedBackreference();
        DisplayedTreeObject<?> color2;
        int last = this.start, i = last;
        while (it.hasNext()) {
            i++;
            ColorObject colorObject = it.next();
            color2 = colorObject.getLastSelectedBackreference();
            if ((color1 == null && color2 != null) || (color1 != null && !color1.equals(color2))) {
                textArea.setStyle(last, i, color1);
                color1 = color2;
                last = i;
            }
        }
        if (i++ != last) {
            textArea.setStyle(last, i, color1);
        }
    }

    public final void onDelete() {
        //TODO: status if was already deleted
        Iterator<ColorObject> it = colorObjects.iterator();
        DisplayedTreeObject<?> color1;
        DisplayedTreeObject<?> color2;
        {
            ColorObject first = it.next();
            first.getBackreferences().remove(this);
            color1 = first.getLastBackreference();
        }
        int last = this.start, i = last;
        while (it.hasNext()) {
            i++;
            ColorObject colorObject = it.next();
            colorObject.getBackreferences().remove(this);
            color2 = colorObject.getLastBackreference();
            if ((color1 == null && color2 != null) || (color1 != null && !color1.equals(color2))) {
                textArea.setStyle(last, i, color1);
                color1 = color2;
                last = i;
            }
        }
        if (i++ != last) {
            textArea.setStyle(last, i, color1);
        }
        this.getChildren().forEach(dto -> ((DisplayedTreeObject ) dto).onDelete());
    }

    public abstract SimpleObjectProperty<Color> colorProperty();
}