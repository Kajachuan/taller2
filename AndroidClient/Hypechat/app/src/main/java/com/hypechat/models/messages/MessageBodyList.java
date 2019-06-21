package com.hypechat.models.messages;

import java.util.List;

public class MessageBodyList {

    private List<List<String>> messages;

    public MessageBodyList(List<List<String>> messages) {
        this.messages = messages;
    }

    public List<List<String>> getMessageList() {
        return this.messages;
    }
}
