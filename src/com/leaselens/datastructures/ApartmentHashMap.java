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
     * It turn the key string into a number that is a valid index
     * @param key the string key to hash
     * @return an index number between 0 and capacity-1
     *
     * pre-condition: key should not be null
     * post-condition: a valid index is returned
     */
    private int hash(String key) {
        int hashCode = 0;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            hashCode = 31 * hashCode + c;
        }
        // make sure its positive and within range
        if (hashCode < 0) {
            hashCode = hashCode * -1;
        }
        return hashCode % capacity;
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
     * It look through all keys and find ones that contain the search text
     * @param query the text to search for
     * @return list of apartments that match
     *
     * pre-condition: query should not be null
     * post-condition: list of matching apartments is returned
     */
    public ArrayList<Apartment> search(String query) {
        ArrayList<Apartment> results = new ArrayList<Apartment>();
        String lowerQuery = query.toLowerCase();

        for (int i = 0; i < capacity; i++) {
            Entry current = table[i];
            while (current != null) {
                // skip if key does not match the query
                if (!current.key.contains(lowerQuery)) {
                    current = current.next;
                    continue;
                }

                // check if we already have this apartment in results
                boolean alreadyAdded = false;
                for (int j = 0; j < results.size(); j++) {
                    if (results.get(j).getId().equals(current.value.getId())) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    results.add(current.value);
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
