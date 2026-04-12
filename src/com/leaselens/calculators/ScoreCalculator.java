package com.leaselens.calculators;

import com.leaselens.model.Apartment;
import com.leaselens.model.UserPreferences;

/**
 * This class is calculating a score for each apartment based on user preferences
 * Higher score means better apartment for what the user wants
 * We use this for the Top Picks ranking on dashboard
 *
 * pre-condition: none
 * post-condition: none
 */
public class ScoreCalculator {

    /**
     * This method is calculating the score for one apartment
     * It look at rent, sqft, distance to T, walk score, amenities, and safety
     * Each one is weighted by how much user cares about it
     * @param apartment the apartment to score
     * @param preferences the user preferences with weights
     * @param maxRent the highest rent among all apartments (for normalizing)
     * @param maxSqft the biggest sqft among all apartments
     * @param maxDistance the farthest distance to T among all apartments
     * @return score between 0 and 100, higher is better
     *
     * pre-condition: apartment and preferences should not be null
     * post-condition: a score number is returned
     */
    public static double calculateScore(Apartment apartment, UserPreferences preferences,
                                         double maxRent, double maxSqft, double maxDistance) {

        double score = 0;

        // rent score - lower rent is better so we flip it
        double rentScore = 0;
        if (maxRent > 0) {
            rentScore = 100 - (apartment.getRent() / maxRent * 100);
        }

        // sqft score - bigger is better
        double sqftScore = 0;
        if (maxSqft > 0) {
            sqftScore = apartment.getSqft() / maxSqft * 100;
        }

        // near T score - closer is better so we flip it
        double nearTScore = 0;
        if (maxDistance > 0 && apartment.getDistanceToT() >= 0) {
            nearTScore = 100 - (apartment.getDistanceToT() / maxDistance * 100);
        }

        // walk score is already 0-100
        double walkScore = 0;
        if (apartment.getWalkScore() >= 0) {
            walkScore = apartment.getWalkScore();
        }

        // amenity score - more amenities is better
        double amenityScore = (apartment.countAmenities() / 6.0) * 100;

        // safety score is already 0-100 from the crime API
        double safetyScoreVal = 0;
        if (apartment.getSafetyScore() >= 0) {
            safetyScoreVal = apartment.getSafetyScore();
        }

        // add them up with weights
        score = (rentScore * preferences.getWeightRent())
              + (sqftScore * preferences.getWeightSqft())
              + (nearTScore * preferences.getWeightNearT())
              + (walkScore * preferences.getWeightWalkScore())
              + (amenityScore * preferences.getWeightAmenities())
              + (safetyScoreVal * preferences.getWeightSafety());

        // round to 1 decimal place
        score = Math.round(score * 10.0) / 10.0;

        return score;
    }
}
