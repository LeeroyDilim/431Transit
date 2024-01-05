package com.example.a431transit.util.api_communication;

import java.util.concurrent.TimeUnit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;

public class TransitAPIClient {
    private static Retrofit retrofit;
    private static TransitAPIService apiService;

    private TransitAPIClient() {
        // Private constructor to prevent instantiation
    }

    public static TransitAPIService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(TransitAPIService.class);
        }
        return apiService;
    }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.SECONDS) // Set the connection timeout
                    .readTimeout(2, TimeUnit.SECONDS)    // Set the read timeout
                    .writeTimeout(2, TimeUnit.SECONDS)   // Set the write timeout
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(TransitAPIService.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
