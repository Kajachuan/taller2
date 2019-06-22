package com.hypechat.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.channels.ChannelInfoBody;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AboutChannelFragment extends Fragment {

    private HypechatRequest mHypechatRequest;
    private LinearLayout mNoConnectionLayout;
    private LinearLayout mInfoChannelLayout;
    private ImageButton mReloadIB;
    private ProgressBar mProgressLoadInfoChannel;
    private TextView mChannelNameTv;
    private TextView mOwnerNameTv;
    private TextView mPrivacyTv;
    private TextView mDescriptionTv;
    private TextView mWelMsgTv;
    private TextView mMsgQuantTv;
    private TextView mMemQuantTv;

    public static AboutChannelFragment newInstance(String organization, String channel) {
        AboutChannelFragment aboutChannelFragment = new AboutChannelFragment();
        Bundle args = new Bundle();
        args.putString("organization", organization);
        args.putString("channel", channel);
        aboutChannelFragment.setArguments(args);
        return aboutChannelFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.action_about_channel);

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


    private boolean isOnline() {
        @SuppressWarnings("ConstantConditions") ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        mChannelNameTv = getView().findViewById(R.id.textView_channel_name);
        mOwnerNameTv = getView().findViewById(R.id.textView_owner_name);
        mPrivacyTv = getView().findViewById(R.id.textView_privacy);
        mDescriptionTv = getView().findViewById(R.id.textView_description);
        mWelMsgTv = getView().findViewById(R.id.textView_welcome_message);
        mMsgQuantTv = getView().findViewById(R.id.textView_msg_quantity);
        mMemQuantTv = getView().findViewById(R.id.textView_members_quantity);

        mProgressLoadInfoChannel = getView().findViewById(R.id.progressBar_about_channel);
        mInfoChannelLayout = getView().findViewById(R.id.channel_statistics);
        mNoConnectionLayout = getView().findViewById(R.id.about_channel_not_connection_layout);

        mReloadIB = getView().findViewById(R.id.imageButton_about_channel);
        mReloadIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoConnectionLayout.setVisibility(View.GONE);
                loadChannelInfo();
            }
        });

        showProgress(true);

        if(!isOnline()){
            showAbChannelError(getString(R.string.error_network));
            mNoConnectionLayout.setVisibility(View.VISIBLE);
            mInfoChannelLayout.setVisibility(View.GONE);
            showProgress(false);
        } else {
            loadChannelInfo();
        }
        super.onActivityCreated(savedInstanceState);
    }

    private void loadChannelInfo() {
        if (getArguments() != null) {
            Call<ChannelInfoBody> searchChannelInfoCall = mHypechatRequest.getChannelInfo(
                    getArguments().getString("organization"),
                    getArguments().getString("channel"));
            searchChannelInfoCall.enqueue(new Callback<ChannelInfoBody>() {
                @Override
                public void onResponse(@NonNull Call<ChannelInfoBody> call, @NonNull Response<ChannelInfoBody> response) {
                    processLoadChannelInfoResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<ChannelInfoBody> call, @NonNull Throwable t) {
                    showAbChannelError(t.getMessage());
                    showProgress(false);
                }
            });
        }
    }

    private void processLoadChannelInfoResponse(Response<ChannelInfoBody> response) {
        // Mostrar progreso
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
            showAbChannelError(error);
        } else {
            if (response.body() != null) {
                if (getArguments() != null) {
                    mChannelNameTv.setText(getArguments().getString("channel"));
                }
                mOwnerNameTv.setText(response.body().getOwner());
                if(response.body().getIs_private().equals("false")){
                    mPrivacyTv.setText(getText(R.string.publico_2));
                } else {
                    mPrivacyTv.setText(getText(R.string.privado_2));
                }

                mDescriptionTv.setText(response.body().getDescription());
                mWelMsgTv.setText(response.body().getWelcome_message());
                mMsgQuantTv.setText(response.body().getMessages());
                mMemQuantTv.setText(response.body().getMembers());
            }
        }
    }

    private void showAbChannelError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about_channel, container, false);
    }

    /**
     * Shows the progress UI and hides the profile form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mInfoChannelLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        mInfoChannelLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mInfoChannelLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressLoadInfoChannel.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressLoadInfoChannel.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressLoadInfoChannel.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
