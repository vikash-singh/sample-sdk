package com.gopaysense.psdata.models;

/**
 * Created by Vikash Singh on 5/10/17.
 */

public class SMS {

    private String id;
    private String body;
    private Long date;
    private String person;
    private Integer threadId;
    private String type;
    private String subject;
    private String creator;

    private String address;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBody() {
        return  body;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public Integer getThreadId() {
        return threadId;
    }

    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String log() {
        return "ID = " + this.id + "\n"
                + "CREATOR = " + this.creator + "\n"
                + "PERSON = " + this.person + "\n"
                + "SUB = "+ this.subject + "\n"
                + "DATE = "+ this.date + "\n"
                + "THREAD = "+ this.threadId + "\n"
                + "TYPE = "+ this.type + "\n"
                + "ADDR = "+ this.address + "\n"
                + "BODY = "+ this.body + "\n";
    }
}
