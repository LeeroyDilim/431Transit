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

import com.example.a431transit.BuildConfig;
import com.example.a431transit.R;
import com.example.a431transit.objects.TransitResponse;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.presentation.Dialogs.SystemDialogs;
import com.example.a431transit.util.bus_stop_list.BusStopAdapter;
import com.example.a431transit.util.bus_stop_list.BusStopViewInterface;
import com.example.a431transit.api.transit_api.TransitAPIService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    //todo communicate through the logic layer
    //Search for bus stops given a user's text input
    private void searchBusStops(String query) {
        // Add a counter for retries
        final int[] retryCount = {0};
        Call<TransitResponse> call;

        //if user inputs nothing, clear the list
        if (query.isEmpty()) {
            busStops = new ArrayList<>();
            busStopAdapter.updateData(busStops);
            return;
        }

        //Check if user is searching by bus stop name or key
        if (query.matches("\\d+")) {
            call = transitService.fetchBusStopsByKey(Integer.parseInt(query), BuildConfig.TRANSIT_API_KEY);
        } else if (query.matches("#\\d+")) {
            //if user searches for a bus key with # at the beginning of the string
            call = transitService.fetchBusStopsByKey(Integer.parseInt(query.substring(1)), BuildConfig.TRANSIT_API_KEY);
        } else {
            //Have to do it like this as retrofit does not allow colons in the urls
            String baseUrl = "https://api.winnipegtransit.com/v3/";
            String path = "stops:" + query + ".json";
            String apiUrl = baseUrl + path;

            call = transitService.fetchBusStopsByName(apiUrl, BuildConfig.TRANSIT_API_KEY);
        }

        //make the api call
        call.enqueue(new Callback<TransitResponse>() {
            @Override
            public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                if (response.isSuccessful()) {
                    TransitResponse transitResponse = response.body();

                    //get the data from the API response
                    busStops = transitResponse.getStops();

                    //searching by key only returns one stop
                    if (busStops == null) {
                        busStops = new ArrayList<BusStop>();
                        busStops.add(transitResponse.getStop());
                    }

                    //if no stops exist with the searched name, then display text signifying so
                    //if not update the list with the newly acquired information
                    if (busStops.isEmpty()) {
                        emptySearchView.setVisibility(View.VISIBLE);
                        busStopView.setVisibility(View.GONE);
                    } else {
                        emptySearchView.setVisibility(View.GONE);
                        busStopView.setVisibility(View.VISIBLE);

                        busStopAdapter.updateData(busStops);
                    }
                } else {
                    //signify that there are no bus stops to display with the search
                    emptySearchView.setVisibility(View.GONE);
                    busStopView.setVisibility(View.VISIBLE);

                    busStops = new ArrayList<BusStop>();
                    busStopAdapter.updateData(busStops);

                    //if failure did not come from a user entering a invalid string
                    if (response.code() != 404) {
                        Log.e("transitService", "Error: " + response.code() + " - " + response.message());
                        SystemDialogs.showAlert(getContext(), "Search Error", "Could not fulfill your request. Please try again later");
                    }
                }
            }

            @Override
            public void onFailure(Call<TransitResponse> call, Throwable t) {
                Log.e("transitService", "Network request failed", t);
                t.printStackTrace();

                // Retry the request up to three times
                if (retryCount[0] < 3) {
                    retryCount[0]++;
                    Log.i("transitService", "Retrying network request (Retry " + retryCount[0] + ")");
                    call.clone().enqueue(this);
                } else {
                    Log.e("transitService", "Network request failed after three retries");
                    SystemDialogs.showAlert(getContext(), "Search Error", "Could not fulfill your request. Please try again later");

                    //signify that there are no bus stops to display with the failed search
                    emptySearchView.setVisibility(View.GONE);
                    busStopView.setVisibility(View.VISIBLE);

                    busStops = new ArrayList<BusStop>();
                    busStopAdapter.updateData(busStops);
                }
            }

        });
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