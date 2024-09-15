package com.example.a431transit.presentation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.a431transit.R;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.api.transit_api.TransitAPIClient;
import com.example.a431transit.api.transit_api.TransitAPIService;
import com.example.a431transit.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    //todo make api call retries be delegated to a single class
    ActivityMainBinding binding;

    TransitAPIService transitService;
    SavedStopsFragment savedStopsFragment;
    SearchFragment searchFragment;
    BottomNavigationView bottomNavigationView;

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
        transitService = TransitAPIClient.getApiService();

        //get and init components
        savedStopsFragment = new SavedStopsFragment(transitService);
        searchFragment = new SearchFragment(transitService);
        bottomNavigationView = findViewById(R.id.bottomNavigationView3);

        //On startup, the Saved Stop fragment is displayed
        replaceFragment(savedStopsFragment);
        bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.icon_saved_stops_filled);

        //Once a user has chosen to move to another page from the bottom navigation view, display the appropriate fragment
        bottomNavigationView.setOnItemSelectedListener(this::determineFragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    public static void startIntent(Context context, BusStop busStop){
        Intent intent = new Intent(context, BusArrivals.class);

        intent.putExtra("BUS_STOP", busStop);

        context.startActivity(intent);
    }

    public boolean determineFragment(MenuItem item){
        int itemId = item.getItemId();

        //Do not do anything if user has clicked to display the fragment already displayed
        if (itemId == currentFragment) {
            return false;
        }

        currentFragment = itemId;

        //If the current fragment was the search page, retrieve the searched list to display once the user comes back
        if (currentFragment == bottomNavigationView.getMenu().getItem(2).getItemId()) {
            busStopsList = searchFragment.getBusStops();
        }

        //display the fragment requested by the user
        if (itemId == R.id.saved_stops) {
            replaceFragment(savedStopsFragment);

            //Update Icons
            item.setIcon(R.drawable.icon_saved_stops_filled);
            bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.icon_map);
            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.icon_search);
        } else if (itemId == R.id.map) {
            replaceFragment(new MapFragment(transitService));

            //Update Icons
            item.setIcon(R.drawable.icon_map_filled);
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.icon_saved_stops);
            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.icon_search);
        } else if (itemId == R.id.search) {
            replaceFragment(searchFragment);

            //display the list that was previously searched by the user
            searchFragment.setBusStops(busStopsList);

            //Update Icons
            item.setIcon(R.drawable.icon_search_filled);
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.icon_saved_stops);
            bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.icon_map);
        }

        return true;
    }
}