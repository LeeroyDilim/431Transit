package com.example.a431transit.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a431transit.BuildConfig;
import com.example.a431transit.R;
import com.example.a431transit.model.TransitResponse;
import com.example.a431transit.model.arrivals.ArrivalInstance;
import com.example.a431transit.model.arrivals.RouteSchedule;
import com.example.a431transit.model.arrivals.ScheduledStop;
import com.example.a431transit.model.bus_route.BusRoute;
import com.example.a431transit.model.stops.BusStop;
import com.example.a431transit.util.bus_stop_list.BusArrivalAdapter;
import com.example.a431transit.util.storage_managers.CategoriesManager;
import com.example.a431transit.util.api_communication.TransitAPIClient;
import com.example.a431transit.util.api_communication.TransitAPIService;

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
    private RecyclerView busArrivalView;
    private BusArrivalAdapter busArrivalViewAdapter;
    private BusStop busStop;

    private CountDownTimer countDownTimer;
    private ImageButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_arrivals);

        busStop = getIntent().getParcelableExtra("BUS_STOP");
        categoriesManager = new CategoriesManager(this);

        //get and initialize components
        TextView busNameView = findViewById(R.id.ArrivalsBusName);
        TextView busKeyView = findViewById(R.id.ArrivalsBusKey);

        ImageView busImageView = findViewById(R.id.ArrivalsImageView);
        ImageButton backButton = findViewById(R.id.ArrivalsBackBtn);
        ImageButton savedButton = findViewById(R.id.ArrivalsSavedBtn);
        ImageButton menuButton = findViewById(R.id.ArrivalsBusMenuBtn);

        LinearLayout horizontalScrollView = findViewById(R.id.ArrivalsScrollView);

        refreshButton = findViewById(R.id.ArrivalsRefreshBtn);
        resetRefreshButton();

        //set Saved Button State
        if(categoriesManager.isBusStopSaved(busStop))
        {
            busStop = categoriesManager.getBusStop(busStop);
            savedButton.setImageResource(R.drawable.saved_stops_icon_filled);
        }
        else
        {
            savedButton.setImageResource(R.drawable.saved_stops_icon);
        }

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

        //get latest bus arrivals
        getArrivals();

        //set listener for the buttons

        //go back to the previous activity once user clicks on back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });

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

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getArrivals();

                //if the user is spamming the button, reduce the amount of requests
                //they're making. One request per 10 seconds
                refreshButton.setEnabled(false);
                refreshButton.setImageResource(R.drawable.refresh_disabled_icon);
                startButtonTimer(5000);

                //For user feedback, make list disappear for a short amount of time
                //Signify that the app is doing something
                busArrivalView.setVisibility(View.INVISIBLE);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context wrapper = new ContextThemeWrapper(BusArrivals.this, R.style.MyMenuStyle);
                PopupMenu popupMenu = new PopupMenu(wrapper, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.add_to_collection)
                        {

                        } else if (menuItem.getItemId() == R.id.rename_bus_stop) {
                            showInputDialog();
                        } else if (menuItem.getItemId() == R.id.filter_routes) {

                        }

                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        savedButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                if(categoriesManager.isBusStopSaved(busStop))
                {
                    categoriesManager.removeStopFromCategory("Saved", busStop);
                    savedButton.setImageResource(R.drawable.saved_stops_icon);

                }
                else
                {
                    categoriesManager.addStopToCategory("Saved",busStop);
                    savedButton.setImageResource(R.drawable.saved_stops_icon_filled);
                }
            }
        });
    }

    private void getArrivals() {
        // Add a counter for retries
        final int[] retryCount = {0};
        TextView emptyArrivalsView = findViewById(R.id.emptyArrivalsView);

        Call<TransitResponse> call = transitService.getBusStopArrivals(busStop.getKey(), BuildConfig.TRANSIT_API_KEY);

        Log.i("Arrivals", call.request().url().toString());
        call.enqueue(new Callback<TransitResponse>() {
            @Override
            public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                if (response.isSuccessful()) {
                    TransitResponse transitResponse = response.body();

                    if(transitResponse == null)
                    {
                        return;
                    }

                    List<RouteSchedule> routeSchedules = transitResponse.getStopSchedule().getRouteSchedules();

                    if(routeSchedules.isEmpty())
                    {
                        emptyArrivalsView.setVisibility(View.VISIBLE);
                        busArrivalView.setVisibility(View.GONE);
                    }
                    else
                    {
                        emptyArrivalsView.setVisibility(View.GONE);
                        busArrivalView.setVisibility(View.VISIBLE);

                        prepareArrivalsList(routeSchedules);
                    }
                } else {
                    Log.e("transitService", "Error: " + response.code() + " - " + response.message());
                    showAlert("Arrivals Error", "Could not fulfill your request. Please try again later");
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
                    showAlert("Arrivals Error", "Could not fulfill your request. Please try again later");
                }
            }

        });
    }

    private void prepareArrivalsList(List<RouteSchedule> routeSchedules)
    {
        arrivalInstances.clear();

        for(RouteSchedule routeSchedule : routeSchedules)
        {
            BusRoute busRoute = routeSchedule.getRoute();

            for(ScheduledStop scheduledStop : routeSchedule.getScheduledStops())
            {
                ArrivalInstance arrivalInstance = new ArrivalInstance(busRoute.getBadgeLabel(), busRoute.getBadgeStyle(), busRoute.getName(),
                        scheduledStop.getTimes().getDeparture().getScheduled(), scheduledStop.getTimes().getDeparture().getEstimated(), scheduledStop.isCancelled());

                arrivalInstances.add(arrivalInstance);
            }
        }

        arrivalInstances.sort(Comparator.comparing(ArrivalInstance::getBusActualArrival));

        busArrivalViewAdapter.updateData(arrivalInstances);
    }

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
        refreshButton.setEnabled(true);
        refreshButton.setImageResource(R.drawable.refresh_enabled_icon);
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

    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title)
                .setMessage(message);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter new bus stop name:");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = input.getText().toString();
                // Handle the user input, e.g., show it in a Toast
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Show the dialog
        builder.show();
    }
}