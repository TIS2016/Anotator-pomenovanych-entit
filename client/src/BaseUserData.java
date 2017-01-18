import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by michal on 11/23/16.
 */
public class BaseUserData {

    private int id;
    private final SimpleStringProperty userName = new SimpleStringProperty(); //properties necessary in project
    private final SimpleObjectProperty<String> privileges = new SimpleObjectProperty<>();

    public BaseUserData(int id, String userName, byte privileges) {
        this.setId(id);
        this.setUserName(userName);
        this.setPrivileges(privileges);
    }

    public BaseUserData(int id, String userName, String privileges) {
        this(id, userName, PrivilegeConverter.fromString(privileges));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName.get();
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public String getPrivileges() {
        return privileges.get();
    }

    public SimpleObjectProperty<String> privilegesProperty() {
        return privileges;
    }

    public void setPrivileges(String privileges) {
        this.privileges.set(privileges);
    }

    public void setPrivileges(Byte privileges) {
        this.privileges.set(PrivilegeConverter.toString(privileges));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BaseUserData && this.getId() == ((BaseUserData) other).getId();
    }
}
