package com.isc.hermes.requests;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.models.CarmenContext;
import com.mapbox.geojson.Point;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;

import java.util.Map;

import retrofit2.Response;

public class ReverseGeocoding {

    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;

    public boolean isStreet(double[] coordinates) {
        double longitude = coordinates[0];
        double latitude = coordinates[1];

/*
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken("sk.eyJ1IjoiaGVybWVzLW1hcHMiLCJhIjoiY2xpamxmbnQxMDg2aDNybGc0YmUzcHloaCJ9.__1WydgkE41IAuYtsob0jA")
                .query(Point.fromLngLat(longitude, latitude))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .build();
*/
        if (!isWithinContinentalBounds(longitude, latitude)) {
            return false;
        }

        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken("sk.eyJ1IjoiaGVybWVzLW1hcHMiLCJhIjoiY2xpamxmbnQxMDg2aDNybGc0YmUzcHloaCJ9.__1WydgkE41IAuYtsob0jA")
                .query(Point.fromLngLat(longitude, latitude))
                .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                .geocodingTypes(GeocodingCriteria.TYPE_POI)
                .build();

        try {
            Response<GeocodingResponse> response = mapboxGeocoding.executeCall();
            if (response.isSuccessful()) {
                GeocodingResponse geocodingResponse = response.body();
                if (geocodingResponse != null && !geocodingResponse.features().isEmpty()) {
                    CarmenFeature feature = geocodingResponse.features().get(0);
                    return !hasNaturalContext(feature) && hasStreetContext(feature) ;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean hasStreetContext(CarmenFeature feature) {
        for (CarmenContext context : feature.context()) {
            if (context.id().startsWith("place.")
                    || context.id().startsWith("postcode.")
                    || context.id().startsWith("neighborhood.")
                    || context.id().startsWith("locality.")
                    || context.id().startsWith("region.")
                    || context.id().startsWith("country.")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasNaturalContext(CarmenFeature feature) {
        if (feature.properties().has("category")) {
            String category = feature.properties().get("category").getAsString();
            if (category.contains("lake")
                    || category.contains("water")
                    || category.contains("natural")) {
                System.out.println("ESTO ES NATURAL MI LIDEL");
                return true;
            }
        };
        return false;
    }

    private static boolean isWithinContinentalBounds(double longitude, double latitude) {
        return longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE
                && latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE;
    }

    public static boolean checkStringInJson(JsonObject jsonObject, String searchString) {
        for (Map.Entry<String, JsonElement> element : jsonObject.entrySet()) {
            String value = element.getValue().getAsString();
            if (value.equals(searchString)) {
                return true;
            }
        }
        return false;
    }
}
