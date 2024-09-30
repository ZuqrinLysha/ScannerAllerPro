package com.example.scannerallerpro; // Change this to your actual package name if it's different

public class Contact {

    private String doctorName;
    private String doctorContactPhone;
    private String medicalInstitutionName;
    private String medicalContactPhone;
    private String familyContactName;
    private String familyContactPhone;
    private String relationship;

    // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    public Contact() {
    }

    // Constructor for initializing the Contact object
    public Contact(String doctorName, String doctorContactPhone, String medicalInstitutionName,
                   String medicalContactPhone, String familyContactName, String familyContactPhone,
                   String relationship) {
        this.doctorName = doctorName;
        this.doctorContactPhone = doctorContactPhone;
        this.medicalInstitutionName = medicalInstitutionName;
        this.medicalContactPhone = medicalContactPhone;
        this.familyContactName = familyContactName;
        this.familyContactPhone = familyContactPhone;
        this.relationship = relationship;
    }

    // Getter and setter methods for doctorName
    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    // Getter and setter methods for doctorContactPhone
    public String getDoctorContactPhone() {
        return doctorContactPhone;
    }

    public void setDoctorContactPhone(String doctorContactPhone) {
        this.doctorContactPhone = doctorContactPhone;
    }

    // Getter and setter methods for medicalInstitutionName
    public String getMedicalInstitutionName() {
        return medicalInstitutionName;
    }

    public void setMedicalInstitutionName(String medicalInstitutionName) {
        this.medicalInstitutionName = medicalInstitutionName;
    }

    // Getter and setter methods for medicalContactPhone
    public String getMedicalContactPhone() {
        return medicalContactPhone;
    }

    public void setMedicalContactPhone(String medicalContactPhone) {
        this.medicalContactPhone = medicalContactPhone;
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
