package com.hypechat.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.MainActivity;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.invitations.AcceptInvitationBody;
import com.hypechat.models.organizations.CustomAdapter;
import com.hypechat.models.invitations.InvitationsListBody;
import com.hypechat.prefs.SessionPrefs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
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
    private CustomAdapter mAdapter;
    private FloatingActionButton mFloatingButton;
    ListView listView;

    public static JoinOrganizationFragment newInstance(ArrayList<String> invitations) {
        JoinOrganizationFragment orgFragment = new JoinOrganizationFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("invitations", invitations);
        orgFragment.setArguments(args);
        return orgFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.invitations);

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
        mInvitationsImageView =  getView().findViewById(R.id.organizations_join_image);
        mInvitationsTextView =  getView().findViewById(R.id.tv_invitations);
        mProgressInvitations =  getView().findViewById(R.id.organization_join_progress);
        mFloatingButton = getView().findViewById(R.id.floating_join_organization);
        listView = getView().findViewById(R.id.join_list);

        if (getArguments() != null) {
            ArrayList<String> invitations = getArguments().getStringArrayList("invitations");
            if(invitations.contains("Inicio")){
                mFloatingButton.show();
                mFloatingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((MainActivity) getActivity()).returnToOrganizations();
                    }
                });
            } else {
                mFloatingButton.hide();
            }
        }


        getInvitations();
        super.onActivityCreated(savedInstanceState);
    }

    private void getInvitations(){
        // Show a progress spinner, and kick off a background task to
        // perform the attempt.
        showProgress(true);

        String username = SessionPrefs.get(getContext()).getUsername();

        Call<InvitationsListBody> getInvitationsCall = mHypechatRequest.getUserInvitations(username);
        getInvitationsCall.enqueue(new Callback<InvitationsListBody>() {
            @Override
            public void onResponse(Call<InvitationsListBody> call, Response<InvitationsListBody> response) {
                processResponse(response);
            }

            @Override
            public void onFailure(Call<InvitationsListBody> call, Throwable t) {
                showProgress(false);
                showInvitationsError(t.getMessage());
            }
        });
    }

    private void processResponse(Response<InvitationsListBody> response) {
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
            showProgress(false);
            showInvitationsError(error);
        } else {
            Map<String, String> invitationsList = response.body().getInvitations();
            if (invitationsList.size() > 0){
                mProgressInvitations.setVisibility(View.GONE);
                invitationsList = removeAcceptedInvitations(invitationsList);
                if(invitationsList.size() > 0){
                    mAdapter = new CustomAdapter(invitationsList, getContext(),this);
                    listView.setAdapter(mAdapter);
                } else {
                    showProgress(false);
                }
            } else {
                showProgress(false);
            }

        }
    }

    private Map<String, String> removeAcceptedInvitations(Map<String, String> invitationsList) {
        if (getArguments() != null) {
            ArrayList<String> invitations = getArguments().getStringArrayList("invitations");
            for(Iterator<Map.Entry<String, String>> it = invitationsList.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, String> entry = it.next();
                if (invitations != null && invitations.contains(entry.getValue())) {
                    it.remove();
                }
            }
        }
        return invitationsList;
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

    public void acceptInvitation(final String token, final String organization){
        // Show a progress spinner, and kick off a background task to
        // perform the attempt.
        showProgressAccept(true);
        AcceptInvitationBody acceptBody = new AcceptInvitationBody(token);
        Call<Void> acceptInvitationCall = mHypechatRequest.acceptInvitation(organization,acceptBody);
        acceptInvitationCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                processResponseAccept(response,organization);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showProgressAccept(false);
                showInvitationsError(t.getMessage());
            }
        });
    }

    private void processResponseAccept(Response<Void> response, String organization) {
        showProgressAccept(false);
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
            //noinspection ConstantConditions
            ((MainActivity) getActivity()).addOrganizationToAdapter(organization);
            mAdapter.remove(organization);
            mAdapter.notifyDataSetChanged();
            showInvitationsError("Se aceptó la invitación correctamente");
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgressAccept(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        listView.setVisibility(show ? View.VISIBLE : View.GONE);
        listView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_join_organization, container, false);
    }
}
