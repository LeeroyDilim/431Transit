package com.example.a431transit.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Arrays;
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
    private TextView emptyArrivalsView;
    private BusStop busStop;
    private TextView busNameView;
    private LinearLayout horizontalScrollView;
    private CountDownTimer countDownTimer;
    private ImageButton refreshButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_arrivals);

        busStop = getIntent().getParcelableExtra("BUS_STOP");
        categoriesManager = new CategoriesManager(this);

        //get and initialize components
        busNameView = findViewById(R.id.ArrivalsBusName);
        TextView busKeyView = findViewById(R.id.ArrivalsBusKey);

        ImageView busImageView = findViewById(R.id.ArrivalsImageView);
        ImageButton backButton = findViewById(R.id.ArrivalsBackBtn);
        ImageButton savedButton = findViewById(R.id.ArrivalsSavedBtn);
        ImageButton menuButton = findViewById(R.id.ArrivalsBusMenuBtn);
        horizontalScrollView = findViewById(R.id.ArrivalsScrollView);
        emptyArrivalsView = findViewById(R.id.emptyArrivalsView);
        refreshButton = findViewById(R.id.ArrivalsRefreshBtn);
        resetRefreshButton();

        if (categoriesManager.isBusStopSaved(busStop)) {
            busStop = categoriesManager.getBusStop(busStop);
        }

        //set Saved Button State
        if (categoriesManager.busStopInCategory("Saved", busStop)) {
            savedButton.setImageResource(R.drawable.saved_stops_icon_filled);
        } else {
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
                popupMenu.getMenuInflater().inflate(R.menu.arrivals_popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.add_remove_from_collection) {
                            addOrRemoveFromCategories();
                        } else if (menuItem.getItemId() == R.id.rename_bus_stop) {
                            renameBusStopDialog();
                        } else if (menuItem.getItemId() == R.id.filter_routes) {
                            filterRoutes();
                        }

                        return true;
                    }
                });

                popupMenu.show();
            }
        });

        savedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoriesManager.busStopInCategory("Saved", busStop)) {
                    categoriesManager.removeStopFromCategory("Saved", busStop);
                    savedButton.setImageResource(R.drawable.saved_stops_icon);

                } else {
                    categoriesManager.addStopToCategory("Saved", busStop);
                    savedButton.setImageResource(R.drawable.saved_stops_icon_filled);
                }
            }
        });
    }

    private void getArrivals() {
        // Add a counter for retries
        final int[] retryCount = {0};
        Call<TransitResponse> call = transitService.getBusStopArrivals(busStop.getKey(), BuildConfig.TRANSIT_API_KEY);

        Log.i("Arrivals", call.request().url().toString());
        call.enqueue(new Callback<TransitResponse>() {
            @Override
            public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                if (response.isSuccessful()) {
                    TransitResponse transitResponse = response.body();

                    if (transitResponse == null) {
                        return;
                    }

                    List<RouteSchedule> routeSchedules = transitResponse.getStopSchedule().getRouteSchedules();
                    prepareArrivalsList(routeSchedules);
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

    private void prepareArrivalsList(List<RouteSchedule> routeSchedules) {
        List<String> filteredRoutes = busStop.getFilteredRoutes();

        arrivalInstances.clear();

        for (RouteSchedule routeSchedule : routeSchedules) {
            BusRoute busRoute = routeSchedule.getRoute();

            if(filteredRoutes != null && !filteredRoutes.contains(busRoute.getKey()))
            {
                continue;
            }

            for (ScheduledStop scheduledStop : routeSchedule.getScheduledStops()) {
                ArrivalInstance arrivalInstance = new ArrivalInstance(busRoute.getBadgeLabel(), busRoute.getBadgeStyle(), busRoute.getName(),
                        scheduledStop.getTimes().getDeparture().getScheduled(), scheduledStop.getTimes().getDeparture().getEstimated(), scheduledStop.isCancelled());

                arrivalInstances.add(arrivalInstance);
            }
        }

        if (arrivalInstances.isEmpty()) {
            emptyArrivalsView.setVisibility(View.VISIBLE);
            busArrivalView.setVisibility(View.GONE);
        } else {
            emptyArrivalsView.setVisibility(View.GONE);
            busArrivalView.setVisibility(View.VISIBLE);
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

    private void renameBusStopDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.text_dialog, null);

        EditText inputText = dialogView.findViewById(R.id.input_dialog_edit_text);
        inputText.setHint(busStop.getOriginalName());
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        alert.setView(dialogView);
        final AlertDialog alertDialog = alert.create();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = inputText.getText().toString();

                if (nickname.equals("")) {
                    nickname = null;
                }

                busStop.setNickname(nickname);

                categoriesManager.updateBusStop(busStop);

                busNameView.setText(busStop.getName());

                alertDialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        // Show the dialog
        alertDialog.show();
    }

    private void addOrRemoveFromCategories() {
        List<String> editableCategories = categoriesManager.getEditableCategories();

        // Initialize boolean array to track selected categories
        boolean[] checkedItems = new boolean[editableCategories.size()];

            // Set the initial state based on the existing state of each category
        for (int i = 0; i < editableCategories.size(); i++) {
            checkedItems[i] = busStop.inCategory(editableCategories.get(i));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.multi_choice_dialog, null);
        builder.setView(dialogView);

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();

        // Get references to custom views
        TextView titleTextView = dialogView.findViewById(R.id.input_dialog_title);
        ListView listView = dialogView.findViewById(R.id.input_dialog_multi_choice);
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        // Set dialog title
        titleTextView.setText("Assign Bus Stop to Categories");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, editableCategories) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                CheckedTextView checkedTextView = view.findViewById(android.R.id.text1);
                checkedTextView.setChecked(checkedItems[position]);

                return view;
            }
        };

        listView.setAdapter(adapter);

        // Set multi-choice items and listen for changes
        listView.setOnItemClickListener((parent, view, position, id) -> {
            CheckedTextView checkedTextView = (CheckedTextView) view;
            checkedItems[position] = !checkedItems[position];
            checkedTextView.setChecked(checkedItems[position]);
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < editableCategories.size(); i++) {
                    if (checkedItems[i]) {
                        categoriesManager.addStopToCategory(editableCategories.get(i), busStop);
                    } else {
                        categoriesManager.removeStopFromCategory(editableCategories.get(i), busStop);
                    }
                }

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void filterRoutes() {
        List<BusRoute> busRoutes = busStop.getBusRoutes();
        List<String> filteredRoutes = busStop.getFilteredRoutes();

        if(busRoutes == null)
        {
            return;
        }

        // Initialize boolean array to track selected categories
        boolean[] checkedItems = new boolean[busRoutes.size()];

        for (int i = 0; i < busRoutes.size(); i++) {
            if(filteredRoutes != null)
            {
                checkedItems[i] = filteredRoutes.contains(busRoutes.get(i).getKey());
            } else {
                checkedItems[i] = true;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View dialogView = getLayoutInflater().inflate(R.layout.multi_choice_dialog, null);
        builder.setView(dialogView);

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();

        // Get references to custom views
        TextView titleTextView = dialogView.findViewById(R.id.input_dialog_title);
        ListView listView = dialogView.findViewById(R.id.input_dialog_multi_choice);
        Button cancelButton = dialogView.findViewById(R.id.input_dialog_cancel_btn);
        Button submitButton = dialogView.findViewById(R.id.input_dialog_submit_btn);

        titleTextView.setText("Select Bus Routes to Display");

        ArrayAdapter<BusRoute> adapter = new ArrayAdapter<BusRoute>(this, android.R.layout.simple_list_item_multiple_choice, busRoutes) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                CheckedTextView checkedTextView = view.findViewById(android.R.id.text1);
                checkedTextView.setChecked(checkedItems[position]);

                return view;
            }
        };

        listView.setAdapter(adapter);

        // Set multi-choice items and listen for changes
        listView.setOnItemClickListener((parent, view, position, id) -> {
            CheckedTextView checkedTextView = (CheckedTextView) view;
            checkedItems[position] = !checkedItems[position];
            checkedTextView.setChecked(checkedItems[position]);
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> newFilteredRoutes = new ArrayList<>();

                for (int i = 0; i < busRoutes.size(); i++) {
                    if(checkedItems[i]) {
                        newFilteredRoutes.add(busRoutes.get(i).getKey());
                    }
                }

                busStop.setFilteredRoutes(newFilteredRoutes);
                categoriesManager.updateBusStop(busStop);

                busStop.loadBusRoutes(getBaseContext(),transitService,horizontalScrollView);
                getArrivals();

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}