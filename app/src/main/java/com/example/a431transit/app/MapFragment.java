package com.example.a431transit.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a431transit.BuildConfig;
import com.example.a431transit.R;
import com.example.a431transit.model.TransitResponse;
import com.example.a431transit.model.stops.BusStop;
import com.example.a431transit.util.CustomInfoWindowAdapter;
import com.example.a431transit.util.api_communication.TransitAPIService;
import com.example.a431transit.util.bus_stop_list.BusStopAdapter;
import com.example.a431transit.util.bus_stop_list.BusStopViewInterface;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends Fragment implements OnMapReadyCallback, BusStopViewInterface {

    //connection to the TransitAPI
    private TransitAPIService transitService;

    //tracks if this fragment is currently being displayed on the screen
    private boolean isFragmentValid = false;

    //fragment components
    private MapView mapView;
    private GoogleMap googleMap;
    private ImageButton refreshButton;
    private BusStopAdapter busStopAdapter;

    //marker that user put on the map
    private Marker userMarker;

    //represents how far the app will look for bus stops
    private final int SEARCH_RADIUS = 500;

    //possible locations for the user
    private LatLng currentLocation;
    private final LatLng DEFAULT_LOCATION = new LatLng(49.890800, -97.130737); //if user does not have location permissions granted, show them the Forks!
    private LatLng userMarkerLocation;

    //a circle on the map that represents the search radius
    private Circle currentLocationCircle;

    //use this to get last known location of the user
    private FusedLocationProviderClient fusedLocationProviderClient;

    //when asking for permissions, this value signifies that a user has granted access
    private final int REQUEST_CODE = 0;

    //variables to represent bus stops on the map
    List<BusStop> busStops;
    List<Marker> busStopMarkers;

    public MapFragment() {
        // Required empty public constructor
    }

    public MapFragment(TransitAPIService transitService) {
        this.transitService = transitService;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        //fragment is currently being displayed on screen
        isFragmentValid = true;

        initComponents(rootView);
        initGoogleMap(savedInstanceState);

        return rootView;
    }

    @Override
    //Once the google map is ready for display, modify it to make it fit with our usage requirements
    public void onMapReady(GoogleMap map) {
        //Since this method is asynchronous, check first if the fragment has not been disposed of yet
        if (!isFragmentValid) {
            return;
        }

        googleMap = map;

        //set marker info window into a custom one
        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getContext()));

        //Constrain the camera with location and zoom boundaries so that the user doesn't stray away from Winnipeg
        LatLngBounds winnipegBounds = new LatLngBounds(
                new LatLng(49.7139, -97.3258), // SW bounds
                new LatLng(50.004965, -96.943525)  // NE bounds
        );

        map.setLatLngBoundsForCameraTarget(winnipegBounds);
        map.setMinZoomPreference(10.0f);

        //check permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request location permissions
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE);

            return;
        }

        //disable default location button
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        //get user location and initialize map camera to that location
        googleMap.setMyLocationEnabled(true);
        setMapToCurrentLocation();

        //If a user clicks on the map, set a marker on the tap and record its coordinates
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //remove previous marker if it exists
                if (userMarker != null) {
                    userMarker.remove();
                }

                userMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
                userMarkerLocation = latLng;
            }
        });

        //If user clicks on a bus stop's info window, take them to its Arrivals screen
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                BusStop busStop = (BusStop) marker.getTag();

                if (busStop == null) {
                    return;
                }

                Intent intent = new Intent(getContext(), BusArrivals.class);
                intent.putExtra("BUS_STOP", busStop);

                startActivity(intent);
            }
        });
    }

    //Set the camera and search to the user's last known location
    private void setMapToCurrentLocation() {
        //check permissions
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        //delete user marker if it exists
        if (userMarker != null) {
            userMarker.remove();
        }

        //get last known user location and update the map
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    currentLocation = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                    updateMap(currentLocation);
                }
            }
        });
    }

    //Given a location, center the camera onto that location
    //Then make a call to the Transit API to get all bus stops near that location
    //Then draw on the map the search radius, and markers representing a single bus stop
    private void updateMap(LatLng location) {
        //If fragment is not being displayed anymore, don't run this method
        if (getContext() == null) {
            return;
        }

        final int ZOOM_LEVEL = 15; //for the camera

        //set properties of the circle
        final int CIRCLE_STROKE_WIDTH = 5;
        List<PatternItem> strokePattern = Arrays.asList(
                new Dash(30),  // Dash length in pixels
                new Gap(20)    // Gap length in pixels
        );

        //move camera
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(ZOOM_LEVEL)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //remove previous circle if it exists
        if (currentLocationCircle != null) {
            currentLocationCircle.remove();
        }

        //add circle to the map
        currentLocationCircle = googleMap.addCircle(
                new CircleOptions().center(location)
                        .radius(SEARCH_RADIUS)
                        .strokeWidth(CIRCLE_STROKE_WIDTH)
                        .strokeColor(getContext().getColor(R.color.navy))
                        .strokePattern(strokePattern)
        );

        //make a call to the API
        getNearbyBusStops(location);
    }

    private void getNearbyBusStops(LatLng location) {
        // Add a counter for retries
        final int[] retryCount = {0};

        Call<TransitResponse> call;

        if (location == null) {
            return;
        }

        //make API call
        call = transitService.getBusStopsByLocation(SEARCH_RADIUS, location.latitude, location.longitude, BuildConfig.TRANSIT_API_KEY);

        call.enqueue(new Callback<TransitResponse>() {
            @Override
            public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                if (response.isSuccessful()) {
                    TransitResponse transitResponse = response.body();

                    //get response from the API
                    busStops = transitResponse.getStops();

                    //update map if there are nearby bus stops
                    if (!busStops.isEmpty() && getContext() != null) {
                        updateBusStopMarkers();
                    }

                } else {
                    Log.e("transitService", "Error: " + response.code() + " - " + response.message());
                    showAlert(getContext(), "Search Error", "Could not fulfill your request. Please try again later");

                    //empty the list of any previous searches
                    busStops = new ArrayList<BusStop>();
                    busStopAdapter.updateData(busStops);
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
                    showAlert(getContext(), "Search Error", "Could not fulfill your request. Please try again later");

                    //empty the list of any previous searches
                    busStops = new ArrayList<BusStop>();
                    busStopAdapter.updateData(busStops);
                }
            }
        });
    }

    private void updateBusStopMarkers() {
        //Extract color from resource file and convert to hue
        int color = getResources().getColor(R.color.bus_marker_color);
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        float busMarkerHue = hsl[0];

        //delete previous bus stop markers
        if (busStopMarkers != null) {
            for (Marker busStopMarker : busStopMarkers) {
                busStopMarker.remove();
            }
        }

        busStopMarkers = new ArrayList<>();

        //For each bus stop that was found, signify its presence with a marker
        for (BusStop busStop : busStops) {
            LatLng busStopLatLng = new LatLng(Double.parseDouble(busStop.getCentre().getGeographic().getLatitude()), Double.parseDouble(busStop.getCentre().getGeographic().getLongitude()));

            //create marker and pass the bus stop instance onto it
            Marker busStopMarker = googleMap.addMarker(new MarkerOptions().position(busStopLatLng)
                    .title(busStop.getName())
                    .snippet("#" + busStop.getKey())
                    .icon(BitmapDescriptorFactory.defaultMarker(busMarkerHue)));

            busStopMarker.setTag(busStop);

            busStopMarkers.add(busStopMarker);
        }

        //update the list display
        busStopAdapter.updateData(busStops);
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentValid = false;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    //Timer for the refresh button cooldown
    private void startButtonTimer(long millisInFuture) {
        // Timer finished, enable the button and reset its state
        //timer for the refresh button
        CountDownTimer countDownTimer = new CountDownTimer(millisInFuture, 1000) {

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
    //Method that is called once the user has decided on whether to grant or deny location access
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //if user agrees to permission request, update map with their last location
        //if not, set map to a default location
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setMapToCurrentLocation();
            } else {
                updateMap(DEFAULT_LOCATION);
            }
        }
    }

    //Once a user has clicked a bus stop, create a new screen displaying the arrival times for that bus stop
    @Override
    public void onItemClick(int position) {
        if (busStops.size() > 0 && position >= 0 & position < busStops.size()) {
            Intent intent = new Intent(getContext(), BusArrivals.class);

            intent.putExtra("BUS_STOP", busStops.get(position));

            startActivity(intent);
        }
    }

    private void initComponents(View rootView) {
        //get and init components
        mapView = rootView.findViewById(R.id.mapView);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        refreshButton = rootView.findViewById(R.id.MapRefreshLocationButton);
        ImageButton currentLocationButton = rootView.findViewById(R.id.MapCurrentLocationButton);

        RecyclerView busStopListView = rootView.findViewById(R.id.MapRecyclerView);
        busStopListView.setLayoutManager(new LinearLayoutManager(requireContext()));
        busStopAdapter = new BusStopAdapter(this, requireContext(), busStops, transitService);
        busStopListView.setAdapter(busStopAdapter);

        //set appropriate collapsed height for the bottom sheet
        //hardcoded! sorry
        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(rootView.findViewById(R.id.mapBottomSheet));
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(dpToPx(getContext(), 135));

        //initialize listeners for the buttons
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMarker == null) {
                    return;
                }

                updateMap(userMarkerLocation);

                //if the user is spamming the button, reduce the amount of requests
                //they're making. One request per 10 seconds
                refreshButton.setEnabled(false);
                refreshButton.setImageResource(R.drawable.refresh_disabled_icon);
                startButtonTimer(5000);
            }
        });

        //Update the map to the users current location
        currentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMapToCurrentLocation();
            }
        });
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(BuildConfig.GOOGLE_API_KEY);
        }

        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(BuildConfig.GOOGLE_API_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(BuildConfig.GOOGLE_API_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    // Additional method to convert dp to pixels
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    private void showAlert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the title and message for the alert dialog
        builder.setTitle(title)
                .setMessage(message);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}