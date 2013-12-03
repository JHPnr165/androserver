/*
 * Copyright (c) 2012 Romain Gallet
 *
 * This file is part of Geocalc.
 *
 * Geocalc is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Geocalc is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Geocalc. If not, see http://www.gnu.org/licenses/.
 */

package com.grum.geocalc;

import static java.lang.Math.*;

/**
 * Earth related calculations.
 *
 * @author rgallet
 */
public class EarthCalc {

    public static final double EARTH_DIAMETER = 6371.01 * 1000; //meters

    /**
     * Returns the distance between two points
     *
     * @param standPoint The stand point
     * @param forePoint  The fore point
     * @return The distance, in meters
     */
    public static double getDistance(Point standPoint, Point forePoint) {

        double diffLongitudes = toRadians(abs(forePoint.getLongitude() - standPoint.getLongitude()));
        double slat = toRadians(standPoint.getLatitude());
        double flat = toRadians(forePoint.getLatitude());

        //spherical law of cosines
        double c = acos((sin(slat) * sin(flat)) + (cos(slat) * cos(flat) * cos(diffLongitudes)));

        //Vincenty formula
//        double c = sqrt(pow(cos(flat) * sin(diffLongitudes), 2d) + pow(cos(slat) * sin(flat) - sin(slat) * cos(flat) * cos(diffLongitudes), 2d));
//        c = c / (sin(slat) * sin(flat) + cos(slat) * cos(flat) * cos(diffLongitudes));
//        c = atan(c);

        return EARTH_DIAMETER * c;
    }

    /**
     * Returns the bearing, in decimal degrees, from standPoint to forePoint
     *
     * @param standPoint
     * @param forePoint
     * @return bearing, in decimal degrees
     */
    public static double getBearing(Point standPoint, Point forePoint) {
        double latitude1 = toRadians(standPoint.getLatitude());
        double longitude1 = standPoint.getLongitude();

        double latitude2 = toRadians(forePoint.getLatitude());
        double longitude2 = forePoint.getLongitude();

        double longDiff = toRadians(longitude2 - longitude1);

        //invertedBearing because it represents the angle, in radians, of standPoint from forePoint's point of view
        //we want the opposite
        double invertedBearing = ((atan2(sin(longDiff) * cos(latitude2),
                cos(latitude1) * sin(latitude2) - sin(latitude1) * cos(latitude2) * cos(longDiff))));

        double rbearing = (-invertedBearing + 2 * PI) % (2 * PI);

        return toDegrees(rbearing);
    }
}
