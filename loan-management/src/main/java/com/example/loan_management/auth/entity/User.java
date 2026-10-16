
package com.example.loan_management.auth.entity;

import com.example.loan_management.entity.Cmrc;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // USER DETAILS
    // =========================================================

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "USER";

    // =========================================================
    // LOCATION DETAILS
    // =========================================================

    @Column(name = "district")
    private String district;

    @Column(name = "taluka")
    private String taluka;

    // =========================================================
    // ACCOUNT DETAILS
    // =========================================================

    @Column(name = "account_no")
    private String accountNo;

    @Column(name = "account_opening_date")
    private LocalDate accountOpeningDate;

    // =========================================================
    // CMRC RELATIONSHIP
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmrc_id")
    private Cmrc cmrc;

    // =========================================================
    // CONSTRUCTORS
    // =========================================================

    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // =========================================================
    // GETTERS AND SETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public LocalDate getAccountOpeningDate() {
        return accountOpeningDate;
    }

    public void setAccountOpeningDate(LocalDate accountOpeningDate) {
        this.accountOpeningDate = accountOpeningDate;
    }

    public Cmrc getCmrc() {
        return cmrc;
    }

    public void setCmrc(Cmrc cmrc) {
        this.cmrc = cmrc;
    }
}
