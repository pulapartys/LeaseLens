package com.leaselens.model;

import java.util.UUID;

/**
 * This class is storing all the info about one apartment
 * Like the name, address, rent, amenities, and API data
 *
 * pre-condition: none
 * post-condition: an Apartment object is created to store apartment info
 */
public class Apartment {

    // basic info
    private String id;
    private String name;
    private String address;
    private double rent;
    private double sqft;
    private int bedrooms;
    private int bathrooms;

    // amenities - true or false
    private boolean hasParking;
    private boolean hasLaundry;
    private boolean hasDishwasher;
    private boolean hasAC;
    private boolean petFriendly;
    private boolean furnished;

    // lease stuff
    private String availableDate;
    private int leaseLength;
    private boolean brokerFee;
    private boolean utilitiesIncluded;

    // where user found it
    private String source;
    private String sourceURL;
    private String notes;

    // data from APIs
    private double latitude;
    private double longitude;
    private int walkScore;
    private int transitScore;
    private int bikeScore;
    private String nearestTStop;
    private String nearestTLine;
    private double distanceToT;

    // nearby amenity counts from Overpass API
    private int nearbyFood;
    private int nearbyShops;
    private int nearbyServices;
    private int nearbyTransit;
    private int nearbyLeisure;
    private int nearbyBike;

    // crime and safety data from Boston Police API
    private int safetyScore;
    private int crimeCount;
    private String crimeBreakdown;

    // recreation data from Recreation.gov API
    private int recreationCount;
    private String nearbyRecreation;

    // status and dates
    private Status status;
    private String dateAdded;

    /**
     * This constructor is making a new apartment with the basic info
     * It also give it a random ID and set status to new
     * @param name what the user want to call this apartment
     * @param address the street address of the apartment
     * @param rent how much rent is per month
     * @param sqft how big the apartment is
     * @param bedrooms how many bedrooms it has
     * @param bathrooms how many bathrooms it has
     *
     * pre-condition: name and address should not be empty
     * post-condition: new apartment is created with ID and status NEW
     */
    public Apartment(String name, String address, double rent, double sqft, int bedrooms, int bathrooms) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.address = address;
        this.rent = rent;
        this.sqft = sqft;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.status = Status.NEW;
        this.dateAdded = java.time.LocalDate.now().toString();

