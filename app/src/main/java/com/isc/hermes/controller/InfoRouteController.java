package com.isc.hermes.controller;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.isc.hermes.R;
import com.isc.hermes.model.CurrentLocationModel;
import com.isc.hermes.model.Utils.MapPolyline;
import com.isc.hermes.model.navigation.LiveRouteEstimationsWorker;
import com.isc.hermes.model.navigation.NavigationOrchestrator;
import com.isc.hermes.model.navigation.RoutesRepository;
import com.isc.hermes.model.navigation.TransportationType;
import com.isc.hermes.model.navigation.UserRouteTracker;
import com.isc.hermes.model.navigation.directions.RouteDirectionsProvider;
import com.isc.hermes.utils.Animations;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


/**
 * The InfoRouteController class is responsible for managing the information related to routes,
 * such as distance and estimated time, and displaying it in the user interface.
 */
public class InfoRouteController {
    private static InfoRouteController instanceNavigationController;
    private ConstraintLayout layout;
    private boolean isActive;
    private Button cancelButton;
    private Button buttonRouteA;
    private Button buttonRouteB;
    private Button buttonRouteC;
    private Button startNavigationButton;
    private Button recalculateRouteButton;
    private TextView timeText;
    private TextView distanceText;
    private ArrayList<Integer> colorsInfoRoutes;
    private MapPolyline mapPolyline;
    private ArrayList<JSONObject> jsonObjects;
    private NavigationOptionsController navigationOptionsController;
    private NavigationDirectionController navigationDirectionController;
    private boolean isRouteASelected, isRouteBSelected, isRouteCSelected;

    private int elapsedSeconds;
    private int timeEstimate;
    private String routes;
    private String selectedRoute = "Route A";

    private final NavigationOrchestrator navigationOrchestrator;
    private TransportationType transportationType;
    private Context contextObj;
    /**
     * Constructs a new InfoRouteController object.
     *
     * @param context                     The context of the activity.
     * @param navigationOptionsController route navigation's options controller
     */
    private InfoRouteController(Context context, NavigationOptionsController navigationOptionsController) {
        setViewComponents(context);
        navigationDirectionController = new NavigationDirectionController(context);
        this.navigationOptionsController = navigationOptionsController;
        colorsInfoRoutes = new ArrayList<>();
        isActive = false;
        isRouteASelected = false;
        isRouteBSelected = false;
        isRouteCSelected = false;
        jsonObjects = new ArrayList<>();
        setActionButtons();
        navigationOrchestrator = new NavigationOrchestrator("Route A", this);
    }

    /**
     * Retrieves an instance of InfoRouteController.
     *
     * @param context The context of the activity.
     * @return The InfoRouteController instance.
     */
    public static InfoRouteController getInstance(Context context, NavigationOptionsController navigationOptionsController) {
        if (instanceNavigationController == null || instanceNavigationController.contextObj== null || instanceNavigationController.contextObj!=context) {
            instanceNavigationController = new InfoRouteController(context, navigationOptionsController);
        }
        return instanceNavigationController;
    }

    /**
     * This method set the view of the components.
     *
     * @param context is the context.
     */
    private void setViewComponents(Context context) {
        contextObj = context;
        Activity activity = ((AppCompatActivity) context);
        layout = activity.findViewById(R.id.distance_time_view);
        cancelButton = activity.findViewById(R.id.cancel_navigation_button);
        timeText = activity.findViewById(R.id.timeText);
        distanceText = activity.findViewById(R.id.distanceText);
        buttonRouteC = activity.findViewById(R.id.ButtonRouteThree);
        buttonRouteB = activity.findViewById(R.id.ButtonRouteTwo);
        buttonRouteA = activity.findViewById(R.id.ButtonRouteOne);
        recalculateRouteButton = activity.findViewById(R.id.recalculateFromCurrentLocation);
        buttonRouteB.setAlpha(0.3f);
        buttonRouteC.setAlpha(0.3f);
        startNavigationButton = activity.findViewById(R.id.startNavigationButton);
    }

