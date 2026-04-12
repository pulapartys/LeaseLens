package com.leaselens.model;

/**
 * This class is storing what the user prefer when ranking apartments
 * Like their budget and how much they care about rent vs location
 *
 * pre-condition: none
 * post-condition: preferences object is created with default values
 */
public class UserPreferences {

    private double minBudget;
    private double maxBudget;
    private double weightRent;
    private double weightSqft;
    private double weightNearT;
    private double weightWalkScore;
    private double weightAmenities;
    private double weightSafety;

    /**
     * This constructor is setting up default preferences
     * All weights start equal at 20% each, safety start at 0
     *
     * pre-condition: none
     * post-condition: preferences is created with default values
     */
    public UserPreferences() {
        this.minBudget = 0;
        this.maxBudget = 5000;
        this.weightRent = 0.20;
        this.weightSqft = 0.20;
        this.weightNearT = 0.20;
        this.weightWalkScore = 0.20;
        this.weightAmenities = 0.20;
        this.weightSafety = 0.0;
    }

    // ---- GETTERS AND SETTERS ----

    public double getMinBudget() { return minBudget; }
    public void setMinBudget(double minBudget) { this.minBudget = minBudget; }

    public double getMaxBudget() { return maxBudget; }
    public void setMaxBudget(double maxBudget) { this.maxBudget = maxBudget; }

    public double getWeightRent() { return weightRent; }
    public void setWeightRent(double weightRent) { this.weightRent = weightRent; }

    public double getWeightSqft() { return weightSqft; }
    public void setWeightSqft(double weightSqft) { this.weightSqft = weightSqft; }

    public double getWeightNearT() { return weightNearT; }
    public void setWeightNearT(double weightNearT) { this.weightNearT = weightNearT; }

    public double getWeightWalkScore() { return weightWalkScore; }
    public void setWeightWalkScore(double weightWalkScore) { this.weightWalkScore = weightWalkScore; }

    public double getWeightAmenities() { return weightAmenities; }
    public void setWeightAmenities(double weightAmenities) { this.weightAmenities = weightAmenities; }

    public double getWeightSafety() { return weightSafety; }
    public void setWeightSafety(double weightSafety) { this.weightSafety = weightSafety; }
}
