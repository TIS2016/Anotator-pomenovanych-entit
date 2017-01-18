/**
 * Created by michal on 12/28/16.
 */
//TODO?: more attributes, etc.
public class UserData extends BaseUserData {

    public UserData(int id, String userName, byte privileges) {
        super(id, userName, privileges);
    }

    public UserData(int id, String userName, String privileges) {
        super(id, userName, privileges);
    }
}
