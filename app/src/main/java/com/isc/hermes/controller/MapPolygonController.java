package com.isc.hermes.controller;

import android.content.Context;

import androidx.annotation.NonNull;

import com.isc.hermes.controller.interfaces.MapClickConfigurationController;

import com.isc.hermes.generators.CoordinateParser;
import com.isc.hermes.utils.Animations;

import com.isc.hermes.view.MapPolygonStyle;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * Class to validate and draw polygon on map with each user click on mapbox map
 */
public class MapPolygonController implements MapClickConfigurationController {
    private final MapboxMap mapboxMap;
    private final PolygonOptionsController polygonOptionsController;
    private final List<List<Point>> allPolygonsPoints;
    private List<Point> polygonPoints;
    private List<Double[]> coordinates;
    private CoordinateParser coordinateParser;
    private GeometryFactory geometryFactory;

    public MapPolygonController(MapboxMap mapboxMap, Context context ) {
        this.mapboxMap = mapboxMap;
        polygonOptionsController = new PolygonOptionsController(context, this);
        polygonOptionsController.displayComponents();
        Animations.loadAnimations();
        this.allPolygonsPoints = new ArrayList<>();
        this.polygonPoints = new ArrayList<>();
        this.allPolygonsPoints.add(polygonPoints);
        coordinates = new ArrayList<>();
        coordinateParser = new CoordinateParser();
        geometryFactory = new GeometryFactory();
        polygonOptionsController.showPolygonMessage();
    }

    /**
     * This method is used to sort all points selected by user and sort them to make a valid polygon
     */
    private void buildPolygon() {
        polygonPoints.clear();
        List<Coordinate> polygonCoordinates = coordinateParser.parseToCoordinates(coordinates);
        DelaunayTriangulationBuilder triangulationBuilder = new DelaunayTriangulationBuilder();
        triangulationBuilder.setSites(polygonCoordinates);
        Geometry polygon = triangulationBuilder.getTriangles(geometryFactory).union();
        List<Double[]> coordinates = coordinateParser.parseToDoubles(polygon);
        coordinates.forEach(doubles -> {
            polygonPoints.add(Point.fromLngLat(doubles[0], doubles[1]));
        });

    }

    /**
     * Method to set action when map is clicked
     * @param point The projected map coordinate the user clicked on.
     * @return false
     */
    public boolean onMapClick(@NonNull LatLng point) {
        coordinates.add(new Double[]{point.getLongitude(), point.getLatitude()});

        if (coordinates.size() > 2) {
            buildPolygon();
            new MapPolygonStyle(this.mapboxMap,this.allPolygonsPoints);
        }else {
            polygonOptionsController.showPolygonVertexesMessage(3-coordinates.size());
        }
        return false;
    }

    /**
     * Method to discard this controller from map selected
     * @param mapboxMap is mapbox map witch will disable this controller
     */
    public void discardFromMap(MapboxMap mapboxMap) {
        mapboxMap.removeOnMapClickListener(this);
    }

    /**
     * Method to delete all marker from map
     */
    public void deleteMarks() {
        for (Marker marker:mapboxMap.getMarkers()) {
            mapboxMap.removeMarker(marker);
        }
    }
}
