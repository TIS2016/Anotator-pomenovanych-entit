import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyledTextArea;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by michal on 12/14/16.
 */
public abstract class DisplayedTreeObject<T extends DisplayedTreeObject<?>> extends TreeObject<T> {

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
        //TODO: move to treeObjectItem?
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

    @Override
    public final String getName() {
        return String.format("[%d:%d] %s", this.start, this.end, super.getName());
    }

    public final Status getStatus() {
        return status;
    }

    public final void setStatus(Status status) {
        this.status = status;
    }

    public final void onSelect() {
        Iterator<ColorObject> it = colorObjects.iterator();
        DisplayedTreeObject<?> dto1 = it.next().getLastSelectedBackreference();
        DisplayedTreeObject<?> dto2;
        int last = this.start, i = last;
        while (it.hasNext()) {
            i++;
            ColorObject colorObject = it.next();
            dto2 = colorObject.getLastSelectedBackreference();
            if (dto1 != null && !dto1.equals(dto2) || (dto2 != null && !dto2.equals(dto1))) {
                textArea.setStyle(last, i, dto1);
                dto1 = dto2;
                last = i;
            }
        }
        if (i != this.end) {
            textArea.setStyle(last, this.end, dto1);
        }
    }

    public final void onDelete() {
        Iterator<ColorObject> it = colorObjects.iterator();
        DisplayedTreeObject<?> dto1;
        DisplayedTreeObject<?> dto2;
        {
            ColorObject first = it.next();
            first.getBackreferences().remove(this);
            dto1 = first.getLastBackreference();
        }
        int last = this.start, i = last;
        while (it.hasNext()) {
            i++;
            ColorObject colorObject = it.next();
            colorObject.getBackreferences().remove(this);
            dto2 = colorObject.getLastSelectedBackreference();
            if (dto1 != null && !dto1.equals(dto2) || (dto2 != null && !dto2.equals(dto1))) {
                textArea.setStyle(last, i, dto1);
                dto1 = dto2;
                last = i;
            }
        }
        if (i != this.end) {
            textArea.setStyle(last, this.end, dto1);
        }
    }

    public abstract SimpleObjectProperty<Color> colorProperty();
}