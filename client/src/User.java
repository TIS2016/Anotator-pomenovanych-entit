import javafx.beans.property.SimpleBooleanProperty;

/**
 * Created by michal on 11/23/16.
 */
public class User {

    public static SimpleBooleanProperty isConnected = new SimpleBooleanProperty(true); //true for testing
    public static SimpleBooleanProperty hasActiveSession = new SimpleBooleanProperty(true);

    public static void init() {
        //TODO ?
    }
}
