package com.example.gapshap.models;

public class GroupMessagesModel {

    String uId;
    String message;
    Long timestamp;

    public GroupMessagesModel(String uId, String message, Long timestamp) {
        this.uId = uId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public GroupMessagesModel(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }

    public GroupMessagesModel() {
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
