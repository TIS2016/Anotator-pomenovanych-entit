import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.IndexRange;
import javafx.scene.paint.Color;

/**
 * Created by michal on 12/14/16.
 */
public abstract class DisplayedTreeObject<T extends DisplayedTreeObject<?>> extends TreeObject<T> {

    public enum Status {
        DEFAULT,
        UPDATING,
    }

    private final ColorObject[] colorObjects;
    private int start, end;
    private Status status = Status.DEFAULT;

    public DisplayedTreeObject(final TreeObject<?> parent) {
        super(MainLayout.textArea.getSelectedText());
        this.setParent(parent);
        IndexRange range = MainLayout.textArea.getSelection();
        colorObjects = new ColorObject[range.getLength()];
        this.start = range.getStart();
        this.end = range.getEnd();
        this.selectedProperty().addListener((observable, oldValue, newValue) -> this.onSelect());
        this.init();
    }

    private void init() {
        for (int i = this.start, j = 0; i < this.end; i++) {
            ColorObject colorObject = SessionData.colorObjects.get(i);
            if (colorObject != null) {
                colorObject.getBackreferences().add(this);
            } else {
                colorObject = new ColorObject(i, this);
                SessionData.colorObjects.put(i, colorObject);
            }
            colorObjects[j++] = colorObject;
        }
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
        //TODO?: ASYNC
        int it = 0;
        DisplayedTreeObject<?> dto1 = colorObjects[it].getLastSelectedBackreference();
        DisplayedTreeObject<?> dto2;
        int last = this.start, i = last;
        while (++it < colorObjects.length) {
            i++;
            dto2 = colorObjects[it].getLastSelectedBackreference();
            if ((dto1 != null && !dto1.equals(dto2)) || (dto2 != null && !dto2.equals(dto1))) {
                MainLayout.textArea.setStyle(last, i, dto1);
                dto1 = dto2;
                last = i;
            }
        }
        if (i != this.end) {
            MainLayout.textArea.setStyle(last, this.end, dto1);
        }
    }

    public final int getSize() {
        return colorObjects.length;
    }

    public final void onDelete() {
        int it = 0;
        DisplayedTreeObject<?> dto1;
        DisplayedTreeObject<?> dto2;
        {
            ColorObject first = colorObjects[it];
            first.getBackreferences().remove(this);
            dto1 = first.getLastBackreference();
        }
        int last = this.start, i = last;
        while (++it < colorObjects.length) {
            i++;
            ColorObject colorObject = colorObjects[it];
            colorObject.getBackreferences().remove(this);
            dto2 = colorObject.getLastSelectedBackreference();
            if (dto1 != null && !dto1.equals(dto2) || (dto2 != null && !dto2.equals(dto1))) {
                MainLayout.textArea.setStyle(last, i, dto1);
                dto1 = dto2;
                last = i;
            }
        }
        if (i != this.end) {
            MainLayout.textArea.setStyle(last, this.end, dto1);
        }
    }

    public abstract SimpleObjectProperty<Color> colorProperty();
}