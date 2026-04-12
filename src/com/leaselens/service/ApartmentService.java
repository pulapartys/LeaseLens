package com.leaselens.service;

import com.leaselens.model.Apartment;
import com.leaselens.model.Action;
import com.leaselens.model.Status;
import com.leaselens.model.UserPreferences;
import com.leaselens.datastructures.ApartmentBag;
import com.leaselens.datastructures.ApartmentHashMap;
import com.leaselens.datastructures.UndoRedoStack;
import com.leaselens.datastructures.ApartmentSorter;
import com.leaselens.api.GeocodingService;
import com.leaselens.api.WalkScoreService;
import com.leaselens.api.MBTAService;
import com.leaselens.api.CrimeDataService;
import com.leaselens.api.RecreationService;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * This class is the main service that manage all apartments
 * It connect all the data structures and APIs together
 * When user add or remove apartment this class update everything
 *
 * pre-condition: none
 * post-condition: service is created with all data structures ready
 */
public class ApartmentService {

    // our data structures
    private ApartmentBag allApartments;
    private ApartmentHashMap searchMap;
    private UndoRedoStack undoStack;
    private UndoRedoStack redoStack;

    // our API services
    private GeocodingService geocodingService;
    private WalkScoreService walkScoreService;
    private MBTAService mbtaService;
    private CrimeDataService crimeDataService;
    private RecreationService recreationService;

    // user preferences
    private UserPreferences preferences;

    /**
     * This constructor is setting up all the data structures and services
     *
     * pre-condition: none
     * post-condition: all data structures is empty and ready to use
     */
    public ApartmentService() {
        allApartments = new ApartmentBag();
        searchMap = new ApartmentHashMap(100);
        undoStack = new UndoRedoStack();
        redoStack = new UndoRedoStack();

        geocodingService = new GeocodingService();
        walkScoreService = new WalkScoreService();
        mbtaService = new MBTAService();
        crimeDataService = new CrimeDataService();
        recreationService = new RecreationService();

        preferences = new UserPreferences();
    }

    /**
     * This method is adding a new apartment to all data structures
     * It also try to get API data for the apartment
     * @param apartment the apartment to add
     * @return true if added successfully
     *
     * pre-condition: apartment should not be null
     * post-condition: apartment is in bag and hash map
     */
    public boolean addApartment(Apartment apartment) {
        // add to all data structures
        allApartments.add(apartment);
        searchMap.put(apartment.getNeighborhood(), apartment);

        // save action for undo
        Action action = new Action("ADD", null, apartment.makeCopy());
        undoStack.push(action);
        redoStack.clear(); // new action clears redo

        return true;
    }

