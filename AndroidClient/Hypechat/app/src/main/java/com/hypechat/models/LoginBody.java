package com.hypechat.models;

import com.google.gson.annotations.SerializedName;

public class LoginBody {

    @SerializedName("id")
    private String userId;
    private String name;
    private String password;

    public LoginBody(String userId, String password) {
        this.userId = userId;
        this.name = name;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
