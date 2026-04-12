package com.leaselens.datastructures;

import com.leaselens.model.NearbyPlace;

/**
 * This class is a min-heap data structure for nearby places
 * It is a complete binary tree stored in an array where the parent
 * always has smaller distance than its children
 * The place with the smallest distance is always at the root (index 0)
 *
 * pre-condition: none
 * post-condition: an empty min-heap is created
 */
public class PlaceMinHeap implements HeapInterface {

    // the array that hold all the places in heap order
    private NearbyPlace[] heap;

    // how many places are in the heap right now
    private int size;

    /**
     * This constructor is making a new min-heap with given capacity
     * @param capacity how many places it can hold at start
     *
     * pre-condition: capacity should be bigger than 0
     * post-condition: empty min-heap is created
     */
    public PlaceMinHeap(int capacity) {
        heap = new NearbyPlace[capacity];
        size = 0;
    }

    /**
     * This method is adding a place to the heap
     * It put the place at the end and then bubble it up to the right spot
     * @param place the nearby place to add
     *
     * pre-condition: place should not be null
     * post-condition: place is added in the right position
     */
    public void insert(NearbyPlace place) {
        // if the heap is full, make it bigger
        if (size >= heap.length) {
            NearbyPlace[] bigger = new NearbyPlace[heap.length * 2];
            for (int i = 0; i < heap.length; i++) {
                bigger[i] = heap[i];
            }
            heap = bigger;
        }

        // put the new place at the end
        heap[size] = place;
        size = size + 1;

        // bubble it up to the right spot
        bubbleUp(size - 1);
    }

    /**
     * This method is removing and returning the closest place
     * It take the root which is smallest distance
     * Then put the last item at root and bubble it down
     * @return the place with the smallest distance, or null if empty
     *
     * pre-condition: none
     * post-condition: closest place is removed and returned
     */
    public NearbyPlace removeMin() {
        if (size == 0) {
            return null;
        }

        // save the minimum which is the root
        NearbyPlace min = heap[0];

        // move the last item to the root
        size = size - 1;
        heap[0] = heap[size];
        heap[size] = null;

        // bubble it down to the right spot
        if (size > 0) {
            bubbleDown(0);
        }

        return min;
    }

    /**
     * This method is looking at the closest place without removing it
     * @return the place with the smallest distance, or null if empty
     *
     * pre-condition: none
     * post-condition: heap is not changed
     */
    public NearbyPlace peek() {
        if (size == 0) {
            return null;
        }
        return heap[0];
    }

    /**
     * This method is bubbling a place up to fix the heap
     * If the place has smaller distance than its parent we swap them
     * Keep going until the heap order is good
     * @param index where the place is right now
     *
     * pre-condition: index should be valid
     * post-condition: heap order is fixed going up
     */
    private void bubbleUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;

            // if current place is closer than parent, swap them
            if (heap[index].getDistance() < heap[parentIndex].getDistance()) {
                NearbyPlace temp = heap[index];
                heap[index] = heap[parentIndex];
                heap[parentIndex] = temp;

                // move up to parent position
                index = parentIndex;
            } else {
                // parent is already smaller so we are done
                break;
            }
        }
    }

    /**
     * This method is bubbling a place down to fix the heap
     * If the place has bigger distance than its children we swap with smaller child
     * Keep going until the heap order is good
     * @param index where the place is right now
     *
     * pre-condition: index should be valid
     * post-condition: heap order is fixed going down
     */
    private void bubbleDown(int index) {
        while (true) {
            int leftChild = 2 * index + 1;
            int rightChild = 2 * index + 2;
            int smallest = index;

            // check if left child is smaller
            if (leftChild < size) {
                if (heap[leftChild].getDistance() < heap[smallest].getDistance()) {
                    smallest = leftChild;
                }
            }

            // check if right child is even smaller
            if (rightChild < size) {
                if (heap[rightChild].getDistance() < heap[smallest].getDistance()) {
                    smallest = rightChild;
                }
            }

            // if a child is smaller, swap and keep going
            if (smallest != index) {
                NearbyPlace temp = heap[index];
                heap[index] = heap[smallest];
                heap[smallest] = temp;

                index = smallest;
            } else {
                // current is smaller than both children so we are done
                break;
            }
        }
    }

    /**
     * This method is telling how many places are in the heap
     * @return the number of places
     *
     * pre-condition: none
     * post-condition: size is returned
     */
    public int size() {
        return size;
    }

    /**
     * This method is checking if the heap is empty
     * @return true if no places in heap
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
     * This method is removing all places from heap
     *
     * pre-condition: none
     * post-condition: heap is empty and size is 0
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            heap[i] = null;
        }
        size = 0;
    }
}
