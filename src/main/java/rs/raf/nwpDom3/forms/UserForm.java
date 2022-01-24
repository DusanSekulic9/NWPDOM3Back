package rs.raf.nwpDom3.forms;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class UserForm {

    @NotNull(message = "Username is mandatory")
    private String username;
    @NotNull(message = "Password is mandatory")
    private String password;
    @NotNull(message = "Name is mandatory")
    private String name;
    @NotNull(message = "Surname is mandatory")
    private String surname;
    private boolean can_read_users = false;
    private boolean can_create_users = false;
    private boolean can_update_users = false;
    private boolean can_delete_users = false;
    private boolean can_search_machines = false;
    private boolean can_start_machines = false;
    private boolean can_stop_machines = false;
    private boolean can_restart_machines = false;
    private boolean can_create_machines = false;
    private boolean can_destroy_machines = false;


}
