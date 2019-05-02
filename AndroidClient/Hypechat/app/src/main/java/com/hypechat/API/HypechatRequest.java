package com.hypechat.API;

import com.hypechat.models.LoginBody;
import com.hypechat.models.ProfileBodySave;
import com.hypechat.models.ProfileBodyLoad;
import com.hypechat.models.RegisterBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface HypechatRequest {

    public static final String BASE_URL = "https://hypechat-taller2-staging.herokuapp.com/";

    @POST("login")
    Call<Void> login(@Body LoginBody loginBody);

    @POST("register")
    Call<Void> register(@Body RegisterBody registerBody);

    @POST("profile")
    Call<Void> saveProfile(@Body ProfileBodySave profileBody);

    @GET("profile/{username}")
    Call<ProfileBodyLoad> getProfileInformation(@Path("username") String username);

    @DELETE("logout")
    Call<Void> logout();
}
