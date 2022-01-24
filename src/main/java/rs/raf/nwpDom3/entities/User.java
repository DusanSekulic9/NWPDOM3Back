package rs.raf.nwpDom3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String surname;

    @Column(unique=true)
    private String email;

    private String password;

    private boolean can_read_users = false;
    private boolean can_create_users = false;
    private boolean can_delete_users = false;
    private boolean can_update_users = false;
    private boolean can_search_machines = false;
    private boolean can_start_machines = false;
    private boolean can_stop_machines = false;
    private boolean can_restart_machines = false;
    private boolean can_create_machines = false;
    private boolean can_destroy_machines = false;

    @OneToMany(mappedBy="createdBy")
    private transient List<Machine> machines;
}