    /**
     * This method is trying to get API data for an apartment
     * It get coordinates, walk score, and nearest T stop
     * @param apartment the apartment to enrich with API data
     *
     * pre-condition: apartment should have address set
     * post-condition: apartment have API data filled in (if APIs work)
     */
    public void enrichWithApiData(Apartment apartment) {
        // step 1: get coordinates first because all other APIs need them
        double[] coords = null;
        try {
            coords = geocodingService.getCoordinates(apartment.getAddress());
            if (coords != null) {
                apartment.setLatitude(coords[0]);
                apartment.setLongitude(coords[1]);
                System.out.println("Geocoding done: " + coords[0] + ", " + coords[1]);
            }
        } catch (Exception e) {
            System.out.println("Error in geocoding: " + e.getMessage());
        }

        // if we dont have coordinates we cant do the other steps
        if (coords == null) {
            System.out.println("No coordinates found, skipping other APIs");
            return;
        }

        // step 2: get walk scores
        try {
            int[] scores = walkScoreService.getScores(
                apartment.getAddress(), coords[0], coords[1]);
            if (scores != null) {
                apartment.setWalkScore(scores[0]);
                apartment.setTransitScore(scores[1]);
                apartment.setBikeScore(scores[2]);
                if (scores.length > 3) {
                    apartment.setNearbyFood(scores[3]);
                    apartment.setNearbyShops(scores[4]);
                    apartment.setNearbyServices(scores[5]);
                    apartment.setNearbyTransit(scores[6]);
                    apartment.setNearbyLeisure(scores[7]);
                    apartment.setNearbyBike(scores[8]);
                }
                System.out.println("Walk scores done");
            }
        } catch (Exception e) {
            System.out.println("Error in walk scores: " + e.getMessage());
        }

        // step 3: find nearest T stop
        try {
            String[] nearestStop = mbtaService.findNearestStop(coords[0], coords[1]);
            if (nearestStop != null) {
                apartment.setNearestTStop(nearestStop[0]);
                apartment.setDistanceToT(Double.parseDouble(nearestStop[1]));
                System.out.println("MBTA done: " + nearestStop[0]);
            }
        } catch (Exception e) {
            System.out.println("Error in MBTA: " + e.getMessage());
        }

        // step 4: get crime / safety data
        try {
            int[] crimeData = crimeDataService.getCrimeData(coords[0], coords[1]);
            if (crimeData != null) {
                apartment.setSafetyScore(crimeData[0]);
                apartment.setCrimeCount(crimeData[1]);
                apartment.setCrimeBreakdown(crimeDataService.getLastBreakdown());
                System.out.println("Crime data done");
            }
        } catch (Exception e) {
            System.out.println("Error in crime data: " + e.getMessage());
        }

        // step 5: get nearby recreation areas
        try {
            String[] recData = recreationService.getNearbyRecreation(coords[0], coords[1]);
            if (recData != null) {
                apartment.setRecreationCount(Integer.parseInt(recData[0]));
                apartment.setNearbyRecreation(recData[1]);
                System.out.println("Recreation data done");
            }
        } catch (Exception e) {
            System.out.println("Error in recreation data: " + e.getMessage());
        }
    }

    /**
     * This method is removing an apartment from everywhere
     * @param id the apartment ID to remove
     * @return true if removed, false if not found
     *
     * pre-condition: id should not be null
     * post-condition: apartment is removed from all data structures
     */
    public boolean removeApartment(String id) {
        Apartment apartment = searchMap.get(id);
        if (apartment == null) {
            return false;
        }

        // save for undo before removing
        Action action = new Action("DELETE", apartment.makeCopy(), null);
        undoStack.push(action);
        redoStack.clear();

        // remove from all data structures
        allApartments.remove(id);
        searchMap.remove(id);

        return true;
    }

    /**
     * This method is changing the status of an apartment
     * @param id the apartment ID
     * @param newStatus the new status to set
     * @return true if changed, false if apartment not found
     *
     * pre-condition: id and newStatus should not be null
     * post-condition: apartment status is updated
     */
    public boolean changeStatus(String id, Status newStatus) {
        Apartment apartment = searchMap.get(id);
        if (apartment == null) {
            return false;
        }

        // save for undo
        Apartment beforeCopy = apartment.makeCopy();
        apartment.setStatus(newStatus);
        Apartment afterCopy = apartment.makeCopy();

        Action action = new Action("STATUS_CHANGE", beforeCopy, afterCopy);
        undoStack.push(action);
        redoStack.clear();

        return true;
    }

