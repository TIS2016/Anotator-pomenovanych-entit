/**
 * Created by michal on 11/28/16.
 */
public class CategoryObject extends TreeObject<TreeObject<?>> {

    public CategoryObject(String name) {
        super(name);
        //TODO: add more attributes
    }

    public final void deleteChildrenRecursive() {
        this.getChildren().stream().filter(child -> child instanceof CategoryObject).forEach(child -> {
            ((CategoryObject) child).deleteChildrenRecursive();
        });
        this.getChildren().clear();
    }
}
