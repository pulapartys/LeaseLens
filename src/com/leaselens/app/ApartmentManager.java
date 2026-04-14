package com.leaselens.app;

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
public class ApartmentManager {

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
    public ApartmentManager() {
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
     * This method is trying to get API data for a apartment
     * It try to get coordinates, walk score, crime, transit, parks
     * If something fail it try one more time then give up
     * It give back a string of warning messages so we tell user what happen
     * @param apartment the apartment we want data for
     * @return string with warning messages, empty string if all good
     *
     * pre-condition: apartment should have address
     * post-condition: apartment have API data if APIs working
     */
    public String enrichWithApiData(Apartment apartment) {
        String warnings = "";

        // step 1: get coordinates because all other APIs need it
        // try first time
        double[] coords = null;
        try {
            coords = geocodingService.getCoordinates(apartment.getAddress());
        } catch (Exception e) {
            System.out.println("Geocoding try 1 fail: " + e.getMessage());
        }
        // if first time fail try one more time
        if (coords == null) {
            try {
                coords = geocodingService.getCoordinates(apartment.getAddress());
            } catch (Exception e) {
                System.out.println("Geocoding try 2 fail: " + e.getMessage());
            }
        }
        // if still no coordinates we cant do anything
        if (coords == null) {
            System.out.println("No coordinates, skip other APIs");
            warnings = warnings + "Could not find this address on the map. Please check your street address, city, and zip code. Walk Score, Crime Data, Transit, and Parks will not be available.\n";
            return warnings;
        }
        // save the coordinates
        apartment.setLatitude(coords[0]);
        apartment.setLongitude(coords[1]);
        System.out.println("Geocoding done: " + coords[0] + ", " + coords[1]);

        // step 2: get walk scores
        int[] scores = null;
        try {
            scores = walkScoreService.getScores(apartment.getAddress(), coords[0], coords[1]);
        } catch (Exception e) {
            System.out.println("Walk score try 1 fail: " + e.getMessage());
        }
        if (scores == null) {
            try {
                scores = walkScoreService.getScores(apartment.getAddress(), coords[0], coords[1]);
            } catch (Exception e) {
                System.out.println("Walk score try 2 fail: " + e.getMessage());
            }
        }
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
        } else {
            warnings = warnings + "Walk Score: Could not get walkability scores. The server might be busy. Try editing this apartment later to refresh.\n";
        }

        // step 3: find nearest T stop
        String[] nearStop = null;
        try {
            nearStop = mbtaService.findNearestStop(coords[0], coords[1]);
        } catch (Exception e) {
            System.out.println("MBTA try 1 fail: " + e.getMessage());
        }
        if (nearStop == null) {
            try {
                nearStop = mbtaService.findNearestStop(coords[0], coords[1]);
            } catch (Exception e) {
                System.out.println("MBTA try 2 fail: " + e.getMessage());
            }
        }
        if (nearStop != null) {
            apartment.setNearestTStop(nearStop[0]);
            apartment.setDistanceToT(Double.parseDouble(nearStop[1]));
            System.out.println("MBTA done: " + nearStop[0]);
        } else {
            warnings = warnings + "Transit: Could not find nearby T stops. The MBTA service might be down right now.\n";
        }

        // step 4: get crime data
        int[] crime = null;
        try {
            crime = crimeDataService.getCrimeData(coords[0], coords[1]);
        } catch (Exception e) {
            System.out.println("Crime try 1 fail: " + e.getMessage());
        }
        if (crime == null) {
            try {
                crime = crimeDataService.getCrimeData(coords[0], coords[1]);
            } catch (Exception e) {
                System.out.println("Crime try 2 fail: " + e.getMessage());
            }
        }
        if (crime != null) {
            apartment.setSafetyScore(crime[0]);
            apartment.setCrimeCount(crime[1]);
            apartment.setCrimeBreakdown(crimeDataService.getLastBreakdown());
            System.out.println("Crime data done");
        } else {
            warnings = warnings + "Safety: Could not load crime data. The Boston data service might be down. Safety score will show N/A.\n";
        }

        // step 5: get nearby parks
        String[] rec = null;
        try {
            rec = recreationService.getNearbyRecreation(coords[0], coords[1]);
        } catch (Exception e) {
            System.out.println("Parks try 1 fail: " + e.getMessage());
        }
        if (rec == null) {
            try {
                rec = recreationService.getNearbyRecreation(coords[0], coords[1]);
            } catch (Exception e) {
                System.out.println("Parks try 2 fail: " + e.getMessage());
            }
        }
        if (rec != null) {
            apartment.setRecreationCount(Integer.parseInt(rec[0]));
            apartment.setNearbyRecreation(rec[1]);
            System.out.println("Parks done");
        } else {
            warnings = warnings + "Parks: Could not find nearby parks. The Recreation.gov service might be down.\n";
        }

        return warnings;
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
     * It check if the status change is allowed before doing it
     * @param id the apartment ID
     * @param newStatus the new status to set
     * @return true if changed, false if apartment not found or transition not allowed
     *
     * pre-condition: id and newStatus should not be null
     * post-condition: apartment status is updated if allowed
     */
    public boolean changeStatus(String id, Status newStatus) {
        Apartment apartment = searchMap.get(id);
        if (apartment == null) {
            return false;
        }

        // check if this status change is allowed
        if (isValidStatusChange(apartment.getStatus(), newStatus) == false) {
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
     * This method check if a status change is allowed
     * NEW can go to SHORTLISTED or REJECTED
     * SHORTLISTED can go to TOURED or REJECTED
     * TOURED can go to SHORTLISTED or REJECTED
     * REJECTED is final and cant go anywhere
     * @param current the current status
     * @param next the status user want to change to
     * @return true if this change is ok, false if not allowed
     *
     * pre-condition: both status should not be null
     * post-condition: return true or false
     */
    public boolean isValidStatusChange(Status current, Status next) {
        // same status is not a change
        if (current == next) {
            return false;
        }

        // REJECTED is final - cant change from rejected
        if (current == Status.REJECTED) {
            return false;
        }

        // NEW can go to SHORTLISTED or REJECTED
        if (current == Status.NEW) {
            if (next == Status.SHORTLISTED || next == Status.REJECTED) {
                return true;
            }
            return false;
        }

        // SHORTLISTED can go to TOURED or REJECTED
        if (current == Status.SHORTLISTED) {
            if (next == Status.TOURED || next == Status.REJECTED) {
                return true;
            }
            return false;
        }

        // TOURED can go to SHORTLISTED or REJECTED
        if (current == Status.TOURED) {
            if (next == Status.SHORTLISTED || next == Status.REJECTED) {
                return true;
            }
            return false;
        }

        return false;
    }

    /**
     * This method give a friendly message about what status changes is allowed
     * @param current the current status of the apartment
     * @return a string telling user what they can change to
     *
     * pre-condition: current should not be null
     * post-condition: return a helpful message string
     */
    public String getAllowedStatusMessage(Status current) {
        if (current == Status.NEW) {
            return "This apartment is NEW. You can Shortlist it or Reject it.";
        }
        if (current == Status.SHORTLISTED) {
            return "This apartment is SHORTLISTED. You can mark it as Toured or Reject it.";
        }
        if (current == Status.TOURED) {
            return "This apartment is TOURED. You can move it back to Shortlisted or Reject it.";
        }
        if (current == Status.REJECTED) {
            return "This apartment is REJECTED. Rejected apartments cannot change status. Use Undo if you made a mistake.";
        }
        return "Unknown status.";
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
