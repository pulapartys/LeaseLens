package com.leaselens.model;

/**
 * This class is storing info about one nearby place like a restaurant or shop
 * It have the name, what type it is, what category, and how far from apartment
 *
 * pre-condition: none
 * post-condition: none
 */
public class NearbyPlace {

    // the name of the place like "Starbucks"
    private String name;

    // the type like "restaurant" or "cafe" or "convenience"
    private String type;

    // the category like "food" or "shop" or "service" or "leisure"
    private String category;

    // the latitude and longitude of this place
    private double latitude;
    private double longitude;

    // how far this place is from the apartment in meters
    private double distance;

    /**
     * This constructor is making a new NearbyPlace with all the info
     * @param name the name of the place
     * @param type the specific type like "restaurant"
     * @param category the big category like "food"
     * @param latitude the lat of the place
     * @param longitude the lon of the place
     * @param distance how far from apartment in meters
     *
     * pre-condition: name should not be empty
     * post-condition: new NearbyPlace is created
     */
    public NearbyPlace(String name, String type, String category, double latitude, double longitude, double distance) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    /**
     * This is getting the name
     * @return the name of the place
     *
     * pre-condition: none
     * post-condition: name is returned
     */
    public String getName() {
        return name;
    }

    /**
     * This is getting the type
     * @return the type like "restaurant"
     *
     * pre-condition: none
     * post-condition: type is returned
     */
    public String getType() {
        return type;
    }

    /**
     * This is getting the category
     * @return the category like "food"
     *
     * pre-condition: none
     * post-condition: category is returned
     */
    public String getCategory() {
        return category;
    }

    /**
     * This is getting the latitude
     * @return the latitude
     *
     * pre-condition: none
     * post-condition: latitude is returned
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * This is getting the longitude
     * @return the longitude
     *
     * pre-condition: none
     * post-condition: longitude is returned
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * This is getting the distance from apartment
     * @return distance in meters
     *
     * pre-condition: none
     * post-condition: distance is returned
     */
    public double getDistance() {
        return distance;
    }

    /**
     * This is making a nice string to show the place
     * @return string like "Starbucks (cafe) - 150m"
     *
     * pre-condition: none
     * post-condition: string is returned
     */
    public String toString() {
        int distRounded = (int) Math.round(distance);
        return name + " (" + type + ") - " + distRounded + "m";
    }
}
