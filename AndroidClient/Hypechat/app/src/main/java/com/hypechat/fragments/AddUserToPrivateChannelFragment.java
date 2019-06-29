package com.hypechat.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hypechat.API.HypechatRequest;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddUserToPrivateChannelFragment extends Fragment {

    private HypechatRequest mHypechatRequest;
    private EditText mChannelName;
    private Button mCreateNewChannel;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.new_channel);

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

    public static AddUserToPrivateChannelFragment newInstance(String organization,String channel) {
        AddUserToPrivateChannelFragment chatFragment = new AddUserToPrivateChannelFragment();
        Bundle args = new Bundle();
        args.putString("organization", organization);
        args.putString("channel", channel);
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mChannelName = getView().findViewById(R.id.new_channel_et);
        mCreateNewChannel = getView().findViewById(R.id.new_channel_button);
        mCreateNewChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAnUserToPrivateChannel();
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    private void addAnUserToPrivateChannel() {

    }
}
