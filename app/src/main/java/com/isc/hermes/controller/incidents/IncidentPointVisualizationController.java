package com.isc.hermes.controller.incidents;

import android.content.Context;

import com.isc.hermes.R;
import com.isc.hermes.model.incidents.Incident;
import android.widget.Toast;

import com.isc.hermes.model.incidents.IncidentGetterModel;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.os.Handler;
import android.os.Looper;


/**
 * The controller class for visualizing incident points on the map.
 */
public class IncidentPointVisualizationController {
    private static IncidentPointVisualizationController instance;
    private final MapboxMap mapboxMap;
    private final Context context;
    private IncidentGetterModel pointList = new IncidentGetterModel();

    /**
     * Private constructor to create an instance of the controller.
     * @param mapboxMap the MapboxMap object representing the map
     * @param context the context of the application
     */
    private IncidentPointVisualizationController(MapboxMap mapboxMap, Context context) {
        this.mapboxMap = mapboxMap;
        this.context = context;

        try {
            displayPoint(pointList);
        } catch (JSONException e) {
            Toast.makeText(context, R.string.incidents_uploaded, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * This method creates and displays waypoints on the map.
     * @param pointList the list of incidents to be marked on the map
     * @throws JSONException if there is an error in JSON parsing
     */
    public void displayPoint(IncidentGetterModel pointList) throws JSONException {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < pointList.getIncidentList().size(); i++) {
                    LatLng pointCoordinates = null;
                    try {
                        pointCoordinates = getPointCoordinates(pointList.getIncidentList(), i);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    MarkerOptions waypoint = createMarkerOptions(pointCoordinates);

                    Set<String> desiredTypes = getDesiredTypes();
                    addMarkersToMap(waypoint, pointList.getIncidentList(), desiredTypes);
                }
            }
        });
    }

    /**
     * Retrieves the point coordinates of an incident from the list.
     * @param pointList the list of incidents
     * @param index the index of the incident
     * @return the LatLng object representing the point coordinates
     * @throws JSONException if there is an error in JSON parsing
     */
    private LatLng getPointCoordinates(List<Incident> pointList, int index) throws JSONException {
        return pointList.get(index).getPointCoordinates();
    }

    /**
     * Creates a MarkerOptions object for the given point coordinates.
     * @param pointCoordinates the LatLng object representing the point coordinates
     * @return the MarkerOptions object
     */
    private MarkerOptions createMarkerOptions(LatLng pointCoordinates) {
        return new MarkerOptions().position(pointCoordinates);
    }

    /**
     * Retrieves the desired types of incidents to be displayed.
     * @return a Set of Strings containing the desired types
     */
    private Set<String> getDesiredTypes() {
        return new HashSet<>(Arrays.asList("Accident", "Social-Event", "Street obstruction"));
    }

    /**
     * Adds markers to the map for the incidents that match the desired types.
     * @param waypoint the MarkerOptions object representing the waypoint
     * @param pointList the list of incidents
     * @param desiredTypes the Set of desired incident types
     */
    private void addMarkersToMap(MarkerOptions waypoint, List<Incident> pointList, Set<String> desiredTypes) {
        for (Incident incident : pointList) {
            String incidentType = incident.getType();
            if (desiredTypes.contains(incidentType)) {
                mapboxMap.addMarker(waypoint);
            }
        }
    }

    /**
     * Retrieves the singleton instance of the controller.
     * @param mapboxMap the MapboxMap object representing the map
     * @param context the context of the application
     * @return the singleton instance of the controller
     */
    public static IncidentPointVisualizationController getInstance(MapboxMap mapboxMap, Context context) {
        if (instance == null) {
            instance = new IncidentPointVisualizationController(mapboxMap, context);
        }
        return instance;
    }
}