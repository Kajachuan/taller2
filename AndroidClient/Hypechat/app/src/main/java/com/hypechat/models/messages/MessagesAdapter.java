package com.hypechat.models.messages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hypechat.R;
import com.hypechat.cookies.ApplicationContextProvider;
import com.hypechat.prefs.SessionPrefs;

import java.util.ArrayList;
import java.util.List;

import static com.hypechat.cookies.ApplicationContextProvider.getContext;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private List<Message> mMessagesList;
    private Context mContext;

    public MessagesAdapter(Context context, List<Message> messageList) {
        mContext = context;
        this.mMessagesList = messageList;
    }

    public void add(Message message){
        this.mMessagesList.add(this.getItemCount(),message);
        notifyDataSetChanged();
    }

    public void addAtFirst(Message message){
        this.mMessagesList.add(0,message);
        notifyDataSetChanged();
    }

    public Message getLastMessage(){
        if(this.mMessagesList.size() > 0){
            return this.mMessagesList.get(this.getItemCount()-1);
        } else {
            return null;
        }
    }

    // Inflates the appropriate layout according to the ViewType.
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.their_message, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = (Message) this.mMessagesList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
                break;
        }
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = this.mMessagesList.get(position);

        if (message.getSender().equals(SessionPrefs.get(getContext()).getUsername())) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        if (this.mMessagesList != null)
            return this.mMessagesList.size();
        return 0;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    public Bitmap stringToBitmap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        public ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.tm_message_body);
            timeText = (TextView) itemView.findViewById(R.id.tm_time);
            nameText = (TextView) itemView.findViewById(R.id.tm_name);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getCreatedAt());
            nameText.setText(message.getSender());
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        public SentMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.m_message_body);
            timeText = (TextView) itemView.findViewById(R.id.m_time);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getCreatedAt());
        }
    }
}