package com.hypechat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hypechat.API.HypechatRequest;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchProfileActivity extends AppCompatActivity {

    private HypechatRequest mHypechatRequest;
    private Button mSearchProfileButton;
    private AutoCompleteTextView mUsernameProfileEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search_profile);
        toolbar.setTitle(R.string.action_search_profile);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

    private void showProfile() {
        String username = mUsernameProfileEt.getText().toString();


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
