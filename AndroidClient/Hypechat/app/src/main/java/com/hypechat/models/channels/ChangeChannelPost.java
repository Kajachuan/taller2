package com.hypechat.models.channels;

public class ChangeChannelPost {

    private boolean privado;
    private String description;
    private String welcome_message;

    public ChangeChannelPost(     boolean privado,
             String description,
             String welcome_message){
        this.privado = privado;
        this.description = description;
        this.welcome_message = welcome_message;
    }
}
