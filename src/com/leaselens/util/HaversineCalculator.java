package com.leaselens.util;

/**
 * This class is calculating distance between two places on earth
 * It use the haversine formula with latitude and longitude numbers
 * We use this to find how far apartment is from T stops
 *
 * pre-condition: none
 * post-condition: none
 */
public class HaversineCalculator {

    /**
     * This method is calculating distance in miles between two points
     * @param lat1 latitude of first place
     * @param lon1 longitude of first place
     * @param lat2 latitude of second place
     * @param lon2 longitude of second place
     * @return distance in miles
     *
     * pre-condition: lat and lon should be valid numbers
     * post-condition: distance in miles is returned
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 3958.8; // earth radius in miles

        // convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = earthRadius * c;

        // round to 2 decimal places
        distance = Math.round(distance * 100.0) / 100.0;

        return distance;
    }
}
