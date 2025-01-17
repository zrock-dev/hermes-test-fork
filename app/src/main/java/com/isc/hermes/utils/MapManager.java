package com.isc.hermes.utils;


import com.isc.hermes.controller.interfaces.MapClickConfigurationController;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

/**
 * Class to controll click controller that mapbox map will adopt
 */
public class MapManager {

    private static MapManager instance;
    private MapboxMap mapboxMap;
    private MapClickConfigurationController configurationController;
    private boolean isOffLineMode;


    public static MapManager getInstance() {
        if (instance == null) instance = new MapManager();
        return instance;
    }

    /**
     * Method to set mapbox map when this instance will init
     *
     * @param mapboxMap is map will be set
     */
    public void setMapboxMap(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    /**
     * Method to add a new mapbox map click configuration
     *
     * @param controller is controller that will map adopt to control its click event
     */
    public void setMapClickConfiguration(MapClickConfigurationController controller) {
        this.configurationController = controller;
        mapboxMap.addOnMapClickListener(configurationController);
    }

    /**
     * Method to remove current click controller map have
     */
    public void removeCurrentClickController() {
        configurationController.discardFromMap(this.getMapboxMap());
    }


    /**
     * This method animates the camera position on the Mapbox map to the specified latitude and longitude with a given zoom level. * * @param latLng The latitude and longitude to move the camera to. * @param zoom The zoom level for the camera position.
     */
    public void animateCameraPosition(LatLng latLng, double zoom) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(zoom).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 1500);
    }

    /**
     * Method to obtain current mapbox map
     * @return map from this class
     */
    public MapboxMap getMapboxMap() {
        return this.mapboxMap;
    }

    /**
     * Method to set this class to offLine state
     * @param isOfflineMode is param to set
     */
    public void setOfflineMode(boolean isOfflineMode){this.isOffLineMode = isOfflineMode;}

    /**
     * Method to obtain current state of manager
     * @return True if is offline, False if is Online
     */
    public boolean isOffLine(){return this.isOffLineMode;}
}
