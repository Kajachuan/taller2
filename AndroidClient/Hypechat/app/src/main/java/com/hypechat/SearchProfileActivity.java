package com.hypechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.models.profile.ProfileBodyLoad;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchProfileActivity extends AppCompatActivity {

    private HypechatRequest mHypechatRequest;
    private Button mSearchProfileButton;
    private AutoCompleteTextView mUsernameProfileEt;
    private TextView mUsernameTv;
    private TextView mFirstNameTv;
    private TextView mLastNameTv;
    private TextView mEmailTv;
    private TextView mChannelsTv;
    private TextView mMessagesTv;
    private TextView mBanDateTv;
    private TextView mBanReasonTv;
    private LinearLayout mBanLayout;
    private LinearLayout mSearchLayout;
    private LinearLayout mStatisticsLayout;
    private TextView mRolTv;
    private ImageView mUserImage;
    private ProgressBar mProgressSearchProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search_profile);
        toolbar.setTitle(R.string.action_search_profile);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mUsernameTv = findViewById(R.id.username_searched);
        mFirstNameTv = findViewById(R.id.name_searched);
        mEmailTv = findViewById(R.id.email_searched);
        mLastNameTv = findViewById(R.id.last_name_searched);
        mChannelsTv = findViewById(R.id.channels_searched);
        mMessagesTv = findViewById(R.id.messages_searched);
        mBanDateTv = findViewById(R.id.ban_date_searched);
        mBanReasonTv = findViewById(R.id.ban_reason_searched);
        mBanLayout = findViewById(R.id.ban_layout);
        mUserImage = findViewById(R.id.imageView_user_searched);
        mSearchLayout = findViewById(R.id.search_user_prompt);
        mRolTv = findViewById(R.id.rol_searched);
        mProgressSearchProfile = findViewById(R.id.progressBar_profile_searched);
        mStatisticsLayout = findViewById(R.id.user_statistics);

        mUsernameProfileEt = findViewById(R.id.user_search_one_profile);
        mSearchProfileButton = findViewById(R.id.search_user_profile_button);
        mSearchProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfile();
            }
        });

        // Crear conexión al servicio REST
        Retrofit mRestAdapter = new Retrofit.Builder()
                .baseUrl(HypechatRequest.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Crear conexión a la API
        mHypechatRequest = mRestAdapter.create(HypechatRequest.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search_profile) {
            mStatisticsLayout.setVisibility(View.GONE);
            TextInputLayout usernameTIL = findViewById(R.id.search_one_user_layout);
            usernameTIL.setVisibility(View.VISIBLE);
            mSearchProfileButton.setVisibility(View.VISIBLE);
            ImageView ivUsers = findViewById(R.id.imageView_search_user);
            ivUsers.setVisibility(View.VISIBLE);
            TextView tvSearchOne = findViewById(R.id.search_one_profile);
            tvSearchOne.setVisibility(View.VISIBLE);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProfile() {
        showProgress(true);
        final String username = mUsernameProfileEt.getText().toString();
        if (!username.isEmpty()) {
            Call<ProfileBodyLoad> profileSearchCall = mHypechatRequest.getProfileInformation(username);
            profileSearchCall.enqueue(new Callback<ProfileBodyLoad>() {
                @Override
                public void onResponse(@NonNull Call<ProfileBodyLoad> call, @NonNull Response<ProfileBodyLoad> response) {
                    // Mostrar progreso
                    showProgress(false);
                    processLoadProfileResponse(response,username);
                }

                @Override
                public void onFailure(@NonNull Call<ProfileBodyLoad> call, @NonNull Throwable t) {
                    showProfileError(t.getMessage());
                    showProgress(false);
                }
            });

        } else {
            showProgress(false);
        }
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mStatisticsLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        mSearchLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mSearchLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressSearchProfile.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressSearchProfile.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressSearchProfile.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void processLoadProfileResponse(final Response<ProfileBodyLoad> response, String username) {

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
            showProfileError(error);
        } else {
            if (response.body() != null) {

                TextInputLayout usernameTIL = findViewById(R.id.search_one_user_layout);
                usernameTIL.setVisibility(View.GONE);
                mSearchProfileButton.setVisibility(View.GONE);
                ImageView ivUsers = findViewById(R.id.imageView_search_user);
                ivUsers.setVisibility(View.GONE);
                TextView tvSearchOne = findViewById(R.id.search_one_profile);
                tvSearchOne.setVisibility(View.GONE);

                mStatisticsLayout.setVisibility(View.VISIBLE);
                mUsernameTv.setText(username);
                mFirstNameTv.setText(response.body().getName());
                mLastNameTv.setText(response.body().getLastName());
                mEmailTv.setText(response.body().getEmail());
                mMessagesTv.setText(response.body().getMessages());
                String imageString = response.body().getImage();
                if(imageString != null) {
                    Bitmap imageBitmap = stringToBitmap(imageString);
                    mUserImage.setImageBitmap(imageBitmap);
                }
                if(response.body().getBan_date() != null){
                    mBanLayout.setVisibility(View.VISIBLE);
                    mBanDateTv.setText(response.body().getBan_date());
                    mBanReasonTv.setText(response.body().getBan_reason());
                } else {
                    mBanLayout.setVisibility(View.GONE);
                }

                List<String> organizationsList =  response.body().getOrganizationsList();
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, organizationsList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Spinner organizationsSpinner = (Spinner) findViewById(R.id.organizations_searched);
                organizationsSpinner.setAdapter(adapter);

                organizationsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                    {
                        String selectedItem = parent.getItemAtPosition(position).toString();
                        Map<String, Map<String,String>> secondLevel = response.body().getSecondLevel(selectedItem);

                        //noinspection ConstantConditions
                        List<String> channels = new ArrayList<>(secondLevel.get("channels").keySet());
                        StringBuilder builder = new StringBuilder();
                        for (String channel : channels) {
                            builder.append(channel).append("\n");
                        }
                        mChannelsTv.setText(builder.toString());

                        //noinspection ConstantConditions
                        String rol = secondLevel.get("rol").values().toString();
                        rol = rol.replace( '[', ' ');
                        rol = rol.replace( ']', ' ');
                        mRolTv.setText(rol);

                    } // to close the onItemSelected
                    public void onNothingSelected(AdapterView<?> parent)
                    {

                    }
                });
            }
        }
    }
    public Bitmap stringToBitmap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    private void showProfileError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
