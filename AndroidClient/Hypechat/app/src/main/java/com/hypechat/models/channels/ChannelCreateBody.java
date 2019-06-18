package com.hypechat.models;

public class ChannelCreateBody {

    private String name;
    private String privado;

    public ChannelCreateBody(String name, String privado) {
        this.name = name;
        this.privado = privado;
    }

}
