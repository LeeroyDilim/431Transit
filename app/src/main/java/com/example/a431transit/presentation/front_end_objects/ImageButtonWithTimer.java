package com.example.a431transit.presentation.front_end_objects;

import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;

import com.example.a431transit.R;

public class ImageButtonWithTimer {
    //timer for the refresh button cooldown
    private CountDownTimer countDownTimer;
    private long delayMillis = 5000;
    private ImageButton imageButton;

    public ImageButtonWithTimer(ImageButton imageButton){
        this.imageButton = imageButton;
    }

    // Method to set up the button listener with the timer
    public void setOnClickListenerWithTimer(final View.OnClickListener listener) {
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the actual click listener
                listener.onClick(v);

                // Disable the button and change the icon
                imageButton.setEnabled(false);
                imageButton.setImageResource(R.drawable.icon_refresh_disabled);

                // Start the timer to re-enable the button
                startButtonTimer(delayMillis);
            }
        });
    }

    //If user has refreshed the arrivals list,
    //prevent them from refreshing the list again for a set amount of time
    private void startButtonTimer(long millisInFuture) {
        //if the user is spamming the button, reduce the amount of requests
        //they're making. One request per 10 seconds
        imageButton.setEnabled(false);
        imageButton.setImageResource(R.drawable.icon_refresh_disabled);

        countDownTimer = new CountDownTimer(millisInFuture, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                // Timer finished, enable the button and reset its state
                resetButton();
            }
        };

        countDownTimer.start();
    }

    public void resetButton() {
        imageButton.setEnabled(true);
        imageButton.setImageResource(R.drawable.icon_refresh_enabled);
    }
}
