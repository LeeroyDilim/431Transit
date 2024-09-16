package com.example.a431transit.api.transit_api;

import com.example.a431transit.objects.TransitResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface TransitAPIService {
    String BASE_URL = "https://api.winnipegtransit.com/v3/";

    @GET
    Call<TransitResponse> fetchBusStopsByName(@Url String url, @Query("api-key") String apiKey);

    @GET("stops/{stopKey}.json")
    Call<TransitResponse> fetchBusStopsByKey(@Path("stopKey") int stopKey, @Query("api-key") String apiKey);

    @GET("routes.json")
    Call<TransitResponse> fetchBusStopRoutes(@Query("stop") int stop, @Query("api-key") String apiKey);

    @GET("stops/{stopKey}/schedule.json")
    Call<TransitResponse> fetchBusStopSchedule(@Path("stopKey") int stopKey, @Query("api-key") String apiKey);

    @GET("stops.json")
    Call<TransitResponse> fetchBusStopsByLocation(@Query("distance") int searchRadius, @Query("lat") double lat, @Query("lon") double lon, @Query("api-key") String apiKey);
}
