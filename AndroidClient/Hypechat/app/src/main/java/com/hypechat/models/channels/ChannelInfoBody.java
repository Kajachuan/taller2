package com.hypechat.models.channels;

public class ChannelInfoBody {

    private String owner;
    private String is_private;
    private String description;
    private String welcome_message;
    private String messages;
    private String members;

    public ChannelInfoBody(     String owner,
             String is_private,
             String description,
             String welcome_message,
             String messages,
             String members){
        this.owner = owner;
        this.is_private = is_private;
        this.description = description;
        this.welcome_message = welcome_message;
        this.messages = messages;
        this.members = members;
    }


    public String getOwner() {
        return owner;
    }

    public String getIs_private() {
        return is_private;
    }

    public String getDescription() {
        return description;
    }

    public String getWelcome_message() {
        return welcome_message;
    }

    public String getMessages() {
        return messages;
    }

    public String getMembers() {
        return members;
    }
}
