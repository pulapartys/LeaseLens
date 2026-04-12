package com.leaselens.datastructures;


/**
 * This class is a hash map for storing expense categories and dollar amounts
 * It use an array of linked lists (separate chaining) for handling collisions
 * We use this in the expense calculator tab to store category name and amount
 *
 * pre-condition: none
 * post-condition: an empty hash map is created
 */
public class ExpenseHashMap implements HashMapInterface<Double> {

    /**
     * This inner class is one entry in the hash map
     * It store a key and value pair and point to next entry for chaining
     */
    private class Entry {
        String key;
        Double value;
        Entry next;

        Entry(String key, Double value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }

    private Entry[] table;
    private int size;
    private int capacity;

    /**
     * This constructor is making a new hash map with given capacity
     * @param capacity how many buckets the hash map should have
     *
     * pre-condition: capacity should be more than 0
     * post-condition: empty hash map is created
     */
    public ExpenseHashMap(int capacity) {
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
     * @param key the category name like "Utilities"
     * @param value the dollar amount
     *
     * pre-condition: key and value should not be null
     * post-condition: value is stored in the hash map
     */
    public void put(String key, Double value) {
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
     * This method is getting a dollar amount by category name
     * @param key the category name to look up
     * @return the dollar amount if found, null if not found
     *
     * pre-condition: key should not be null
     * post-condition: value is returned or null
     */
    public Double get(String key) {
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
     * This method is checking if a category exist in the map
     * @param key the category name to check
     * @return true if key exist, false if not
     *
     * pre-condition: key should not be null
     * post-condition: true or false is returned
     */
    public boolean containsKey(String key) {
        return get(key) != null;
    }

    /**
     * This method is removing a category from the map
     * @param key the category name to remove
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
     * This method is getting all category names in the map
     * @return list of all category name strings
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
