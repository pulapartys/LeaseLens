package com.leaselens.datastructures;

/**
 * This interface is for Deque data structure
 * Deque is like queue but you can add and remove from both ends
 */
public interface DequeInterface {

    /**
     * This method is adding item to front of deque
     * @param item the string we want to add
     *
     * pre-condition: item should not be null
     * post-condition: item is at front and size go up by 1
     */
    public abstract void addFirst(String item);

    /**
     * This method is adding item to back of deque
     * @param item the string we want to add
     *
     * pre-condition: item should not be null
     * post-condition: item is at back and size go up by 1
     */
    public abstract void addLast(String item);

    /**
     * This method is removing and giving back front item
     * @return the item from front, null if deque empty
     *
     * pre-condition: none
     * post-condition: front item is removed and size go down by 1
     */
    public abstract String removeFirst();

    /**
     * This method is removing and giving back back item
     * @return the item from back, null if deque empty
     *
     * pre-condition: none
     * post-condition: back item is removed and size go down by 1
     */
    public abstract String removeLast();

    /**
     * This method is looking at front item but not removing
     * @return the item at front, null if deque empty
     *
     * pre-condition: none
     * post-condition: deque is not changed
     */
    public abstract String peekFirst();

    /**
     * This method is looking at back item but not removing
     * @return the item at back, null if deque empty
     *
     * pre-condition: none
     * post-condition: deque is not changed
     */
    public abstract String peekLast();

    /**
     * This method is checking if deque is empty
     * @return true if empty, false if has items
     *
     * pre-condition: none
     * post-condition: deque is not changed
     */
    public abstract boolean isEmpty();
}
