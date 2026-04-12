package com.leaselens.datastructures;

import com.leaselens.model.Apartment;
import java.util.ArrayList;

/**
 * This class is a hash map data structure for fast apartment lookups by neighborhood
 * It use an array of linked lists (separate chaining) for handling collisions
 * Key is neighborhood name, value is list of apartments in that neighborhood
 *
 * ADT: HashMap
 * Data Structure: Array + Linked List (Separate Chaining)
 *
 * pre-condition: none
 * post-condition: an empty hash map is created
 */
public class ApartmentHashMap implements HashMapInterface<Apartment> {

    /**
     * This inner class is one entry in the hash map
     * It store neighborhood name and list of apartments in that neighborhood
     */
    private class Entry {
        String neighborhood;
        ArrayList<Apartment> apartments;
        Entry next;

        Entry(String neighborhood) {
            this.neighborhood = neighborhood;
            this.apartments = new ArrayList<Apartment>();
            this.next = null;
        }
    }

    // the array of buckets
    private Entry[] table;
    // how many neighborhoods we have
    private int size;
    // how many buckets the array has
    private int capacity;

    /**
     * This constructor is making a new hash map with given capacity
     * @param capacity how many buckets the hash map should have
     *
     * pre-condition: capacity should be more than 0
     * post-condition: empty hash map is created
     */
    public ApartmentHashMap(int capacity) {
        this.capacity = capacity;
        this.table = new Entry[capacity];
        this.size = 0;
    }

    /**
     * This method is calculating the hash code for a key
     * It add up all the characters to get a number
     * @param key the string key to hash
     * @return an index number between 0 and capacity-1
     *
     * pre-condition: key should not be null
     * post-condition: a valid index is returned
     */
    private int hash(String key) {
        // add up all the characters
        int total = 0;
        for (int i = 0; i < key.length(); i++) {
            total = total + key.charAt(i);
        }
        // make sure its positive
        if (total < 0) {
            total = total * -1;
        }
        return total % capacity;
    }

    /**
     * This method is adding apartment to its neighborhood bucket
     * If neighborhood not exist it create new entry
     * If neighborhood exist it add apartment to the list
     * @param neighborhood the neighborhood name (key)
     * @param apartment the apartment object to store
     *
     * pre-condition: neighborhood and apartment should not be null
     * post-condition: apartment is added to that neighborhood list
     */
    public void put(String neighborhood, Apartment apartment) {
        if (neighborhood == null || neighborhood.isEmpty()) {
            return;
        }
        String lowerKey = neighborhood.toLowerCase();
        int index = hash(lowerKey);

        // check if neighborhood already exist in this bucket
        Entry current = table[index];
        while (current != null) {
            if (current.neighborhood.equals(lowerKey)) {
                // neighborhood exist, add apartment to list
                current.apartments.add(apartment);
                return;
            }
            current = current.next;
        }

        // neighborhood not found, add new entry at front of chain
        Entry newEntry = new Entry(lowerKey);
        newEntry.apartments.add(apartment);
        newEntry.next = table[index];
        table[index] = newEntry;
        size++;
    }

    /**
     * This method is getting all apartments in a neighborhood
     * This is O(1) fast lookup because we go straight to the bucket
     * @param neighborhood the neighborhood to search
     * @return list of apartments in that neighborhood
     *
     * pre-condition: neighborhood should not be null
     * post-condition: list of apartments is returned (empty if not found)
     */
    public ArrayList<Apartment> getByNeighborhood(String neighborhood) {
        if (neighborhood == null || neighborhood.isEmpty()) {
            return new ArrayList<Apartment>();
        }
        String lowerKey = neighborhood.toLowerCase();
        int index = hash(lowerKey);

        Entry current = table[index];
        while (current != null) {
            if (current.neighborhood.equals(lowerKey)) {
                return current.apartments;
            }
            current = current.next;
        }
        return new ArrayList<Apartment>();
    }

    /**
     * This method is getting single apartment by its id
     * It need to search all buckets because id is not the key
     * @param id the apartment id to find
     * @return the apartment if found, null if not found
     *
     * pre-condition: id should not be null
     * post-condition: apartment is returned or null
     */
    public Apartment get(String id) {
        if (id == null) {
            return null;
        }
        String lowerId = id.toLowerCase();

        // loop through all buckets to find by id
        for (int i = 0; i < capacity; i++) {
            Entry current = table[i];
            while (current != null) {
                for (int j = 0; j < current.apartments.size(); j++) {
                    Apartment apt = current.apartments.get(j);
                    if (apt.getId().toLowerCase().equals(lowerId)) {
                        return apt;
                    }
                }
                current = current.next;
            }
        }
        return null;
    }

    /**
     * This method is checking if a neighborhood exist in the map
     * @param neighborhood the neighborhood to check
     * @return true if neighborhood exist, false if not
     *
     * pre-condition: neighborhood should not be null
     * post-condition: true or false is returned
     */
    public boolean containsKey(String neighborhood) {
        return getByNeighborhood(neighborhood).size() > 0;
    }

    /**
     * This method is removing an apartment by its id
     * @param id the apartment id to remove
     * @return true if removed, false if not found
     *
     * pre-condition: id should not be null
     * post-condition: apartment is removed from its neighborhood list
     */
    public boolean remove(String id) {
        if (id == null) {
            return false;
        }
        String lowerId = id.toLowerCase();

        // loop through all buckets to find and remove
        for (int i = 0; i < capacity; i++) {
            Entry current = table[i];
            while (current != null) {
                for (int j = 0; j < current.apartments.size(); j++) {
                    Apartment apt = current.apartments.get(j);
                    if (apt.getId().toLowerCase().equals(lowerId)) {
                        current.apartments.remove(j);
                        return true;
                    }
                }
                current = current.next;
            }
        }
        return false;
    }

    /**
     * This method is searching apartments by neighborhood
     * This is O(1) fast lookup because neighborhood is the key
     * @param neighborhood the neighborhood to search for
     * @return list of apartments in that neighborhood
     *
     * pre-condition: neighborhood should not be null
     * post-condition: list of matching apartments is returned
     */
    public ArrayList<Apartment> search(String neighborhood) {
        return getByNeighborhood(neighborhood);
    }

    /**
     * This method is getting all neighborhood names in the map
     * @return array of all neighborhood strings
     *
     * pre-condition: none
     * post-condition: array of keys is returned
     */
    public String[] getAllKeys() {
        String[] keys = new String[size];
        int index = 0;
        for (int i = 0; i < capacity; i++) {
            Entry current = table[i];
            while (current != null) {
                keys[index] = current.neighborhood;
                index++;
                current = current.next;
            }
        }
        return keys;
    }

    /**
     * This method is telling how many neighborhoods is in the map
     * @return the number of neighborhoods
     *
     * pre-condition: none
     * post-condition: size is returned
     */
    public int size() {
        return size;
    }

    /**
     * This method is checking if map is empty
     * @return true if empty, false if not
     *
     * pre-condition: none
     * post-condition: true or false is returned
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * This method is removing all entries from map
     *
     * pre-condition: none
     * post-condition: map is empty and size is 0
     */
    public void clear() {
        for (int i = 0; i < capacity; i++) {
            table[i] = null;
        }
        size = 0;
    }
}
