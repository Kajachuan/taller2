package com.hypechat.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.messages.Message;
import com.hypechat.models.messages.MessageBodyList;
import com.hypechat.models.messages.MessageBodyPost;
import com.hypechat.models.messages.MessagesAdapter;
import com.hypechat.prefs.SessionPrefs;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;
import static java.lang.Math.round;

public class ChatChannelFragment extends Fragment {

    private RecyclerView mMessageRecycler;
    private MessagesAdapter mMessageAdapter;
    private HypechatRequest mHypechatRequest;
    private EditText mEtMessage;
    private SwipeRefreshLayout swipeContainer;
    private Handler handler = new Handler();

    public static ChatChannelFragment newInstance(String organization, String channel) {
        ChatChannelFragment chatFragment = new ChatChannelFragment();
        Bundle args = new Bundle();
        args.putString("channel", channel);
        args.putString("organization", organization);
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(getArguments().getString("channel",null));

        OkHttpClient.Builder okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60 * 5, TimeUnit.SECONDS)
                .readTimeout(60 * 5, TimeUnit.SECONDS)
                .writeTimeout(60 * 5, TimeUnit.SECONDS);
        okHttpClient.interceptors().add(new AddCookiesInterceptor());
        okHttpClient.interceptors().add(new ReceivedCookiesInterceptor());

        // Crear conexión al servicio REST
        Retrofit mMainRestAdapter = new Retrofit.Builder()
                .baseUrl(HypechatRequest.BASE_URL)
                .client(okHttpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Crear conexión a la API
        mHypechatRequest = mMainRestAdapter.create(HypechatRequest.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mEtMessage = (EditText) getView().findViewById(R.id.edittext_chatbox);

        swipeContainer = (SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMessages(mMessageAdapter.getItemCount() - 1,mMessageAdapter.getItemCount() + 5,true);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.white);
        swipeContainer.setProgressBackgroundColorSchemeResource(R.color.colorPrimary);

        final LinearLayout attachments = getView().findViewById(R.id.attachments_layout);

        ImageButton mSendButton = (ImageButton) getView().findViewById(R.id.button_chatbox_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = "text";
                sendMessage(mEtMessage.getText().toString(),type);
            }
        });

