package com.hypechat.models;

public class ProfileBodyLoad {

    private String first_name;
    private String last_name;

    public ProfileBodyLoad(String username, String name, String lastName) {
        this.first_name = name;
        this.last_name = lastName;
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
