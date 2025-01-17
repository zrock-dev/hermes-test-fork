package com.isc.hermes.generators;

import com.isc.hermes.model.Radium;
import com.isc.hermes.model.incidents.GeometryType;

import java.util.ArrayList;
import java.util.List;

/**
 * This method has the responsibility to generate point coordinates data.
 */
public class PointGenerator extends CoordinateGen implements CoordinatesGenerable {

    private final int MAX_ATTEMPTS = 500;

    /**
     * This method generate a point coordinate within a habitable zone.
     *
     * @param referencePoint Receive a reference point to generate a point around this point.
     * @param radium Receives the radius in which the point will be generated.
     * @return point coordinate.
     */
    public Double[] getStreetPoint(Double[] referencePoint, Radium radium) {
        int indexAttemps = 0;
        Double[] randomPoint;
        Boolean found = false;
        do {
            randomPoint = getNearPoint(referencePoint, radium);
            indexAttemps++;
            if( streetValidator.isPointStreet(randomPoint[1], randomPoint[0])){
                found = true;
            }
        } while (!found && indexAttemps < MAX_ATTEMPTS);
        return randomPoint;
    }

    /**
     * This method generate a valid point surrounded by another as a reference.
     *
     * @param referencePoint to found others points.
     * @param radium the size radium valid to found a coordinates.
     * @return near point.
     */
    public Double[] getNearPoint(Double[] referencePoint, Radium radium) {
        double randomAngle = getRandomAngle();
        double distance = getNearDistance(radium);
        Double[] coordinate = new Double[2];
        coordinate[0] = referencePoint[0] + distance * Math.cos(randomAngle);
        coordinate[1] = referencePoint[1] + distance * Math.sin(randomAngle);
        return coordinate;
    }

    /**
     * Method to generate a point close to another using an angle for displacement.
     *
     * @param referencePoint to found others points.
     * @param radium the size radium valid to found a coordinates.
     * @param angle for horizontal and vertical displacement.
     * @return near point.
     */
    public Double[] getNearPoint(Double[] referencePoint, Radium radium, double angle) {
        double distance = radium.getValue();
        Double[] coordinate = new Double[2];
        coordinate[0] = referencePoint[0] + distance * Math.cos(angle);
        coordinate[1] = referencePoint[1] + distance * Math.sin(angle);
        return coordinate;
    }


    /**
     * This method generate a multi points coordinates list on a limit area.
     *
     * @param referencePoint to get the main point of limit area.
     * @param radium to get points of limit area.
     * @param amountPoints are the amount the points to found.
     * @return multi points coordinates.
     */
    public List<Double[]> getMultiPoint(
            Double[] referencePoint,
            Radium radium,
            int amountPoints
    ) {
        List<Double[]> coordinatesGenerated = new ArrayList<>();
        if (isValidReferencePoint(referencePoint))
        {
            if(amountPoints == 1){
                Double[] pointGenerated = getStreetPoint(referencePoint, radium);
                coordinatesGenerated.add(pointGenerated);
            }else {
                for (int i = 0; i < amountPoints; i++){
                    coordinatesGenerated.add(getNearPoint(referencePoint, radium));
                }
            }
        }
        return coordinatesGenerated;
    }

    /**
     * This method generates coordinates that form geometric objects.
     *
     * @param referencePoint to get the main point reference of limited range.
     * @param radium to get the radium using the reference point.
     * @param amountPoints It is used depending on the type of implementation and type of figure to be built.
     */
    @Override
    public List<Double[]> generate(
            Double[] referencePoint,
            Radium radium,
            int amountPoints
    ) {
        List<Double[]> points = new ArrayList<>();
        points.add(getNearPoint(referencePoint, radium));
        return points;
    }

    /**
     * This method returns the type of geometric object that is generated.
     *
     * @return TypeGeometry type.
     */
    @Override
    public GeometryType getTypeGeometry() {
        return GeometryType.POINT;
    }
}
