package com.example.scannerallerpro; // Change this to your actual package name if it's different

public class Contact {

    private String healthCareContactName;
    private String healthCareContactPhone;
    private String medicalCenterContactName;
    private String medicalCenterContactPhone;
    private String familyContactName;
    private String familyContactPhone;
    private String relationship;

    // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    public Contact() {
    }

    // Constructor for initializing the Contact object
    public Contact(String healthCareContactName, String healthCareContactPhone,
                   String medicalCenterContactName, String medicalCenterContactPhone,
                   String familyContactName, String familyContactPhone,
                   String relationship) {
        this.healthCareContactName = healthCareContactName;
        this.healthCareContactPhone = healthCareContactPhone;
        this.medicalCenterContactName = medicalCenterContactName;
        this.medicalCenterContactPhone = medicalCenterContactPhone;
        this.familyContactName = familyContactName;
        this.familyContactPhone = familyContactPhone;
        this.relationship = relationship;
    }

    // Getter and setter methods for healthCareContactName
    public String getHealthCareContactName() {
        return healthCareContactName;
    }

    public void setHealthCareContactName(String healthCareContactName) {
        this.healthCareContactName = healthCareContactName;
    }

    // Getter and setter methods for healthCareContactPhone
    public String getHealthCareContactPhone() {
        return healthCareContactPhone;
    }

    public void setHealthCareContactPhone(String healthCareContactPhone) {
        this.healthCareContactPhone = healthCareContactPhone;
    }

    // Getter and setter methods for medicalCenterContactName
    public String getMedicalCenterContactName() {
        return medicalCenterContactName;
    }

    public void setMedicalCenterContactName(String medicalCenterContactName) {
        this.medicalCenterContactName = medicalCenterContactName;
    }

    // Getter and setter methods for medicalCenterContactPhone
    public String getMedicalCenterContactPhone() {
        return medicalCenterContactPhone;
    }

    public void setMedicalCenterContactPhone(String medicalCenterContactPhone) {
        this.medicalCenterContactPhone = medicalCenterContactPhone;
    }

    // Getter and setter methods for familyContactName
    public String getFamilyContactName() {
        return familyContactName;
    }

    public void setFamilyContactName(String familyContactName) {
        this.familyContactName = familyContactName;
    }

    // Getter and setter methods for familyContactPhone
    public String getFamilyContactPhone() {
        return familyContactPhone;
    }

    public void setFamilyContactPhone(String familyContactPhone) {
        this.familyContactPhone = familyContactPhone;
    }

    // Getter and setter methods for relationship
    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
