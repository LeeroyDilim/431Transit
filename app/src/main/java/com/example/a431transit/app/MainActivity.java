package com.example.a431transit.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.example.a431transit.R;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.util.api_communication.TransitAPIClient;
import com.example.a431transit.util.api_communication.TransitAPIService;
import com.example.a431transit.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    //keep track of the list of bus stops the user searched for in the Search Fragment
    List<BusStop> busStopsList = null;

    //id of currentFragment that is being displayed
    int currentFragment;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        initComponents();
    }

    private void initComponents() {
        //initialize a connection with the WPG Transit API
        TransitAPIService transitService = TransitAPIClient.getApiService();

        //get and init components
        SavedStopsFragment savedStopsFragment = new SavedStopsFragment(transitService);
        SearchFragment searchFragment = new SearchFragment(transitService);
        BottomNavigationView bottomNavigationView3 = findViewById(R.id.bottomNavigationView3);

        //On startup, the Saved Stop fragment is displayed
        replaceFragment(savedStopsFragment);
        bottomNavigationView3.getMenu().getItem(0).setIcon(R.drawable.icon_saved_stops_filled);

        //todo: move to separate method
        //Once a user has chosen to move to another page from the bottom navigation view, display the appropriate fragment
        bottomNavigationView3.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            //Do not do anything if user has clicked to display the fragment already displayed
            if (itemId == currentFragment) {
                return false;
            }

            currentFragment = itemId;

            //If the current fragment was the search page, retrieve the searched list to display once the user comes back
            if (currentFragment == bottomNavigationView3.getMenu().getItem(2).getItemId()) {
                busStopsList = searchFragment.getBusStops();
            }

            //display the fragment requested by the user
            if (itemId == R.id.saved_stops) {
                replaceFragment(savedStopsFragment);

                //Update Icons
                item.setIcon(R.drawable.icon_saved_stops_filled);
                bottomNavigationView3.getMenu().getItem(1).setIcon(R.drawable.icon_map);
                bottomNavigationView3.getMenu().getItem(2).setIcon(R.drawable.icon_search);
            } else if (itemId == R.id.map) {
                replaceFragment(new MapFragment(transitService));

                //Update Icons
                item.setIcon(R.drawable.icon_map_filled);
                bottomNavigationView3.getMenu().getItem(0).setIcon(R.drawable.icon_saved_stops);
                bottomNavigationView3.getMenu().getItem(2).setIcon(R.drawable.icon_search);
            } else if (itemId == R.id.search) {
                replaceFragment(searchFragment);

                //display the list that was previously searched by the user
                searchFragment.setBusStops(busStopsList);

                //Update Icons
                item.setIcon(R.drawable.icon_search_filled);
                bottomNavigationView3.getMenu().getItem(0).setIcon(R.drawable.icon_saved_stops);
                bottomNavigationView3.getMenu().getItem(1).setIcon(R.drawable.icon_map);
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}