package com.hypechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.models.LoginBody;
import com.hypechat.prefs.SessionPrefs;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private TextInputLayout mFloatLabelUserId;
    private TextInputLayout mFloatLabelPassword;
    private View mProgressView;
    private View mLoginFormView;

    private HypechatRequest mHypechatRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_login);
        toolbar.setTitle(R.string.action_sign_in);
        setSupportActionBar(toolbar);*/

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.activity_background));

        // Set up the login form.
        mUserView = (AutoCompleteTextView) findViewById(R.id.user);
        mFloatLabelUserId = (TextInputLayout) findViewById(R.id.user_ti_layout);
        mFloatLabelPassword = (TextInputLayout) findViewById(R.id.pw_ti_layout);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if (!isOnline()) {
                        showLoginError(getString(R.string.error_network));
                        return false;
                    }
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOnline()) {
                    showLoginError(getString(R.string.error_network));
                    return;
                }
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Crear conexión al servicio REST
        Retrofit mLoginRestAdapter = new Retrofit.Builder()
                .baseUrl(HypechatRequest.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Crear conexión a la API
        mHypechatRequest = mLoginRestAdapter.create(HypechatRequest.class);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mFloatLabelUserId.setError(null);
        mFloatLabelPassword.setError(null);

        // Store values at the time of the login attempt.
        final String userId = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mFloatLabelPassword.setError(getString(R.string.error_field_required));
            focusView = mFloatLabelPassword;
            cancel = true;
        } else if (!isPasswordValid(password)){
            mFloatLabelPassword.setError(getString(R.string.error_invalid_password));
            focusView = mFloatLabelPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(userId)) {
            mFloatLabelUserId.setError(getString(R.string.error_field_required));
            focusView = mFloatLabelUserId;
            cancel = true;
        } else if (!isUserValid(userId)) {
            mFloatLabelUserId.setError(getString(R.string.error_invalid_user));
            focusView = mFloatLabelUserId;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            final LoginBody userLoginBody = new LoginBody(userId, password);

            Call<Void> loginCall = mHypechatRequest.login(userLoginBody);
            loginCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    processResponse(response,userLoginBody);
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showProgress(false);
                    showLoginError(t.getMessage());
                }
            });
        }
    }

    private void processResponse(Response<Void> response, LoginBody userLoginBody) {
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
                if(error.equals("BAD REQUEST")) {
                    error = "El usuario o contraseña ingresado son incorrectos.";
                }
            }
            showLoginError(error);
        } else {
            // Guardar usuario en preferencias
            SessionPrefs.get(LoginActivity.this).saveUser(userLoginBody);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private boolean isUserValid(String userId) {
        return userId.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public void openRegisterActivity(View view) {
        startActivity(new Intent(this, RegistroActivity.class));
    }

    private void showLoginError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
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

        if(show){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

