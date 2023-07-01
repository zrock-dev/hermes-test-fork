package com.isc.hermes.model.navigation;

import static com.isc.hermes.model.navigation.NavigationTrackerTools.isPointInsideSegment;

import com.isc.hermes.model.CurrentLocationModel;
import com.isc.hermes.model.navigation.exceptions.RouteOutOfTracksException;
import com.isc.hermes.model.navigation.exceptions.UnrecoverableTrackException;
import com.isc.hermes.model.navigation.exceptions.UserOutsideTrackException;
import com.isc.hermes.utils.CoordinatesDistanceCalculator;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * The UserRouteTracker class tracks the user's route and provides information about the user's progress.
 */
public class UserRouteTracker {

    private final CoordinatesDistanceCalculator distanceCalculator;
    private final CurrentLocationModel currentLocation;
    private final UserLocationTracker userLocationTracker;
    private List<RouteSegmentRecord> routeSegments;
    private RouteSegmentRecord currentSegment;
    private int routeSegmentIndex;
    private JSONObject routeInformation;
    private LatLng usersDestination;
    private boolean isUserTrackLost;
    private TrackRecoveryProtocol trackRecoveryProtocol;

    /**
     * Constructs a UserRouteTracker object with the provided GeoJSON route information.
     *
     * @param geoJSON The GeoJSON route information.
     */
    public UserRouteTracker(String geoJSON) {
        try {
            routeInformation = new JSONObject(geoJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        distanceCalculator = CoordinatesDistanceCalculator.getInstance();
        currentLocation = CurrentLocationModel.getInstance();
        userLocationTracker = new UserLocationTracker();
        routeSegmentIndex = 0;
        isUserTrackLost = false;
    }

    public void parseRoute() {
        routeSegments = NavigationRouteParser.makeRouteSegments(routeInformation);
        if (routeSegments.isEmpty()) {
            throw new RouteOutOfTracksException("Could not start the navigation, route is empty");
        }
        usersDestination = routeSegments.get(routeSegments.size() - 1).getEnd();
        routeSegments.get(0).setStart(currentLocation.getLatLng());
        trackRecoveryProtocol = new TrackRecoveryProtocol(routeSegments);
        System.out.printf("Starting route with: %s Tracks\n", routeSegments.size());

        try {
            pickNextTrack(currentLocation.getLatLng());
        } catch (UserOutsideTrackException e) {
            recoverTrack(false);
        }
    }

    /**
     * Gets the distance remaining in the current route segment until reaching the end point.
     *
     * @return The distance remaining in the current route segment.
     */
    public double getTraveledDistance() {
        LatLng userLocation = currentLocation.getLatLng();
        if (!isUserTrackLost) {
            if (!isUserOnTrack(userLocation)) {
                try {
                    pickNextTrack(userLocation);
                } catch (UserOutsideTrackException e) {
                    recoverTrack(false);
                }

                return distanceCalculator.calculateDistance(
                        userLocation,
                        currentSegment.getEnd()
                );
            }
            System.out.printf("User on track #%d\n", routeSegmentIndex);
        } else {
            recoverTrack(true);
        }

        return 0.0;
    }

    /**
     * Updates the current route segment based on the user's current location.
     * If there are available segments, the next segment is assigned as the current segment.
     * If the user is outside the current segment, a UserOutsideRouteException is thrown.
     *
     * @param userLocation The user's current location.
     * @throws UserOutsideTrackException If the user is outside the current route segment.
     */
    private void pickNextTrack(LatLng userLocation) throws UserOutsideTrackException {
        if (!routeSegments.isEmpty() && routeSegmentIndex < routeSegments.size()) {
            currentSegment = routeSegments.get(routeSegmentIndex);
            routeSegmentIndex++;

            if (!isPointInsideSegment(currentSegment, userLocation, isUserTrackLost)) {
                throw new UserOutsideTrackException(currentSegment, userLocation);
            }

        } else {
            throw new RouteOutOfTracksException("The current route does not have any more unvisited tracks");
        }
    }


    /**
     * Checks if the user has arrived at the destination point.
     *
     * @return true if the user has arrived, false otherwise.
     */
    public boolean hasUserArrived() {
        return NavigationTrackerTools.isPointReached(usersDestination, currentLocation.getLatLng());
    }

    /**
     * Checks if the user is inside the current route segment.
     *
     * @param userLocation The user's current location.
     * @return true if the user is inside the current route segment, false otherwise.
     */
    public boolean isUserOnTrack(LatLng userLocation) {
        boolean isInsideSegment = isPointInsideSegment(currentSegment, userLocation, isUserTrackLost);
        boolean isEndReached = NavigationTrackerTools.isPointReached(currentSegment.getEnd(), userLocation);

        return isInsideSegment && !isEndReached;
    }

    /**
     * Gets the route information in the form of a JSON object.
     *
     * @return The route information JSON object.
     */
    public JSONObject getRouteInformation() {
        return routeInformation;
    }

    public double getUnvisitedRouteSize() {
        double totalDistance = 0.0;
        for (int i = routeSegmentIndex; i < routeSegments.size(); i++) {
            totalDistance += routeSegments.get(i).getDistance();
        }

        return totalDistance;
    }

    public boolean hasUserMoved() {
        return userLocationTracker.hasUserMoved();
    }

    private void recoverTrack(boolean deepAttempt) {
        if (deepAttempt) {
            trackRecoveryProtocol.attemptDeepRecovery();
        } else {
            trackRecoveryProtocol.attemptSimpleRecovery(routeSegmentIndex);
        }

        if (trackRecoveryProtocol.isAttemptSuccessful()) {
            System.err.print("User has been found");
            currentSegment = trackRecoveryProtocol.getTrack();
            routeSegments.indexOf(currentSegment);
            isUserTrackLost = false;

        } else if (deepAttempt) {
            System.out.println("Deep recovery has failed");
            throw new UnrecoverableTrackException();
        } else {
            isUserTrackLost = true;
            System.err.println("Track of the user has been lost");
        }
    }
}
