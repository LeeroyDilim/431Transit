package com.example.a431transit.api;

import android.util.Log;

public class APIManager {
    private static final String TAG = "APIManager";
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
                Log.e(TAG, "API call failed. Attempt " + attempt + " of " + MAX_RETRIES, e);
                if (attempt >= MAX_RETRIES) {
                    Log.e(TAG, "Max retries reached. API call failed.");
                }
            }
        }
    }
}
