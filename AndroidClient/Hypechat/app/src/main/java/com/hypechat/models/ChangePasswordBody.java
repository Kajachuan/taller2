package com.hypechat.models;

public class ChangePasswordBody {

    private String username;
    private String new_password;
    private String new_password_confirmation;

    public ChangePasswordBody(String username,String new_password, String new_password_confirmation) {
        this.username = username;
        this.new_password = new_password;
        this.new_password_confirmation = new_password_confirmation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }
}
