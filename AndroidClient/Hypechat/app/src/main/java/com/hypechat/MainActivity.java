package com.hypechat;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.fragments.ChatChannelFragment;
import com.hypechat.fragments.JoinOrganizationFragment;
import com.hypechat.fragments.OrganizationFragment;
import com.hypechat.models.ChannelListBody;
import com.hypechat.models.LoginBody;
import com.hypechat.prefs.SessionPrefs;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private HypechatRequest mHypechatRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Redirección al Login
        if (!SessionPrefs.get(MainActivity.this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_canal);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView nvDrawer = (NavigationView) findViewById(R.id.nav_view);
            nvDrawer.setNavigationItemSelectedListener(this);

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

            //Select Organizations by default
            Fragment fragment = new OrganizationFragment();
            displaySelectedFragment(fragment);
        }
    }

    public void setupChannels(List<String> organizations){
        final String primaryOrganization = organizations.get(0);
        Call<ChannelListBody> channelsCall = mHypechatRequest.getOrganizationChannels(primaryOrganization);
        channelsCall.enqueue(new Callback<ChannelListBody>() {
            @Override
            public void onResponse(Call<ChannelListBody> call, Response<ChannelListBody> response) {
                processResponseChannels(response,primaryOrganization);
            }

            @Override
            public void onFailure(Call<ChannelListBody> call, Throwable t) {
                showMainError(t.getMessage());
            }
        });
    }

    public void setupChannels(final String organization){
        Call<ChannelListBody> channelsCall = mHypechatRequest.getOrganizationChannels(organization);
        channelsCall.enqueue(new Callback<ChannelListBody>() {
            @Override
            public void onResponse(Call<ChannelListBody> call, Response<ChannelListBody> response) {
                processResponseChannels(response,organization);
            }

            @Override
            public void onFailure(Call<ChannelListBody> call, Throwable t) {
                showMainError(t.getMessage());
            }
        });
    }

    private void processResponseChannels(Response<ChannelListBody> response, String primaryOrganization) {
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
            showMainError(error);
        } else {
            //setear navigation drawer channels
            NavigationView nvDrawer = (NavigationView) findViewById(R.id.nav_view);
            Menu menu = nvDrawer.getMenu();
            menu.clear();
            final SubMenu channelsMenu = menu.addSubMenu(R.string.channels);
            final List<String> channels = response.body().getChannels();
            for(int i = 0; i < channels.size(); i++){
                channelsMenu.add(Menu.NONE, Menu.NONE, i, channels.get(i));
                final int finalI = i;
                channelsMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override public boolean onMenuItemClick(MenuItem item) {
                        onBackPressed();
                        Fragment fragment = ChatChannelFragment.newInstance(channelsMenu.getItem(finalI).getTitle().toString());
                        displaySelectedFragment(fragment);
                        return true;
                    }
                });
                //setear que pasa cuando se clickean
                //TO DO
            }
            SubMenu addChannelMenu = menu.addSubMenu(null);
            addChannelMenu.add(Menu.NONE, Menu.NONE, 0,"+ Añadir canal");

            //setear nuevo fragment
            TextView header_nav = findViewById(R.id.header_nav_text);
            header_nav.setText(primaryOrganization);
            if(channels.size() > 0){
                Fragment fragment = ChatChannelFragment.newInstance(channels.get(0));
                displaySelectedFragment(fragment);
            } else {
                //sino crashea pero siempre deberia haber un canal general en teoria
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void showMainError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    }

    public void logOut(){
        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Cerrando sesión, por favor espere...");
        dialog.setCancelable(false);
        dialog.show();

        Call<Void> logoutCall = mHypechatRequest.logout();
        logoutCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                processResponse(response,dialog);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showMainError(t.getMessage());
            }
        });

    }

    private void processResponse(Response<Void> response, ProgressDialog progressDialog) {
        progressDialog.dismiss();
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
            showMainError(error);
        } else {
            SessionPrefs.get(MainActivity.this).logOut();
            finish();
            Intent login = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(login);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            Intent profile = new Intent(this, ProfileActivity.class);
            startActivity(profile);
        } else
            if(id == R.id.action_logout){

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        MainActivity.this);

                // set dialog message
                alertDialogBuilder
                        .setMessage("¿Está seguro que desea cerrar sesión?")
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                logOut();
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
            }else
            if(id == R.id.action_organization) {
                Fragment fragment = new OrganizationFragment();
                displaySelectedFragment(fragment);
            }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void createInvitationsFragment(){
        Fragment fragment = new JoinOrganizationFragment();
        displaySelectedFragment(fragment);
    }

    /**
     * Loads the specified fragment to the frame
     *
     * @param fragment
     * fragmento que se rellena
     */
    private void displaySelectedFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.flContent, fragment);
        fragmentTransaction.commit();
    }
}
