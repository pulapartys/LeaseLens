package com.leaselens.datastructures;

import com.leaselens.model.NearbyPlace;

/**
 * This class is a simple FIFO queue for nearby places
 * FIFO means first in first out, like a line at a store
 * We use it to hold all the raw places from the API
 * Then we dequeue each one and check if it match the filter
 * It use linked nodes inside so it can grow as big as needed
 *
 * pre-condition: none
 * post-condition: none
 */
public class PlaceFilterQueue implements QueueInterface {

    /**
     * This inner class is one node in the queue
     * It hold one place and a pointer to the next node
     *
     * pre-condition: none
     * post-condition: none
     */
    private class Node {
        NearbyPlace place;
        Node next;

        Node(NearbyPlace place) {
            this.place = place;
            this.next = null;
        }
    }

    // the front of the queue (where we dequeue from)
    private Node front;

    // the back of the queue (where we enqueue to)
    private Node back;

    // how many places are in the queue
    private int size;

    /**
     * This constructor is making a new empty queue
     *
     * pre-condition: none
     * post-condition: empty queue is created
     */
    public PlaceFilterQueue() {
        front = null;
        back = null;
        size = 0;
    }

    /**
     * This method is adding a place to the back of the queue
     * Like getting in line at the back
     * @param place the nearby place to add
     *
     * pre-condition: place should not be null
     * post-condition: place is added to the back
     */
    public void enqueue(NearbyPlace place) {
        Node newNode = new Node(place);

        if (back == null) {
            // queue is empty so front and back are both the new node
            front = newNode;
            back = newNode;
        } else {
            // add to the back
            back.next = newNode;
            back = newNode;
        }

        size = size + 1;
    }

    /**
     * This method is removing and returning the place at the front
     * Like the first person in line getting served
     * @return the place at the front, or null if queue is empty
     *
     * pre-condition: none
     * post-condition: front place is removed and returned
     */
    public NearbyPlace dequeue() {
        if (front == null) {
            return null;
        }

        NearbyPlace place = front.place;
        front = front.next;

        // if the queue is now empty, back should also be null
        if (front == null) {
            back = null;
        }

        size = size - 1;
        return place;
    }

    /**
     * This method is looking at the front place without removing it
     * @return the place at the front, or null if empty
     *
     * pre-condition: none
     * post-condition: queue is not changed
     */
    public NearbyPlace peek() {
        if (front == null) {
            return null;
        }
        return front.place;
    }

    /**
     * This method is telling how many places are in the queue
     * @return the number of places
     *
     * pre-condition: none
     * post-condition: size is returned
     */
    public int size() {
        return size;
    }

    /**
     * This method is checking if the queue is empty
     * @return true if no places in queue
     *
     * pre-condition: none
     * post-condition: true or false is returned
     */
    public boolean isEmpty() {
        if (size == 0) {
            return true;
        }
        return false;
    }

    /**
     * This method is removing all places from queue
     *
     * pre-condition: none
     * post-condition: queue is empty and size is 0
     */
    public void clear() {
        front = null;
        back = null;
        size = 0;
    }
}
