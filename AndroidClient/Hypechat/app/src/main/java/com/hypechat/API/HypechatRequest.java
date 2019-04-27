package com.hypechat.API;

import com.hypechat.models.LoginBody;
import com.hypechat.models.RegisterBody;
import com.hypechat.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HypechatRequest {

    public static final String BASE_URL = "https://hypechat-taller2-staging.herokuapp.com/";

    @POST("login")
    Call<Void> login(@Body LoginBody loginBody);

    @POST("register")
    Call<Void> register(@Body RegisterBody registerBody);
}
