package com.leaselens.datastructures;

import com.leaselens.model.NearbyPlace;

/**
 * Sorted Linked List data structure implementing Priority Queue ADT
 * The place with smallest distance is always at the front
 * When we insert we walk the list and put it in the right spot
 * So the list is always sorted from smallest to biggest distance
 *
 * pre-condition: none
 * post-condition: none
 */
public class PlacePriorityQueue implements PriorityQueueInterface {

    /**
     * This inner class is one node in the list
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

    // the front of the list (smallest distance)
    private Node head;

    // how many places are in the queue
    private int size;

    /**
     * This constructor is making a new empty priority queue
     *
     * pre-condition: none
     * post-condition: empty priority queue is created
     */
    public PlacePriorityQueue() {
        head = null;
        size = 0;
    }

    /**
     * This method is adding a place to the queue in sorted order
     * We walk the list and find the right spot based on distance
     * @param place the nearby place to add
     *
     * pre-condition: place should not be null
     * post-condition: place is added in the right position
     */
    public void insert(NearbyPlace place) {
        Node newNode = new Node(place);

        // if list is empty or new place is smaller than head
        if (head == null) {
            head = newNode;
        } else if (place.getDistance() < head.place.getDistance()) {
            // new place go at the front
            newNode.next = head;
            head = newNode;
        } else {
            // walk the list to find the right spot
            Node current = head;

            // keep going while next node exist and next node is smaller
            while (current.next != null && current.next.place.getDistance() < place.getDistance()) {
                current = current.next;
            }

            // insert after current
            newNode.next = current.next;
            current.next = newNode;
        }

        size = size + 1;
    }

    /**
     * This method is removing and returning the closest place
     * The closest is always at the front because list is sorted
     * @return the place with smallest distance, or null if empty
     *
     * pre-condition: none
     * post-condition: closest place is removed and returned
     */
    public NearbyPlace removeMin() {
        if (head == null) {
            return null;
        }

        NearbyPlace minPlace = head.place;
        head = head.next;
        size = size - 1;

        return minPlace;
    }

    /**
     * This method is looking at the closest place without removing it
     * @return the place with smallest distance, or null if empty
     *
     * pre-condition: none
     * post-condition: queue is not changed
     */
    public NearbyPlace peek() {
        if (head == null) {
            return null;
        }
        return head.place;
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
        head = null;
        size = 0;
    }
}
