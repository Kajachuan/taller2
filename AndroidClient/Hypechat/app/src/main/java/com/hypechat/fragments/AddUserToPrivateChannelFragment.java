package com.hypechat.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.channels.AddUserToChannelBody;
import com.hypechat.models.messages.Message;
import com.hypechat.models.messages.MessageBodyList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddUserToPrivateChannelFragment extends Fragment {

    private HypechatRequest mHypechatRequest;
    private EditText mUserName;
    private Button mAddUser;
    private ProgressBar pbarAddToChannel;
    private TextInputLayout userTi;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            getActivity().setTitle(getString(R.string.add_user) + " " + getArguments().getString("channel",null));
        }

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
        AddUserToPrivateChannelFragment addUserToPrivateFragment = new AddUserToPrivateChannelFragment();
        Bundle args = new Bundle();
        args.putString("organization", organization);
        args.putString("channel", channel);
        addUserToPrivateFragment.setArguments(args);
        return addUserToPrivateFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        userTi =  getView().findViewById(R.id.add_private_ti_layout);
        mUserName = getView().findViewById(R.id.add_private_et);
        pbarAddToChannel = getView().findViewById(R.id.add_private_progress);
        mAddUser = getView().findViewById(R.id.add_private_button);
        mAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAnUserToPrivateChannel();
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_user_to_private_channel, container, false);
    }

    private void addAnUserToPrivateChannel() {
        String username = mUserName.getText().toString();
        if(getArguments() != null && !username.isEmpty()){
            pbarAddToChannel.setVisibility(View.VISIBLE);
            userTi.setVisibility(View.GONE);
            mAddUser.setVisibility(View.GONE);
            String organization = getArguments().getString("organization");
            String channel = getArguments().getString("channel");
            AddUserToChannelBody addBody = new AddUserToChannelBody(username);
            Call<Void> addUserCall = mHypechatRequest.addUserToPrivateChannel(organization,channel,addBody);
            addUserCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    processResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void processResponse(Response<Void> response) {
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
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        } else {
            pbarAddToChannel.setVisibility(View.GONE);
            userTi.setVisibility(View.VISIBLE);
            mAddUser.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Se agregó correctamente al usuario al canal", Toast.LENGTH_LONG).show();
        }
    }
}
