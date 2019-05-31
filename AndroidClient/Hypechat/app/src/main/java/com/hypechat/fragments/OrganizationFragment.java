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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.MainActivity;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.ChannelCreateBody;
import com.hypechat.models.OrganizationCreateBody;
import com.hypechat.models.OrganizationListBody;
import com.hypechat.prefs.SessionPrefs;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrganizationFragment extends Fragment {

    private HypechatRequest mHypechatRequest;
    private View mProgressOrganizationView;
    private ImageView mOrganizationImage;
    private TextInputLayout mTiOrganizationName;
    private Button mJoinButton;
    private ImageButton mCloseButton;
    private Button mCreateOnlyButton;
    private Button mCreateButton;

    public static OrganizationFragment newInstance(Boolean alreadyHasOrganizations) {
        OrganizationFragment orgFragment = new OrganizationFragment();
        Bundle args = new Bundle();
        args.putBoolean("alreadyHasOrganizations", alreadyHasOrganizations);
        orgFragment.setArguments(args);
        return orgFragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.organizations);

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
        mProgressOrganizationView = getView().findViewById(R.id.organization_progress);
        mOrganizationImage = getView().findViewById(R.id.organizations_image);
        mTiOrganizationName = getView().findViewById(R.id.organization_name_ti_layout);
        mJoinButton = getView().findViewById(R.id.organizations_join_button);
        mCloseButton =  getView().findViewById(R.id.organizations_creation_close);
        mCreateOnlyButton =  getView().findViewById(R.id.organizations_create_only_button);
        mCreateButton =  getView().findViewById(R.id.organizations_create_button);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCreateButton.setVisibility(View.GONE);
                mCreateOnlyButton.setVisibility(View.VISIBLE);
                mTiOrganizationName.setVisibility(View.VISIBLE);
                mJoinButton.setVisibility(View.GONE);
                mCloseButton.setVisibility(View.VISIBLE);
            }
        });
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCreateButton.setVisibility(View.VISIBLE);
                mCreateOnlyButton.setVisibility(View.GONE);
                mTiOrganizationName.setVisibility(View.GONE);
                mJoinButton.setVisibility(View.VISIBLE);
                mCloseButton.setVisibility(View.INVISIBLE);
            }
        });

        mCreateOnlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOrganizationWithGeneralChannel();
            }
        });

        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection ConstantConditions
                ((MainActivity) getActivity()).createInvitationsFragment();
            }
        });

        if(getArguments().getBoolean("alreadyHasOrganizations",false)){
            //show
        } else {
            getOrganizations();
        }
        super.onActivityCreated(savedInstanceState);
    }

    private void createOrganizationWithGeneralChannel() {
        showProgressWhileLoadingCreation(true);
        //noinspection ConstantConditions
        EditText mEditTextOrganization = getView().findViewById(R.id.organization_name);
        final String organizationName = mEditTextOrganization.getText().toString();
        OrganizationCreateBody organization = new OrganizationCreateBody(organizationName);
        Call<Void> createOrganizationsCall = mHypechatRequest.createOrganization(organization);
        createOrganizationsCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                processResponseOrganizationCreation(response,organizationName);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showOrganizationError(t.getMessage());
            }
        });

    }

    private void processResponseOrganizationCreation(Response<Void> response, String organizationName) {
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
                showProgressWhileLoadingCreation(false);
            } else {
                error = response.message();

                showProgressWhileLoadingCreation(false);
            }
            showOrganizationError(error);
        } else {
            createGeneralChannelAndStartOrganization(organizationName);
        }
    }

    private void createGeneralChannelAndStartOrganization(final String organizationName) {
        ChannelCreateBody channelCreateBody = new ChannelCreateBody("general","False");
        Call<Void> createOrganizationsCall = mHypechatRequest.createChannel(organizationName,channelCreateBody);
        createOrganizationsCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                processResponseChannelCreation(response,organizationName);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showOrganizationError(t.getMessage());
            }
        });
    }

    private void processResponseChannelCreation(Response<Void> response, String organizationName) {
        showProgressWhileLoadingCreation(false);
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
            showOrganizationError(error);
        } else {
            //noinspection ConstantConditions
            ((MainActivity) getActivity()).setupChannels(organizationName);
        }
    }

    private void getOrganizations(){
        // Show a progress spinner, and kick off a background task to
        // perform the attempt.
        showProgressWhileLoading(true);
        String username = SessionPrefs.get(getContext()).getUsername();

        Call<OrganizationListBody> getOrganizationsCall = mHypechatRequest.getUserOrganizations(username);
        getOrganizationsCall.enqueue(new Callback<OrganizationListBody>() {
            @Override
            public void onResponse(Call<OrganizationListBody> call, Response<OrganizationListBody> response) {
                processResponse(response);
            }

            @Override
            public void onFailure(Call<OrganizationListBody> call, Throwable t) {
                showOrganizationError(t.getMessage());
            }
        });

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgressWhileLoading(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mOrganizationImage.setVisibility(show ? View.VISIBLE : View.GONE);
        mOrganizationImage.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mOrganizationImage.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressOrganizationView.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgressOrganizationView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressOrganizationView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        mCreateButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mCreateButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCreateButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mJoinButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mJoinButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mJoinButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgressWhileLoadingCreation(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mOrganizationImage.setVisibility(show ? View.VISIBLE : View.GONE);
        mOrganizationImage.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mOrganizationImage.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressOrganizationView.setVisibility(show ? View.GONE : View.VISIBLE);
        mProgressOrganizationView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressOrganizationView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        mCreateOnlyButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mCreateOnlyButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCreateOnlyButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mCloseButton.setVisibility(show ? View.VISIBLE : View.GONE);
        mCloseButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCloseButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mTiOrganizationName.setVisibility(show ? View.VISIBLE : View.GONE);
        mTiOrganizationName.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mTiOrganizationName.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void processResponse(Response<OrganizationListBody> response) {
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
            showOrganizationError(error);
        } else {
            List<String> organizations = response.body().getOrganizations();
            if(organizations.size() > 0) {
                //noinspection ConstantConditions
                ((MainActivity) getActivity()).setupChannels(organizations);
            } else {
                String[] myResArray = getResources().getStringArray(R.array.org_initial_array);
                List<String> myResArrayList = Arrays.asList(myResArray);
                //noinspection ConstantConditions
                ((MainActivity) getActivity()).initializeSpinner(myResArrayList);
                showProgressWhileLoading(false);
            }
        }
    }

    private void showOrganizationError(String error) {
        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_organization, container, false);
    }
}
