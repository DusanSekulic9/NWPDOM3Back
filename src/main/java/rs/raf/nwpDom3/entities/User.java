package rs.raf.nwpDom3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.HashSet;
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
}
