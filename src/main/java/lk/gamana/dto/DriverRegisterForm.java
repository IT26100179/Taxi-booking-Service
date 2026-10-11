package lk.gamana.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DriverRegisterForm {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "0[0-9]{9}", message = "Phone must be a 10-digit number starting with 0")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String confirmPassword;

    @NotBlank(message = "NIC number is required")
    private String nic;

    @NotBlank(message = "Driving license number is required")
    private String licenseNo;

    @NotNull(message = "Please select a valid vehicle type")
    @Min(value = 1, message = "Please select a valid vehicle type")
    private Integer typeId;

    @NotBlank(message = "Vehicle plate number is required")
    private String plateNo;

    @NotBlank(message = "Vehicle make & model is required")
    private String makeModel;

    @NotBlank(message = "Vehicle color is required")
    private String color;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }

    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }

    public Integer getTypeId() { return typeId; }
    public void setTypeId(Integer typeId) { this.typeId = typeId; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }

    public String getMakeModel() { return makeModel; }
    public void setMakeModel(String makeModel) { this.makeModel = makeModel; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
