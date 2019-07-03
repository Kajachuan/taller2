package com.hypechat.models.channels;

import com.hypechat.fragments.DirectChatChannelFragment;

import java.util.List;

public class DirectChannelsGet {

    private List<String> direct_channels;

    public DirectChannelsGet(List<String> direct_channels){
        this.direct_channels = direct_channels;
    }

    public List<String> getDirect_channels() {
        return direct_channels;
    }
}
