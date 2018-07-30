package com.example.android.medex;

import android.net.Uri;

import java.io.Serializable;
import java.net.URI;

public class Person implements Serializable {

    private String personName;
    private String personEmail;
    private String personId;
    private String personToken;
    private String personMobile;
    private String personDistrict;
    private String personBloodGroup;
    private String personPhoto;

    public String getPersonEmail() {
        return personEmail;
    }

    public void setPersonEmail(String personEmail) {
        this.personEmail = personEmail;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public String getPersonToken() {
        return personToken;
    }

    public void setPersonToken(String personToken) {
        this.personToken = personToken;
    }

    public String getPersonMobile() {
        return personMobile;
    }

    public void setPersonMobile(String personMobile) {
        this.personMobile = personMobile;
    }

    public String getPersonDistrict() {
        return personDistrict;
    }

    public void setPersonDistrict(String personDistrict) {
        this.personDistrict = personDistrict;
    }

    public String getPersonBloodGroup() {
        return personBloodGroup;
    }

    public void setPersonBloodGroup(String personBloodGroup) {
        this.personBloodGroup = personBloodGroup;
    }

    public String getPersonPhoto() {
        return personPhoto;
    }

    public void setPersonPhoto(String personPhoto) {
        this.personPhoto = personPhoto;
    }

    public String getPersonName() {

        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

}
