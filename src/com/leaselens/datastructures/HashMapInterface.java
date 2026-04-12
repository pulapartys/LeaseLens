package com.leaselens.datastructures;

/**
 * This interface is for HashMap data structure
 * HashMap is like dictionary - you use key to find value very fast
 * It is generic so it can store any type of value
 *
 * @param <T> the type of value stored in the map
 */
public interface HashMapInterface<T> {

    /**
     * This method is putting key and value into map
     * @param key the key string
     * @param value the value to store
     *
     * pre-condition: key and value should not be null
     * post-condition: value is stored with that key
     */
    public abstract void put(String key, T value);

    /**
     * This method is getting value by key
     * @param key the key to look for
     * @return the value if found, null if not found
     *
     * pre-condition: key should not be null
     * post-condition: map is not changed
     */
    public abstract T get(String key);

    /**
     * This method is checking if key exist in map
     * @param key the key to look for
     * @return true if key exist, false if not
     *
     * pre-condition: key should not be null
     * post-condition: map is not changed
     */
    public abstract boolean containsKey(String key);

    /**
     * This method is removing entry by key
     * @param key the key to remove
     * @return true if removed, false if key not found
     *
     * pre-condition: key should not be null
     * post-condition: entry is removed and size go down by 1
     */
    public abstract boolean remove(String key);

    /**
     * This method is checking if map is empty
     * @return true if empty, false if has items
     *
     * pre-condition: none
     * post-condition: map is not changed
     */
    public abstract boolean isEmpty();

    /**
     * This method is removing all entries from map
     *
     * pre-condition: none
     * post-condition: map is empty and size is 0
     */
    public abstract void clear();

    /**
     * This method is telling how many entries is in the map
     * @return the number of entries
     *
     * pre-condition: none
     * post-condition: map is not changed
     */
    public abstract int size();

    /**
     * This method is getting all keys in the map
     * @return array of all key strings
     *
     * pre-condition: none
     * post-condition: map is not changed
     */
    public abstract String[] getAllKeys();
}
