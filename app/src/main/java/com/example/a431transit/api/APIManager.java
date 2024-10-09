package com.example.a431transit.api;

import android.util.Log;

import java.util.function.Consumer;

public class APIManager {
    private static final int MAX_RETRIES = 3;

    public static void executeWithRetry(Runnable apiCall) {
        int attempt = 0;
        boolean success = false;

        while (attempt < MAX_RETRIES && !success) {
            try {
                apiCall.run();
                success = true;
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("Failed to fetch data after retrying up to " + MAX_RETRIES + "times");
                }
            }
        }
    }
}
