import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collection;

/**
 * Created by michal on 12/28/16.
 */
public final class ProjectData {

    private int id;
    private String projName = "";
    private String docName = "";
    private BaseUserData owner;

    private SimpleBooleanProperty hasPasswdProperty = new SimpleBooleanProperty();
    private SimpleObjectProperty<Byte> defPrivProperty = new SimpleObjectProperty<>();
    private ObservableList<BaseUserData> listedUsers = FXCollections.observableArrayList();

    public ProjectData(int id,
                       String projName,
                       String docName,
                       BaseUserData owner,
                       boolean hasPasswd,
                       byte defaultPriv,
                       Collection<? extends BaseUserData> listedUsers) {
        this.id = id;
        this.projName = projName;
        this.docName = docName;
        this.setHasPasswd(hasPasswd);
        this.setDefPriv(defaultPriv);
        this.listedUsers.addAll(listedUsers);
        this.owner = owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProjName() {
        return projName;
    }

    public void setProjName(String projName) {
        this.projName = projName;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public BaseUserData getOwner() {
        return owner;
    }

    public void setOwner(BaseUserData owner) {
        this.owner = owner;
    }

    public boolean hasPasswd() {
        return this.hasPasswdProperty.get();
    }

    public SimpleBooleanProperty hasPasswdProperty() {
        return this.hasPasswdProperty;
    }

    public void setHasPasswd(boolean hasPasswd) {
        this.hasPasswdProperty.set(hasPasswd);
    }

    public Byte getDefPriv() {
        return this.defPrivProperty.get();
    }

    public SimpleObjectProperty<Byte> defPrivProperty() {
        return this.defPrivProperty;
    }

    public void setDefPriv(Byte defPriv) {
        this.defPrivProperty.set(defPriv);
    }

    public ObservableList<BaseUserData> getListedUsers() {
        return this.listedUsers;
    }

    public void setListedUsers(ObservableList<BaseUserData> listedUsers) {
        this.listedUsers = listedUsers;
    }
}