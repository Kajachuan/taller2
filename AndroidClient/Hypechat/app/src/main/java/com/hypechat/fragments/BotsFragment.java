package com.hypechat.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.bots.BotsCreationPost;
import com.hypechat.models.bots.BotsDeletePost;
import com.hypechat.models.messages.MessageBodyPost;
import com.hypechat.prefs.SessionPrefs;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BotsFragment extends Fragment {

    private HypechatRequest mHypechatRequest;
    private Button mCreateButton;
    private Button mDeleteButton;
    private ImageButton mCloseButton;
    private Button mCreateABotButton;
    private Button mDeleteABotButton;
    private LinearLayout mBotCreateDeleteLayout;
    private TextInputLayout mUrlTi;
    private TextView organization;
    private TextView channel;
    private AutoCompleteTextView mBotName;
    private AutoCompleteTextView mBotUrl;

    public static BotsFragment newInstance(String organization, String channel) {
        BotsFragment botFragment = new BotsFragment();
        Bundle args = new Bundle();
        args.putString("channel", channel);
        args.putString("organization", organization);
        botFragment.setArguments(args);
        return botFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.action_bots);

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

        mCloseButton = getView().findViewById(R.id.bot_close);
        mCreateABotButton = getView().findViewById(R.id.bot_create_only_button);
        mBotCreateDeleteLayout = getView().findViewById(R.id.bot_hidden);
        mDeleteABotButton = getView().findViewById(R.id.bot_delete_only_button);
        mDeleteButton = getView().findViewById(R.id.bot_delete_button);
        mUrlTi = getView().findViewById(R.id.bot_url_ti_layout);

        mBotName = getView().findViewById(R.id.bot_name);
        mBotUrl = getView().findViewById(R.id.bot_url);

        organization = getView().findViewById(R.id.textView_organization_bot_real);
        channel = getView().findViewById(R.id.textView_channel_bot_real);

        if(getArguments() != null){
            organization.setText(getArguments().getString("organization",null));
            channel.setText(getArguments().getString("channel",null));
        }

        mCreateButton = getView().findViewById(R.id.bot_create_button);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloseButton.setVisibility(View.VISIBLE);
                mBotCreateDeleteLayout.setVisibility(View.VISIBLE);
                mCreateABotButton.setVisibility(View.VISIBLE);
                mUrlTi.setVisibility(View.VISIBLE);
                mDeleteABotButton.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.GONE);
                mCreateButton.setVisibility(View.GONE);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloseButton.setVisibility(View.VISIBLE);
                mBotCreateDeleteLayout.setVisibility(View.VISIBLE);
                mDeleteABotButton.setVisibility(View.VISIBLE);
                mCreateABotButton.setVisibility(View.GONE);
                mUrlTi.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.GONE);
                mCreateButton.setVisibility(View.GONE);
            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloseButton.setVisibility(View.INVISIBLE);
                mBotCreateDeleteLayout.setVisibility(View.GONE);
                mDeleteButton.setVisibility(View.VISIBLE);
                mCreateButton.setVisibility(View.VISIBLE);
            }
        });

        mDeleteABotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteBot();
            }
        });

        mCreateABotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBot();
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    private void createBot() {
        String botName = mBotName.getText().toString();
        String botURL = mBotUrl.getText().toString();
        if(getArguments() != null && !botName.isEmpty() && !botURL.isEmpty()){
            String organization = getArguments().getString("organization");
            String channel = getArguments().getString("channel");
            BotsCreationPost botPost = new BotsCreationPost(botName,botURL);
            Call<Void> botCreationCall = mHypechatRequest.createBot(organization,channel,botPost);
            botCreationCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    processResponseCreation(response);
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    showBotError(t.getMessage());
                }
            });
        }

    }

    private void processResponseCreation(Response<Void> response) {
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
            showBotError(error);
        } else {
            showBotError("El bot se creo correctamente");
        }
    }

    private void deleteBot() {
        String botName = mBotName.getText().toString();
        if(getArguments() != null && !botName.isEmpty()){
            String organization = getArguments().getString("organization");
            String channel = getArguments().getString("channel");
            Call<Void> botDeleteCall = mHypechatRequest.deleteBot(organization,channel,botName);
            botDeleteCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    processResponseDeletion(response);
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    showBotError(t.getMessage());
                }
            });
        }

    }

    private void processResponseDeletion(Response<Void> response) {
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
            showBotError(error);
        } else {
            showBotError("El bot se eliminó correctamente");
        }
    }

    private void showBotError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bots, container, false);
    }
}
