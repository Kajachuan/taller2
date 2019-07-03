package com.hypechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.fragments.AboutChannelFragment;
import com.hypechat.fragments.AddUserToPrivateChannelFragment;
import com.hypechat.fragments.BotsFragment;
import com.hypechat.fragments.ChatChannelFragment;
import com.hypechat.fragments.JoinOrganizationFragment;
import com.hypechat.fragments.NewChannelFragment;
import com.hypechat.fragments.OrganizationFragment;
import com.hypechat.models.channels.ChannelListBody;
import com.hypechat.models.firebase.TokenPost;
import com.hypechat.models.messages.Message;
import com.hypechat.prefs.SessionPrefs;

import java.util.ArrayList;
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
    private Spinner mOrgsSpinner;
    private ArrayAdapter<String> dataAdapter;
    private Menu mMainMenu;
    Typeface tfteko;

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
            Fragment fragment = OrganizationFragment.newInstance(false);
            displaySelectedFragment(fragment);
        }
    }

    public void returnToOrganizations(){
        //Select Organizations by default
        Fragment fragment = OrganizationFragment.newInstance(false);
        displaySelectedFragment(fragment);
    }

    public void initializeSpinner(List<String> list){
        mOrgsSpinner = (Spinner) findViewById(R.id.spinner_header);
        dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list) {
            @NonNull
            public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                tfteko = Typeface.createFromAsset(getAssets(),"fonts/tekoregular.otf");
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTypeface(tfteko);
                v.setTextColor(Color.WHITE);
                v.setTextSize(35);
                return v;
            }
            public View getDropDownView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTypeface(tfteko);
                v.setTextColor(Color.WHITE);
                v.setTextSize(35);
                return v;
            }
        };
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mOrgsSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        final String organization = parent.getItemAtPosition(position).toString();
                        if(!organization.equals("Inicio")){
                            Call<ChannelListBody> channelsCall = mHypechatRequest.getOrganizationChannels(organization);
                            channelsCall.enqueue(new Callback<ChannelListBody>() {
                                @Override
                                public void onResponse(@NonNull Call<ChannelListBody> call, @NonNull Response<ChannelListBody> response) {
                                    processResponseChannels(response,organization);
                                }

                                @Override
                                public void onFailure(@NonNull Call<ChannelListBody> call, @NonNull Throwable t) {
                                    showMainError(t.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        mOrgsSpinner.setAdapter(dataAdapter);
    }

    public void updateChannels(final String organization){
        if(!organization.equals("Inicio")){
            Call<ChannelListBody> channelsCall = mHypechatRequest.getOrganizationChannels(organization);
            channelsCall.enqueue(new Callback<ChannelListBody>() {
                @Override
                public void onResponse(@NonNull Call<ChannelListBody> call, @NonNull Response<ChannelListBody> response) {
                    processResponseChannels2(response,organization);
                }

                @Override
                public void onFailure(@NonNull Call<ChannelListBody> call, @NonNull Throwable t) {
                    showMainError(t.getMessage());
                }
            });
        }
    }

    public void setupChannels(List<String> organizations){
        final String primaryOrganization = organizations.get(0);
        MenuItem organizationsItem = mMainMenu.getItem(0);
        if(!organizationsItem.isVisible()){
            organizationsItem.setVisible(true);
        }
        initializeSpinner(organizations);
    }

    public void setupChannels(final String organization){
        List<String> auxList = new ArrayList<>();
        auxList.add(organization);
        if(!mOrgsSpinner.getSelectedItem().toString().equals("Inicio")){
            MenuItem organizationsItem = mMainMenu.getItem(0);
            if(!organizationsItem.isVisible()){
                organizationsItem.setVisible(true);
            }
            dataAdapter.add(organization);
            dataAdapter.notifyDataSetChanged();
            mOrgsSpinner.setSelection(dataAdapter.getPosition(organization));
        } else {
            initializeSpinner(auxList);
        }
    }

    private void processResponseChannels(Response<ChannelListBody> response, final String primaryOrganization) {
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
            final List<List<String>> channels = response.body().getChannels();
            for(int i = 0; i < channels.size(); i++){
                int unicodePrivate = 0x1F512;
                int unicodePublic = 0x1F4E2;
                if(channels.get(i).get(1).equals("private")){
                    channelsMenu.add(Menu.NONE, Menu.NONE, i, getEmojiByUnicode(unicodePrivate)+" "+channels.get(i).get(0));
                } else {
                    channelsMenu.add(Menu.NONE, Menu.NONE, i, getEmojiByUnicode(unicodePublic)+" "+channels.get(i).get(0));
                }

                final int finalI = i;
                channelsMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override public boolean onMenuItemClick(MenuItem item) {
                        onBackPressed();
                        Fragment fragment = ChatChannelFragment.newInstance(primaryOrganization,channels.get(finalI).get(0),channels.get(finalI).get(1));
                        displaySelectedFragment(fragment);
                        return true;
                    }
                });
            }

            SubMenu addChannelMenu = menu.addSubMenu(null);
            addChannelMenu.add(Menu.NONE, Menu.NONE, 0,"+ Añadir canal");
            addChannelMenu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override public boolean onMenuItemClick(MenuItem item) {
                    onBackPressed();
                    Fragment fragment = NewChannelFragment.newInstance(mOrgsSpinner.getSelectedItem().toString());
                    displaySelectedFragment(fragment);
                    return true;
                }
            });

            //setear nuevo fragment
            if(channels.size() > 0){
                Fragment fragment = ChatChannelFragment.newInstance(primaryOrganization,channels.get(0).get(0),channels.get(0).get(1));
                displaySelectedFragment(fragment);
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    onBackPressed();
                }
                sendFirebaseToken();
            } else {
                //sino crashearia pero siempre deberia haber un canal general en teoria
            }
        }
    }

    public void addChatFragmentAdditionalMenuOptions(){

        MenuItem organizations = mMainMenu.getItem(0);
        if(!organizations.isVisible()){
            organizations.setVisible(true);
        }

        MenuItem maps = mMainMenu.getItem(1);
        if(!maps.isVisible()){
            maps.setVisible(true);
        }

        MenuItem aboutChannel = mMainMenu.getItem(2);
        if(!aboutChannel.isVisible()){
            aboutChannel.setVisible(true);
        }

        MenuItem bots = mMainMenu.getItem(3);
        if(!bots.isVisible()){
            bots.setVisible(true);
        }

    }

    public String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    private void sendFirebaseToken() {
        final String username = SessionPrefs.get(this).getUsername();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TOKEN_NOT_SUCCESSFUL", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String tokenResult = task.getResult().getToken();
                        TokenPost tokenBody = new TokenPost(tokenResult);
                        Call<Void> tokenCall = mHypechatRequest.sendFirebaseToken(username,tokenBody);
                        tokenCall.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                                processTokenResponse(response);
                            }

                            @Override
                            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                showMainError(t.getMessage());
                            }
                        });

                    }
                });
    }


    private void processTokenResponse(Response<Void> response) {
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
            Log.d("TOKEN", "ENVIO OK");
        }
    }

    private void processResponseChannels2(Response<ChannelListBody> response, final String primaryOrganization) {
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
            final List<List<String>> channels = response.body().getChannels();
            if(!((channels.size()) == (menu.size() - 1))){
                menu.clear();
                final SubMenu channelsMenu = menu.addSubMenu(R.string.channels);
                for(int i = 0; i < channels.size(); i++){
                    int unicodePrivate = 0x1F512;
                    int unicodePublic = 0x1F4E2;
                    if(channels.get(i).get(1).equals("private")){
                        channelsMenu.add(Menu.NONE, Menu.NONE, i, getEmojiByUnicode(unicodePrivate)+" "+channels.get(i).get(0));
                    } else {
                        channelsMenu.add(Menu.NONE, Menu.NONE, i, getEmojiByUnicode(unicodePublic)+" "+channels.get(i).get(0));
                    }
                    final int finalI = i;
                    channelsMenu.getItem(i).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override public boolean onMenuItemClick(MenuItem item) {
                            onBackPressed();
                            Fragment fragment = ChatChannelFragment.newInstance(primaryOrganization,channels.get(finalI).get(0),channels.get(finalI).get(1));
                            displaySelectedFragment(fragment);
                            return true;
                        }
                    });
                }

                SubMenu addChannelMenu = menu.addSubMenu(null);
                addChannelMenu.add(Menu.NONE, Menu.NONE, 0,"+ Añadir canal");
                addChannelMenu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override public boolean onMenuItemClick(MenuItem item) {
                        onBackPressed();
                        Fragment fragment = NewChannelFragment.newInstance(mOrgsSpinner.getSelectedItem().toString());
                        displaySelectedFragment(fragment);
                        return true;
                    }
                });
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
        mMainMenu = menu;
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
            LoginManager.getInstance().logOut();
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
            } else
            if(id == R.id.action_organization) {
                if(mOrgsSpinner.getAdapter().getCount() > 0 &&
                        (!mOrgsSpinner.getSelectedItem().toString().equals("Inicio"))){
                    Fragment fragment = OrganizationFragment.newInstance(true);
                    displaySelectedFragment(fragment);
                }
            } else if(id == R.id.action_search_profile) {
                Intent search_profile = new Intent(this, SearchProfileActivity.class);
                startActivity(search_profile);
            } else if(id == R.id.action_about_channel){
                Fragment fragment = AboutChannelFragment.newInstance(mOrgsSpinner.getSelectedItem().toString(), removeUnicodeFromTitle(String.valueOf(getTitle())));
                displaySelectedFragment(fragment);
            }else if(id == R.id.action_bots){
                Fragment fragment = BotsFragment.newInstance(mOrgsSpinner.getSelectedItem().toString(), removeUnicodeFromTitle(String.valueOf(getTitle())));
                displaySelectedFragment(fragment);
            } else if(id == R.id.action_map){
                Intent map = new Intent(this, MapsActivity.class);
                map.putExtra("organization",mOrgsSpinner.getSelectedItem().toString());
                startActivity(map);
            }

        return super.onOptionsItemSelected(item);
    }

    private String removeUnicodeFromTitle(String title){
        int unicodePrivate = 0x1F512;
        int unicodePublic = 0x1F4E2;
        String publico = getEmojiByUnicode(unicodePublic);
        String privado = getEmojiByUnicode(unicodePrivate);

        String tituloReemplazadoSinPublico = title.replaceAll(publico, "");
        String tituloReemplazadoSinPrivado = tituloReemplazadoSinPublico.replaceAll(privado, "");

        return tituloReemplazadoSinPrivado.replaceFirst(" ","");
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
        ArrayList<String> organizationsList = new ArrayList<>();
        for(int i=0 ; i< dataAdapter.getCount() ; i++){
            organizationsList.add(dataAdapter.getItem(i));
        }
        Fragment fragment = JoinOrganizationFragment.newInstance(organizationsList);
        displaySelectedFragment(fragment);
    }

    public void createNewChannelFragment(final String organizationName, final String newChannelName, final String privado){
        NavigationView nvDrawer = (NavigationView) findViewById(R.id.nav_view);
        SubMenu channels = nvDrawer.getMenu().getItem(0).getSubMenu();
        int unicodePrivate = 0x1F512;
        int unicodePublic = 0x1F4E2;
        if(privado.equals("private")){
            channels.add(Menu.NONE, Menu.NONE, channels.size(),getEmojiByUnicode(unicodePrivate)+" "+newChannelName);
        } else {
            channels.add(Menu.NONE, Menu.NONE, channels.size(),getEmojiByUnicode(unicodePublic)+" "+newChannelName);
        }
        channels.getItem(channels.size()-1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
                onBackPressed();
                Fragment fragment = ChatChannelFragment.newInstance(organizationName,newChannelName,privado);
                displaySelectedFragment(fragment);
                return true;
            }
        });
        Fragment fragment = ChatChannelFragment.newInstance(organizationName,newChannelName,privado);
        displaySelectedFragment(fragment);
    }

    public void addOrganizationToAdapter(String organization){
        dataAdapter.add(organization);
        dataAdapter.notifyDataSetChanged();
    }

    public void createAddUserToPrivateChannelFragment(String organization, String channel){
        Fragment fragment = AddUserToPrivateChannelFragment.newInstance(organization,channel);
        displaySelectedFragment(fragment);
    }

    /**
     * Loads the specified fragment to the frame
     *
     * @param fragment
     * fragmento que se rellena
     */
    public void displaySelectedFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.flContent, fragment);
        fragmentTransaction.commit();
    }

    public void setupInvitationsSpinner(Spinner invitations_spinner) {
        List<String> list = new ArrayList<>();
        for(int i=0 ; i< dataAdapter.getCount() ; i++){
            list.add(dataAdapter.getItem(i));
        }
        ArrayAdapter<String> auxAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_invitations_item, list) {
            @NonNull
            public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                tfteko = Typeface.createFromAsset(getAssets(), "fonts/tekoregular.otf");
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTypeface(tfteko);
                v.setTextColor(getResources().getColor(R.color.colorAccent));
                v.setBackgroundResource(R.drawable.spinner_border);
                v.setTextSize(30);
                v.setPadding(30,0,0,0);
                return v;
            }

            public View getDropDownView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTypeface(tfteko);
                v.setTextColor(getResources().getColor(R.color.colorAccent));
                v.setTextSize(30);
                return v;
            }
        };
        auxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        invitations_spinner.setAdapter(auxAdapter);
    }
}