    /**
     * This method is updating apartment info after user edit it
     * @param updated the apartment with new values
     * @return true if updated, false if not found
     *
     * pre-condition: updated apartment should have valid ID
     * post-condition: apartment is updated in all data structures
     */
    public boolean updateApartment(Apartment updated) {
        Apartment existing = allApartments.getById(updated.getId());
        if (existing == null) {
            return false;
        }

        // save for undo
        Apartment beforeCopy = existing.makeCopy();

        // update the fields
        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setNeighborhood(updated.getNeighborhood());
        existing.setRent(updated.getRent());
        existing.setSqft(updated.getSqft());
        existing.setBedrooms(updated.getBedrooms());
        existing.setBathrooms(updated.getBathrooms());
        existing.setHasParking(updated.getHasParking());
        existing.setHasLaundry(updated.getHasLaundry());
        existing.setHasDishwasher(updated.getHasDishwasher());
        existing.setHasAC(updated.getHasAC());
        existing.setPetFriendly(updated.getPetFriendly());
        existing.setFurnished(updated.getFurnished());
        existing.setAvailableDate(updated.getAvailableDate());
        existing.setLeaseLength(updated.getLeaseLength());
        existing.setBrokerFee(updated.getBrokerFee());
        existing.setUtilitiesIncluded(updated.getUtilitiesIncluded());
        existing.setSource(updated.getSource());
        existing.setSourceURL(updated.getSourceURL());
        existing.setNotes(updated.getNotes());

        // update API data fields too (needed when address changes)
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setWalkScore(updated.getWalkScore());
        existing.setTransitScore(updated.getTransitScore());
        existing.setBikeScore(updated.getBikeScore());
        existing.setNearbyFood(updated.getNearbyFood());
        existing.setNearbyShops(updated.getNearbyShops());
        existing.setNearbyServices(updated.getNearbyServices());
        existing.setNearbyTransit(updated.getNearbyTransit());
        existing.setNearbyLeisure(updated.getNearbyLeisure());
        existing.setNearbyBike(updated.getNearbyBike());
        existing.setNearestTStop(updated.getNearestTStop());
        existing.setDistanceToT(updated.getDistanceToT());
        existing.setSafetyScore(updated.getSafetyScore());
        existing.setCrimeCount(updated.getCrimeCount());
        existing.setCrimeBreakdown(updated.getCrimeBreakdown());
        existing.setRecreationCount(updated.getRecreationCount());
        existing.setNearbyRecreation(updated.getNearbyRecreation());

        Action action = new Action("EDIT", beforeCopy, existing.makeCopy());
        undoStack.push(action);
        redoStack.clear();

        return true;
    }

    /**
     * This method is undoing the last action
     * @return description of what was undone, or null if nothing to undo
     *
     * pre-condition: none
     * post-condition: last action is reversed
     */
    public String undo() {
        if (undoStack.isEmpty()) {
            return null;
        }

        Action action = undoStack.pop();
        redoStack.push(action);

        if (action.getType().equals("ADD")) {
            // undo add = remove the apartment
            String id = action.getAfter().getId();
            allApartments.remove(id);
            searchMap.remove(id);

        } else if (action.getType().equals("DELETE")) {
            // undo delete = add it back
            Apartment apt = action.getBefore();
            allApartments.add(apt);
            searchMap.put(apt.getNeighborhood(), apt);

        } else if (action.getType().equals("EDIT") || action.getType().equals("STATUS_CHANGE")) {
            // undo edit = restore the before state
            Apartment before = action.getBefore();
            Apartment current = allApartments.getById(before.getId());
            if (current != null) {
                copyFields(before, current);
            }
        }

        return "Undid: " + action.getDescription();
    }

    /**
     * This method is redoing the last undone action
     * @return description of what was redone, or null if nothing to redo
     *
     * pre-condition: none
     * post-condition: last undone action is reapplied
     */
    public String redo() {
        if (redoStack.isEmpty()) {
            return null;
        }

        Action action = redoStack.pop();
        undoStack.push(action);

        if (action.getType().equals("ADD")) {
            Apartment apt = action.getAfter();
            allApartments.add(apt);
            searchMap.put(apt.getNeighborhood(), apt);

        } else if (action.getType().equals("DELETE")) {
            String id = action.getBefore().getId();
            allApartments.remove(id);
            searchMap.remove(id);

        } else if (action.getType().equals("EDIT") || action.getType().equals("STATUS_CHANGE")) {
            Apartment after = action.getAfter();
            Apartment current = allApartments.getById(after.getId());
            if (current != null) {
                copyFields(after, current);
            }
        }

        return "Redid: " + action.getDescription();
    }

