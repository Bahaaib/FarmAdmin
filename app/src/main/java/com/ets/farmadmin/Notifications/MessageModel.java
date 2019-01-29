package com.ets.farmadmin.Notifications;

public class MessageModel {

    private String title;

    private String message;

    public MessageModel() {
        //Required Empty Constructor
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
