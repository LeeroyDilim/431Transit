package com.example.a431transit.util;

import com.example.a431transit.model.TransitResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface TransitAPIService {
    String BASE_URL = "https://api.winnipegtransit.com/v3/";
    @GET
    Call<TransitResponse> searchBusStopsByName(@Url String url, @Query("api-key") String apiKey);
    @GET("stops/{stopKey}.json")
    Call<TransitResponse> searchBusStopsByKey(@Path("stopKey") int stopKey, @Query("api-key") String apiKey);
    @GET("routes.json")
    Call<TransitResponse> getBusStopRoutes(@Query("stop") int stop, @Query("api-key") String apiKey);

    @GET("stops/{stopKey}/schedule.json")
    Call<TransitResponse> getBusStopArrivals(@Path("stopKey") int stopKey, @Query("api-key") String apiKey);
}
