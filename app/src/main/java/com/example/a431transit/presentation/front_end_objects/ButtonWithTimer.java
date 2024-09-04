package com.example.a431transit.presentation.front_end_objects;

import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.a431transit.R;

public class ButtonWithTimer {
    //timer for the refresh button cooldown
    private CountDownTimer countDownTimer;
    private ImageButton imageButton;

    public ButtonWithTimer(ImageButton imageButton){
        this.imageButton = imageButton;
    }

    //If user has refreshed the arrivals list,
    //prevent them from refreshing the list again for a set amount of time
    private void startButtonTimer(long millisInFuture) {
        countDownTimer = new CountDownTimer(millisInFuture, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                // Timer finished, enable the button and reset its state
                resetRefreshButton();
            }
        };

        countDownTimer.start();
    }

    private void resetRefreshButton() {
        imageButton.setEnabled(true);
        imageButton.setImageResource(R.drawable.icon_refresh_enabled);
    }
}
