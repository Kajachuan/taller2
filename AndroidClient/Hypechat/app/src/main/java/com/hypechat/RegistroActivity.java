package com.hypechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.ApiError;
import com.hypechat.API.HypechatRequest;
import com.hypechat.models.RegisterBody;
import com.hypechat.models.User;
import com.hypechat.prefs.SessionPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegistroActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private AutoCompleteTextView mEmailView;
    private EditText mConfirmPasswordView;
    private TextInputLayout mFloatLabelUsername;
    private TextInputLayout mFloatLabelEmail;
    private TextInputLayout mFloatLabelPassword;
    private TextInputLayout mFloatLabelConfirmPassword;
    private View mProgressView;
    private View mRegisterFormView;

    private Retrofit mRestAdapter;
    private HypechatRequest mHypechatRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_register);
        toolbar.setTitle(R.string.action_register);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set up the register form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username_reg);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email_reg);
        mConfirmPasswordView = (EditText) findViewById(R.id.password_conf_reg);
        mFloatLabelUsername = (TextInputLayout) findViewById(R.id.username_reg_ti);
        mFloatLabelEmail = (TextInputLayout) findViewById(R.id.email_reg_ti);
        mFloatLabelPassword = (TextInputLayout) findViewById(R.id.password_reg_ti);
        mFloatLabelConfirmPassword = (TextInputLayout) findViewById(R.id.password_confirm_reg_ti);

        mPasswordView = (EditText) findViewById(R.id.password_reg);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if (!isOnline()) {
                        showRegisterError(getString(R.string.error_network));
                        return false;
                    }
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mRegisterButton = (Button) findViewById(R.id.email_reg_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOnline()) {
                    showRegisterError(getString(R.string.error_network));
                    return;
                }
                attemptRegister();
            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);

        // Crear conexión al servicio REST
        mRestAdapter = new Retrofit.Builder()
                .baseUrl(HypechatRequest.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Crear conexión a la API
        mHypechatRequest = mRestAdapter.create(HypechatRequest.class);
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void showRegisterError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private boolean isUserValid(String userId) {
        return userId.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".com");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private void attemptRegister() {
        // Reset errors.
        mFloatLabelUsername.setError(null);
        mFloatLabelPassword.setError(null);
        mFloatLabelConfirmPassword.setError(null);
        mFloatLabelEmail.setError(null);

        // Store values at the time of the register attempt.
        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;



        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mFloatLabelPassword.setError(getString(R.string.error_field_required));
            focusView = mFloatLabelPassword;
            cancel = true;
        } else if (!isPasswordValid(password)){
            mFloatLabelPassword.setError(getString(R.string.error_incorrect_password));
            focusView = mFloatLabelPassword;
            cancel = true;
        }

        // Check for a valid confirm password, if the user entered one.
        if (TextUtils.isEmpty(confirmPassword)) {
            mFloatLabelConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = mFloatLabelConfirmPassword;
            cancel = true;
        } else if (!isPasswordValid(confirmPassword)){
            mFloatLabelConfirmPassword.setError(getString(R.string.error_incorrect_password));
            focusView = mFloatLabelConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) { //check if password match
            mFloatLabelPassword.setError(getString(R.string.error_pw_match));
            focusView = mFloatLabelPassword;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            mFloatLabelUsername.setError(getString(R.string.error_field_required));
            focusView = mFloatLabelUsername;
            cancel = true;
        } else if (!isUserValid(username)) {
            mFloatLabelUsername.setError(getString(R.string.error_invalid_user));
            focusView = mFloatLabelUsername;
            cancel = true;
        }

        // Check for a valid email
        if (TextUtils.isEmpty(email)) {
            mFloatLabelEmail.setError(getString(R.string.error_field_required));
            focusView = mFloatLabelEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mFloatLabelEmail.setError(getString(R.string.error_invalid_email));
            focusView = mFloatLabelEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);

            final RegisterBody reg_body = new RegisterBody(username, email, password);

            Call<User> registerCall = mHypechatRequest.register(reg_body);
            registerCall.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    processResponse(response);
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    showProgress(false);
                    showRegisterError(t.getMessage());
                }
            });
        }
    }

    private void processResponse(Response<User> response) {
        // Mostrar progreso
        showProgress(false);

        // Procesar errores
        if (!response.isSuccessful()) {
            String error;
            if (response.errorBody()
                    .contentType()
                    .subtype()
                    .equals("application/json")) {
                ApiError apiError = ApiError.fromResponseBody(response.errorBody());
                Log.d("LoginActivity","asd");
                assert apiError != null;
                error = apiError.getMessage();
                Log.d("LoginActivity", apiError.getDeveloperMessage());
            } else {
                error = response.message();
                Log.d("LoginActivity",error);
            }

            showRegisterError(error);
            return;
        }

        showLoginScreen();

    }

    private void showLoginScreen() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}


