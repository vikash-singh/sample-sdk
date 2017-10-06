package com.gopaysense.psdata.models;

/**
 * Created by Vikash Singh on 6/10/17.
 */

public class UserContactFeature {

    UserContact userContact;
    Integer normalizedCallDurationScore = 0;
    Integer normalizedClosenessScore = 0;
    Integer normalizedNoOfCalls = 0;
    Integer callDurationScore = 0;
    Integer closenessScore = 0;
    Integer noOfCalls = 0;

    public String log() {
        return this.userContact.getName() + ", " + this.userContact.getContactNos() + ", "
                + "(" + this.callDurationScore + ", " + this.normalizedCallDurationScore + ")"
                + "(" + this.closenessScore + ", " + this.normalizedClosenessScore + ")"
                + "(" + this.noOfCalls + ", " + this.normalizedNoOfCalls + ")";
    }
    public UserContactFeature(UserContact userContact) {
        this.userContact = userContact;
    }

    public UserContact getUserContact() {
        return userContact;
    }

    public void setUserContact(UserContact userContact) {
        this.userContact = userContact;
    }

    public Integer getNormalizedCallDurationScore() {
        return normalizedCallDurationScore;
    }

    public void setNormalizedCallDurationScore(Integer normalizedCallDurationScore) {
        this.normalizedCallDurationScore = normalizedCallDurationScore;
    }

    public Integer getNormalizedClosenessScore() {
        return normalizedClosenessScore;
    }

    public void setNormalizedClosenessScore(Integer normalizedClosenessScore) {
        this.normalizedClosenessScore = normalizedClosenessScore;
    }

    public Integer getCallDurationScore() {
        return callDurationScore;
    }

    public void setCallDurationScore(Integer callDurationScore) {
        this.callDurationScore = callDurationScore;
    }

    public Integer getClosenessScore() {
        return closenessScore;
    }

    public void setClosenessScore(Integer closenessScore) {
        this.closenessScore = closenessScore;
    }

    public Integer getNoOfCalls() {
        return noOfCalls;
    }

    public void setNoOfCalls(Integer noOfCalls) {
        this.noOfCalls = noOfCalls;
    }

    public Integer getNormalizedNoOfCalls() {
        return normalizedNoOfCalls;
    }

    public void setNormalizedNoOfCalls(Integer normalizedNoOfCalls) {
        this.normalizedNoOfCalls = normalizedNoOfCalls;
    }
}
