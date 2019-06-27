package com.hypechat.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
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
import android.widget.Switch;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.MainActivity;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.channels.ChannelCreateBody;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewChannelFragment extends Fragment {

    private HypechatRequest mHypechatRequest;
    private EditText mChannelName;
    private Button mCreateNewChannel;
    private Switch mSwitchPrivate;

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

    public static NewChannelFragment newInstance(String organization) {
        NewChannelFragment chatFragment = new NewChannelFragment();
        Bundle args = new Bundle();
        args.putString("organization", organization);
        chatFragment.setArguments(args);
        return chatFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mChannelName = getView().findViewById(R.id.new_channel_et);
        mSwitchPrivate = getView().findViewById(R.id.switch_new_channel);
        mCreateNewChannel = getView().findViewById(R.id.new_channel_button);
        mCreateNewChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewChannel();
            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_channel, container, false);
    }

    private void createNewChannel(){
        // Show a progress spinner, and kick off a background task to
        // perform the attempt.
        showProgress(true);
        final String channelName = mChannelName.getText().toString();
        boolean privado = mSwitchPrivate.isChecked();
        String canalPrivado = "False";
        if (privado){
            canalPrivado = "True";
        }
        String organization_name = null;
        if (getArguments() != null) {
            organization_name = getArguments().getString("organization",null);
        }
        if(!channelName.isEmpty()){
            ChannelCreateBody channelCreateBody = new ChannelCreateBody(channelName, canalPrivado);
            Call<Void> createOrganizationsCall = mHypechatRequest.createChannel(organization_name, channelCreateBody);
            final String finalOrganizationName = organization_name;
            createOrganizationsCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    processResponseNewChannelCreation(response, finalOrganizationName, channelName);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showNewChannelError(t.getMessage());
                }
            });
        } else {

        }
    }

    private void processResponseNewChannelCreation(Response<Void> response, String organizationName, String channelName) {
        showProgress(false);
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
            showNewChannelError(error);
        } else {
            //noinspection ConstantConditions
            ((MainActivity) getActivity()).createNewChannelFragment(organizationName,channelName);
        }
    }



    private void showNewChannelError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @SuppressWarnings("ConstantConditions")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        final TextInputLayout ti_new_channel = getView().findViewById(R.id.new_channel_ti_layout);
        final ProgressBar new_channel_pb = getView().findViewById(R.id.new_channel_progress);

        mSwitchPrivate.setVisibility(show ? View.VISIBLE : View.GONE);
        mSwitchPrivate.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSwitchPrivate.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mCreateNewChannel.setVisibility(show ? View.VISIBLE : View.GONE);
        mCreateNewChannel.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCreateNewChannel.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        ti_new_channel.setVisibility(show ? View.VISIBLE : View.GONE);
        ti_new_channel.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ti_new_channel.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        new_channel_pb.setVisibility(show ? View.GONE : View.VISIBLE);
        new_channel_pb.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                new_channel_pb.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
