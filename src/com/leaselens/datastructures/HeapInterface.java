package com.leaselens.datastructures;

import com.leaselens.model.NearbyPlace;

/**
 * This interface is for Heap data structure
 * Heap is a complete binary tree where parent is always smaller than children (min-heap)
 * We use this to always get the closest place first
 */
public interface HeapInterface {

    /**
     * This method is inserting a place into the heap
     * It add to the end and bubble up to maintain heap order
     * @param place the nearby place to add
     *
     * pre-condition: place should not be null
     * post-condition: place is in the heap at correct position and size go up by 1
     */
    public abstract void insert(NearbyPlace place);

    /**
     * This method is removing and returning the minimum element (root)
     * It replace root with last element and bubble down to fix heap
     * @return the place with smallest distance, null if empty
     *
     * pre-condition: none
     * post-condition: min element is removed and size go down by 1
     */
    public abstract NearbyPlace removeMin();

    /**
     * This method is looking at the minimum element without removing
     * @return the place with smallest distance, null if empty
     *
     * pre-condition: none
     * post-condition: heap is not changed
     */
    public abstract NearbyPlace peek();

    /**
     * This method is telling how many elements are in the heap
     * @return the number of elements
     *
     * pre-condition: none
     * post-condition: heap is not changed
     */
    public abstract int size();

    /**
     * This method is checking if the heap is empty
     * @return true if empty, false if has items
     *
     * pre-condition: none
     * post-condition: heap is not changed
     */
    public abstract boolean isEmpty();

    /**
     * This method is removing all elements from the heap
     *
     * pre-condition: none
     * post-condition: heap is empty and size is 0
     */
    public abstract void clear();
}
