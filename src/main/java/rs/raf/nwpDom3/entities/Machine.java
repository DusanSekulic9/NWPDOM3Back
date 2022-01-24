package rs.raf.nwpDom3.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@Table(name="machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private MachineStatus status;

    @ManyToOne
    @JoinColumn(name="createdBy")
    @JsonIgnore
    private User createdBy;

    private Boolean active;

    private String name;

    private Date creation;

    @Version
    private Integer version = 0;
}
