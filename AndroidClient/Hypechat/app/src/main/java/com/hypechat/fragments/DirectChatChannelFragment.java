package com.hypechat.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.MainActivity;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.messages.Message;
import com.hypechat.models.messages.MessageBodyList;
import com.hypechat.models.messages.MessageBodyPost;
import com.hypechat.models.messages.MessageDirectBodyPost;
import com.hypechat.models.messages.MessagesAdapter;
import com.hypechat.prefs.SessionPrefs;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class DirectChatChannelFragment extends Fragment {

    private RecyclerView mMessageRecycler;
    private MessagesAdapter mMessageAdapter;
    private HypechatRequest mHypechatRequest;
    private EditText mEtMessage;
    private SwipeRefreshLayout swipeContainer;
    private Handler handlerChannels = new Handler();
    private LinearLayout attachments;
    private ImageButton mSendButton;
    private ProgressBar pbSendMessage;
    private ProgressBar mProgressBarLoadMessage;
    BroadcastReceiver mMessageReceiver;

    public static DirectChatChannelFragment newInstance(String organization, String channel) {
        DirectChatChannelFragment chatFragment = new DirectChatChannelFragment();
        Bundle args = new Bundle();
        args.putString("channel", channel);
        args.putString("organization", organization);
        chatFragment.setArguments(args);
        return chatFragment;
    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int unicodeUser = 0x1F47E;
        getActivity().setTitle(getEmojiByUnicode(unicodeUser) + " " + getArguments().getString("channel",null));

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

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //noinspection ConstantConditions
                Message new_message = new Message(intent.getExtras().getString("message"),
                        intent.getExtras().getString("sender"),
                        intent.getExtras().getString("timestamp"),
                        intent.getExtras().getString("type"));

                if (new_message.getType().equals("img")){
                    getDirectMessages(1,5,false);
                } else {
                    mMessageAdapter.add(new_message);
                    mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);
                }

            }
        };
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //noinspection ConstantConditions
        ((MainActivity) getActivity()).addDirectChatFragmentAdditionalMenuOptions();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mEtMessage = (EditText) getView().findViewById(R.id.edittext_chatbox);
        mProgressBarLoadMessage = (ProgressBar) getView().findViewById(R.id.progressBar_load_messages);

        swipeContainer = (SwipeRefreshLayout) getView().findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDirectMessages(mMessageAdapter.getItemCount() + 1,mMessageAdapter.getItemCount() + 21 ,true);
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.white);
        swipeContainer.setProgressBackgroundColorSchemeResource(R.color.colorPrimary);

        attachments = getView().findViewById(R.id.attachments_layout);

        mSendButton = (ImageButton) getView().findViewById(R.id.button_chatbox_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = "text";
                sendDirectMessage(mEtMessage.getText().toString(),type);
            }
        });

        pbSendMessage = (ProgressBar) getView().findViewById(R.id.progressBar_send_message);

        ImageButton mSendImageButton = (ImageButton) getView().findViewById(R.id.imageButton_image);
        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        ImageButton mSendSnippetButton = (ImageButton) getView().findViewById(R.id.imageButton_snippet);
        mSendSnippetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupSnippet();
            }
        });

        ImageButton mSendFileButton = (ImageButton) getView().findViewById(R.id.imageButton_file);
        mSendFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = null;
                new ChooserDialog().with(getActivity())
                        .withStartFile(path)
                        .withChosenListener(new ChooserDialog.Result() {
                            @Override
                            public void onChoosePath(String path, File pathFile) {
                                String type = "file";
                                String encodedFile = encodeFileToBase64Binary(pathFile);
                                String fileName = path.substring(path.lastIndexOf("/") + 1);
                                String fileNameEncoded = fileName.concat("fileencoded");
                                String finalFilename = fileNameEncoded.concat(encodedFile);
                                sendDirectMessage(finalFilename,type);
                            }
                        })
                        .build()
                        .show();
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
        getDirectMessages(1,10,false);
        handlerChannels.postDelayed(runnableChannels,60000);
        super.onActivityCreated(savedInstanceState);
    }

    private void popupSnippet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Code snippet");

        final EditText input = new EditText(getContext());

        input.setTextScaleX(1.2f);
        input.setTextColor(Color.GRAY);
        input.setHint("Escriba aquí su código");
        input.setTypeface(Typeface.MONOSPACE);
        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setSingleLine(false);
        input.setLines(5);
        input.setGravity(Gravity.START | Gravity.TOP);
        builder.setView(input);

        builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String type = "snippet";
                sendDirectMessage(input.getText().toString(),type);
            }
        });

        builder.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        boolean visible = (attachments.getVisibility() == View.VISIBLE);
                        attachments.setVisibility(visible ? View.GONE : View.VISIBLE);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private Runnable runnableChannels = new Runnable() {
        @Override
        public void run() {
            if (getArguments() != null) {
                //noinspection ConstantConditions
                ((MainActivity) getActivity()).updateChannels(getArguments().getString("organization"));
                ((MainActivity) getActivity()).updateDirectChannels(getArguments().getString("organization"));
            }
            handlerChannels.postDelayed(this, 60000);
        }
    };

    private String encodeFileToBase64Binary(File file){
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int)file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = Base64.encodeToString(bytes,Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return encodedfile;
    }

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
                                resizedBitmap = Bitmap.createScaledBitmap(bitmap,500,500,false);
                                Drawable icon = new BitmapDrawable(getResources(), bitmap);
                                String image = bitmapToString(resizedBitmap);
                                String type = "img";
                                sendDirectMessage(image, type);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
            }
    }

    private Bitmap scaleBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int maxWidth = 512;
        int maxHeight = 512;

        if (width > height) {
            // landscape
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / maxHeight;
            height = maxHeight;
            width = (int)(width / ratio);
        } else {
            // square
            height = maxHeight;
            width = maxWidth;
        }

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

    public String bitmapToString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onPause () {
        handlerChannels.removeCallbacks(runnableChannels);
        if(getContext() != null){
            LocalBroadcastManager.getInstance(getActivity())
                    .unregisterReceiver(mMessageReceiver);
        }
        super.onPause ();
    }

    @Override
    public void onResume () {
        handlerChannels.postDelayed(runnableChannels,60000);
        if(getContext() != null){
            LocalBroadcastManager.getInstance(getActivity())
                    .registerReceiver(mMessageReceiver, new IntentFilter("Data"));
        }
        super.onResume ();
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
            mProgressBarLoadMessage.setVisibility(View.GONE);
            showChatError(error);
        } else {
            mProgressBarLoadMessage.setVisibility(View.GONE);
            if (response.body() != null) {
                List<List<String>> getList = response.body().getMessageList();
                if(!scrollData){
                    int sizeOfMessages = mMessageAdapter.getItemCount();
                    for(int i = 0; i < getList.size(); i++){
                        Message lastMessage = mMessageAdapter.getLastMessage();
                        Message getMessage = new Message(getList.get(i).get(2),getList.get(i).get(1),getList.get(i).get(0),getList.get(i).get(3));
                        if(lastMessage != null){
                            if(lastMessage.getDate() != null){
                                if(getMessage.getDate().compareTo(lastMessage.getDate()) > 0){
                                    mMessageAdapter.add(getMessage);
                                }
                            } else {
                                if(getMessage.getDate().compareTo(lastMessage.getDateFromAndroid()) > 0){
                                    mMessageAdapter.add(getMessage);
                                }
                            }

                        } else {
                            mMessageAdapter.add(getMessage);
                        }
                    }
                    if(sizeOfMessages < mMessageAdapter.getItemCount()){
                        mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);
                    }
                } else {
                    int sizeOfMessages = mMessageAdapter.getItemCount();
                    for(int i = getList.size() - 1; i > 0; i--){
                        Message getMessage = new Message(getList.get(i).get(2),getList.get(i).get(1),getList.get(i).get(0),getList.get(i).get(3));
                        mMessageAdapter.addAtFirst(getMessage);
                    }

                    if(mMessageAdapter.getItemCount() > sizeOfMessages){
                        mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount()-sizeOfMessages);
                    }

                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);

                }

            }
        }
    }

    private void processResponseSendMessage(Response<Void> response, String type) {
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
            pbSendMessage.setVisibility(View.GONE);
            mSendButton.setVisibility(View.VISIBLE);
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
            if(!type.equals("text")){
                boolean visible = (attachments.getVisibility() == View.VISIBLE);
                attachments.setVisibility(visible ? View.GONE : View.VISIBLE);
            }
            pbSendMessage.setVisibility(View.GONE);
            mSendButton.setVisibility(View.VISIBLE);
        }
    }

    private void showChatError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    public void sendDirectMessage(String message, final String type){
            if(!message.isEmpty()){
                pbSendMessage.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.GONE);
                if(getArguments() != null) {
                    String organization = getArguments().getString("organization");
                    String channel = getArguments().getString("channel");
                    String username = SessionPrefs.get(getContext()).getUsername();
                    MessageDirectBodyPost messageBody = new MessageDirectBodyPost(message,username,channel,type);
                    Call<Void> messagesSendCall = mHypechatRequest.sendDirectMessage(organization,messageBody);
                    messagesSendCall.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            processResponseSendMessage(response,type);
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            showChatError(t.getMessage());
                            pbSendMessage.setVisibility(View.GONE);
                            mSendButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
    }

    private void getDirectMessages(int init, int end, final boolean scrollData){
        if(getArguments() != null){
            String organization = getArguments().getString("organization");
            String channel = getArguments().getString("channel");
            String currentUsername = SessionPrefs.get(getContext()).getUsername();
            Call<MessageBodyList> messagesGetCall = mHypechatRequest.getDirectChannelMessages(organization,init,end,currentUsername,channel);
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
                    mProgressBarLoadMessage.setVisibility(View.GONE);
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
