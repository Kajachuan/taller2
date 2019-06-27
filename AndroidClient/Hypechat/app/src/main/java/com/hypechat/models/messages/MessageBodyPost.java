package com.hypechat.models.messages;

public class MessageBodyPost {
    private String message;
    private String sender;
    private String type;

    public MessageBodyPost (String message,String sender, String type){
        this.message = message;
        this.sender = sender;
        this.type = type;
    }
}
