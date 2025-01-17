package com.isc.hermes.model.navigation;

import androidx.annotation.Nullable;

import com.isc.hermes.model.graph.Node;
import com.isc.hermes.utils.CoordinatesDistanceCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a route in a navigation system.
 * A route consists of a sequence of nodes and the total distance traveled along the route.
 */
public class Route {
    private List<Node> path;
    private double totalEstimatedDistance;
    private int totalEstimatedArrivalTime;

    /**
     * Constructs a new Route object starting from the initial node.
     *
     * @param initialNode the initial node of the route
     */
    public Route(Node initialNode) {
        path = new ArrayList<>();
        path.add(initialNode);
        totalEstimatedDistance = 0.0;
    }

    /**
     * Constructs a new Route object with the same path and total distance as another route.
     *
     * @param other the other route to copy the path and total distance from
     */
    public Route(Route other, TransportationType transportationType) {
        path = new ArrayList<>(other.path);
        totalEstimatedDistance = other.totalEstimatedDistance;
        setTotalEstimatedArrivalTime(transportationType);
    }

    /**
     * Constructs a new Route object with the given path.
     *
     * @param path the path of nodes in the route
     */
    public Route(List<Node> path, TransportationType transportationType) {
        this.path = path;
        CoordinatesDistanceCalculator calculator = CoordinatesDistanceCalculator.getInstance();
        for (int i = 0; i < path.size() - 1; i++) {
            totalEstimatedDistance += calculator.calculateDistance(path.get(i), path.get(i + 1));
        }
        setTotalEstimatedArrivalTime(transportationType);
    }

    /**
     * Sets the total estimated arrival time within the transportation type.
     * This will get the estimated arrival time with the transportation velocity
     * @param transportationType the transportation to be used
     */
    private void setTotalEstimatedArrivalTime(TransportationType transportationType) {
        totalEstimatedArrivalTime =
                (int) Math.ceil(((totalEstimatedDistance / transportationType.getVelocity()) * 60));
    }

    /**
     * Retrieves the total estimated arrival time of the route.
     *
     * @return the total estimated arrival time in minutes
     */
    public int getTotalEstimatedArrivalTime() {
        return totalEstimatedArrivalTime;
    }

    /**
     * Adds a node to the route and increases the total distance by the given distance.
     *
     * @param node     the node to add to the route
     * @param distance the distance to travel to the new node
     */
    public void addNode(Node node, double distance) {
        path.add(node);
        totalEstimatedDistance += distance;
    }

    /**
     * Retrieves the path of nodes in the route.
     *
     * @return the list of nodes representing the path
     */
    public List<Node> getPath() {
        return path;
    }

    /**
     * Retrieves the total distance traveled along the route.
     *
     * @return the total distance
     */
    public double getTotalEstimatedDistance() {
        return totalEstimatedDistance;
    }

    /**
     * Retrieves the current node at the end of the route.
     *
     * @return the current node
     */
    public Node getCurrentNode() {
        return path.get(path.size() - 1);
    }

    /**
     * Checks if a specific node has been visited in the route.
     *
     * @param node the node to check for visitation
     * @return true if the node has been visited, false otherwise
     */
    public boolean visitedNode(Node node) {
        return path.contains(node);
    }

    /**
     * Checks if the current route is equal to the given object.
     * Two routes are considered equal if their paths contain the same nodes.
     *
     * @param obj the object to compare for equality
     * @return true if the routes are equal, false otherwise
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof Route route)) return false;
        return this.path.containsAll(route.path);
    }
}