    /**
     * This method is copying all fields from one apartment to another
     * @param from the apartment to copy from
     * @param to the apartment to copy to
     *
     * pre-condition: both should not be null
     * post-condition: to have same values as from
     */
    private void copyFields(Apartment from, Apartment to) {
        to.setName(from.getName());
        to.setAddress(from.getAddress());
        to.setNeighborhood(from.getNeighborhood());
        to.setRent(from.getRent());
        to.setSqft(from.getSqft());
        to.setBedrooms(from.getBedrooms());
        to.setBathrooms(from.getBathrooms());
        to.setHasParking(from.getHasParking());
        to.setHasLaundry(from.getHasLaundry());
        to.setHasDishwasher(from.getHasDishwasher());
        to.setHasAC(from.getHasAC());
        to.setPetFriendly(from.getPetFriendly());
        to.setFurnished(from.getFurnished());
        to.setSource(from.getSource());
        to.setSourceURL(from.getSourceURL());
        to.setNotes(from.getNotes());
        to.setSafetyScore(from.getSafetyScore());
        to.setCrimeCount(from.getCrimeCount());
        to.setCrimeBreakdown(from.getCrimeBreakdown());
        to.setRecreationCount(from.getRecreationCount());
        to.setNearbyRecreation(from.getNearbyRecreation());
        to.setStatus(from.getStatus());
    }

    // ---- SEARCH AND FILTER METHODS ----

    /**
     * This method is searching apartments by neighborhood
     * This is O(1) fast lookup because neighborhood is the hash map key
     * @param neighborhood the neighborhood to search for
     * @return list of apartments in that neighborhood
     *
     * pre-condition: neighborhood should not be null
     * post-condition: list of matches is returned
     */
    public ArrayList<Apartment> search(String neighborhood) {
        return searchMap.search(neighborhood);
    }

    /**
     * This method is getting apartments in a price range
     * @param min minimum rent
     * @param max maximum rent
     * @return list of apartments in range
     *
     * pre-condition: min should be less than max
     * post-condition: filtered list is returned
     */
    public ArrayList<Apartment> filterByPriceRange(double min, double max) {
        ArrayList<Apartment> results = new ArrayList<Apartment>();
        for (int i = 0; i < allApartments.getCurrentSize(); i++) {
            Apartment apt = allApartments.get(i);
            if (apt.getRent() >= min && apt.getRent() <= max) {
                results.add(apt);
            }
        }
        return results;
    }

    /**
     * This method is getting all apartments sorted by given criteria
     * @param comparator how to sort them
     * @return sorted array of apartments
     *
     * pre-condition: comparator should not be null
     * post-condition: sorted array is returned
     */
    public Apartment[] getSorted(Comparator<Apartment> comparator) {
        Apartment[] arr = allApartments.toArray();
        ApartmentSorter.mergeSort(arr, comparator);
        return arr;
    }

    // ---- GETTER METHODS ----

    public ApartmentBag getAllApartments() { return allApartments; }
    public UserPreferences getPreferences() { return preferences; }
    public void setPreferences(UserPreferences preferences) { this.preferences = preferences; }
    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
    public MBTAService getMbtaService() { return mbtaService; }

    /**
     * This method is getting the total count of apartments
     * @return total number
     *
     * pre-condition: none
     * post-condition: count is returned
     */
    public int getTotalCount() { return allApartments.getCurrentSize(); }

    /**
     * This method is counting apartments with a specific status
     * @param status the status to count
     * @return how many apartments have that status
     *
     * pre-condition: none
     * post-condition: count is returned
     */
    public int getCountByStatus(Status status) {
        int count = 0;
        for (int i = 0; i < allApartments.getCurrentSize(); i++) {
            Apartment apt = allApartments.get(i);
            if (apt.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    /**
     * This method is calculating average rent of all apartments
     * @return the average rent, or 0 if no apartments
     *
     * pre-condition: none
     * post-condition: average rent number is returned
     */
    public double getAverageRent() {
        if (allApartments.getCurrentSize() == 0) {
            return 0;
        }
        double total = 0;
        for (int i = 0; i < allApartments.getCurrentSize(); i++) {
            Apartment apt = allApartments.get(i);
            total = total + apt.getRent();
        }
        return Math.round(total / allApartments.getCurrentSize() * 100.0) / 100.0;
    }
}
