package com.hypechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.models.ProfileBodySave;
import com.hypechat.models.ProfileBodyLoad;
import com.hypechat.prefs.SessionPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileActivity extends AppCompatActivity {

    private HypechatRequest mHypechatRequest;
    private EditText mName;
    private EditText mLastName;
    private View mNameLayout;
    private View mLastNameLayout;
    private ImageView mProfileImage;
    private TextView mTextNameView;
    private ProgressBar mPb_name;
    private ProgressBar mPb_image;
    private ProgressBar mPb_last_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_profile);
        toolbar.setTitle(R.string.action_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mNameLayout = (RelativeLayout) findViewById(R.id.rl_name);
        mLastNameLayout = (RelativeLayout) findViewById(R.id.rl_last_name);
        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mTextNameView = (TextView) findViewById(R.id.profile_text_name);

        mPb_name = (ProgressBar) findViewById(R.id.progressBar_name);
        mPb_image = (ProgressBar) findViewById(R.id.progressBar_profile_image);
        mPb_last_name = (ProgressBar) findViewById(R.id.progressBar_lastName);

        mName = (EditText) findViewById(R.id.name_text);
        mLastName = (EditText) findViewById(R.id.last_name_text);

        // Show a progress spinner, and kick off a background task to
        // perform the user load profile attempt.
        showProgress(true);

        // Crear conexión al servicio REST
        Retrofit mProfileRestAdapter = new Retrofit.Builder()
                .baseUrl(HypechatRequest.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Crear conexión a la API
        mHypechatRequest = mProfileRestAdapter.create(HypechatRequest.class);

        if(!isOnline()){
            showProfileError(getString(R.string.error_network));
            showProgress(false);
        } else {
            loadInformation();
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_profile) {
            if(!isOnline()) {
                showProfileError(getString(R.string.error_network));
                showProgress(false);
            } else {
                saveProfile();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveProfile() {
        // Show a progress spinner, and kick off a background task to
        // perform the user save profile attempt.
        showProgress(true);

        String name = mName.getText().toString();
        String lastName = mLastName.getText().toString();
        String username = SessionPrefs.get(ProfileActivity.this).getUsername();

        final ProfileBodySave profileBody = new ProfileBodySave(username,name,lastName);

        Call<Void> profileCall = mHypechatRequest.saveProfile(profileBody);
        profileCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                processSaveProfileResponse(response);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showProfileError(t.getMessage());
                showProgress(false);
            }
        });

    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void loadInformation(){
        String username = SessionPrefs.get(ProfileActivity.this).getUsername();

        Call<ProfileBodyLoad> profileCall = mHypechatRequest.getProfileInformation(username);
        profileCall.enqueue(new Callback<ProfileBodyLoad>() {
            @Override
            public void onResponse(Call<ProfileBodyLoad> call, Response<ProfileBodyLoad> response) {
                processLoadProfileResponse(response);
            }

            @Override
            public void onFailure(Call<ProfileBodyLoad> call, Throwable t) {
                showProfileError(t.getMessage());
                showProgress(false);
            }
        });
    }

    private void processLoadProfileResponse(Response<ProfileBodyLoad> response) {
        // Mostrar progreso
        showProgress(false);

        // Procesar errores
        if (!response.isSuccessful()) {
            String error;
            if (response.errorBody()
                    .contentType()
                    .subtype()
                    .equals("application/json")) {
                APIError apiError = ErrorUtils.parseError(response);
                assert apiError != null;
                error = apiError.message();
            } else {
                error = response.message();
            }
            showProfileError(error);
        } else {
            mName.setText(response.body().getName());
            mLastName.setText(response.body().getLastName());
            String nameSpace = response.body().getName().concat(" ");
            String fullName = nameSpace.concat(response.body().getLastName());
            mTextNameView.setText(fullName);
        }
    }

    private void processSaveProfileResponse(Response<Void> response) {

        showProgress(false);

        // Procesar errores
        if (!response.isSuccessful()) {
            String error;
            if (response.errorBody()
                    .contentType()
                    .subtype()
                    .equals("application/json")) {
                APIError apiError = ErrorUtils.parseError(response);
                assert apiError != null;
                error = apiError.message();
            } else {
                error = response.message();
            }
            showProfileError(error);
        } else {
            Toast.makeText(this, R.string.saved_profile, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showProfileError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    public void setFocusOnNameEditText(View view) {
        mName.setSelection(mName.getText().length());
        mName.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mName, InputMethodManager.SHOW_IMPLICIT);
    }

    public void setFocusOnLastNameEditText(View view) {
        mLastName.setSelection(mLastName.getText().length());
        mLastName.requestFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mLastName, InputMethodManager.SHOW_IMPLICIT);
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

        mNameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        mNameLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mNameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mLastNameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        mLastNameLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLastNameLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProfileImage.setVisibility(show ? View.GONE : View.VISIBLE);
        mProfileImage.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProfileImage.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });


        mTextNameView.setVisibility(show ? View.GONE : View.VISIBLE);
        mTextNameView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mTextNameView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mPb_name.setVisibility(show ? View.VISIBLE : View.GONE);
        mPb_name.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPb_name.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        mPb_image.setVisibility(show ? View.VISIBLE : View.GONE);
        mPb_image.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPb_image.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

        mPb_last_name.setVisibility(show ? View.VISIBLE : View.GONE);
        mPb_last_name.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mPb_last_name.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
