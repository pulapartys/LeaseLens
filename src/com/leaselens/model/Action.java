package com.leaselens.model;

/**
 * This class is for saving what action the user did
 * So we can undo it later if they want to go back
 *
 * pre-condition: none
 * post-condition: an action object is created to track what user did
 */
public class Action {

    // what type of action it is
    private String type;  // "ADD", "DELETE", "EDIT", "STATUS_CHANGE"
    private Apartment before;  // how apartment looked before
    private Apartment after;   // how apartment looked after

    /**
     * This constructor is making a new action
     * @param type what kind of action like "ADD" or "DELETE"
     * @param before the apartment before the change (null if adding new)
     * @param after the apartment after the change (null if deleting)
     *
     * pre-condition: type should be "ADD", "DELETE", "EDIT", or "STATUS_CHANGE"
     * post-condition: action object is created
     */
    public Action(String type, Apartment before, Apartment after) {
        this.type = type;
        this.before = before;
        this.after = after;
    }

    /**
     * This method is giving back what type of action it is
     * @return the type string like "ADD" or "DELETE"
     *
     * pre-condition: none
     * post-condition: type string is returned
     */
    public String getType() {
        return type;
    }

    /**
     * This method is giving back the apartment before the change
     * @return apartment object from before, or null if it was ADD
     *
     * pre-condition: none
     * post-condition: before apartment is returned
     */
    public Apartment getBefore() {
        return before;
    }

    /**
     * This method is giving back the apartment after the change
     * @return apartment object from after, or null if it was DELETE
     *
     * pre-condition: none
     * post-condition: after apartment is returned
     */
    public Apartment getAfter() {
        return after;
    }

    /**
     * This method is making a description of what happened
     * @return a string like "Added Elm Street Place"
     *
     * pre-condition: none
     * post-condition: readable description is returned
     */
    public String getDescription() {
        if (type.equals("ADD")) {
            return "Added " + after.getName();
        } else if (type.equals("DELETE")) {
            return "Deleted " + before.getName();
        } else if (type.equals("EDIT")) {
            return "Edited " + after.getName();
        } else if (type.equals("STATUS_CHANGE")) {
            return "Changed " + after.getName() + " to " + after.getStatus();
        }
        return "Unknown action";
    }
}
