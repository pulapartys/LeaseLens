package com.leaselens.datastructures;

/**
 * This class is a deque (double ended queue) for search history
 * Deque means you can add and remove from both front and back
 * We use it to save what the user searched for
 * When they click undo it remove the last search from the back
 * It use doubly linked nodes so we can go both directions
 *
 * pre-condition: none
 * post-condition: none
 */
public class SearchHistoryDeque implements DequeInterface {

    /**
     * This inner class is one node in the deque
     * It hold one search term and pointers to previous and next node
     *
     * pre-condition: none
     * post-condition: none
     */
    private class Node {
        String searchTerm;
        Node prev;
        Node next;

        Node(String searchTerm) {
            this.searchTerm = searchTerm;
            this.prev = null;
            this.next = null;
        }
    }

    // the front of the deque (oldest search)
    private Node front;

    // the back of the deque (newest search)
    private Node back;

    // how many searches are saved
    private int size;

    /**
     * This constructor is making a new empty deque
     *
     * pre-condition: none
     * post-condition: empty deque is created
     */
    public SearchHistoryDeque() {
        front = null;
        back = null;
        size = 0;
    }

    /**
     * This method is adding a search term to the back of the deque
     * This is the newest search the user typed
     * @param term the search text the user typed
     *
     * pre-condition: term should not be null
     * post-condition: term is added to the back
     */
    public void addLast(String term) {
        Node newNode = new Node(term);

        if (back == null) {
            // deque is empty so front and back are both the new node
            front = newNode;
            back = newNode;
        } else {
            // link new node to the back
            newNode.prev = back;
            back.next = newNode;
            back = newNode;
        }

        size = size + 1;
    }

    /**
     * This method is removing and returning the last search term
     * This is for undo - remove the most recent search
     * @return the last search term, or null if deque is empty
     *
     * pre-condition: none
     * post-condition: last term is removed and returned
     */
    public String removeLast() {
        if (back == null) {
            return null;
        }

        String term = back.searchTerm;

        if (front == back) {
            // only one item, deque becomes empty
            front = null;
            back = null;
        } else {
            // move back to the previous node
            back = back.prev;
            back.next = null;
        }

        size = size - 1;
        return term;
    }

    /**
     * This method is looking at the last search without removing it
     * @return the last search term, or null if empty
     *
     * pre-condition: none
     * post-condition: deque is not changed
     */
    public String peekLast() {
        if (back == null) {
            return null;
        }
        return back.searchTerm;
    }

    /**
     * This method is adding a search term to the front of the deque
     * @param term the search text to add at front
     *
     * pre-condition: term should not be null
     * post-condition: term is added to the front
     */
    public void addFirst(String term) {
        Node newNode = new Node(term);

        if (front == null) {
            // deque is empty
            front = newNode;
            back = newNode;
        } else {
            // link new node to the front
            newNode.next = front;
            front.prev = newNode;
            front = newNode;
        }

        size = size + 1;
    }

    /**
     * This method is removing and returning the first search term
     * This is the oldest search
     * @return the first search term, or null if empty
     *
     * pre-condition: none
     * post-condition: first term is removed and returned
     */
    public String removeFirst() {
        if (front == null) {
            return null;
        }

        String term = front.searchTerm;

        if (front == back) {
            // only one item
            front = null;
            back = null;
        } else {
            // move front to the next node
            front = front.next;
            front.prev = null;
        }

        size = size - 1;
        return term;
    }

    /**
     * This method is looking at the first search without removing it
     * @return the first search term, or null if empty
     *
     * pre-condition: none
     * post-condition: deque is not changed
     */
    public String peekFirst() {
        if (front == null) {
            return null;
        }
        return front.searchTerm;
    }

    /**
     * This method is telling how many searches are saved
     * @return the number of searches
     *
     * pre-condition: none
     * post-condition: size is returned
     */
    public int size() {
        return size;
    }

    /**
     * This method is checking if the deque is empty
     * @return true if no searches saved
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
     * This method is removing everything from the deque
     *
     * pre-condition: none
     * post-condition: deque is empty
     */
    public void clear() {
        front = null;
        back = null;
        size = 0;
    }
}
