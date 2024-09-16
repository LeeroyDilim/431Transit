package com.example.a431transit.presentation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a431transit.R;
import com.example.a431transit.logic.BusStopHandler;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.presentation.front_end_objects.bus_stop_list.BusStopAdapter;
import com.example.a431transit.presentation.front_end_objects.bus_stop_list.BusStopViewInterface;
import com.example.a431transit.api.transit_api.TransitAPIService;

import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;

public class SearchFragment extends Fragment implements BusStopViewInterface {
    private TransitAPIService transitService;
    List<BusStop> busStops; //tracks the results of the most recent search

    //Components that will be referred to in multiple methods
    private SearchView searchView;
    private TextView emptySearchView;
    private RecyclerView busStopView;
    private BusStopAdapter busStopAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }

    public SearchFragment(TransitAPIService transitService) {
        this.transitService = transitService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initComponents(view);

        return view;
    }

    private void searchBusStops(String query) {
        //clear the list if the user inputs nothing
        if (query.isEmpty()) {
            busStops = new ArrayList<>();
            busStopAdapter.updateData(busStops);
            return;
        }

        //determine which API call to make based on the query
        if (query.matches("\\d+")) {
            BusStopHandler.fetchBusStopsByKey(Integer.parseInt(query), this::updateBusStopList, this::showError);
        } else if (query.matches("#\\d+")) {
            BusStopHandler.fetchBusStopsByKey(Integer.parseInt(query.substring(1)), this::updateBusStopList, this::showError);
        } else {
            BusStopHandler.fetchBusStopsByName(query, this::updateBusStopList, this::showError);
        }
    }

    private void showError(String s) {
        Log.e("error", s);
    }

    private void updateBusStopList(List<BusStop> busStops) {
        this.busStops = busStops;

        // If no stops exist, show empty view
        if (busStops == null || busStops.isEmpty()) {
            emptySearchView.setVisibility(View.VISIBLE);
            busStopView.setVisibility(View.GONE);
        } else {
            emptySearchView.setVisibility(View.GONE);
            busStopView.setVisibility(View.VISIBLE);
            busStopAdapter.updateData(busStops);
        }
    }


    //Once a user has clicked a bus stop, create a new screen displaying the arrival times for that bus stop
    @Override
    public void onItemClick(int position) {
        if (busStops.size() > 0 && position >= 0 & position < busStops.size()) {
            MainActivity.startIntent(getContext(), busStops.get(position));
        }
    }

    public List<BusStop> getBusStops() {
        return busStops;
    }

    public void setBusStops(List<BusStop> busStops) {
        if (busStops == null) {
            return;
        }

        this.busStops = busStops;

        //update list with the newly set busStops
        busStopAdapter.updateData(busStops);
    }

    private void initComponents(View view) {
        searchView = view.findViewById(R.id.searchView);
        emptySearchView = view.findViewById(R.id.emptySearchView);

        busStopView = view.findViewById(R.id.searchedBusStopView);
        busStopView.setLayoutManager(new LinearLayoutManager(requireContext()));
        busStopAdapter = new BusStopAdapter(this, requireContext(), busStops, transitService);
        busStopView.setAdapter(busStopAdapter);

        // Set up the SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchBusStops(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();

        searchView.clearFocus();
    }
}