import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.StyledTextArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michal on 11/28/16.
 */
public class AnnotationObject extends DisplayedTreeObject<ReferenceObject> {

    private List<String> links = new ArrayList<>();
    private String description; //TODO: use string property?

    public AnnotationObject(final CategoryObject categoryObject,
                            String description,
                            List<String> links) {
        super(categoryObject);
        this.description = description;
        this.links = links;
    }

    public final void setLinks(final List<String> links) {
        this.links = links;
    }

    public final List<String> getLinks() {
        return links;
    }

    public final String getDescription() {
        return this.description;
    }

    public final void setDescription(String description) {
        this.description = description;
    }

    @Override
    public final SimpleObjectProperty<Color> colorProperty() {
        return ((CategoryObject) this.getParent()).colorProperty();
    }
}
