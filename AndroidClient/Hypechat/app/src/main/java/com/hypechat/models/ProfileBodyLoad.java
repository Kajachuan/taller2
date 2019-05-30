package com.hypechat.models;

public class ProfileBodyLoad {

    private String first_name;
    private String last_name;
    private String image;

    public ProfileBodyLoad(String name, String lastName, String image) {
        this.first_name = name;
        this.last_name = lastName;
        this.image = image;
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

    public String getImage() {
        return this.image;
    }

}
