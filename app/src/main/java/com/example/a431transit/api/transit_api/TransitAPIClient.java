package com.example.a431transit.api.transit_api;

import com.example.a431transit.BuildConfig;
import com.example.a431transit.api.APIManager;
import com.example.a431transit.application.AppConstants;
import com.example.a431transit.application.Conversion;
import com.example.a431transit.application.Services;
import com.example.a431transit.objects.TransitResponse;
import com.example.a431transit.objects.bus_arrivals.route_schedules.RouteSchedule;
import com.example.a431transit.objects.bus_route.BusRoute;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.objects.exceptions.NetworkErrorException;
import com.example.a431transit.persistence.IBusCache;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;

public class TransitAPIClient {
    private static Retrofit retrofit;
    private static TransitAPIService apiService;
    private static final IBusCache BUS_CACHE = Services.getBusCache();

    public static void fetchBusStopsByName(String query, Consumer<List<BusStop>> onSuccess) {
        //prepare API call to be executed
        Runnable apiCallRunnable = () -> {
            //have to do this because of api limitations :(
            String baseUrl = "https://api.winnipegtransit.com/v3/";
            String path = "stops:" + query + ".json";
            String apiUrl = baseUrl + path;

            Call<TransitResponse> call = getApiService()
                    .fetchBusStopsByName(apiUrl, BuildConfig.TRANSIT_API_KEY);

            call.enqueue(new Callback<TransitResponse>() {
                @Override
                public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        onSuccess.accept(response.body().getStops());
                    } else {
                        throw new NetworkErrorException("Error: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<TransitResponse> call, Throwable throwable) {
                    throw new NetworkErrorException("Network request failed: " + throwable.getMessage());
                }
            });
        };

        APIManager.executeWithRetry(apiCallRunnable);
    }

    public static void fetchBusStopsByKey(int query, Consumer<List<BusStop>> onSuccess) {
        //prepare API call to be executed
        Runnable apiCallRunnable = () -> {
            Call<TransitResponse> call = getApiService()
                    .fetchBusStopsByKey(query, BuildConfig.TRANSIT_API_KEY);

            call.enqueue(new Callback<TransitResponse>() {
                @Override
                public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<BusStop> busStops = new ArrayList<>();
                        busStops.add(response.body().getStop());
                        onSuccess.accept(busStops);
                    } else {
                        throw new NetworkErrorException("Error: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<TransitResponse> call, Throwable throwable) {
                    throw new NetworkErrorException("Network request failed: " + throwable.getMessage());
                }
            });
        };

        APIManager.executeWithRetry(apiCallRunnable);
    }

    public static void fetchBusStopsByLocation(LatLng location, Consumer<List<BusStop>> onSuccess) {
        //prepare API call to be executed
        Runnable apiCallRunnable = () -> {
            Call<TransitResponse> call = getApiService().fetchBusStopsByLocation(AppConstants.getSearchRadius(), location.latitude, location.longitude, BuildConfig.TRANSIT_API_KEY);

            call.enqueue(new Callback<TransitResponse>() {
                @Override
                public void onResponse(Call<TransitResponse> call, Response<TransitResponse> transitResponse) {
                    if (transitResponse.isSuccessful() && transitResponse.body() != null) {
                        onSuccess.accept(transitResponse.body().getStops());
                    } else {
                        throw new NetworkErrorException("Error: " + transitResponse.code() + " - " + transitResponse.message());
                    }
                }

                @Override
                public void onFailure(Call<TransitResponse> call, Throwable throwable) {
                    throw new NetworkErrorException("Network request failed: " + throwable.getMessage());
                }
            });
        };

        APIManager.executeWithRetry(apiCallRunnable);
    }

    public static void fetchBusStopRoutes(BusStop busStop, Consumer<List<BusRoute>> onSuccess) {
        //prepare API call to be executed
        Runnable apiCallRunnable = () -> {
            Call<TransitResponse> call = getApiService().fetchBusStopRoutes(busStop.getKey(), BuildConfig.TRANSIT_API_KEY);

            call.enqueue(new Callback<TransitResponse>() {
                @Override
                public void onResponse(Call<TransitResponse> call, Response<TransitResponse> transitResponse) {
                    if (transitResponse.isSuccessful() && transitResponse.body() != null) {
                        //cache response
                        BUS_CACHE.putRoutes(Conversion.busKeyToRouteCacheKey(busStop), transitResponse.body().getBusRoutes());
                        onSuccess.accept(transitResponse.body().getBusRoutes());
                    } else {
                        throw new NetworkErrorException("Error: " + transitResponse.code() + " - " + transitResponse.message());
                    }
                }

                @Override
                public void onFailure(Call<TransitResponse> call, Throwable throwable) {
                    throw new NetworkErrorException("Network request failed: " + throwable.getMessage());
                }
            });
        };

        APIManager.executeWithRetry(apiCallRunnable);
    }

    public static void fetchBusStopSchedule(BusStop busStop, Consumer<List<RouteSchedule>> onSuccess) {
        //prepare API call to be executed
        Runnable apiCallRunnable = () -> {
            Call<TransitResponse> call = getApiService().fetchBusStopSchedule(busStop.getKey(), BuildConfig.TRANSIT_API_KEY);

            call.enqueue(new Callback<TransitResponse>() {
                @Override
                public void onResponse(Call<TransitResponse> call, Response<TransitResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        onSuccess.accept(response.body().getStopSchedule().getRouteSchedules());
                    } else {
                        throw new NetworkErrorException("Error: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<TransitResponse> call, Throwable throwable) {
                    throw new NetworkErrorException("Network request failed: " + throwable.getMessage());
                }
            });
        };

        APIManager.executeWithRetry(apiCallRunnable);
    }

    public static TransitAPIService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(TransitAPIService.class);
        }
        return apiService;
    }

    private static Retrofit getRetrofit() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .readTimeout(2, TimeUnit.SECONDS)
                    .writeTimeout(2, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(TransitAPIService.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
