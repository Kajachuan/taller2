package com.hypechat.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hypechat.R;

public class ChatChannelFragment extends Fragment {

    public static ChatChannelFragment newInstance(String channel) {
        ChatChannelFragment chatFragment = new ChatChannelFragment();
        Bundle args = new Bundle();
        args.putString("channel", channel);
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getArguments().getString("channel",null));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_channel, container, false);
    }
}
