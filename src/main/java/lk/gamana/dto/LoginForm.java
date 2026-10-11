package lk.gamana.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginForm {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "0[0-9]{9}", message = "Phone number must be 10 digits starting with 0 (e.g., 0771234567)")
    private String phone;

    @NotBlank(message = "Password is required")
    private String password;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
