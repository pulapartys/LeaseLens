package com.leaselens.datastructures;

import com.leaselens.model.NearbyPlace;

/**
 * This interface is for Priority Queue data structure
 * Priority queue is like emergency room - most important one come out first
 * We use min heap so smallest distance come out first
 */
public interface PriorityQueueInterface {

    /**
     * This method is adding place to priority queue
     * It go to the right spot based on distance
     * @param place the place we want to add
     *
     * pre-condition: place should not be null
     * post-condition: place is in queue at right position and size go up by 1
     */
    public abstract void insert(NearbyPlace place);

    /**
     * This method is removing and giving back closest place
     * @return the place with smallest distance, null if queue empty
     *
     * pre-condition: none
     * post-condition: closest place is removed and size go down by 1
     */
    public abstract NearbyPlace removeMin();

    /**
     * This method is looking at closest place but not removing
     * @return the place with smallest distance, null if queue empty
     *
     * pre-condition: none
     * post-condition: queue is not changed
     */
    public abstract NearbyPlace peek();

    /**
     * This method is checking if queue is empty
     * @return true if empty, false if has items
     *
     * pre-condition: none
     * post-condition: queue is not changed
     */
    public abstract boolean isEmpty();

    /**
     * This method is removing all places from queue
     *
     * pre-condition: none
     * post-condition: queue is empty and size is 0
     */
    public abstract void clear();
}
