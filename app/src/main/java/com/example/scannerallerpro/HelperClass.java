package com.example.scannerallerpro;

public class HelperClass {
    String fullName, email, username, password, height, weight, bmi, bloodType, allergicHistory, phoneNumber;

    // Constructor
    public HelperClass(String fullName, String email, String username, String password,
                       String height, String weight, String bmi,
                       String bloodType, String allergicHistory, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.height = height;
        this.weight = weight;
        this.bmi = bmi;
        this.bloodType = bloodType;
        this.allergicHistory = allergicHistory;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters (Optional, depending on your needs)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


}