package com.isc.hermes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import com.isc.hermes.controller.authentication.AuthenticationFactory;
import com.isc.hermes.controller.authentication.AuthenticationServices;
import com.isc.hermes.model.MapboxEventManager;
import com.isc.hermes.model.MapboxMapListener;
import com.isc.hermes.model.Searcher;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.isc.hermes.controller.CurrentLocationController;
import com.isc.hermes.utils.MapConfigure;
import com.isc.hermes.view.MapDisplay;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

/**
 * Class for displaying a map using a MapView object and a MapConfigure object.
 * Handles current user location functionality.
 */
public class MainActivity extends AppCompatActivity implements MapboxMapListener {
    private MapView mapView;
    private MapDisplay mapDisplay;
    private String mapStyle = "default";
    private CurrentLocationController currentLocationController;
    private boolean visibilityMenu = false;
    private SearchView searchView;

    /**
     * Method for creating the map and configuring it using the MapConfigure object.
     *
     * @param savedInstanceState the saved state of the instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMapbox();
        setContentView(R.layout.activity_main);
        initMapView();
        mapDisplay = MapDisplay.getInstance(this, mapView, new MapConfigure());
        mapDisplay.onCreate(savedInstanceState);
        mapStyleListener();
        initCurrentLocationController();
        searchView = findViewById(R.id.searchView);
        changeSearchView();
    }

    /**
<<<<<<< HEAD
     * Method to add the searcher to the main scene above the map
     */
    private void addMapboxSearcher() {
        Searcher searcher = new Searcher();
        /*SearcherController searcherController = new SearcherController(searcher,
                findViewById(R.id.searchResults),findViewById(R.id.searchView));
        searcherController.runSearcher();*/
        MapboxEventManager mapboxEventManager = MapboxEventManager.getInstance();
        mapboxEventManager.setMapboxMap(mapDisplay.getMapboxMap());

    }

    /**
=======
>>>>>>> feature/ID-198/ImplementationOfWaypointsWithTheSearcher
     * This method is used to display a view where you can see the information about your account.
     *
     * @param view Helps build the view
     */
    public void showAccount(View view) {
        System.out.println("Your account information will be displayed");
    }

    /**
     *This function helps to give functionality to the side menu, so that it can be visible and hidden, when necessary.
     *
     * @param view Helps build the view.
     */
    public void openSideMenu(View view) {
        LinearLayout lateralMenu = findViewById(R.id.lateralMenu);
        if (!visibilityMenu) {
            lateralMenu.setVisibility(View.VISIBLE);
            visibilityMenu = true;
            setMapScrollGesturesEnabled(false);
        } else {
            lateralMenu.setVisibility(View.GONE);
            visibilityMenu = false;
            setMapScrollGesturesEnabled(true);
        }
    }

    private void changeSearchView() {
        searchView.setOnClickListener(v -> {
            addMapboxSearcher();
            new Handler().post(() -> {
                Intent intent = new Intent(MainActivity.this, SearchViewActivity.class);
                startActivity(intent);
            });
        });
    }

    /**
     * Enables or disables map scroll gestures.
     *
     * @param enabled Boolean indicating whether to enable map scroll gestures.
     */
    private void setMapScrollGesturesEnabled(boolean enabled) {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.getUiSettings().setScrollGesturesEnabled(enabled);
            }
        });
    }

    /**
     * Logs out the current user and redirects to the login activity.
     *
     * @param view The view of the button that has been clicked.
     */
    public void logOut(View view){
        SignUpActivityView.authenticator.signOut(this);
        Intent intent = new Intent(MainActivity.this, SignUpActivityView.class);
        startActivity(intent);
    }

    /**
     * This method will init the current location controller to get the real time user location
     */
    private void initCurrentLocationController(){
        currentLocationController = new CurrentLocationController(this, mapDisplay);
        currentLocationController.initLocation();
    }


    /**
     * Method for initializing the Mapbox object instance.
     */
    private void initMapbox() {
        Mapbox.getInstance(this, getString(R.string.access_token));
    }

    /**
     * Method for initializing the MapView object instance.
     */
    private void initMapView() {
        mapView = findViewById(R.id.mapView);
    }

    /**
     * Method for initializing the MapDisplay object instance.
     */
    private void initMapDisplay() {
        mapDisplay = new MapDisplay(this, mapView, new MapConfigure());
    }

    /**
     * Method for starting the MapView object instance.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mapDisplay.onStart();
    }

    /**
     * Method for resuming the MapView object instance.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapDisplay.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                this);
        AuthenticationServices authenticationServices  = AuthenticationServices.getAuthentication(
                sharedPreferences.getInt("cuenta",0));
        if(authenticationServices != null)
            SignUpActivityView.authenticator = AuthenticationFactory.createAuthentication(
                    authenticationServices);

        SharedPreferences sharedPreferences2 = getSharedPreferences("com.isc.hermes", Context.MODE_PRIVATE);
        String placeName = sharedPreferences2.getString("placeName", null);
        double latitude = sharedPreferences2.getFloat("latitude", 0);
        double longitude = sharedPreferences2.getFloat("longitude", 0);

        if (placeName != null) {
            addMarkerToMap(placeName, latitude, longitude);
        }
    }

    private Marker currentMarker;
    private void addMarkerToMap(String placeName, double latitude, double longitude) {
        mapView.getMapAsync(mapboxMap -> {
            if (currentMarker != null) {
                mapboxMap.removeMarker(currentMarker);
                SharedPreferences deleteCurrentMarker = getSharedPreferences("com.isc.hermes", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = deleteCurrentMarker.edit();
                editor.remove("placeName");
                editor.remove("latitude");
                editor.remove("longitude");
                editor.apply();
            }

            MarkerOptions options = new MarkerOptions();
            options.setTitle(placeName);
            options.position(new LatLng(latitude, longitude));
            currentMarker = mapboxMap.addMarker(options);});
    }


    /** Method for pausing the MapView object instance.*/
    @Override
    protected void onPause() {
        super.onPause();
        mapDisplay.onPause();
        SharedPreferences datos = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor miEditor = datos.edit();
        miEditor.putInt("cuenta", SignUpActivityView.authenticator.getServiceType().getID());
        miEditor.apply();}

    /**
     * Method for stopping the MapView object instance.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mapDisplay.onStop();
    }

    /**
     * Method for handling low memory.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapDisplay.onLowMemory();
    }

    /**
     * Method for destroying the MapView object instance.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapDisplay.onDestroy();
    }

    /**
     * Method for saving the state of the MapView object instance.
     *
     * @param outState the state of the instance
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapDisplay.onSaveInstanceState(outState);
    }

    /**
     * Method for adding maps styles.xml listener
     */
    private void mapStyleListener(){
        ImageButton styleButton = findViewById(R.id.btn_change_style);
        styleButton.setOnClickListener(styleMap -> {
            if (mapStyle.equals("default")) mapStyle = "satellite";
            else if (mapStyle.equals("satellite")) mapStyle = "dark";
            else mapStyle = "default";
            mapDisplay.setMapStyle(mapStyle);
        });
    }

    @Override
    public void addMarker(MarkerOptions markerOptions) {
        mapDisplay.getMapboxMap().addMarker(markerOptions);
    }
}