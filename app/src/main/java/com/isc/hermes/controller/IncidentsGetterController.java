package com.isc.hermes.controller;

import com.isc.hermes.database.IncidentsDataProcessor;
import com.isc.hermes.model.incidents.Incident;
import com.isc.hermes.model.incidents.IncidentGetterModel;
import com.isc.hermes.utils.ISO8601Converter;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IncidentsGetterController {
    private final ISO8601Converter iso8601Converter;
    private final IncidentsDataProcessor incidentsDataProcessor;
    private static IncidentsGetterController instance;
    private IncidentGetterModel incidentGetterModel;
    private final ExecutorService executorService;

    public IncidentsGetterController() {
        this.iso8601Converter = ISO8601Converter.getInstance();
        this.incidentsDataProcessor = IncidentsDataProcessor.getInstance();
        this.incidentGetterModel = new IncidentGetterModel();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void getNearIncidentsWithinRadius(MapboxMap mapboxMap) {
        executorService.execute(() -> {
            LatLng cameraFocus = mapboxMap.getCameraPosition().target;
            int cameraZoom = (int) mapboxMap.getCameraPosition().zoom;
            int zoom = cameraZoom > 40 ? 40 : Math.max(cameraZoom, 12);

            JSONArray incidentsArray = incidentsDataProcessor.getNearIncidents(cameraFocus, zoom);
            incidentGetterModel.setIncidentList(parseIncidentResponse(incidentsArray));
            System.out.println(incidentGetterModel.getIncidentList());
        });
    }

    private List<Incident> parseIncidentResponse(JSONArray incidentsArray) {
        List<Incident> incidents = new ArrayList<>();

        try{
            for (int i = 0; i < incidentsArray.length(); i++) {
                JSONObject incidentObj = incidentsArray.getJSONObject(i);
                String id = incidentObj.getString("_id");
                String type = incidentObj.getString("type");
                String reason = incidentObj.getString("reason");
                Date dateCreated =
                        iso8601Converter.convertISO8601ToDate(incidentObj.getString("dateCreated"));
                Date deathDate =
                        iso8601Converter.convertISO8601ToDate(incidentObj.getString("deathDate"));
                JSONObject geometry = incidentObj.getJSONObject("geometry");

                Incident incident = new Incident(id, type, reason, dateCreated, deathDate, geometry);
                incidents.add(incident);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }

        return incidents;
    }

    public static IncidentsGetterController getInstance(){
        if (instance == null) instance = new IncidentsGetterController();
        return instance;
    }
}
