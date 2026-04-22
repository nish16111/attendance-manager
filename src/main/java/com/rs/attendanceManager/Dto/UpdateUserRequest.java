package com.rs.attendanceManager.Dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class UpdateUserRequest {

    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "department is required")
    @Size(max = 100, message = "department cannot exceed 100 characters")
    private String department;

    @NotBlank(message = "subDepartment is required")
    @Size(max = 100, message = "subDepartment cannot exceed 100 characters")
    private String subDepartment;

    @NotNull(message = "totalAttendance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "totalAttendance must be >= 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "totalAttendance must be <= 100")
    private BigDecimal totalAttendance;

    private MultipartFile photo;

    @NotBlank(message = "mobileNumber is required")
    @Size(max = 20, message = "mobileNumber cannot exceed 20 characters")
    private String mobileNumber;

    @NotBlank(message = "area is required")
    @Size(max = 100, message = "area cannot exceed 100 characters")
    private String area;

    @NotNull(message = "age is required")
    @Min(value = 1, message = "age must be at least 1")
    @Max(value = 120, message = "age must be at most 120")
    private Integer age;

    @NotNull(message = "isInitiated is required")
    private Boolean isInitiated;

    @Size(max = 300, message = "remarks cannot exceed 300 characters")
    private String remarks;

    @Email(message = "email must be valid")
    @Size(max = 120, message = "email cannot exceed 120 characters")
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSubDepartment() {
        return subDepartment;
    }

    public void setSubDepartment(String subDepartment) {
        this.subDepartment = subDepartment;
    }

    public BigDecimal getTotalAttendance() {
        return totalAttendance;
    }

    public void setTotalAttendance(BigDecimal totalAttendance) {
        this.totalAttendance = totalAttendance;
    }

    public MultipartFile getPhoto() {
        return photo;
    }

    public void setPhoto(MultipartFile photo) {
        this.photo = photo;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Boolean getIsInitiated() {
        return isInitiated;
    }

    public void setIsInitiated(Boolean initiated) {
        isInitiated = initiated;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
