package com.hypechat.models.messages;

import java.util.List;

public class MessageBody {

    private List<Message> messageList;

    public MessageBody(List<Message> messages) {
        this.messageList = messages;
    }

    public List<Message> getMessageList() {
        return this.messageList;
    }
}
