package com.example.a431transit.presentation;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a431transit.R;
import com.example.a431transit.api.google_static_maps_api.GoogleStaticMapsClient;
import com.example.a431transit.api.transit_api.TransitAPIClient;
import com.example.a431transit.api.transit_api.TransitAPIService;
import com.example.a431transit.application.AppConstants;
import com.example.a431transit.logic.BusStopHandler;
import com.example.a431transit.logic.CategoryHandler;
import com.example.a431transit.logic.SavedBusStopHandler;
import com.example.a431transit.objects.bus_arrivals.route_schedules.RouteSchedule;
import com.example.a431transit.objects.bus_arrivals.route_schedules.scheduled_stops.ScheduledStop;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.presentation.app_dialogs.BusStopDialog;
import com.example.a431transit.presentation.app_dialogs.CategoryDialogs;
import com.example.a431transit.presentation.app_dialogs.SystemDialogs;
import com.example.a431transit.presentation.front_end_objects.ArrivalInstanceView;
import com.example.a431transit.presentation.front_end_objects.BusRouteHolder;
import com.example.a431transit.presentation.front_end_objects.ImageButtonWithTimer;
import com.example.a431transit.presentation.front_end_objects.bus_stop_list.BusArrivalAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BusArrivals extends AppCompatActivity {
    private TransitAPIService transitService = TransitAPIClient.getApiService();
    private List<ArrivalInstanceView> arrivalInstanceViews = new ArrayList<>();

    //The bus stop we are getting information from
    private BusStop busStop;

    //timer for the refresh button cooldown
    private CountDownTimer countDownTimer;

    //components that will be updated throughout multiple methods
    private RecyclerView busArrivalView;
    private BusArrivalAdapter busArrivalViewAdapter;
    private TextView emptyArrivalsView;
    private TextView busNameView;
    private BusRouteHolder busRouteHolder;
    private ImageButtonWithTimer refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_arrivals);

        //get bus stop that was passed through by fragment
        busStop = getIntent().getSerializableExtra("BUS_STOP", BusStop.class);

        //if user has saved this stop, get that copy of the data
        //stored version contains the nickname and filtered routes
        if (SavedBusStopHandler.isBusStopSaved(busStop)) {
            busStop = SavedBusStopHandler.getBusStop(Integer.toString(busStop.getKey()));
        }

        //display bus stop information on the screen
        initComponents();

        //get latest bus arrival times
        try {
            BusStopHandler.fetchBusStopSchedule(busStop, this::prepareArrivalsList);
        } catch (RuntimeException e){
            SystemDialogs.showDefaultAlert(getBaseContext(),"We had trouble fetching your schedule.");
        }
    }

    private void showErrorDialog(String s) {
    }

    //Once we received the scheduled times, update the list and display it
    private void prepareArrivalsList(List<RouteSchedule> routeSchedules) {
        //if user has chosen to filter routes, retrieve the filtered list
        List<String> filteredRoutes = busStop.getFilteredRoutes();

        //clear the previous schedule from the list
        arrivalInstanceViews.clear();

        for (RouteSchedule routeSchedule : routeSchedules) {
            BusRoute busRoute = routeSchedule.getRoute();

            //Do not display the route if user chose to not have it displayed
            if (filteredRoutes != null && !filteredRoutes.contains(busRoute.getKey())) {
                continue;
            }

            //Create an arrival instance for each scheduled stop in each route
            for (ScheduledStop scheduledStop : routeSchedule.getScheduledStops()) {
                ArrivalInstanceView arrivalInstanceView = new ArrivalInstanceView(busRoute.getBadgeLabel(), busRoute.getBadgeStyle(), busRoute.getName(),
                        scheduledStop.getTimes().getDeparture().getScheduled(), scheduledStop.getTimes().getDeparture().getEstimated(), scheduledStop.isCancelled());

                arrivalInstanceViews.add(arrivalInstanceView);
            }
        }

        //Display text for the user to signify that there are no buses scheduled to arrive at this stop
        if (arrivalInstanceViews.isEmpty()) {
            emptyArrivalsView.setVisibility(View.VISIBLE);
            busArrivalView.setVisibility(View.GONE);
        } else {
            emptyArrivalsView.setVisibility(View.GONE);
            busArrivalView.setVisibility(View.VISIBLE);
        }

        //sort this list by their departure times, in the earliest order
        arrivalInstanceViews.sort(Comparator.comparing(ArrivalInstanceView::getBusActualArrival));

        //update the list display
        busArrivalViewAdapter.updateData(arrivalInstanceViews);
    }

    //If user has chosen to rename a bus stop, display a alert dialog with an editable text
    private void renameBusStopDialog() {
        BusStopDialog.renameBusStopDialog(this, busStop, () -> {
            busNameView.setText(busStop.getName());
        });
    }

    private void addOrRemoveFromCategories() {
        CategoryDialogs.showAddOrRemoveFromCategoriesDialog(this, busStop);
    }

    private void filterRoutes() {
        BusStopDialog.showFilterRoutesDialog(this, busStop, () -> {
            // Update the display after filtering
            try {
                BusStopHandler.fetchBusRoutes(busStop, busRouteHolder::updateRouteView);
                BusStopHandler.fetchBusStopSchedule(busStop, this::prepareArrivalsList);
            } catch (RuntimeException e) {
                SystemDialogs.showDefaultAlert(getBaseContext(),"We had trouble getting you the latest bus stop information.");
            }
        });
    }

    private void onError(String s) {
    }

    private void initComponents() {
        //get references to our components
        busNameView = findViewById(R.id.ArrivalsBusName);
        TextView busKeyView = findViewById(R.id.ArrivalsBusKey);
        ImageView busImageView = findViewById(R.id.ArrivalsImageView);
        ImageButton backButton = findViewById(R.id.ArrivalsBackBtn);
        ImageButton savedButton = findViewById(R.id.ArrivalsSavedBtn);
        ImageButton menuButton = findViewById(R.id.ArrivalsBusMenuBtn);
        busRouteHolder = new BusRouteHolder(getBaseContext(), busStop, findViewById(R.id.ArrivalsScrollView));
        emptyArrivalsView = findViewById(R.id.emptyArrivalsView);

        //initialize the refresh button
        refreshButton = new ImageButtonWithTimer(findViewById(R.id.ArrivalsRefreshBtn));
        refreshButton.resetButton();

        //initialize the adapter for the recycler view
        busArrivalView = findViewById(R.id.busArrivalsView);
        busArrivalView.setLayoutManager(new LinearLayoutManager(this));
        busArrivalViewAdapter = new BusArrivalAdapter(this, arrivalInstanceViews, transitService);
        busArrivalView.setAdapter(busArrivalViewAdapter);

        //get bus stop values
        String busName = busStop.getName();
        String busKey = "#" + String.valueOf(busStop.getKey());

        //set information displayed on screen
        busNameView.setText(busName);
        busKeyView.setText(busKey);

        BusStopHandler.fetchBusStopImage(busStop, AppConstants.RectangleImage.NAME, busImageView::setImageBitmap,
                GoogleStaticMapsClient.fetchImageRunnable(busStop, AppConstants.RectangleImage.NAME, getBaseContext(), busImageView));

        try{
            BusStopHandler.fetchBusRoutes(busStop, busRouteHolder::updateRouteView);
        } catch (RuntimeException e){
            SystemDialogs.showDefaultAlert(getBaseContext(), "We had trouble fetching the latest arrivals.");
        }

        //If bus stop is in the "Saved" category, then display that it is so
        if (CategoryHandler.isBusStopInCategory("Saved", busStop)) {
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
            try {
                BusStopHandler.fetchBusStopSchedule(busStop, this::prepareArrivalsList);
            } catch (RuntimeException e){
                SystemDialogs.showDefaultAlert(getBaseContext(),"We had trouble fetching your schedule.");
            }

            // For user feedback, make the list disappear for a short amount of time
            busArrivalView.setVisibility(View.INVISIBLE);

            // Start a timer to make the view reappear after 2 seconds
            new Handler().postDelayed(() -> {
                busArrivalView.setVisibility(View.VISIBLE);
            }, 2000);
        });

        //If user chooses to save/remove this bus stop, update the icon and storage
        savedButton.setOnClickListener(v -> {
            if (CategoryHandler.isBusStopInCategory("Saved", busStop)) {
                CategoryHandler.removeStopFromCategory("Saved", busStop);
                savedButton.setImageResource(R.drawable.icon_saved_stops);

            } else {
                CategoryHandler.addStopToCategory("Saved", busStop);
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