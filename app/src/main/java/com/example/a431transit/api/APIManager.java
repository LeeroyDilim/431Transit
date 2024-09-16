package com.example.a431transit.api;

import android.util.Log;

import java.util.function.Consumer;

public class APIManager {
    private static final String TAG = "APIManager";
    private static final int MAX_RETRIES = 3;

    public static void executeWithRetry(Runnable apiCall, Consumer<String> onError) {
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRIES && !success) {
            try {
                apiCall.run();
                success = true;
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    onError.accept("Network request failed after limited amount of retries.");
                }
            }
        }
    }
}
