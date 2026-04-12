package com.leaselens.datastructures;

import com.leaselens.model.NearbyPlace;

/**
 * This interface is for Queue data structure
 * Queue is like line at store - first one in is first one out (FIFO)
 */
public interface QueueInterface {

    /**
     * This method is adding place to back of queue
     * @param place the place we want to add
     *
     * pre-condition: place should not be null
     * post-condition: place is at back and size go up by 1
     */
    public abstract void enqueue(NearbyPlace place);

    /**
     * This method is removing and giving back front place
     * @return the place from front, null if queue empty
     *
     * pre-condition: none
     * post-condition: front place is removed and size go down by 1
     */
    public abstract NearbyPlace dequeue();

    /**
     * This method is looking at front place but not removing
     * @return the place at front, null if queue empty
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
