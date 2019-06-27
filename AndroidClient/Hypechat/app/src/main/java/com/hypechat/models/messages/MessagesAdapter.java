package com.hypechat.models.messages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.http.DELETE;

import static android.util.Base64.DEFAULT;
import static com.hypechat.cookies.ApplicationContextProvider.getContext;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_IMAGE_MESSAGE_SENT = 3;
    private static final int VIEW_TYPE_IMAGE_MESSAGE_RECEIVED = 4;
    private static final int VIEW_TYPE_SNIPPET_MESSAGE_SENT = 5;
    private static final int VIEW_TYPE_SNIPPET_MESSAGE_RECEIVED = 6;
    private static final int VIEW_TYPE_FILE_MESSAGE_SENT = 7;
    private static final int VIEW_TYPE_FILE_MESSAGE_RECEIVED = 8;

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
        } else if (viewType == VIEW_TYPE_IMAGE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message_image, parent, false);
            return new SentImageMessageHolder(view);
        } else if (viewType == VIEW_TYPE_IMAGE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.their_message_image, parent, false);
            return new ReceivedImageMessageHolder(view);
        } else if (viewType == VIEW_TYPE_FILE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message_file, parent, false);
            return new SentFileMessageHolder(view);
        } else if (viewType == VIEW_TYPE_FILE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.their_message_file, parent, false);
            return new ReceivedFileMessageHolder(view);
        } else if (viewType == VIEW_TYPE_SNIPPET_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_message, parent, false);
            return new SentSnippetMessageHolder(view);
        } else if (viewType == VIEW_TYPE_SNIPPET_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.their_message, parent, false);
            return new ReceivedSnippetMessageHolder(view);
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
            case VIEW_TYPE_IMAGE_MESSAGE_SENT:
                ((SentImageMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_IMAGE_MESSAGE_RECEIVED:
                ((ReceivedImageMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_FILE_MESSAGE_SENT:
                ((SentFileMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_FILE_MESSAGE_RECEIVED:
                ((ReceivedFileMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_SNIPPET_MESSAGE_SENT:
                ((SentSnippetMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_SNIPPET_MESSAGE_RECEIVED:
                ((ReceivedSnippetMessageHolder) holder).bind(message);
                break;
        }
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        Message message = this.mMessagesList.get(position);
        boolean isTheUserLogged = message.getSender().equals(SessionPrefs.get(getContext()).getUsername());
        boolean isTextMessage = message.getType().equals("text");
        boolean isImageMessage = message.getType().equals("img");
        boolean isFileMessage = message.getType().equals("file");
        boolean isSnippetMessage = message.getType().equals("snippet");

        if (isTheUserLogged && isTextMessage) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else  if (!(isTheUserLogged) && isTextMessage){
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        } else if (isTheUserLogged && isImageMessage){
            return VIEW_TYPE_IMAGE_MESSAGE_SENT;
        } else if (!(isTheUserLogged) && isImageMessage){
            return VIEW_TYPE_IMAGE_MESSAGE_RECEIVED;
        } else if (isTheUserLogged && isFileMessage){
            return VIEW_TYPE_FILE_MESSAGE_SENT;
        } else if (!isTheUserLogged && isFileMessage){
            return VIEW_TYPE_FILE_MESSAGE_RECEIVED;
        } else if (isTheUserLogged && isSnippetMessage){
            return VIEW_TYPE_SNIPPET_MESSAGE_SENT;
        } else if (!isTheUserLogged && isSnippetMessage){
            return VIEW_TYPE_SNIPPET_MESSAGE_RECEIVED;
        }

        return 1;
    }

    @Override
    public int getItemCount() {
        if (this.mMessagesList != null)
            return this.mMessagesList.size();
        return 0;
    }

    //////////////////////////////////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////////////////////////////////

    public Bitmap stringToBitmap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString, DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }


    private class ReceivedImageMessageHolder extends RecyclerView.ViewHolder {
        TextView timeText, nameText;
        ImageView messageText;

        public ReceivedImageMessageHolder(View itemView) {
            super(itemView);
            messageText = (ImageView) itemView.findViewById(R.id.tm_message_body);
            timeText = (TextView) itemView.findViewById(R.id.tm_time);
            nameText = (TextView) itemView.findViewById(R.id.tm_name);
        }

        void bind(Message message) {
            String imageString = message.getMessage();
            if(imageString != null){
                Bitmap imageBitmap = stringToBitmap(imageString);
                messageText.setImageBitmap(imageBitmap);
            }

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getCreatedAt());
            nameText.setText(message.getSender());
        }
    }

    private class SentImageMessageHolder extends RecyclerView.ViewHolder {
        TextView timeText;
        ImageView messageText;

        public SentImageMessageHolder(View itemView) {
            super(itemView);
            messageText = (ImageView) itemView.findViewById(R.id.m_message_body);
            timeText = (TextView) itemView.findViewById(R.id.m_time);
        }

        void bind(Message message) {
            String imageString = message.getMessage();
            if(imageString != null){
                Bitmap imageBitmap = stringToBitmap(imageString);
                messageText.setImageBitmap(imageBitmap);
            }

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getCreatedAt());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    private class SentFileMessageHolder extends RecyclerView.ViewHolder {
        TextView timeText, filename;
        ImageView messageText;

        public SentFileMessageHolder(View itemView) {
            super(itemView);
            messageText = (ImageView) itemView.findViewById(R.id.m_message_body);
            timeText = (TextView) itemView.findViewById(R.id.m_time);
            filename = (TextView) itemView.findViewById(R.id.m_filename);
        }

        void bind(Message message) {
            String nameOfFile = before(message.getMessage(),"fileencoded");
            filename.setText(nameOfFile);
            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getCreatedAt());
        }
    }

    static String before(String value, String a) {
        // Return substring containing all characters before a string.
        int posA = value.indexOf(a);
        if (posA == -1) {
            return "";
        }
        return value.substring(0, posA);
    }

    static String after(String value, String a) {
        // Returns a substring containing all characters after a string.
        int posA = value.lastIndexOf(a);
        if (posA == -1) {
            return "";
        }
        int adjustedPosA = posA + a.length();
        if (adjustedPosA >= value.length()) {
            return "";
        }
        return value.substring(adjustedPosA);
    }


    private class ReceivedFileMessageHolder extends RecyclerView.ViewHolder {
        TextView timeText, fileName, nameText;
        ImageView messageText;

        public ReceivedFileMessageHolder(View itemView) {
            super(itemView);
            messageText = (ImageView) itemView.findViewById(R.id.tm_message_body);
            fileName = (TextView)  itemView.findViewById(R.id.tm_filename);
            nameText = (TextView)  itemView.findViewById(R.id.tm_name);
            timeText = (TextView) itemView.findViewById(R.id.tm_time);
        }

        void bind(Message message) {
            String nameOfFile = before(message.getMessage(),"fileencoded");
            fileName.setText(nameOfFile);

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getCreatedAt());
            nameText.setText(message.getSender());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    private class ReceivedSnippetMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText, nameText;

        public ReceivedSnippetMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.tm_message_body);
            timeText = (TextView) itemView.findViewById(R.id.tm_time);
            nameText = (TextView) itemView.findViewById(R.id.tm_name);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            messageText.setTextColor(Color.GRAY);
            messageText.setTypeface(Typeface.MONOSPACE);
            messageText.setTextScaleX(1.2f);

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getCreatedAt());
            nameText.setText(message.getSender());
        }
    }

    private class SentSnippetMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, timeText;

        public SentSnippetMessageHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.m_message_body);
            timeText = (TextView) itemView.findViewById(R.id.m_time);
        }

        void bind(Message message) {
            messageText.setText(message.getMessage());
            messageText.setTextColor(Color.WHITE);
            messageText.setTypeface(Typeface.MONOSPACE);
            messageText.setTextScaleX(1.2f);

            // Format the stored timestamp into a readable String using method.
            timeText.setText(message.getCreatedAt());
        }
    }


}