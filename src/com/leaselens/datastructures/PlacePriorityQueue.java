package com.leaselens.datastructures;

import com.leaselens.model.NearbyPlace;

/**
 * This class is a priority queue for nearby places
 * It extend PlaceMinHeap so it inherit all the heap operations
 * The place with the smallest distance come out first
 *
 * pre-condition: none
 * post-condition: an empty priority queue is created
 */
public class PlacePriorityQueue extends PlaceMinHeap implements PriorityQueueInterface {

    /**
     * This constructor is making a new priority queue
     * It call the parent PlaceMinHeap constructor to set up the heap
     * @param capacity how many places it can hold at start
     *
     * pre-condition: capacity should be bigger than 0
     * post-condition: empty priority queue is created
     */
    public PlacePriorityQueue(int capacity) {
        super(capacity);
    }
}
