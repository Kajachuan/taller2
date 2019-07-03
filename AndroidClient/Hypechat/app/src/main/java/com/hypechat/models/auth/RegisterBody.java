package com.hypechat.models.auth;

import com.google.gson.annotations.SerializedName;

public class RegisterBody {


    private String username;

    private String email;
    private String password;
    private String password_confirmation;
    private double lat;
    private double lon;

    public RegisterBody(String username, String email, String password, String password_confirmation,
                        double lat, double lon) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.password_confirmation = password_confirmation;
        this.lat = lat;
        this.lon = lon;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword_confirmation() {
        return password_confirmation;
    }

    public void setPassword_confirmation(String password_confirmation) {
        this.password_confirmation = password_confirmation;
    }
}
