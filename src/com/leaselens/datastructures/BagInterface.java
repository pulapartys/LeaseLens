package com.leaselens.datastructures;

import com.leaselens.model.Apartment;

/**
 * This interface is for Bag data structure
 * Bag is like a container where we put things and dont care about order
 */
public interface BagInterface {

    /**
     * This method is adding apartment to the bag
     * @param apartment the apartment we want to add
     * @return true if added, false if not
     *
     * pre-condition: apartment should not be null
     * post-condition: apartment is in the bag and size go up by 1
     */
    public abstract boolean add(Apartment apartment);

    /**
     * This method is removing any one apartment from bag
     * It pick one and remove it, we dont care which one
     * @return the apartment that was removed, null if bag empty
     *
     * pre-condition: none
     * post-condition: one apartment is removed and size go down by 1
     */
    public abstract Apartment remove();

    /**
     * This method is removing apartment by its id
     * @param id the id of apartment we want to remove
     * @return true if removed, false if not found
     *
     * pre-condition: id should not be null
     * post-condition: apartment is removed and size go down by 1
     */
    public abstract boolean remove(String id);

    /**
     * This method is checking if apartment exist in bag
     * @param id the id to look for
     * @return true if found, false if not
     *
     * pre-condition: id should not be null
     * post-condition: bag is not changed
     */
    public abstract boolean contains(String id);

    /**
     * This method is telling how many apartments in bag
     * @return the number of apartments
     *
     * pre-condition: none
     * post-condition: bag is not changed
     */
    public abstract int getCurrentSize();

    /**
     * This method is counting how many times apartment appear in bag
     * @param apartment the apartment we want to count
     * @return how many times it appear (0 if not found)
     *
     * pre-condition: apartment should not be null
     * post-condition: bag is not changed
     */
    public abstract int getFrequencyOf(Apartment apartment);

    /**
     * This method is checking if bag is empty
     * @return true if empty, false if has items
     *
     * pre-condition: none
     * post-condition: bag is not changed
     */
    public abstract boolean isEmpty();

    /**
     * This method is removing all apartments from bag
     *
     * pre-condition: none
     * post-condition: bag is empty and size is 0
     */
    public abstract void clear();

    /**
     * This method is giving back all apartments as array
     * @return array with all apartments
     *
     * pre-condition: none
     * post-condition: bag is not changed
     */
    public abstract Apartment[] toArray();
}
