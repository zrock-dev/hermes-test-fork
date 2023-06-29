package com.isc.hermes.controller;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.isc.hermes.R;
import com.isc.hermes.model.User.TypeUser;
import com.isc.hermes.model.User.UserRepository;
import com.isc.hermes.requests.geocoders.StreetValidator;
import com.isc.hermes.utils.Animations;
import com.isc.hermes.utils.MapManager;
import com.mapbox.mapboxsdk.geometry.LatLng;


/**
 * This is the controller class for "waypoints_options_fragment" view.
 */
public class WaypointOptionsController {

    private StreetValidator streetValidator;
    private final RelativeLayout waypointOptions;
    private final IncidentFormController incidentFormController;
    private final NavigationOptionsController navigationOptionsFormController;
    private final LinearLayout reportIncidentsView;
    private final Button navigateButton;
    private TrafficAutomaticFormController trafficAutomaticFormController;
    private final Button reportIncidentButton;
    private final Button reportTrafficButton;
    private final Button reportNaturalDisasterButton;
    private final Context context;
    private TextView placeName;

    /**
     * This is the constructor method. Init all the components of UI.
     *
     * @param context Is the context application.
     * @param mapWayPointController Is the controller of the map.
     */
    public WaypointOptionsController(Context context, MapWayPointController mapWayPointController) {
        this.context = context;
        trafficAutomaticFormController = new TrafficAutomaticFormController(context, mapWayPointController);
        streetValidator = new StreetValidator();
        waypointOptions = ((AppCompatActivity)context).findViewById(R.id.waypoint_options);
        incidentFormController = new IncidentFormController(context, mapWayPointController);
        navigationOptionsFormController = new NavigationOptionsController(context, mapWayPointController);
        navigateButton = ((AppCompatActivity) context).findViewById(R.id.navigate_button);
        trafficAutomaticFormController = new TrafficAutomaticFormController(context, mapWayPointController);
        reportIncidentButton = ((AppCompatActivity) context).findViewById(R.id.report_incident_button);
        reportTrafficButton = ((AppCompatActivity) context).findViewById(R.id.report_traffic_button);
        reportNaturalDisasterButton = ((AppCompatActivity) context).findViewById(R.id.report_natural_disaster_button);
        placeName = ((AppCompatActivity) context).findViewById(R.id.place_name);
        reportIncidentsView = ((AppCompatActivity) context).findViewById(R.id.report_incidents);
        setButtonsOnClick();
    }

    /**
     * Method to assign functionality to the buttons of the view.
     */
    private void setButtonsOnClick(){
        navigateButton.setOnClickListener(v -> {
            waypointOptions.startAnimation(Animations.exitAnimation);
            navigationOptionsFormController.getNavOptionsForm().startAnimation(Animations.entryAnimation);
            navigationOptionsFormController.getNavOptionsForm().setVisibility(View.VISIBLE);
            waypointOptions.setVisibility(View.GONE);
        });

        reportIncidentButton.setOnClickListener(v -> {
            waypointOptions.startAnimation(Animations.exitAnimation);
            incidentFormController.getIncidentForm().startAnimation(Animations.entryAnimation);
            incidentFormController.getIncidentForm().setVisibility(View.VISIBLE);
            waypointOptions.setVisibility(View.GONE);
        });

        reportTrafficButton.setOnClickListener(v -> {

            waypointOptions.startAnimation(Animations.exitAnimation);


            AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... voids) {

                    return trafficAutomaticFormController.uploadTrafficDataBase();
                }
                @Override
                protected void onPostExecute(Integer responseCode) {
                    trafficAutomaticFormController.handleUploadResponse(responseCode);
                }
            };

            task.execute();
            waypointOptions.setVisibility(View.GONE);
            incidentFormController.getMapController().deleteMarks();


        });

        reportNaturalDisasterButton.setOnClickListener(v->{
            MapManager.getInstance().removeCurrentClickController();
            MapManager.getInstance().setMapClickConfiguration(new MapPolygonController(MapManager.getInstance().getMapboxMap(), this.context));
            waypointOptions.startAnimation(Animations.exitAnimation);
            waypointOptions.setVisibility(View.GONE);
        });
    }

    /**
     * This is a getter method to waypoint options layout.
     * @return Return a layout.
     */
    public RelativeLayout getWaypointOptions() {
        return waypointOptions;
    }

    /**
     * This is the getter method to incident form controller.
     * @return Return the controller class of incident form view.
     */
    public IncidentFormController getIncidentFormController() {
        return incidentFormController;
    }

    /**
     * This is the getter method to get the navigation options controller instance.
     * @return Return the navigation options controller form view.
     */
    public NavigationOptionsController getNavOptionsFormController() {
        return navigationOptionsFormController;
    }

    /**
     * This method set the report incident status view if the point market is a street.
     *
     * @param point is the coordinate point market.
     */
    public void setReportIncidentStatus(LatLng point) {
        if (!streetValidator.hasStreetContext(point) || UserRepository.getInstance().
                getUserContained().getTypeUser().equals(TypeUser.GENERAL.getTypeUser()))
            reportIncidentsView.setVisibility(View.GONE);
        else reportIncidentsView.setVisibility(View.VISIBLE);
    }
}
