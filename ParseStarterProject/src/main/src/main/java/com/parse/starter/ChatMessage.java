package com.parse.starter;

/**
 * Created by Technovibe on 17-04-2015.
 */
public class ChatMessage {
    public static String TEXT_TYPE = "text";
    public static String PICTURE_TYPE = "picture";
    private long id;
    private boolean isMe;
    private String message;
    private String messageType;
    private Long userId;
    private String dateTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean getIsme() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String message_type) {
        this.messageType = message_type;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getDate() {
        return dateTime;
    }

    public void setDate(String dateTime) {
        this.dateTime = dateTime;
    }
}
