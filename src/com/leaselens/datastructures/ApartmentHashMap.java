package com.leaselens.datastructures;

import com.leaselens.model.Apartment;
import java.util.ArrayList;

/**
 * This class is a hash map data structure for fast apartment lookups
 * It use an array of linked lists (separate chaining) for handling collisions
 * We use this for the search bar and duplicate detection
 *
 * pre-condition: none
 * post-condition: an empty hash map is created
 */
public class ApartmentHashMap implements HashMapInterface<Apartment> {

    /**
     * This inner class is one entry in the hash map
     * It store a key and value pair and point to next entry for chaining
     */
    private class Entry {
        String key;
        Apartment value;
        Entry next;

        Entry(String key, Apartment value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }
    // the array of buckets
    private Entry[] table;
    // how many entries are currently stored
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
     * This method is putting a key-value pair into the hash map
     * If the key already exist it update the value
     * @param key the string key (like apartment id or name)
     * @param value the apartment object to store
     *
     * pre-condition: key and value should not be null
     * post-condition: apartment is stored in the hash map
     */
    public void put(String key, Apartment value) {
        String lowerKey = key.toLowerCase();
        int index = hash(lowerKey);

        // check if key already exist in this bucket
        Entry current = table[index];
        while (current != null) {
            if (current.key.equals(lowerKey)) {
                // key already exist, update the value
                current.value = value;
                return;
            }
            current = current.next;
        }

        // key not found, add new entry at front of chain
        Entry newEntry = new Entry(lowerKey, value);
        newEntry.next = table[index];
        table[index] = newEntry;
        size++;
    }

    /**
     * This method is getting an apartment by its key
     * @param key the key to look up
     * @return the apartment if found, null if not found
     *
     * pre-condition: key should not be null
     * post-condition: apartment is returned or null
     */
    public Apartment get(String key) {
        String lowerKey = key.toLowerCase();
        int index = hash(lowerKey);

        Entry current = table[index];
        while (current != null) {
            if (current.key.equals(lowerKey)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    /**
     * This method is checking if a key exist in the map
     * @param key the key to check
     * @return true if key exist, false if not
     *
     * pre-condition: key should not be null
     * post-condition: true or false is returned
     */
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    /**
     * This method is removing a key-value pair from the map
     * @param key the key to remove
     * @return true if removed, false if key not found
     *
     * pre-condition: key should not be null
     * post-condition: entry is removed and size go down by 1
     */
    public boolean remove(String key) {
        String lowerKey = key.toLowerCase();
        int index = hash(lowerKey);

        Entry current = table[index];
        Entry previous = null;

        while (current != null) {
            if (current.key.equals(lowerKey)) {
                if (previous == null) {
                    // removing first entry in bucket
                    table[index] = current.next;
                } else {
                    // removing middle or last entry
                    previous.next = current.next;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    /**
     * This method is searching for apartments that match a query
     * It look through all apartments and check if name or address contain the search text
     * @param query the text to search for
     * @return list of apartments that match
     *
     * pre-condition: query should not be null
     * post-condition: list of matching apartments is returned
     */
    public ArrayList<Apartment> search(String query) {
        ArrayList<Apartment> results = new ArrayList<Apartment>();
        String lowerQuery = query.toLowerCase();

        // loop through all buckets
        for (int i = 0; i < capacity; i++) {
            Entry current = table[i];
            while (current != null) {
                Apartment apt = current.value;

                // check if name or address contain the query
                String name = apt.getName().toLowerCase();
                String address = apt.getAddress().toLowerCase();

                if (name.contains(lowerQuery) || address.contains(lowerQuery)) {
                    results.add(apt);
                }
                current = current.next;
            }
        }
        return results;
    }

    /**
     * This method is getting all keys in the map
     * @return list of all key strings
     *
     * pre-condition: none
     * post-condition: list of keys is returned
     */
    public String[] getAllKeys() {
        String[] keys = new String[size];
        int index = 0;
        for (int i = 0; i < capacity; i++) {
            Entry current = table[i];
            while (current != null) {
                keys[index] = current.key;
                index++;
                current = current.next;
            }
        }
        return keys;
    }

    /**
     * This method is telling how many entries is in the map
     * @return the number of entries
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
