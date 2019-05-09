package com.hypechat.cookies;

import com.hypechat.prefs.SessionPrefs;

import java.io.IOException;
import java.util.HashSet;

import okhttp3.Interceptor;
import okhttp3.Response;

public class ReceivedCookiesInterceptor implements Interceptor{
        @SuppressWarnings("NullableProblems")
        @Override
        public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        if (!originalResponse.headers("Set-Cookie").isEmpty()) {
            HashSet<String> cookies = new HashSet<>(originalResponse.headers("Set-Cookie"));
            SessionPrefs.setCookies(ApplicationContextProvider.getContext(), cookies);
        }
        return originalResponse;
    }
}
