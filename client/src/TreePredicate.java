import java.util.function.Predicate;

/**
 * Source: http://www.kware.net/?p=204
 */

@FunctionalInterface
public interface TreePredicate<T extends TreeObject<?>> {

    boolean test(TreeObjectItem<T> parent, T value);
    static <T extends TreeObject<?>> TreePredicate<T> create(Predicate<T> predicate) {
        return ((parent, value) -> predicate.test(value));
    }
}
