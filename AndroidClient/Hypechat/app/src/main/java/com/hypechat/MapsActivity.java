package com.hypechat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.appindexing.builders.StickerBuilder;
import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.map.MapGetLocations;
import com.hypechat.models.profile.ProfileBodyLoad;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    private HypechatRequest mHypechatRequest;
    String organization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent i = getIntent();
        organization = i.getStringExtra("organization");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_maps);
        toolbar.setTitle(getString(R.string.mapa)+ " " + organization);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        Call<MapGetLocations> locationsCall = mHypechatRequest.getLocations(organization);
        locationsCall.enqueue(new Callback<MapGetLocations>() {
            @Override
            public void onResponse(Call<MapGetLocations> call, Response<MapGetLocations> response) {
                processResponse(response,googleMap);
            }

            @Override
            public void onFailure(Call<MapGetLocations> call, Throwable t) {
                showError(t.getMessage());
            }
        });
    }


    private void showError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    private void processResponse(Response<MapGetLocations> response, GoogleMap googleMap) {
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
            showError(error);
        } else {
            if(response.body() != null){
                Iterator it = response.body().getLocations().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry)it.next();
                    String username = (String) pair.getKey();
                    Map<String, String> latlon = (Map<String, String>) pair.getValue();
                    String lat = latlon.get("lat");
                    String lon = latlon.get("lon");
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(lat),Double.parseDouble(lon)))
                            .title(username));
                    it.remove(); // avoids a ConcurrentModificationException
                }
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-34.603722, -58.381592), 10));
            }

        }
    }
}