        // default values
        this.hasParking = false;
        this.hasLaundry = false;
        this.hasDishwasher = false;
        this.hasAC = false;
        this.petFriendly = false;
        this.furnished = false;
        this.brokerFee = false;
        this.utilitiesIncluded = false;
        this.leaseLength = 12;
        this.source = "";
        this.sourceURL = "";
        this.notes = "";
        this.availableDate = "";
        this.nearestTStop = "";
        this.nearestTLine = "";
        this.walkScore = -1;
        this.transitScore = -1;
        this.bikeScore = -1;
        this.distanceToT = -1.0;
        this.nearbyFood = -1;
        this.nearbyShops = -1;
        this.nearbyServices = -1;
        this.nearbyTransit = -1;
        this.nearbyLeisure = -1;
        this.nearbyBike = -1;
        this.safetyScore = -1;
        this.crimeCount = -1;
        this.crimeBreakdown = "";
        this.recreationCount = -1;
        this.nearbyRecreation = "";
    }

    /**
     * Empty constructor needed for loading from JSON file
     *
     * pre-condition: none
     * post-condition: empty apartment object is created
     */
    public Apartment() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.status = Status.NEW;
    }

    /**
     * This method is making a copy of the apartment
     * We need copy for undo/redo so original dont get changed
     * @return a new apartment object with same values
     *
     * pre-condition: apartment should have values set
     * post-condition: a new copy is returned
     */
    public Apartment makeCopy() {
        Apartment copy = new Apartment();
        copy.id = this.id;
        copy.name = this.name;
        copy.address = this.address;
        copy.rent = this.rent;
        copy.sqft = this.sqft;
        copy.bedrooms = this.bedrooms;
        copy.bathrooms = this.bathrooms;
        copy.hasParking = this.hasParking;
        copy.hasLaundry = this.hasLaundry;
        copy.hasDishwasher = this.hasDishwasher;
        copy.hasAC = this.hasAC;
        copy.petFriendly = this.petFriendly;
        copy.furnished = this.furnished;
        copy.availableDate = this.availableDate;
        copy.leaseLength = this.leaseLength;
        copy.brokerFee = this.brokerFee;
        copy.utilitiesIncluded = this.utilitiesIncluded;
        copy.source = this.source;
        copy.sourceURL = this.sourceURL;
        copy.notes = this.notes;
        copy.latitude = this.latitude;
        copy.longitude = this.longitude;
        copy.walkScore = this.walkScore;
        copy.transitScore = this.transitScore;
        copy.bikeScore = this.bikeScore;
        copy.nearestTStop = this.nearestTStop;
        copy.nearestTLine = this.nearestTLine;
        copy.distanceToT = this.distanceToT;
        copy.nearbyFood = this.nearbyFood;
        copy.nearbyShops = this.nearbyShops;
        copy.nearbyServices = this.nearbyServices;
        copy.nearbyTransit = this.nearbyTransit;
        copy.nearbyLeisure = this.nearbyLeisure;
        copy.nearbyBike = this.nearbyBike;
        copy.safetyScore = this.safetyScore;
        copy.crimeCount = this.crimeCount;
        copy.crimeBreakdown = this.crimeBreakdown;
        copy.recreationCount = this.recreationCount;
        copy.nearbyRecreation = this.nearbyRecreation;
        copy.status = this.status;
        copy.dateAdded = this.dateAdded;
        return copy;
    }

    /**
     * This method is counting how many amenities the apartment have
     * @return number of amenities that is true
     *
     * pre-condition: none
     * post-condition: returns count between 0 and 6
     */
    public int countAmenities() {
        int count = 0;
        if (hasParking) count++;
        if (hasLaundry) count++;
        if (hasDishwasher) count++;
        if (hasAC) count++;
        if (petFriendly) count++;
        if (furnished) count++;
        return count;
    }

    // Getter and setter method

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getRent() { return rent; }
    public void setRent(double rent) { this.rent = rent; }

    public double getSqft() { return sqft; }
    public void setSqft(double sqft) { this.sqft = sqft; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public int getBathrooms() { return bathrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }

    public boolean getHasParking() { return hasParking; }
    public void setHasParking(boolean hasParking) { this.hasParking = hasParking; }

    public boolean getHasLaundry() { return hasLaundry; }
    public void setHasLaundry(boolean hasLaundry) { this.hasLaundry = hasLaundry; }

    public boolean getHasDishwasher() { return hasDishwasher; }
    public void setHasDishwasher(boolean hasDishwasher) { this.hasDishwasher = hasDishwasher; }

    public boolean getHasAC() { return hasAC; }
    public void setHasAC(boolean hasAC) { this.hasAC = hasAC; }

    public boolean getPetFriendly() { return petFriendly; }
    public void setPetFriendly(boolean petFriendly) { this.petFriendly = petFriendly; }

    public boolean getFurnished() { return furnished; }
    public void setFurnished(boolean furnished) { this.furnished = furnished; }

    public String getAvailableDate() { return availableDate; }
    public void setAvailableDate(String availableDate) { this.availableDate = availableDate; }

    public int getLeaseLength() { return leaseLength; }
    public void setLeaseLength(int leaseLength) { this.leaseLength = leaseLength; }

    public boolean getBrokerFee() { return brokerFee; }
    public void setBrokerFee(boolean brokerFee) { this.brokerFee = brokerFee; }

    public boolean getUtilitiesIncluded() { return utilitiesIncluded; }
    public void setUtilitiesIncluded(boolean utilitiesIncluded) { this.utilitiesIncluded = utilitiesIncluded; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSourceURL() { return sourceURL; }
    public void setSourceURL(String sourceURL) { this.sourceURL = sourceURL; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getWalkScore() { return walkScore; }
    public void setWalkScore(int walkScore) { this.walkScore = walkScore; }

    public int getTransitScore() { return transitScore; }
    public void setTransitScore(int transitScore) { this.transitScore = transitScore; }

    public int getBikeScore() { return bikeScore; }
    public void setBikeScore(int bikeScore) { this.bikeScore = bikeScore; }

    public String getNearestTStop() { return nearestTStop; }
    public void setNearestTStop(String nearestTStop) { this.nearestTStop = nearestTStop; }

    public String getNearestTLine() { return nearestTLine; }
    public void setNearestTLine(String nearestTLine) { this.nearestTLine = nearestTLine; }

    public double getDistanceToT() { return distanceToT; }
    public void setDistanceToT(double distanceToT) { this.distanceToT = distanceToT; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getDateAdded() { return dateAdded; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }

    public int getNearbyFood() { return nearbyFood; }
    public void setNearbyFood(int nearbyFood) { this.nearbyFood = nearbyFood; }

    public int getNearbyShops() { return nearbyShops; }
    public void setNearbyShops(int nearbyShops) { this.nearbyShops = nearbyShops; }

    public int getNearbyServices() { return nearbyServices; }
    public void setNearbyServices(int nearbyServices) { this.nearbyServices = nearbyServices; }

    public int getNearbyTransit() { return nearbyTransit; }
    public void setNearbyTransit(int nearbyTransit) { this.nearbyTransit = nearbyTransit; }

    public int getNearbyLeisure() { return nearbyLeisure; }
    public void setNearbyLeisure(int nearbyLeisure) { this.nearbyLeisure = nearbyLeisure; }

    public int getNearbyBike() { return nearbyBike; }
    public void setNearbyBike(int nearbyBike) { this.nearbyBike = nearbyBike; }

    public int getSafetyScore() { return safetyScore; }
    public void setSafetyScore(int safetyScore) { this.safetyScore = safetyScore; }

    public int getCrimeCount() { return crimeCount; }
    public void setCrimeCount(int crimeCount) { this.crimeCount = crimeCount; }

    public String getCrimeBreakdown() { return crimeBreakdown; }
    public void setCrimeBreakdown(String crimeBreakdown) { this.crimeBreakdown = crimeBreakdown; }

    public int getRecreationCount() { return recreationCount; }
    public void setRecreationCount(int recreationCount) { this.recreationCount = recreationCount; }

    public String getNearbyRecreation() { return nearbyRecreation; }
    public void setNearbyRecreation(String nearbyRecreation) { this.nearbyRecreation = nearbyRecreation; }

    /**
     * This method is giving back a simple string about the apartment
     * @return string with name address and rent
     *
     * pre-condition: none
     * post-condition: a readable string is returned
     */
    @Override
    public String toString() {
        return name + " - " + address + " ($" + rent + "/mo)";
    }
}
