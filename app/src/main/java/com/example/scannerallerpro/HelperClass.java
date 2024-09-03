package com.example.scannerallerpro;

public class HelperClass {
    private String fullname;
    private String email;
    private String username;
    private String password;
    private String confirmPassword;
    private String height;
    private String weight;
    private String bmi;
    private String bloodType;
    private String allergicHistory;

    // Constructor with all fields
    public HelperClass(String fullname, String email, String username, String password, String confirmPassword,
                       String height, String weight, String bmi, String bloodType, String allergicHistory) {
        this.fullname = fullname;
        this.email = email;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.height = height;
        this.weight = weight;
        this.bmi = bmi;
        this.bloodType = bloodType;
        this.allergicHistory = allergicHistory;
    }

    // Default constructor
    public HelperClass() {
    }

    // Getters and setters for all fields
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBmi() {
        return bmi;
    }

    public void setBmi(String bmi) {
        this.bmi = bmi;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergicHistory() {
        return allergicHistory;
    }

    public void setAllergicHistory(String allergicHistory) {
        this.allergicHistory = allergicHistory;
    }
}
