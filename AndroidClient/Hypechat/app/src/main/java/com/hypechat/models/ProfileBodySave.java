package com.hypechat.models;

public class ProfileBodySave {

    private String username;
    private String first_name;
    private String last_name;

    public ProfileBodySave(String username, String name, String lastName) {
        this.username = username;
        this.first_name = name;
        this.last_name = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.first_name = name;
    }

    public void setLastName(String lastName) {
        this.last_name = lastName;
    }

    public String getLastName() {
        return this.last_name;
    }

    public String getName() {
        return this.first_name;
    }

}
