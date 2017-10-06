package com.gopaysense.psdata.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by Vikash Singh on 5/10/17.
 */

public class UserContact implements Serializable {

    private String id;

    private String name;

//    @Expose
//    @SerializedName("contact_no")
//    private String contactNo;

    private Set<String> contactNos;

    @Expose
    private String email;

    private String uriString;

    private Boolean isStarred;

    private Integer pinned;

    private Boolean isSelected = false;

    private String photoUri;

    public void addContact(String phoneNumber) {
        if (contactNos == null) {
            contactNos = new HashSet<>();
        }

        if (phoneNumber == null || phoneNumber.isEmpty())
            return;

        contactNos.add(phoneNumber);
    }

    public String log() {
        return this.id + " - " + this.name + " - " + this.contactNos
                + " - " + this.email + " - " + this.isStarred + " - " + this.pinned + " - " + this.photoUri;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public Boolean getStarred() {
        return isStarred;
    }

    public void setStarred(Boolean starred) {
        isStarred = starred;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }


    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

//    public String getContactNo() {
//        return contactNo;
//    }
//
//    public void setContactNo(String contactNo) {
//        this.contactNo = contactNo;
//    }


    public Integer getPinned() {
        return pinned;
    }

    public void setPinned(Integer pinned) {
        this.pinned = pinned;
    }

    public Set<String> getContactNos() {
        return contactNos;
    }

    public void setContactNos(Set<String> contactNos) {
        this.contactNos = contactNos;
    }
}
