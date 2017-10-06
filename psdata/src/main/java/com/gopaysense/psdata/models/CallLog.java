package com.gopaysense.psdata.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/*
 * Created by Vikash Singh on 5/10/17.
 */

public class CallLog {

    @SerializedName("contact_no")
    @Expose
    private String contactNo;

    @SerializedName("call_type")
    @Expose
    private String callType;

    @Expose
    private Long timestamp;

    @Expose
    private Integer duration;

    private Integer weight;

    public CallLog() {

    }

    public CallLog(String contactNo, String callType, Long timestamp, Integer duration) {
        this.contactNo = contactNo;
        this.callType = callType;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String log() {
        return this.contactNo + " - " + this.callType + " - " + this.timestamp + " - " + this.duration;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }


    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
