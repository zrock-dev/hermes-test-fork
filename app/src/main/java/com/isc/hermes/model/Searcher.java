package com.isc.hermes.model;

import android.content.Context;
import android.os.StrictMode;
import com.isc.hermes.R;
import com.isc.hermes.utils.PlaceByTypeSearch;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Response;


/**
 * Class to represent the searcher functionality of the app,
 * setting the methods to make a call at the api to receive
 * the real time suggestions of a query received
 *
 * @see CarmenFeature a feature reresented with geojson properties
 */
public class Searcher {

    protected CurrentLocationModel currentLocationModel;
    private Context context;
    private PlaceByTypeSearch placeByTypeSearch;

    /**
     * This method is the constructor of the class
     */
    public Searcher(){
        placeByTypeSearch = new PlaceByTypeSearch();
    }

    /**
     * Overloading constructor of the class
     */
    public Searcher(Context applicationContext) {
        currentLocationModel  = new CurrentLocationModel();
        this.context = applicationContext;
    }


    /**
     * Retrieves a list of suggestion features for a given user location and query.
     *
     * @param userLocation The current user location as a CurrentLocationModel object.
     * @param query        The search query for which suggestion features will be obtained.
     * @param country      The country code to limit geocoding results (optional, can be null).
     * @return A list of CarmenFeature objects representing the found suggestion features.
     */
    public List<CarmenFeature> getSuggestionsFeatures(CurrentLocationModel userLocation, String query, String country) {
        List<CarmenFeature> features = new ArrayList<>();

        for (String geocodingType : getGeocodingTypeOrders()) {
            MapboxGeocoding client = buildGeocodingClient(userLocation, query, country, geocodingType);
            Response<GeocodingResponse> geocodingResponse = executeGeocodingCall(client);
            if (isGeocodingResponseValid(geocodingResponse)) {
                features.addAll(getFeaturesFromGeocodingResponse(geocodingResponse));
                break;
            }
        }
        return features;
    }

    /**
     * This method is used to get the type of the place that will be searched
     * @return the types of the place
     */
    private List<String> getGeocodingTypeOrders() {
        return Arrays.asList(
                GeocodingCriteria.TYPE_POI,
                GeocodingCriteria.TYPE_ADDRESS,
                GeocodingCriteria.TYPE_LOCALITY,
                GeocodingCriteria.TYPE_PLACE,
                GeocodingCriteria.TYPE_COUNTRY);
    }

    /**
     * Builds a Mapbox Geocoding client based on the provided user location, query, and country.
     *
     * @param userLocation The current user location as a CurrentLocationModel object.
     * @param query        The search query to be used for geocoding.
     * @param country      The country code to limit geocoding results (optional, can be null).
     * @return A MapboxGeocoding object configured with the specified parameters.
     */
    private MapboxGeocoding buildGeocodingClient(CurrentLocationModel userLocation, String query, String country, String geocodingType) {
        MapboxGeocoding.Builder builder = MapboxGeocoding.builder()
                .accessToken(context.getString(R.string.access_token))
                .query(query)
                .proximity(Point.fromLngLat(userLocation.getLongitude(), userLocation.getLatitude()))
                .autocomplete(true)
                .geocodingTypes(geocodingType)
                .mode(GeocodingCriteria.MODE_PLACES);

        if (country != null) {
            builder.country(country);
        }

        return builder.build();
    }

    /**
     * Executes a geocoding call using the provided MapboxGeocoding client.
     *
     * @param client The MapboxGeocoding client configured for the geocoding request.
     * @return The Response object containing the geocoding response.
     */
    private Response<GeocodingResponse> executeGeocodingCall(MapboxGeocoding client) {
        try {
            StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(gfgPolicy);
            return client.executeCall();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the geocoding response is valid.
     *
     * @param geocodingResponse The Response object containing the geocoding response.
     * @return True if the response is valid, false otherwise.
     */
    private boolean isGeocodingResponseValid(Response<GeocodingResponse> geocodingResponse) {
        return geocodingResponse.isSuccessful()
                && geocodingResponse.body() != null
                && !geocodingResponse.body().features().isEmpty();
    }

    /**
     * Retrieves the list of CarmenFeature objects from the geocoding response.
     *
     * @param geocodingResponse The Response object containing the geocoding response.
     * @return A list of CarmenFeature objects representing the found features.
     */
    private List<CarmenFeature> getFeaturesFromGeocodingResponse(Response<GeocodingResponse> geocodingResponse) {
        return Objects.requireNonNull(geocodingResponse.body()).features();
    }

    /**
     * Method to get the searcher Suggestions places information setting a waypoint to use it later as a location in map
     *
     * @param query the consult of the searcher field text
     * @return the features list with the waypoint of the suggestions
     */
    public List<WayPoint> getSearcherSuggestionsPlacesInfo(String query) {
        if (query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<List<CarmenFeature>> boSuggestions = executor.submit(() -> getSuggestionsFeatures(currentLocationModel, query, "BO"));
        Future<List<CarmenFeature>> globalSuggestions = executor.submit(() -> getSuggestionsFeatures(currentLocationModel, query, null));

        return getWayPointsFromFeatures(getCombinedSuggestions(boSuggestions, globalSuggestions));
    }

    /**
     * Method to get the combination of the suggestions of the searcher
     * @param boSuggestions the suggestions of the searcher in Bolivia
     * @param globalSuggestions the suggestions of the searcher in the world
     * @return the combination of the suggestions
     */
    private List<CarmenFeature> getCombinedSuggestions(Future<List<CarmenFeature>> boSuggestions, Future<List<CarmenFeature>> globalSuggestions) {
        List<CarmenFeature> combinedFeatures = new ArrayList<>();
        try {
            combinedFeatures.addAll(boSuggestions.get());
            combinedFeatures.addAll(globalSuggestions.get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return combinedFeatures;
    }

    /**
     * Method to get the waypoints of the features of the suggestions
     * @param features the features of the suggestions
     * @return the waypoints of the features
     */
    private List<WayPoint> getWayPointsFromFeatures(List<CarmenFeature> features) {
        List<WayPoint> wayPoints = new ArrayList<>();
        for (CarmenFeature feature : features) {
            wayPoints.add(instanceWaypointFeature(feature));
        }
        return wayPoints;
    }

    /**
     * Method to instance a waypoint feature passing the latitude and longitude of a geo point of the feature
     *
     * @param feature to pass the attributes of a waypoint
     * @return the instanced waypoint
     */
    private WayPoint instanceWaypointFeature(CarmenFeature feature) {
        Point point = feature.center();
        assert point != null;
        return new WayPoint(
                feature.placeName(),
                feature.properties(),
                point.latitude(),
                point.longitude());
    }

    /**
     * Getter of application context.
     * @return context.
     */
    public Context getContext() {
        return context;
    }
}
