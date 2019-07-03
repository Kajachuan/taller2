package com.hypechat;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.models.auth.RegisterBody;

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
    LocationManager locationManager;
    double longitude;
    double latitude;

    private HypechatRequest mHypechatRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.activity_background));

        ImageView viewGoBack = (ImageView) findViewById(R.id.go_back);
        viewGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        // Set up the register form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username_reg);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email_reg);
        mPasswordView = (EditText) findViewById(R.id.password_reg);
        mFloatLabelUsername = (TextInputLayout) findViewById(R.id.username_reg_ti);
        mFloatLabelEmail = (TextInputLayout) findViewById(R.id.email_reg_ti);
        mFloatLabelPassword = (TextInputLayout) findViewById(R.id.password_reg_ti);
        mFloatLabelConfirmPassword = (TextInputLayout) findViewById(R.id.password_confirm_reg_ti);

        mConfirmPasswordView = (EditText) findViewById(R.id.password_conf_reg);
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if (!isOnline()) {
                        showRegisterError(getString(R.string.error_network));
                        return false;
                    }
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mConfirmPasswordView.getWindowToken(), 0);
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
        Retrofit mRegisterRestAdapter = new Retrofit.Builder()
                .baseUrl(HypechatRequest.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Crear conexión a la API
        mHypechatRequest = mRegisterRestAdapter.create(HypechatRequest.class);

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {android.Manifest.permission.ACCESS_FINE_LOCATION,
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    101);
        } else {
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            if (!isLocationEnabled()) {
                showAlert();
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                    if (!isLocationEnabled()) {
                        showAlert();
                    }
                } else {
                    finish();
                }
            }
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

    };

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

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Activar ubicación")
                .setMessage("La ubicación está desactivada.\nPor favor, activela para usar esta app")
                .setPositiveButton("Configuración ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1){
            if (!isLocationEnabled()) {
                finish();
            } else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempts to sign in or register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private void attemptRegister() {
        Log.d("lat", String.valueOf(latitude));
        Log.d("lon", String.valueOf(longitude));
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
        } else if (!isPasswordValid(password)) {
            mFloatLabelPassword.setError(getString(R.string.error_incorrect_password));
            focusView = mFloatLabelPassword;
            cancel = true;
        }

        // Check for a valid confirm password, if the user entered one.
        if (TextUtils.isEmpty(confirmPassword)) {
            mFloatLabelConfirmPassword.setError(getString(R.string.error_field_required));
            focusView = mFloatLabelConfirmPassword;
            cancel = true;
        } else if (!isPasswordValid(confirmPassword)) {
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
            if(locationDiffersFromZero()) {
                final RegisterBody reg_body = new RegisterBody(username, email, password, confirmPassword, latitude, longitude);
                Call<Void> registerCall = mHypechatRequest.register(reg_body);
                registerCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        processResponse(response);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        showProgress(false);
                        showRegisterError(t.getMessage());
                    }
                });
            } else {
                showProgress(false);
                showRegisterError("Se está computando la ubicación, por favor espere unos segundos y reintente");
            }
        }
    }

    public boolean locationDiffersFromZero(){
        boolean latIsZero = (latitude > -1 && latitude < 1);
        boolean lonIsZero = (longitude > -1 && longitude < 1);

        return (!latIsZero && !lonIsZero);
    }

    private void processResponse(Response<Void> response) {
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
                if(error.equals("INTERNAL SERVER ERROR")) {
                    //SUPUESTAMENTE TIRA ESTO CUANDO YA ESTA CREADO EL USER PERO RARO OJO
                    //TODO: Revisar esta response
                    error = "El usuario ya fue creado";
                } else {
                    error = "Se produjo un error, por favor intente de nuevo";
                }
            }
            showRegisterError(error);
        } else {
            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
            finish();
        }
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

        if(show){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

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


