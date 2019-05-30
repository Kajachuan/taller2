package com.hypechat.models;

public class OrganizationCreateBody {

    private String name;

    public OrganizationCreateBody(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
