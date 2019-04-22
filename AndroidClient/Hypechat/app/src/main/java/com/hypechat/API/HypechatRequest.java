package com.hypechat.API;

import com.hypechat.models.LoginBody;
import com.hypechat.models.RegisterBody;
import com.hypechat.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface HypechatRequest {

    // Cambiar host por "10.0.0.2" para Genymotion.
    // Cambiar host por "10.0.0.3" para AVD.
    // Cambiar host por IP de PC para dispositivo real.
    //public static final String BASE_URL = "http://10.0.0.2/api.hypechat.com/v1/";
    public static final String BASE_URL = "http://virtserver.swaggerhub.com/FabrizioCozza/Hypechat/1.0.0/";

    @POST("users/login")
    Call<User> login(@Body LoginBody loginBody);

    @POST("users/register")
    Call<User> register(@Body RegisterBody registerBody);
}
