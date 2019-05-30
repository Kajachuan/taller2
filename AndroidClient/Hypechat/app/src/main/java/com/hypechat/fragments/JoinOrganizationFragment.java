package com.hypechat.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.MainActivity;
import com.hypechat.R;
import com.hypechat.models.InvitationsBody;
import com.hypechat.models.OrganizationCreateBody;
import com.hypechat.models.OrganizationListBody;
import com.hypechat.prefs.SessionPrefs;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JoinOrganizationFragment extends Fragment {

    private HypechatRequest mHypechatRequest;
    private ImageView mInvitationsImageView;
    private TextView mInvitationsTextView;
    private ProgressBar mProgressInvitations;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.invitations);

        // Crear conexión al servicio REST
        Retrofit mOrganizationsRestAdapter = new Retrofit.Builder()
                .baseUrl(HypechatRequest.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Crear conexión a la API
        mHypechatRequest = mOrganizationsRestAdapter.create(HypechatRequest.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mInvitationsImageView =  getView().findViewById(R.id.organizations_join_image);
        mInvitationsTextView =  getView().findViewById(R.id.tv_invitations);
        mProgressInvitations =  getView().findViewById(R.id.organization_join_progress);
        getInvitations();
        super.onActivityCreated(savedInstanceState);
    }

    private void getInvitations(){
        // Show a progress spinner, and kick off a background task to
        // perform the attempt.
        showProgress(true);

        String username = SessionPrefs.get(getContext()).getUsername();

        Call<Void> getInvitationsCall = mHypechatRequest.getUserInvitations(username);
        getInvitationsCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                processResponse(response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showInvitationsError(t.getMessage());
            }
        });
    }

    private void processResponse(Response<Void> response) {
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
            showInvitationsError(error);
        } else {

        }
    }

    private void showInvitationsError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mInvitationsImageView.setVisibility(show ? View.VISIBLE : View.GONE);
        mInvitationsImageView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mInvitationsImageView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressInvitations.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgressInvitations.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressInvitations.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        mInvitationsTextView.setVisibility(show ? View.VISIBLE : View.GONE);
        mInvitationsTextView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mInvitationsTextView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_organization, container, false);
    }
}
