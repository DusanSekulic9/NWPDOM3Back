package rs.raf.nwpDom3.forms;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class LogInForm {

    @NotNull
    private String username;
    @NotNull
    private String password;
}
