package com.hypechat.API;

import com.hypechat.models.ChangePasswordBody;
import com.hypechat.models.ChannelCreateBody;
import com.hypechat.models.ChannelListBody;
import com.hypechat.models.InvitationsBody;
import com.hypechat.models.LoginBody;
import com.hypechat.models.OrganizationCreateBody;
import com.hypechat.models.OrganizationListBody;
import com.hypechat.models.ProfileBodySave;
import com.hypechat.models.ProfileBodyLoad;
import com.hypechat.models.RegisterBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    @POST("organization")
    Call<Void> createOrganization(@Body OrganizationCreateBody organizationCreateBody);

    @POST("organization/{organization_name}/channels")
    Call<Void> createChannel(@Path("organization_name") String name, @Body ChannelCreateBody channelCreateBody);

    @GET("profile/{username}/organizations")
    Call<OrganizationListBody> getUserOrganizations(@Path("username") String username);

    @GET("organization/{organization_name}/channels")
    Call<ChannelListBody> getOrganizationChannels(@Path("organization_name") String organization);

    @GET("profile/{username}/invitations")
    Call<Void> getUserInvitations(@Path("username") String username);

    @POST("recovery")
    Call<InvitationsBody> passwordRecovery(@Body InvitationsBody passwordRestoreBody);

    @POST("password")
    Call<ChangePasswordBody> changePassword(@Header("Authorization") String token, @Body ChangePasswordBody changePasswordBody);
}
