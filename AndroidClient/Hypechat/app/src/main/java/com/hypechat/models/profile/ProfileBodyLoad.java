package com.hypechat.models.profile;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileBodyLoad {

    private String first_name;
    private String last_name;
    private String image;
    private String email;
    private String messages;
    private String ban_date;
    private String ban_reason;
    private Map<String,Map<String,Map<String,String>>> organizations;

    public ProfileBodyLoad(String name,
                           String lastName,
                           String image,
                           String email,
                           Map<String,Map<String,Map<String,String>>> organizations,
                           String messages,
                           String ban_date,
                           String ban_reason) {
        this.first_name = name;
        this.last_name = lastName;
        this.image = image;
        this.email = email;
        this.organizations = organizations;
        this.messages = messages;
        this.ban_date = ban_date;
        this.ban_reason = ban_reason;
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

    public String getEmail() {
        return email;
    }

    public Map<String,Map<String,Map<String,String>>> getOrganizations() {
        return organizations;
    }

    public List<String> getOrganizationsList(){
        return (new ArrayList<>(organizations.keySet()));
    }

    public Map<String,Map<String,String>> getSecondLevel(String organizatonName){
        return organizations.get(organizatonName);
    }

    public String getMessages() {
        return messages;
    }

    public String getBan_date() {
        return ban_date;
    }

    public String getBan_reason() {
        return ban_reason;
    }
}
