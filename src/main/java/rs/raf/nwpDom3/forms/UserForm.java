package rs.raf.nwpDom3.forms;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserForm {

    private String username;
    private String password;
    private String name;
    private String surname;
    private boolean can_read_users = false;
    private boolean can_create_users = false;
    private boolean can_update_users = false;
    private boolean can_delete_users = false;


}
