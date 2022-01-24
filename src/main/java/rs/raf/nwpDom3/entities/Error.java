package rs.raf.nwpDom3.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@ToString
@Entity
@Table(name="errors")
public class Error implements Comparable<Error> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private Date date;

    private String operation;

    @ManyToOne
    @JoinColumn(name="machine_id")
    @JsonIgnore
    private Machine machine;

    @Override
    public int compareTo(Error o) {
        if(this.date.before(o.getDate())) return 1;
        else if(this.date.after(o.getDate())) return -1;
        else return 0;
    }
}
