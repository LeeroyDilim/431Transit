package com.example.a431transit.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;

import com.example.a431transit.R;
import com.example.a431transit.model.stops.BusStop;
import com.example.a431transit.util.TransitAPIClient;
import com.example.a431transit.util.TransitAPIService;
import com.example.a431transit.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    List<BusStop> busStopsList = null;
    int currentFragment;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        TransitAPIService transitService = TransitAPIClient.getApiService();

        SavedStopsFragment savedStopsFragment = new SavedStopsFragment(transitService);
        MapFragment mapFragment = new MapFragment(transitService);
        SearchFragment searchFragment = new SearchFragment(transitService);

        replaceFragment(savedStopsFragment);

        BottomNavigationView bottomNavigationView3 = findViewById(R.id.bottomNavigationView3);
        bottomNavigationView3.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if(itemId == currentFragment)
            {
                return false;
            }

            currentFragment = itemId;

            if (currentFragment == bottomNavigationView3.getMenu().getItem(2).getItemId())
            {
                busStopsList = searchFragment.getBusStops();
            }

            if(itemId == R.id.saved_stops){
                replaceFragment(savedStopsFragment);

                //Update Icons
                item.setIcon(R.drawable.saved_stops_icon_filled);
                bottomNavigationView3.getMenu().getItem(1).setIcon(R.drawable.map_icon);
                bottomNavigationView3.getMenu().getItem(2).setIcon(R.drawable.search_icon);
            } else if (itemId == R.id.map) {
                replaceFragment(mapFragment);

                //Update Icons
                item.setIcon(R.drawable.map_icon_filled);
                bottomNavigationView3.getMenu().getItem(0).setIcon(R.drawable.saved_stops_icon);
                bottomNavigationView3.getMenu().getItem(2).setIcon(R.drawable.search_icon);
            } else if (itemId == R.id.search) {
                replaceFragment(searchFragment);
                searchFragment.setBusStops(busStopsList);
                //Update Icons
                item.setIcon(R.drawable.search_icon_filled);
                bottomNavigationView3.getMenu().getItem(0).setIcon(R.drawable.saved_stops_icon);
                bottomNavigationView3.getMenu().getItem(1).setIcon(R.drawable.map_icon);
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}