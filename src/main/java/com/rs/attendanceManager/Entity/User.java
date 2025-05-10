package com.rs.attendanceManager.Entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(unique = true, nullable = false)
    private String grNo;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String subDepartment;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal totalAttendance;

    @Lob
    @Column(nullable = true, columnDefinition = "LONGBLOB")
    private byte[] photo; //stores url of the photo

    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String area;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private boolean isInitiated;

    @Column
    private String remarks;

    @Column
    private String email;

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getGrNo() {
        return this.grNo;
    }

    public void setGrNo(String grNo) {
        this.grNo = grNo;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return this.department;
    }

    public String getSubDepartment() {
        return subDepartment;
    }

    public void setSubDepartment(String subDepartment) {
        this.subDepartment = subDepartment;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getTotalAttendance() {
        return this.totalAttendance;
    }

    public void setTotalAttendance(BigDecimal totalAttendance) {
        this.totalAttendance = totalAttendance;
    }

    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getArea() {
        return this.area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getAge() {
        return this.age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean getIsInitiated() {
        return this.isInitiated;
    }

    public void setIsInitiated(boolean isInitiated) {
        this.isInitiated = isInitiated;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