    /**
     * This method set the routes' color
     *
     * @param size is the size of the route.
     */
    private void setColorsInfoRoutes(int size) {
        colorsInfoRoutes.clear();
        colorsInfoRoutes.add(size > 2 ? 0XFF686C6C : 0XFFFF6E26);
        colorsInfoRoutes.add(0xFF2350A3);
        colorsInfoRoutes.add(size > 1 ? 0XFFFF6E26 : 0XFF686C6C);
    }

    /**
     * Sets the action listeners for the buttons.
     */
    private void setActionButtons() {
        isActive = false;
        cancelButton.setOnClickListener(v -> {
            closeNavigation();
            Toast.makeText(layout.getContext(), "Closing navigation mode", Toast.LENGTH_SHORT).show();
        });

        buttonRouteA.setOnClickListener(v -> {
            setRouteInformation(jsonObjects.size() - 1, true, false, false);
            selectedRoute = "Route A";
            navigationOrchestrator.setRoute("Route A");
        });
        buttonRouteB.setOnClickListener(v -> {
            setRouteInformation(1, false, true, false);
            selectedRoute = "Route B";
            navigationOrchestrator.setRoute("Route B");
        });
        buttonRouteC.setOnClickListener(v -> {
            setRouteInformation(0, false, false, true);
            selectedRoute = "Route C";
            navigationOrchestrator.setRoute("Route C");
        });

        setNavigationButtonsEvent();
    }
    /**
     * Cancels the navigation hiding the routes modal and the routes in map
     */
    private void closeNavigation(){
        mapPolyline.hidePolylines();
        layout.startAnimation(Animations.exitAnimation);
        layout.setVisibility(View.GONE);
        navigationOptionsController.setStartPoint(CurrentLocationModel.getInstance().getLatLng());
        navigationOptionsController.handleCancelAction();
        if (navigationOptionsController.getNavOptionsForm().getVisibility() == View.VISIBLE)
            navigationDirectionController.getDirectionsForm()
                    .startAnimation(Animations.exitAnimation);
        navigationDirectionController.getDirectionsForm().setVisibility(View.GONE);
        isActive = false;
        navigationOrchestrator.stopLiveUpdates();
    }

    /**
     * Shows or hides start navigation or recalculate buttons depending on start point if
     * it was the current location selected or not
     */
    private void setNavigationButtonsVisibility(){
        startNavigationButton.setVisibility(
                navigationOptionsController.isCurrentLocationSelected() ? View.VISIBLE : View.GONE);
        recalculateRouteButton.setVisibility(
                navigationOptionsController.isCurrentLocationSelected() ? View.GONE : View.VISIBLE);
    }

    /**
     * Sets the start navigation and recalculate buttons on click listeners actions
     */
    private void setNavigationButtonsEvent(){
        recalculateRouteButton.setOnClickListener(event -> {
            navigationOptionsController.setStartPoint(CurrentLocationModel.getInstance().getLatLng());
            closeNavigation();
            navigationOptionsController.setIsCurrentLocationSelected(true);
            navigationOptionsController.handleAcceptButtonClick();
        });

        startNavigationButton.setOnClickListener(event -> {
            startLiveUpdates();
            long startTime = System.currentTimeMillis();

            navigationDirectionController.getDirectionsForm().startAnimation(Animations.entryAnimation);
            navigationDirectionController.getDirectionsForm().setVisibility(View.VISIBLE);
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            int elapsedSeconds2 = (int) (elapsedTime / 1000);
            setElapsedSeconds(elapsedSeconds2);
            Toast.makeText(layout.getContext(), "Navigation mode started", Toast.LENGTH_SHORT).show();
            startNavigationButton.setVisibility(View.GONE);
        });
    }

