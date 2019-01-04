package pe.fcg.kth.id1212.filecatalog.presentation.model;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class SignUpForm {
    @Pattern(regexp = "[a-z]{4,8}", message = "The username must consist only of 4 to 8 lowercase letters")
    private String username;
    @Size(min = 8, max = 20, message = "The password must be between 8 and 20 characters long")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
