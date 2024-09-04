package com.example.a431transit.application;

import android.content.Context;

public class Conversion {
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }
}
