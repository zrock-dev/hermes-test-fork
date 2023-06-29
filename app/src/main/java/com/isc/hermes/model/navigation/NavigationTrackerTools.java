package com.isc.hermes.model.navigation;

import com.isc.hermes.utils.CoordinatesDistanceCalculator;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * The NavigationTrackerTools class provides utility methods for navigation tracking.
 */
public class NavigationTrackerTools {
    public static double CLOSE_ENOUGH = 10;

    /**
     * Checks if a point has been reached based on the target and the current point.
     *
     * @param target The target point to reach.
     * @param point  The current point.
     * @return true if the point has been reached, false otherwise.
     */
    public static boolean isPointReached(LatLng target, LatLng point) {
        CoordinatesDistanceCalculator distanceCalculator = CoordinatesDistanceCalculator.getInstance();
        double distance = distanceCalculator.calculateDistance(point, target);
        return distance <= CLOSE_ENOUGH;
    }

    /**
     * Checks if a point is inside a given route segment.
     *
     * @param segment The route segment.
     * @param point   The point to check.
     * @return true if the point is inside the segment, false otherwise.
     */
    public static boolean isInsideSegment(RouteSegmentRecord segment, LatLng point) {
        int floorLng = Double.compare(segment.getStart().getLongitude(), point.getLongitude());
        int floorLat = Double.compare(segment.getStart().getAltitude(), point.getLatitude());

        boolean isHigherThanLowerPoint = (floorLat >= 0) && (floorLng >= 0);

        int ceilLng = Double.compare(segment.getEnd().getLongitude(), point.getLongitude());
        int ceilLat = Double.compare(segment.getEnd().getAltitude(), point.getLatitude());
        boolean isLowerThanHigherPoint = (ceilLat >= 0) && (ceilLng >= 0);

        return isLowerThanHigherPoint && isHigherThanLowerPoint;
    }
}