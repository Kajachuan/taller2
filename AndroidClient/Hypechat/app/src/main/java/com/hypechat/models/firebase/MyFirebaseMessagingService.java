package com.hypechat.models.firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hypechat.API.APIError;
import com.hypechat.API.ErrorUtils;
import com.hypechat.API.HypechatRequest;
import com.hypechat.MainActivity;
import com.hypechat.R;
import com.hypechat.cookies.AddCookiesInterceptor;
import com.hypechat.cookies.ReceivedCookiesInterceptor;
import com.hypechat.models.messages.Message;
import com.hypechat.prefs.SessionPrefs;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.widget.Toast.LENGTH_LONG;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG = "NOTIFICACION";

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
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
        final HypechatRequest mHypechatRequest = mMainRestAdapter.create(HypechatRequest.class);
        final String username = SessionPrefs.get(getApplicationContext()).getUsername();
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
                                Toast.makeText(getApplicationContext(), t.getMessage(), LENGTH_LONG).show();
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
            Toast.makeText(getApplicationContext(), error, LENGTH_LONG).show();
        } else {
            Log.d("TOKEN", "ENVIO OK");
        }
    }

    private static ArrayList<Long> alreadyNotifiedTimestamps = new ArrayList<>();

    // Workaround for Firebase duplicate pushes
    private boolean isDuplicate(String timestamp) {
        java.sql.Timestamp ts = java.sql.Timestamp.valueOf(timestamp);
        long tsTime = ts.getTime();
        if (alreadyNotifiedTimestamps.contains(tsTime)) {
            alreadyNotifiedTimestamps.remove(tsTime);
            return true;
        } else {
            alreadyNotifiedTimestamps.add(tsTime);
        }

        return false;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Intent intent = new Intent("Data");
            intent.putExtra("message", remoteMessage.getData().get("message"));
            intent.putExtra("sender", remoteMessage.getData().get("sender"));
            intent.putExtra("timestamp", remoteMessage.getData().get("timestamp"));
            intent.putExtra("type",remoteMessage.getData().get("type"));

            if(!isDuplicate(remoteMessage.getData().get("timestamp"))){
                LocalBroadcastManager.getInstance(getApplicationContext())
                        .sendBroadcast(intent);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
           sendNotification(remoteMessage.getNotification());
        }
    }

    private void sendNotification(RemoteMessage.Notification remoteMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        @SuppressWarnings("ConstantConditions")
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.logo_transparent)
                .setContentTitle(remoteMessage.getTitle())
                .setContentText(remoteMessage.getBody())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent);;

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }




}
