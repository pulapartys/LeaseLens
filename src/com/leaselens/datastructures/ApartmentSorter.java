package com.leaselens.datastructures;

import com.leaselens.model.Apartment;
import java.util.Comparator;

/**
 * This class is having the merge sort algorithm for sorting apartments
 * Merge sort is dividing the list in half, sorting each half, then merging them
 * We wrote this ourself instead of using Collections.sort()
 *
 * pre-condition: none
 * post-condition: apartments is sorted based on the comparator we give
 */
public class ApartmentSorter {

    /**
     * This method is sorting an array of apartments using merge sort
     * @param apartments the array of apartments to sort
     * @param comparator tells us how to compare two apartments (by rent, sqft, etc)
     *
     * pre-condition: array should not be null and comparator should not be null
     * post-condition: the array is sorted in the order the comparator says
     */
    public static void mergeSort(Apartment[] apartments, Comparator<Apartment> comparator) {
        if (apartments == null || apartments.length <= 1) {
            return; // nothing to sort
        }

        // make a temporary array for merging
        Apartment[] temp = new Apartment[apartments.length];
        mergeSortHelper(apartments, temp, 0, apartments.length - 1, comparator);
    }

    /**
     * This is the recursive helper method that is doing the actual sorting
     * It split the array in half and sort each half then merge them
     * @param arr the array we sorting
     * @param temp temporary array for merging
     * @param left the start index
     * @param right the end index
     * @param comp the comparator
     *
     * pre-condition: left and right should be valid indexes
     * post-condition: the section from left to right is sorted
     */
    private static void mergeSortHelper(Apartment[] arr, Apartment[] temp, int left, int right, Comparator<Apartment> comp) {
        if (left >= right) {
            return; // base case - only one element
        }

        // find the middle
        int mid = (left + right) / 2;

        // sort the left half
        mergeSortHelper(arr, temp, left, mid, comp);

        // sort the right half
        mergeSortHelper(arr, temp, mid + 1, right, comp);

        // merge the two sorted halves together
        merge(arr, temp, left, mid, right, comp);
    }

    /**
     * This method is merging two sorted halves into one sorted section
     * @param arr the main array
     * @param temp temporary array to help with merging
     * @param left start of left half
     * @param mid end of left half / start of right half
     * @param right end of right half
     * @param comp the comparator
     *
     * pre-condition: left half and right half should already be sorted
     * post-condition: the whole section from left to right is now sorted
     */
    private static void merge(Apartment[] arr, Apartment[] temp, int left, int mid, int right, Comparator<Apartment> comp) {
        // copy everything to temp array
        for (int i = left; i <= right; i++) {
            temp[i] = arr[i];
        }

        int i = left;      // pointer for left half
        int j = mid + 1;   // pointer for right half
        int k = left;      // pointer for where to put in main array

        // compare and put smaller one first
        while (i <= mid && j <= right) {
            if (comp.compare(temp[i], temp[j]) <= 0) {
                arr[k] = temp[i];
                i++;
            } else {
                arr[k] = temp[j];
                j++;
            }
            k++;
        }

        // copy remaining from left half
        while (i <= mid) {
            arr[k] = temp[i];
            i++;
            k++;
        }

        // copy remaining from right half
        while (j <= right) {
            arr[k] = temp[j];
            j++;
            k++;
        }
    }

    // =============================================
    // COMPARATORS FOR DIFFERENT SORT OPTIONS
    // =============================================

    /**
     * This method is giving a comparator to sort by rent low to high
     * @return comparator that compare apartments by rent
     *
     * pre-condition: none
     * post-condition: comparator is returned
     */
    public static Comparator<Apartment> byRentLowToHigh() {
        return new Comparator<Apartment>() {
            public int compare(Apartment a, Apartment b) {
                if (a.getRent() < b.getRent()) return -1;
                if (a.getRent() > b.getRent()) return 1;
                return 0;
            }
        };
    }

    /**
     * This method is giving a comparator to sort by bedrooms high to low
     * @return comparator for bedrooms
     *
     * pre-condition: none
     * post-condition: comparator is returned
     */
    public static Comparator<Apartment> byBedroomsHighToLow() {
        return new Comparator<Apartment>() {
            public int compare(Apartment a, Apartment b) {
                return b.getBedrooms() - a.getBedrooms();
            }
        };
    }

    /**
     * This method is giving a comparator to sort by walk score high to low
     * @return comparator for walk score
     *
     * pre-condition: none
     * post-condition: comparator is returned
     */
    public static Comparator<Apartment> byWalkScoreHighToLow() {
        return new Comparator<Apartment>() {
            public int compare(Apartment a, Apartment b) {
                return b.getWalkScore() - a.getWalkScore();
            }
        };
    }

    /**
     * This method is giving a comparator to sort by distance to T low to high
     * @return comparator for T distance
     *
     * pre-condition: none
     * post-condition: comparator is returned
     */
    public static Comparator<Apartment> byDistanceToTLowToHigh() {
        return new Comparator<Apartment>() {
            public int compare(Apartment a, Apartment b) {
                if (a.getDistanceToT() < b.getDistanceToT()) return -1;
                if (a.getDistanceToT() > b.getDistanceToT()) return 1;
                return 0;
            }
        };
    }

}