        ImageButton mSendImageButton = (ImageButton) getView().findViewById(R.id.imageButton_image);
        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        mMessageRecycler = (RecyclerView) getView().findViewById(R.id.reyclerview_message_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mMessageRecycler.setLayoutManager(linearLayoutManager);
        mMessageRecycler.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    mMessageRecycler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mMessageRecycler.getAdapter().getItemCount() > 0){
                                mMessageRecycler.smoothScrollToPosition(
                                        mMessageRecycler.getAdapter().getItemCount() - 1);
                            }
                        }
                    }, 50);
                }
            }
        });

        ImageButton mAttachButton = (ImageButton) getView().findViewById(R.id.button_attach);
        mAttachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visible = (attachments.getVisibility() == View.VISIBLE);
                attachments.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
        });

        List<Message> messagesList = new ArrayList<>();
        mMessageAdapter = new MessagesAdapter(getActivity(), messagesList);

        mMessageRecycler.setAdapter(mMessageAdapter);

        //noinspection ConstantConditions
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float height = size.y;
        height = height - 300; //altura del telefono - (send message box + menu + barrita)
        if(height > 0){
            int messagesQuantity = round(height / 100);  // /dp general de un mensaje
            getMessages(1,7,false);
        }
        handler.postDelayed(runnable, 5000);
        super.onActivityCreated(savedInstanceState);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getMessages(1,7,false);
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri targetUri = data.getData();
                Bitmap bitmap = null;
                try {
                    if (targetUri != null) {
                        //noinspection ConstantConditions
                        bitmap = BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(targetUri));
                        Bitmap resizedBitmap = null;
                        resizedBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, false);
                        Drawable icon = new BitmapDrawable(getResources(),bitmap);
                        String image = bitmapToString(resizedBitmap);
                        String type = "img";
                        sendMessage(image,type);
                        //mProfileImage.setImageBitmap(bitmap);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String bitmapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    public void onDestroy () {
        handler.removeCallbacks(runnable);
        super.onDestroy ();
    }

    private void processResponseMessages(Response<MessageBodyList> response, boolean scrollData) {
        // Procesar errores
        if (!response.isSuccessful()) {
            String error;
            if (response.errorBody()
                    .contentType()
                    .subtype()
                    .equals("json")) {
                APIError apiError = ErrorUtils.parseError(response);
                assert apiError != null;
                error = apiError.message();
            } else {
                error = response.message();
            }
            if(scrollData){
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }
            showChatError(error);
        } else {
            if (response.body() != null) {
                List<List<String>> getList = response.body().getMessageList();
                if(!scrollData){
                    for(int i = 0; i < getList.size(); i++){
                        Message lastMessage = mMessageAdapter.getLastMessage();
                        Message getMessage = new Message(getList.get(i).get(2),getList.get(i).get(1),getList.get(i).get(0),getList.get(i).get(3));
                        if(lastMessage != null){
                            if(getMessage.getDate().compareTo(lastMessage.getDate()) > 0){
                                mMessageAdapter.add(getMessage);
                            }
                        } else {
                            mMessageAdapter.add(getMessage);
                        }
                    }
                    mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);
                } else {
                    for(int i = 0; i < getList.size(); i++){
                        Message getMessage = new Message(getList.get(i).get(2),getList.get(i).get(1),getList.get(i).get(0),getList.get(i).get(3));
                        mMessageAdapter.addAtFirst(getMessage);
                    }
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                }

            }
        }
    }

    private void processResponseSendMessage(Response<Void> response, Message messageBody) {
        // Procesar errores
        if (!response.isSuccessful()) {
            String error;
            if (response.errorBody()
                    .contentType()
                    .subtype()
                    .equals("json")) {
                APIError apiError = ErrorUtils.parseError(response);
                assert apiError != null;
                error = apiError.message();
            } else {
                error = response.message();
            }
            showChatError(error);
        } else {
            //noinspection ConstantConditions
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getActivity().getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            //noinspection ConstantConditions
            inputMethodManager.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(), 0);
            mEtMessage.getText().clear();
            mMessageAdapter.add(messageBody);
            mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);
        }
    }

    private void showChatError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    public void sendMessage(String message, String type){
            if(!message.isEmpty()){
                if(getArguments() != null) {
                    String organization = getArguments().getString("organization");
                    String channel = getArguments().getString("channel");
                    String username = SessionPrefs.get(getContext()).getUsername();
                    MessageBodyPost messageBody = new MessageBodyPost(message,username,type);
                    final Message messageForList = new Message(message,username,getCurrentTime(),type);
                    Call<Void> messagesSendCall = mHypechatRequest.sendMessage(organization,channel,messageBody);
                    messagesSendCall.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            processResponseSendMessage(response,messageForList);
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            showChatError(t.getMessage());
                        }
                    });
                }
            }
    }

    private String getCurrentTime(){
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
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
        String timestamp = hourString+":"+ minuteString +":"+secondString;
        return timestamp;
    }

    private void getMessages(int init, int end, final boolean scrollData){
        if(getArguments() != null){
            String organization = getArguments().getString("organization");
            String channel = getArguments().getString("channel");
            Call<MessageBodyList> messagesGetCall = mHypechatRequest.getMessages(organization,channel,init,end);
            messagesGetCall.enqueue(new Callback<MessageBodyList>() {
                @Override
                public void onResponse(@NonNull Call<MessageBodyList> call, @NonNull Response<MessageBodyList> response) {
                    processResponseMessages(response,scrollData);
                }

                @Override
                public void onFailure(@NonNull Call<MessageBodyList> call, @NonNull Throwable t) {
                    if(scrollData){
                        // Now we call setRefreshing(false) to signal refresh has finished
                        swipeContainer.setRefreshing(false);
                    }
                    showChatError(t.getMessage());
                }
            });
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_channel, container, false);
    }
}
