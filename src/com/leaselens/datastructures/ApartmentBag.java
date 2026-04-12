package com.leaselens.datastructures;

import com.leaselens.model.Apartment;

/**
 * This class is a Bag data structure for storing apartments
 * A bag is like a container where you can put things in and look at them
 * but you dont really care about the order
 * We use array inside that grows bigger when it get full
 *
 * pre-condition: none
 * post-condition: an empty bag is created ready to store apartments
 */
public class ApartmentBag implements BagInterface {

    private Apartment[] items;
    private int size;      // actual count of apartments in bag

    /**
     * This constructor is making a new empty bag
     * It start with space for 10 apartments
     *
     * pre-condition: none
     * post-condition: empty bag is created with capacity 10
     */
    public ApartmentBag() {
        items = new Apartment[10];
        size = 0;
    }

    /**
     * This method is adding an apartment to the bag
     * If the bag is full it make the array bigger first
     * @param apartment the apartment we want to add
     * @return true if apartment is added, false if apartment is null
     *
     * pre-condition: apartment should not be null
     * post-condition: apartment is added to the bag and size go up by 1
     */
    public boolean add(Apartment apartment) {
        if (apartment == null) {
            return false;
        }

        // if array is full, make it bigger
        if (size == items.length) {
            resize();
        }

        items[size] = apartment;
        size++;
        return true;
    }

    /**
     * This method is making the array bigger when it becomes full
     * It creates new array that is double the size and copy everything over
     *
     * pre-condition: the array is full
     * post-condition: array is now double the size with same items
     */
    private void resize() {
        Apartment[] bigger = new Apartment[items.length * 2];
        for (int i = 0; i < size; i++) {
            bigger[i] = items[i];
        }
        items = bigger;
    }

    /**
     * This method is checking if the bag have a certain apartment
     * It look through all items and compare by ID
     * @param id the id of the apartment we looking for
     * @return true if found, false if not found
     *
     * pre-condition: id should not be null
     * post-condition: returns true or false, bag is not changed
     */
    public boolean contains(String id) {
        for (int i = 0; i < size; i++) {
            if (items[i].getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is finding an apartment by its ID
     * @param id the id we looking for
     * @return the apartment if found, null if not found
     *
     * pre-condition: id should not be null
     * post-condition: apartment is returned or null
     */
    public Apartment getById(String id) {
        for (int i = 0; i < size; i++) {
            if (items[i].getId().equals(id)) {
                return items[i];
            }
        }
        return null;
    }

    /**
     * This method is removing any one apartment from bag
     * It just remove the last one because that is easiest
     * @return the apartment that was removed, null if bag empty
     *
     * pre-condition: none
     * post-condition: one apartment is removed and size go down by 1
     */
    public Apartment remove() {
        if (size == 0) {
            return null;
        }
        Apartment removed = items[size - 1];
        items[size - 1] = null;
        size--;
        return removed;
    }

    /**
     * This method is removing an apartment from the bag by its ID
     * It find the apartment and then move the last item to fill the gap
     * @param id the id of apartment to remove
     * @return true if removed, false if not found
     *
     * pre-condition: id should not be null
     * post-condition: apartment is removed and size go down by 1
     */
    public boolean remove(String id) {
        for (int i = 0; i < size; i++) {
            if (items[i].getId().equals(id)) {
                // move last item to this spot
                items[i] = items[size - 1];
                items[size - 1] = null;
                size--;
                return true;
            }
        }
        return false;
    }

    /**
     * This method is telling how many apartments is in the bag
     * @return the number of apartments
     *
     * pre-condition: none
     * post-condition: the size number is returned
     */
    public int getCurrentSize() {
        return size;
    }

    /**
     * This method is counting how many times apartment appear in bag
     * We compare by ID because each apartment have unique ID
     * @param apartment the apartment we want to count
     * @return how many times it appear (0 or 1 since IDs are unique)
     *
     * pre-condition: apartment should not be null
     * post-condition: bag is not changed
     */
    public int getFrequencyOf(Apartment apartment) {
        if (apartment == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < size; i++) {
            if (items[i].getId().equals(apartment.getId())) {
                count++;
            }
        }
        return count;
    }

    /**
     * This method is checking if the bag is empty
     * @return true if no apartments, false if there is some
     *
     * pre-condition: none
     * post-condition: true or false is returned
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * This method is removing all apartments from the bag
     *
     * pre-condition: none
     * post-condition: bag is empty and size is 0
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            items[i] = null;
        }
        size = 0;
    }

    /**
     * This method is getting apartment at a certain position
     * @param index the position number starting from 0
     * @return the apartment at that position, or null if bad index
     *
     * pre-condition: index should be between 0 and size-1
     * post-condition: apartment is returned or null
     */
    public Apartment get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return items[index];
    }

    /**
     * This method is giving back all apartments as a regular array
     * @return array of all apartments in the bag
     *
     * pre-condition: none
     * post-condition: new array with all apartments is returned
     */
    public Apartment[] toArray() {
        Apartment[] result = new Apartment[size];
        for (int i = 0; i < size; i++) {
            result[i] = items[i];
        }
        return result;
    }

}
