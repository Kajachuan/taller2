package com.hypechat.models;

public class ChannelCreateBody {

    private String name;
    private String _private;

    public ChannelCreateBody(String name, String _private) {
        this.name = name;
        this._private = _private;
    }

}
