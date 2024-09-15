package com.example.a431transit.presentation;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a431transit.BuildConfig;
import com.example.a431transit.R;
import com.example.a431transit.objects.TransitResponse;
import com.example.a431transit.objects.bus_arrivals.ArrivalInstance;
import com.example.a431transit.objects.bus_arrivals.route_schedules.RouteSchedule;
import com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.ScheduledStop;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.presentation.Dialogs.BusStopDialog;
import com.example.a431transit.presentation.Dialogs.CategoryDialogs;
import com.example.a431transit.presentation.Dialogs.SystemDialogs;
import com.example.a431transit.presentation.front_end_objects.ImageButtonWithTimer;
import com.example.a431transit.util.bus_stop_list.BusArrivalAdapter;
import com.example.a431transit.util.storage_managers.CategoriesManager;
import com.example.a431transit.api.transit_api.TransitAPIClient;
import com.example.a431transit.api.transit_api.TransitAPIService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BusArrivals extends AppCompatActivity {
    private CategoriesManager categoriesManager;
    private TransitAPIService transitService = TransitAPIClient.getApiService();
    private List<ArrivalInstance> arrivalInstances = new ArrayList<>();

    //The bus stop we are getting information from
    private BusStop busStop;

    //timer for the refresh button cooldown
    private CountDownTimer countDownTimer;

    //components that will be updated throughout multiple methods
    private RecyclerView busArrivalView;
    private BusArrivalAdapter busArrivalViewAdapter;
    private TextView emptyArrivalsView;
    private TextView busNameView;
    private LinearLayout horizontalScrollView;
    private ImageButtonWithTimer refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_arrivals);

        //get bus stop that was passed through by fragment
        busStop = getIntent().getSerializableExtra("BUS_STOP", BusStop.class);

        //TODO: communicate to logic layer instead
        //get user saved stops from storage
        categoriesManager = new CategoriesManager(this);

        //if user has saved this stop, get that copy of the data
        //stored version contains the nickname and filtered routes
        if (categoriesManager.isBusStopSaved(busStop)) {
            busStop = categoriesManager.getBusStop(busStop);
        }

        //display bus stop information on the screen
        initComponents();

        //get latest bus arrival times
        getArrivals();
    }

    //TODO: remove from presentation layer
    //Make a call to the Winnipeg Transit API to get the bus schedule for this stop.
    //Once a response has been received, render that data onto the screen
    private void getArrivals() {
        // Add a counter for retries
        final int[] retryCount = {0};
        Context context = this;

        //Make API Call
        Call<TransitResponse> call = transitService.fetchBusStopSchedule(busStop.getKey(), BuildConfig.TRANSIT_API_KEY);
        call.enqueue(new Callback<TransitResponse>() {
            @Override
            public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                if (response.isSuccessful()) {
                    //Extract the data from the response if there is any
                    TransitResponse transitResponse = response.body();

                    if (transitResponse == null) {
                        return;
                    }

                    //Render data once received
                    List<RouteSchedule> routeSchedules = transitResponse.getStopSchedule().getRouteSchedules();
                    prepareArrivalsList(routeSchedules);
                } else {
                    //Either the request amount limit has been reached or a server error occured
                    Log.e("transitService", "Error: " + response.code() + " - " + response.message());
                    SystemDialogs.showAlert(context, "Arrivals Error", "Could not fulfill your request. Please try again later");
                }
            }

            @Override
            public void onFailure(Call<TransitResponse> call, Throwable t) {
                Log.e("Arrivals", "Network request failed", t);
                t.printStackTrace();

                // Retry the request up to three times
                if (retryCount[0] < 3) {
                    retryCount[0]++;
                    Log.i("Arrivals", "Retrying network request (Retry " + retryCount[0] + ")");
                    call.clone().enqueue(this);
                } else {
                    Log.e("Arrivals", "Network request failed after three retries");
                    SystemDialogs.showAlert(context, "Arrivals Error", "Could not fulfill your request. Please try again later");
                }
            }

        });
    }

    //Once we received the scheduled times, update the list and display it
    private void prepareArrivalsList(List<RouteSchedule> routeSchedules) {
        //if user has chosen to filter routes, retrieve the filtered list
        List<String> filteredRoutes = busStop.getFilteredRoutes();

        //clear the previous schedule from the list
        arrivalInstances.clear();

        for (RouteSchedule routeSchedule : routeSchedules) {
            BusRoute busRoute = routeSchedule.getRoute();

            //Do not display the route if user chose to not have it displayed
            if (filteredRoutes != null && !filteredRoutes.contains(busRoute.getKey())) {
                continue;
            }

            //Create an arrival instance for each scheduled stop in each route
            for (ScheduledStop scheduledStop : routeSchedule.getScheduledStops()) {
                ArrivalInstance arrivalInstance = new ArrivalInstance(busRoute.getBadgeLabel(), busRoute.getBadgeStyle(), busRoute.getName(),
                        scheduledStop.getTimes().getDeparture().getScheduled(), scheduledStop.getTimes().getDeparture().getEstimated(), scheduledStop.isCancelled());

                arrivalInstances.add(arrivalInstance);
            }
        }

        //Display text for the user to signify that there are no buses scheduled to arrive at this stop
        if (arrivalInstances.isEmpty()) {
            emptyArrivalsView.setVisibility(View.VISIBLE);
            busArrivalView.setVisibility(View.GONE);
        } else {
            emptyArrivalsView.setVisibility(View.GONE);
            busArrivalView.setVisibility(View.VISIBLE);
        }

        //sort this list by their departure times, in the earliest order
        arrivalInstances.sort(Comparator.comparing(ArrivalInstance::getBusActualArrival));

        //update the list display
        busArrivalViewAdapter.updateData(arrivalInstances);
    }

    //If user has chosen to rename a bus stop, display a alert dialog with an editable text
    private void renameBusStopDialog() {
        BusStopDialog.renameBusStopDialog(this, busStop, () -> {
            //todo move to dialog and communicate with logic layer
            categoriesManager.updateBusStop(busStop);

            busNameView.setText(busStop.getName());
        });
    }

    private void addOrRemoveFromCategories() {
        CategoryDialogs.showAddOrRemoveFromCategoriesDialog(this, busStop, categoriesManager);
    }

    private void filterRoutes() {
        BusStopDialog.showFilterRoutesDialog(this, busStop, categoriesManager, transitService, () -> {
            // Update the display after filtering
            busStop.loadBusRoutes(getBaseContext(), transitService, horizontalScrollView);
            getArrivals();
        });
    }

    private void initComponents() {
        //get references to our components
        busNameView = findViewById(R.id.ArrivalsBusName);
        TextView busKeyView = findViewById(R.id.ArrivalsBusKey);
        ImageView busImageView = findViewById(R.id.ArrivalsImageView);
        ImageButton backButton = findViewById(R.id.ArrivalsBackBtn);
        ImageButton savedButton = findViewById(R.id.ArrivalsSavedBtn);
        ImageButton menuButton = findViewById(R.id.ArrivalsBusMenuBtn);
        horizontalScrollView = findViewById(R.id.ArrivalsScrollView);
        emptyArrivalsView = findViewById(R.id.emptyArrivalsView);

        //initialize the refresh button
        refreshButton = new ImageButtonWithTimer(findViewById(R.id.ArrivalsRefreshBtn));
        refreshButton.resetButton();

        //initialize the adapter for the recycler view
        busArrivalView = findViewById(R.id.busArrivalsView);
        busArrivalView.setLayoutManager(new LinearLayoutManager(this));
        busArrivalViewAdapter = new BusArrivalAdapter(this, arrivalInstances, transitService);
        busArrivalView.setAdapter(busArrivalViewAdapter);

        //get bus stop values
        String busName = busStop.getName();
        String busKey = "#" + String.valueOf(busStop.getKey());

        //set information displayed on screen
        busNameView.setText(busName);
        busKeyView.setText(busKey);
        busStop.loadImage(this, busImageView, "square");
        busStop.loadBusRoutes(this, transitService, horizontalScrollView);

        //todo communicate with logic layer
        //If bus stop is in the "Saved" category, then display that it is so
        if (categoriesManager.busStopInCategory("Saved", busStop)) {
            savedButton.setImageResource(R.drawable.icon_saved_stops_filled);
        } else {
            savedButton.setImageResource(R.drawable.icon_saved_stops);
        }

        //go back to the previous activity once user clicks on back button
        backButton.setOnClickListener(v -> onBackPressed());

        //expand text view to reveal full name
        busNameView.setOnClickListener(new View.OnClickListener() {
            boolean isExpanded = false;

            @Override
            public void onClick(View v) {
                isExpanded = !isExpanded;
                if (isExpanded) {
                    busNameView.setMaxLines(Integer.MAX_VALUE);
                } else {
                    busNameView.setMaxLines(1);
                }
            }
        });

        refreshButton.setOnClickListenerWithTimer(v -> {
            getArrivals();

            // For user feedback, make the list disappear for a short amount of time
            busArrivalView.setVisibility(View.INVISIBLE);

            // Start a timer to make the view reappear after 2 seconds
            new Handler().postDelayed(() -> {
                busArrivalView.setVisibility(View.VISIBLE);
            }, 2000);
        });

        //If user chooses to save/remove this bus stop, update the icon and storage
        savedButton.setOnClickListener(v -> {
            //todo communicate with logic layer
            if (categoriesManager.busStopInCategory("Saved", busStop)) {
                categoriesManager.removeStopFromCategory("Saved", busStop);
                savedButton.setImageResource(R.drawable.icon_saved_stops);

            } else {
                categoriesManager.addStopToCategory("Saved", busStop);
                savedButton.setImageResource(R.drawable.icon_saved_stops_filled);
            }
        });

        menuButton.setOnClickListener(view -> BusStopDialog.showEditBusMenu(BusArrivals.this,view,
                this::addOrRemoveFromCategories, this::renameBusStopDialog, this::filterRoutes));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel the timer to avoid memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        // Set the result to OK
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}