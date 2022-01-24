package rs.raf.nwpDom3.forms;

import lombok.Data;

import java.util.Date;

@Data
public class SearchFrom {

    private String name;
    private String status;
    private Date dateFrom;
    private Date dateTo;

}
