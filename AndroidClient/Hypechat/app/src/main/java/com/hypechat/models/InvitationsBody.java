package com.hypechat.models;

public class InvitationsBody {

    private String username;

    public InvitationsBody(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }
}
