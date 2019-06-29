package com.hypechat.models.channels;

import java.util.List;

public class ChannelListBody {

    private List<List<String>> channels;

    public ChannelListBody(List<List<String>> channels) {
        this.channels = channels;
    }

    public List<List<String>> getChannels() {
        return this.channels;
    }
}
