package com.isc.hermes.controller;

import static com.mongodb.assertions.Assertions.assertNotNull;

import android.graphics.Color;

import com.isc.hermes.database.IncidentsDataProcessor;
import com.isc.hermes.view.MapDisplay;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will be in charge of displaying and occluding the Traffic that you locate in the database.
 *
 */
public class ShowTrafficController {
    private IncidentsDataProcessor incidentsDataProcessor = IncidentsDataProcessor.getInstance();
    private List<Polyline> trafficPolylines = new ArrayList<>();


    /**
     * This method will hide the incidents if the user does not want to see them.
     * @param mapDisplay It is the map which will be shown .
     */
    public void hideTraffic(MapDisplay mapDisplay) {
        for (Polyline polyline : trafficPolylines) {
            mapDisplay.getMapboxMap().removePolyline(polyline);
        }
        trafficPolylines.clear();
    }

    /**
     * This method will show the incidents if the user want to see them.
     * @param mapDisplay It is the map which will be shown .
     */
    public void showTraffic(MapDisplay mapDisplay) throws JSONException {
        JSONArray incidentsArray = incidentsDataProcessor.getAllIncidentsByType("Traffic");
        assertNotNull(incidentsArray);

        for (int i = 0; i < incidentsArray.length(); i++) {
            Polyline polyline = createTrafficPolyline(mapDisplay, incidentsArray.getJSONObject(i));
            trafficPolylines.add(polyline);
        }
    }

    /**
     * This method will be in charge of once filtered the data of the points stored in the database,
     * it obtains them and draws the lines.
     * @param mapDisplay It is the map which will be shown .
     * @param incidentObject is the JSON file to extract the data .
     */
    private Polyline createTrafficPolyline(MapDisplay mapDisplay, JSONObject incidentObject) throws JSONException {
        JSONObject geometryObject = incidentObject.getJSONObject("geometry");
        JSONArray coordinatesArray = geometryObject.getJSONArray("coordinates");

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);

        for (int j = 0; j < coordinatesArray.length(); j++) {
            JSONArray coordinate = coordinatesArray.getJSONArray(j);
            double latitude = coordinate.getDouble(0);
            double longitude = coordinate.getDouble(1);
            polylineOptions.add(new LatLng(latitude, longitude));
        }

        Polyline polyline = mapDisplay.getMapboxMap().addPolyline(polylineOptions);
        return polyline;
    }

}
