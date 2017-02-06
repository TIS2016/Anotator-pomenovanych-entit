package org.ape.annotations.treeObjects;

import org.ape.annotations.ColorObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.IndexRange;
import javafx.scene.paint.Color;
import org.ape.AppData;
import org.fxmisc.richtext.model.TwoDimensional;

public abstract class DisplayedTreeObject<T extends DisplayedTreeObject<?>> extends TreeObject<T> {

    private final ChangeListener<? super Boolean> doOnSelect = (observable, oldValue, newValue) -> this.onSelect();
    private ColorObject[] colorObjects;
    private int start, end, lineNum, caretCol;

    private static String FORMAT = "[%d:%d] %s";

    DisplayedTreeObject(long id) {
        super(id, AppData.textArea.getSelectedText());
        final IndexRange range = AppData.textArea.getSelection();
        this.init(range.getStart(), range.getEnd());
    }

    DisplayedTreeObject(long id, int start, int end) {
        super(id, AppData.textArea.getText(start, end));
        this.init(start, end);
    }

    private void init(int start, int end) {
        this.start = start;
        this.end = end;
        this.colorObjects = new ColorObject[this.end - this.start];
        TwoDimensional.Position position = AppData.textArea.offsetToPosition(start, TwoDimensional.Bias.Backward);
        this.lineNum = position.getMajor() + 1;
        this.caretCol = position.getMinor();
        for (int i = this.start, j = 0; i < this.end; i++) {
            ColorObject colorObject = AppData.colorObjects.get(i);
            if (colorObject != null) {
                colorObject.getAllDisplayedTreeObjects().add(this);
            } else {
                colorObject = new ColorObject(this);
                AppData.colorObjects.put(i, colorObject);
            }
            colorObjects[j++] = colorObject;
        }
        this.selectedProperty().addListener(this.doOnSelect);
    }

    public final int getStart() {
        return this.start;
    }

    public final int getEnd() {
        return this.end;
    }

    public final int getLineNum() {
        return this.lineNum;
    }

    public final int getCaretCol() {
        return this.caretCol;
    }

    @Override
    public final String getTreeName() {
        return String.format(DisplayedTreeObject.FORMAT, this.lineNum, this.caretCol, super.getTreeName());
    }

    private void onSelect() {
        int it = 0;
        DisplayedTreeObject<?> dto1 = colorObjects[it].getLastVisibleDislayedTreeObject();
        DisplayedTreeObject<?> dto2;
        int last = this.start, i = last;
        while (++it < colorObjects.length) {
            i++;
            dto2 = colorObjects[it].getLastVisibleDislayedTreeObject();
            if ((dto1 != null && !dto1.equals(dto2)) || (dto2 != null && !dto2.equals(dto1))) {
                final DisplayedTreeObject<?> dto = dto1;
                final int x = last, y = i;
                Platform.runLater(() -> AppData.textArea.setStyle(x, y, dto));
                dto1 = dto2;
                last = i;
            }
        }
        if (i != this.end) {
            final DisplayedTreeObject<?> dto = dto1;
            final int x = last, y = this.end;
            Platform.runLater(() ->  AppData.textArea.setStyle(x, y, dto));
        }
    }

    public final int getSize() {
        return colorObjects.length;
    }

    @Override
    public void onDelete() {
        super.onDelete();
        this.selectedProperty().removeListener(this.doOnSelect);
        int it = 0;
        DisplayedTreeObject<?> dto1 = colorObjects[it].removeDislayedTreeObject(this);
        DisplayedTreeObject<?> dto2;
        int last = this.start, i = last;
        while (++it < colorObjects.length) {
            i++;
            ColorObject colorObject = colorObjects[it];
            colorObject.getAllDisplayedTreeObjects().remove(this);
            dto2 = colorObject.getLastVisibleDislayedTreeObject();
            if (dto1 != null && !dto1.equals(dto2) || (dto2 != null && !dto2.equals(dto1))) {
                final DisplayedTreeObject<?> dto = dto1;
                final int x = last, y = i;
                Platform.runLater(() -> AppData.textArea.setStyle(x, y, dto));
                dto1 = dto2;
                last = i;
            }
        }
        if (i != this.end) {
            final DisplayedTreeObject<?> dto = dto1;
            final int x = last, y = this.end;
            Platform.runLater(() ->  AppData.textArea.setStyle(x, y, dto));
        }
    }

    public abstract SimpleObjectProperty<Color> colorProperty();
}