    /**
     * This method shows which route is selected
     */
    private void setRouteInformation(int index, boolean isRouteASelected, boolean isRouteBSelected, boolean isRouteCSelected) {
        setTimeAndDistanceInformation(jsonObjects.get(index));
        this.isRouteASelected = isRouteASelected;
        this.isRouteBSelected = isRouteBSelected;
        this.isRouteCSelected = isRouteCSelected;
        updateButtonVisibility();
    }

    /**
     * This method sets the visibility of the button.
     */
    private void updateButtonVisibility() {
        buttonRouteA.setAlpha(isRouteASelected ? 1f : 0.3f);
        buttonRouteB.setAlpha(isRouteBSelected ? 1f : 0.3f);
        buttonRouteC.setAlpha(isRouteCSelected ? 1f : 0.3f);
    }

    /**
     * Shows the route information view, including distance and time.
     *
     * @param jsonCoordinates The list of JSON coordinates representing the routes.
     * @param mapPolyline     The MapPolyline object for displaying the routes.
     */
    public void showInfoRoute(List<String> jsonCoordinates, MapPolyline mapPolyline) {
        Toast.makeText(layout.getContext(), "Drawing routes", Toast.LENGTH_SHORT).show();
        this.mapPolyline = mapPolyline;
        isActive = true;
        layout.setVisibility(View.VISIBLE);
        jsonObjects = new ArrayList<>();
        try {
            setNavigationButtonsVisibility();

            for (String currentJson : jsonCoordinates)
                jsonObjects.add(new JSONObject(currentJson));
            setColorsInfoRoutes(jsonCoordinates.size());
            setTimeAndDistanceInformation(jsonObjects.get(jsonObjects.size() - 1));
            mapPolyline.displaySavedCoordinates(jsonCoordinates, colorsInfoRoutes);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the layout of the route information view.
     *
     * @return The ConstraintLayout object representing the layout.
     */
    public ConstraintLayout getLayout() {
        return layout;
    }

    /**
     * Checks if the route information view is active.
     *
     * @return true if the view is active, false otherwise.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the distance and time information based on the provided JSON object.
     *
     * @param jsonObject The JSON object containing the distance and time information.
     */
    private void setTimeAndDistanceInformation(JSONObject jsonObject) {
        setDistanceInfo(jsonObject);
        setEstimatedTimeInfo(jsonObject);
    }

    /**
     * Sets the distance information based on the provided JSON object.
     *
     * @param jsonObject The JSON object containing the distance information.
     */
    private void setDistanceInfo(JSONObject jsonObject) {
        try {
            double meters = jsonObject.getDouble("distance");
            double kilometers = 0;
            while (meters - 1 > 0) {
                meters -= 1;
                kilometers++;
            }
            int decimals = 3;
            DecimalFormat decimalFormat = new DecimalFormat("#." + "0".repeat(decimals));
            if (kilometers > 0) distanceText.setText(
                    kilometers + " km " + decimalFormat.format(meters).substring(1) + " m");
            else distanceText.setText(decimalFormat.format(meters).substring(1) + " m.");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the estimated time information based on the provided JSON object.
     *
     * @param jsonObject The JSON object containing the estimated time information.
     */
    private void setEstimatedTimeInfo(JSONObject jsonObject) {
        try {
            int timeInMinutes = jsonObject.getInt("time");
            int hours = 0;
            while (timeInMinutes - 60 > 0) {
                timeInMinutes -= 60;
                hours++;
            }
            if (hours > 0) timeText.setText(String.format("%s h %s min", hours, timeInMinutes));
            else timeText.setText(String.format("%s min", timeInMinutes));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets a given distance[m] to the view.
     *
     * @param meters The distance amount
     */
    public void setDistanceInfo(double meters) {
        double kilometers = 0;
        while (meters - 1 > 0) {
            meters -= 1;
            kilometers++;
        }
        int decimals = 3;
        DecimalFormat decimalFormat = new DecimalFormat("#." + "0".repeat(decimals));
        if (kilometers > 0) distanceText.setText(
                kilometers + " km " + decimalFormat.format(meters).substring(1) + " m");
        else distanceText.setText(decimalFormat.format(meters).substring(1) + " m.");
    }

    /**
     * Sets a given time[min] to the view.
     *
     * @param timeInMinutes The amount of time to be set.
     */
    public void setEstimatedTimeInfo(int timeInMinutes) {
        int hours = 0;
        while (timeInMinutes - 60 > 0) {
            timeInMinutes -= 60;
            hours++;
        }
        if (hours > 0) timeText.setText(hours + " h " + timeInMinutes + " min");
        else timeText.setText(timeInMinutes + " min");
    }

    /**
     * Sets the thread used for the live estimations
     */
    public void startLiveUpdates(){
        try {
            UserRouteTracker userRouteTracker = navigationOrchestrator.getUserRouteTracker();
            new LiveRouteEstimationsWorker(userRouteTracker, this, transportationType);
            new RouteDirectionsProvider(userRouteTracker, navigationDirectionController);
            navigationOrchestrator.startNavigationMode((t, e) -> {
                Toast.makeText(layout.getContext(), "Navigation mode interrupted", Toast.LENGTH_SHORT).show();
                Timber.e(e);
                navigationOrchestrator.stopLiveUpdates();
            });
        }catch (Exception e){
            Toast.makeText(layout.getContext(), "Navigation mode startup has failed", Toast.LENGTH_LONG).show();
            closeNavigation();
            Timber.e(e);
        }
    }
    /**
     * help me to obtain the routes
     * @return routes
     */
    public String getRoutes() {
        return routes;
    }

    /**
     * help me to set the routes
     */
    public void setRoutes(String routes) {
        this.routes = routes;
    }

    /**
     * help me to set the time Estimate
     */
    public void setTimeEstimate(int timeEstimate) {
        this.timeEstimate = timeEstimate;
    }

    /**
     * help me to obtain the time Estimate
     * @return time Estimate
     */
    public int getTimeEstimate() {
        return timeEstimate;
    }

    /**
     * help me to obtain the elapsed Seconds
     * @return elapsed Seconds
     */
    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    /**
     * help me to set the elapsed Seconds
     */
    public void setElapsedSeconds(int elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    /**
     * Sets the transportation type.
     *
     * @param transportationType the transportation type as enum.
     */
    public void setTransportationType(TransportationType transportationType) {
        this.transportationType = transportationType;
    }

    /**
     * Method to delete all polyline routes
     */
    public void deletePolylineRoutes() {
        if (mapPolyline != null) mapPolyline.hidePolylines();
    }

    /**
     * Performs the protocol when the user has arrived at their destination.
     * This method displays two Toast messages and then closes the navigation mode.
     * It ensures that the UI-related operations are executed on the main thread.
     */
    public void performUserHasArrivedProtocol(){
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(layout.getContext(), "Destination reached", Toast.LENGTH_SHORT).show();
            Toast.makeText(layout.getContext(), "Closing navigation mode", Toast.LENGTH_SHORT).show();
            closeNavigation();
        });
    }

    /**
     * Method to get the selected route
     * @return selected route
     */
    public String getSelectedRoute() {
        return selectedRoute;
    }

    public void resetButtonsVisibility(){
        buttonRouteA.setVisibility(View.VISIBLE);
        buttonRouteB.setVisibility(View.VISIBLE);
        buttonRouteC.setVisibility(View.VISIBLE);
    }

    public void verifyButtonsVisibility(){
        RoutesRepository repository = RoutesRepository.getInstance();
        resetButtonsVisibility();

        if (!repository.hasKey("Route A")){
            buttonRouteA.setVisibility(View.GONE);
            Timber.d("Route A can't be found button hidden");
        }

        if (!repository.hasKey("Route B")){
            buttonRouteB.setVisibility(View.GONE);
            Timber.d("Route B can't be found button hidden");
        }

        if (!repository.hasKey("Route C")){
            buttonRouteC.setVisibility(View.GONE);
            Timber.d("Route C can't be found button hidden");
        }

        buttonRouteA.requestFocus();
    }
}
