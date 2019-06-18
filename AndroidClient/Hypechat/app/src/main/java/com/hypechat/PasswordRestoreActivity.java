package com.hypechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.models.auth.ChangePasswordBody;
import com.hypechat.models.invitations.InvitationsBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PasswordRestoreActivity extends AppCompatActivity {

    private HypechatRequest mHypechatRequest;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_restore);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.activity_background));

        ImageView viewGoBack =(ImageView) findViewById(R.id.go_back_pw_restore);
        viewGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        // Crear conexión al servicio REST
        Retrofit mProfileRestAdapter = new Retrofit.Builder()
                .baseUrl(HypechatRequest.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Crear conexión a la API
        mHypechatRequest = mProfileRestAdapter.create(HypechatRequest.class);
    }

    public void restorePasswordDialog(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                PasswordRestoreActivity.this);

        // set dialog message
        alertDialogBuilder
                .setMessage("¿Está seguro que desea restaurar contraseña?")
                .setCancelable(false)
                .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        dialog.cancel();
                        restorePassword();
                    }
                })
                .setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        final AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void restorePassword(){
        showProgress(true);
        EditText mUsernamePwRestoreEt = (EditText) findViewById(R.id.username_password_restore);
        mUsername = mUsernamePwRestoreEt.getText().toString();
        InvitationsBody pwRestoreBody = new InvitationsBody(mUsername);
        Call<InvitationsBody> pwRestoreCall = mHypechatRequest.passwordRecovery(pwRestoreBody);
        pwRestoreCall.enqueue(new Callback<InvitationsBody>() {
            @Override
            public void onResponse(Call<InvitationsBody> call, Response<InvitationsBody> response) {
                processResponse(response);
            }
            @Override
            public void onFailure(Call<InvitationsBody> call, Throwable t) {
                showPwRestoreError(t.getMessage());
            }
        });
    }

    private void processResponse(Response<InvitationsBody> response) {
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
            showPwRestoreError(error);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    PasswordRestoreActivity.this);

            // set dialog message
            alertDialogBuilder
                    .setMessage("Se envió al email asociado a la cuenta el token correspondiente para contiunar " +
                            "con la restauración de contraseña")
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                            showTokenLayout(getWindow().getDecorView().getRootView());
                        }
                    });

            // create alert dialog
            final AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
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

        final TextInputLayout mUsernamePwRestore = findViewById(R.id.pw_restore_username_ti_layout);
        final Button mRestorePasswordButton = findViewById(R.id.pw_restore_button);
        final ProgressBar mProgressBarPwRestore = findViewById(R.id.pw_restore_progress);
        final Button mGotToken = findViewById(R.id.pw_token_button);

        mUsernamePwRestore.setVisibility(show ? View.GONE : View.VISIBLE);
        mUsernamePwRestore.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mUsernamePwRestore.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mRestorePasswordButton.setVisibility(show ? View.GONE : View.VISIBLE);
        mRestorePasswordButton.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRestorePasswordButton.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mGotToken.setVisibility(show ? View.GONE : View.VISIBLE);
        mGotToken.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mGotToken.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressBarPwRestore.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressBarPwRestore.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBarPwRestore.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void showTokenLayout(View view){
        TextInputLayout mToken = findViewById(R.id.pw_token_ti_layout);
        TextInputLayout mNewPw = findViewById(R.id.new_pw_ti_layout);
        TextInputLayout mNewPwConfirm = findViewById(R.id.confirm_new_pw_ti_layout);
        TextInputLayout mUsername = findViewById(R.id.pw_restore_username_ti_layout);

        Button mChangePw = findViewById(R.id.pw_confirm_restore_button);
        Button mGotToken = findViewById(R.id.pw_token_button);
        Button mRestorePw = findViewById(R.id.pw_restore_button);

        ImageButton mCloseButton = findViewById(R.id.pw_restore_close);

        mUsername.setVisibility(View.GONE);
        mGotToken.setVisibility(View.GONE);
        mRestorePw.setVisibility(View.GONE);

        mCloseButton.setVisibility(View.VISIBLE);
        mChangePw.setVisibility(View.VISIBLE);
        mToken.setVisibility(View.VISIBLE);
        mNewPw.setVisibility(View.VISIBLE);
        mNewPwConfirm.setVisibility(View.VISIBLE);
    }

    public void showGotTokenLayout(View view){
        TextInputLayout mToken = findViewById(R.id.pw_token_ti_layout);
        TextInputLayout mNewPw = findViewById(R.id.new_pw_ti_layout);
        TextInputLayout mNewPwConfirm = findViewById(R.id.confirm_new_pw_ti_layout);
        TextInputLayout mUsername = findViewById(R.id.pw_restore_username_ti_layout);

        Button mChangePw = findViewById(R.id.pw_confirm_restore_button);
        Button mGotToken = findViewById(R.id.pw_token_button);
        Button mRestorePw = findViewById(R.id.pw_restore_button);

        ImageButton mCloseButton = findViewById(R.id.pw_restore_close);

        mUsername.setVisibility(View.VISIBLE);

        mGotToken.setVisibility(View.GONE);
        mRestorePw.setVisibility(View.GONE);

        mCloseButton.setVisibility(View.VISIBLE);
        mChangePw.setVisibility(View.VISIBLE);
        mToken.setVisibility(View.VISIBLE);
        mNewPw.setVisibility(View.VISIBLE);
        mNewPwConfirm.setVisibility(View.VISIBLE);
    }

    public void hideTokenLayout(View view){
        TextInputLayout mToken = findViewById(R.id.pw_token_ti_layout);
        TextInputLayout mNewPw = findViewById(R.id.new_pw_ti_layout);
        TextInputLayout mNewPwConfirm = findViewById(R.id.confirm_new_pw_ti_layout);
        TextInputLayout mUsername = findViewById(R.id.pw_restore_username_ti_layout);

        Button mChangePw = findViewById(R.id.pw_confirm_restore_button);
        Button mGotToken = findViewById(R.id.pw_token_button);
        Button mRestorePw = findViewById(R.id.pw_restore_button);

        ImageButton mCloseButton = findViewById(R.id.pw_restore_close);

        mUsername.setVisibility(View.VISIBLE);
        mGotToken.setVisibility(View.VISIBLE);
        mRestorePw.setVisibility(View.VISIBLE);

        mCloseButton.setVisibility(View.INVISIBLE);
        mChangePw.setVisibility(View.GONE);
        mToken.setVisibility(View.GONE);
        mNewPw.setVisibility(View.GONE);
        mNewPwConfirm.setVisibility(View.GONE);
    }


    private void processResponseChangePassword(Response<ChangePasswordBody> response) {
        // Mostrar progreso
        showProgressChangePassword(false);

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
            showPwRestoreError(error);
        } else {
            Toast.makeText(this, "Se cambió correctamente la contraseña", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void changePassword(View view){
        showProgressChangePassword(true);

        EditText mUsernameRestore = findViewById(R.id.username_password_restore);
        EditText mToken = findViewById(R.id.token_password_restore);
        EditText mNewPw = findViewById(R.id.new_pw_restore);
        EditText mNewPwConfirm = findViewById(R.id.confirm_new_pw_restore);

        // Reset errors.
        mNewPw.setError(null);
        mNewPwConfirm.setError(null);
        mToken.setError(null);
        mUsernameRestore.setError(null);

        // Store values at the time of the register attempt.
        String username = mUsernameRestore.getText().toString();
        String password = mNewPw.getText().toString();
        String confirmPassword = mNewPwConfirm.getText().toString();
        String token = mToken.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameRestore.setError(getString(R.string.error_field_required));
            focusView = mUsernameRestore;
            cancel = true;
        } else {
            mUsername = username;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(token)) {
            mToken.setError(getString(R.string.error_field_required));
            focusView = mToken;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mNewPw.setError(getString(R.string.error_field_required));
            focusView = mNewPw;
            cancel = true;
        } else if (!isPasswordValid(password)){
            mNewPw.setError(getString(R.string.error_incorrect_password));
            focusView = mNewPw;
            cancel = true;
        }

        // Check for a valid confirm password, if the user entered one.
        if (TextUtils.isEmpty(confirmPassword)) {
            mNewPwConfirm.setError(getString(R.string.error_field_required));
            focusView = mNewPwConfirm;
            cancel = true;
        } else if (!isPasswordValid(confirmPassword)){
            mNewPwConfirm.setError(getString(R.string.error_incorrect_password));
            focusView = mNewPwConfirm;
            cancel = true;
        } else if (!password.equals(confirmPassword)) { //check if password match
            mNewPwConfirm.setError(getString(R.string.error_pw_match));
            focusView = mNewPwConfirm;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
            showProgressChangePassword(false);
        } else {

            ChangePasswordBody pwRestoreBody = new ChangePasswordBody(mUsername,password,confirmPassword);
            Call<ChangePasswordBody> pwRestoreCall = mHypechatRequest.changePassword(token,pwRestoreBody);
            pwRestoreCall.enqueue(new Callback<ChangePasswordBody>() {
                @Override
                public void onResponse(Call<ChangePasswordBody> call, Response<ChangePasswordBody> response) {
                    processResponseChangePassword(response);
                }

                @Override
                public void onFailure(Call<ChangePasswordBody> call, Throwable t) {
                    showPwRestoreError(t.getMessage());
                }
            });
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the profile form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgressChangePassword(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        final ProgressBar mProgressBarPwRestore = findViewById(R.id.pw_restore_progress);
        final TextInputLayout mToken = findViewById(R.id.pw_token_ti_layout);
        final TextInputLayout mNewPw = findViewById(R.id.new_pw_ti_layout);
        final TextInputLayout mNewPwConfirm = findViewById(R.id.confirm_new_pw_ti_layout);
        final Button mChangePw = findViewById(R.id.pw_confirm_restore_button);

        mToken.setVisibility(show ? View.GONE : View.VISIBLE);
        mToken.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mToken.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mNewPw.setVisibility(show ? View.GONE : View.VISIBLE);
        mNewPw.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mNewPw.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mNewPwConfirm.setVisibility(show ? View.GONE : View.VISIBLE);
        mNewPwConfirm.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mNewPwConfirm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mChangePw.setVisibility(show ? View.GONE : View.VISIBLE);
        mChangePw.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mChangePw.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressBarPwRestore.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressBarPwRestore.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBarPwRestore.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showPwRestoreError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }
}
