package com.example.a431transit.application;

import android.content.Context;

import java.io.File;

public class AppConstants {
    private static final String EXPANDED_LIST_FILE_NAME = "expanded_list_state.json";
    private static final int REQUEST_CODE = 1;
    private static Context context;

    public static String getExpandedListFileName() {
        return EXPANDED_LIST_FILE_NAME;
    }
    public static int getRequestCode() {
        return REQUEST_CODE;
    }

    public static File getFileDir(){
        if (context == null){
            return null;
        }

        return context.getExternalFilesDir(null);
    }

    public static void setContext(Context context) {
        AppConstants.context = context;
    }


    //FRONT END
    public static int getSearchRadius() {
        return 500;
    }

    public class CircleImage {
        public static final String NAME = "CIRCLE";
        public static final int WIDTH = 300;
        public static final int HEIGHT = 300;
        public static final int ZOOM = 18;
    }

    public class RectangleImage {
        public static final String NAME = "RECTANGLE";
        public static final int WIDTH = 900;
        public static final int HEIGHT = 400;
        public static final int ZOOM = 17;
    }

}
