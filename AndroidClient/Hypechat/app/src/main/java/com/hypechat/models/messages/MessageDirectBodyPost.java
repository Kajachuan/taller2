package com.hypechat.models.messages;

public class MessageDirectBodyPost {
    private String message;
    private String from;
    private String to;
    private String type;

    public MessageDirectBodyPost(String message, String from, String to, String type){
        this.message = message;
        this.from = from;
        this.to = to;
        this.type = type;
    }
}
