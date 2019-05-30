package com.hypechat.models;

import java.util.List;

public class ChannelListBody {

    private List<String> channels;

    public ChannelListBody(List<String> channels) {
        this.channels = channels;
    }

    public List<String> getChannels() {
        return this.channels;
    }
}
