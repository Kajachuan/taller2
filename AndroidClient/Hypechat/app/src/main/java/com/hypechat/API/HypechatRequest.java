package com.hypechat.API;

import com.hypechat.models.invitations.AcceptInvitationBody;
import com.hypechat.models.auth.ChangePasswordBody;
import com.hypechat.models.channels.ChannelCreateBody;
import com.hypechat.models.channels.ChannelListBody;
import com.hypechat.models.invitations.InvitationsBody;
import com.hypechat.models.invitations.InvitationsListBody;
import com.hypechat.models.auth.LoginBody;
import com.hypechat.models.messages.Message;
import com.hypechat.models.messages.MessageBodyGet;
import com.hypechat.models.messages.MessageBodyList;
import com.hypechat.models.messages.MessageBodyPost;
import com.hypechat.models.organizations.OrganizationCreateBody;
import com.hypechat.models.organizations.OrganizationListBody;
import com.hypechat.models.profile.ProfileBodySave;
import com.hypechat.models.profile.ProfileBodyLoad;
import com.hypechat.models.auth.RegisterBody;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HypechatRequest {

    public static final String BASE_URL = "https://hypechat-taller2-staging.herokuapp.com";

    @POST("/login")
    Call<Void> login(@Body LoginBody loginBody);

    @POST("/register")
    Call<Void> register(@Body RegisterBody registerBody);

    @POST("/profile")
    Call<Void> saveProfile(@Body ProfileBodySave profileBody);

    @GET("/profile/{username}")
    Call<ProfileBodyLoad> getProfileInformation(@Path("username") String username);

    @DELETE("/logout")
    Call<Void> logout();

    @POST("/organization")
    Call<Void> createOrganization(@Body OrganizationCreateBody organizationCreateBody);

    @POST("/organization/{organization_name}/channels")
    Call<Void> createChannel(@Path("organization_name") String name, @Body ChannelCreateBody channelCreateBody);

    @GET("/profile/{username}/organizations")
    Call<OrganizationListBody> getUserOrganizations(@Path("username") String username);

    @GET("/organization/{organization_name}/channels")
    Call<ChannelListBody> getOrganizationChannels(@Path("organization_name") String organization);

    @GET("/profile/{username}/invitations")
    Call<InvitationsListBody> getUserInvitations(@Path("username") String username);

    @POST("/recovery")
    Call<InvitationsBody> passwordRecovery(@Body InvitationsBody passwordRestoreBody);

    @POST("/password")
    Call<ChangePasswordBody> changePassword(@Header("Authorization") String token, @Body ChangePasswordBody changePasswordBody);

    @POST("/organization/{organization_name}/invite")
    Call<Void> sendInvitation(@Path("organization_name") String name, @Body InvitationsBody passwordRestoreBody);

    @POST("/organization/{organization_name}/accept-invitation")
    Call<Void>  acceptInvitation(@Path("organization_name") String name, @Body AcceptInvitationBody acceptInvitationBody);

    @GET("/organization/{organization_name}/{channel_name}/messages")
    Call<MessageBodyList> getMessages(@Path("organization_name") String organization_name,
                                      @Path("channel_name") String channel_name,
                                      @Query("init") int init,
                                      @Query("end") int end);

    @POST("/organization/{organization_name}/{channel_name}/message")
    Call<Void> sendMessage(@Path("organization_name") String organization_name,
                                      @Path("channel_name") String channel_name,
                                      @Body MessageBodyPost messageBody);
}
