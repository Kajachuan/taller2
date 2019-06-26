package com.hypechat.models.messages;

import android.util.Log;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Message {

    private Date timestampDate;
    private String timestamp;
    private String sender;
    private String message;
    private String type;

    public Message(String message, String sender, String timestamp, String type){
        DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",Locale.ENGLISH);
        try {
            Calendar cal = Calendar.getInstance();
            this.timestampDate = formatter.parse(timestamp);
            cal.setTime(this.timestampDate);
            int hour = cal.getTime().getHours();
            int minute = cal.getTime().getMinutes();
            int second = cal.getTime().getSeconds();
            String hourString = String.valueOf(hour);
            String minuteString = String.valueOf(minute);
            String secondString = String.valueOf(second);
            if(minute < 10){
                minuteString = "0" + String.valueOf(minute);
            }
            if(hour < 10){
                hourString = "0" + String.valueOf(hourString);
            }
            if(second < 10){
                secondString = "0" + String.valueOf(secondString);
            }
            timestamp = hourString+":"+ minuteString +":"+secondString;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.timestamp = timestamp;
        this.sender = sender;
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public String getType() {
        return type;
    }

    public Date getDate() {
        return timestampDate;
    }
}
