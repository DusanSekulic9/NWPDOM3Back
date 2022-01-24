package rs.raf.nwpDom3.forms;

import lombok.Data;

import java.util.Date;

@Data
public class MachineForm {

    private String name;

    private Long id;

    private Date date;

    private Integer version;


}
