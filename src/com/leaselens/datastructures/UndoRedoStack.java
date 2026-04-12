package com.leaselens.datastructures;

import com.leaselens.model.Action;

/**
 * This class is a Stack data structure using linked nodes
 * Stack is like a pile of plates - last one put on top is first one taken off
 * We use this for undo and redo so user can go back on their actions
 *
 * pre-condition: none
 * post-condition: an empty stack is created
 */
public class UndoRedoStack implements StackInterface {

    /**
     * This inner class is one node in the stack
     * Each node hold one action and point to the next node below it
     */
    private class Node {
        Action data;
        Node next;

        Node(Action data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node top;
    private int size;

    /**
     * This constructor is making a new empty stack
     *
     * pre-condition: none
     * post-condition: empty stack is created with size 0
     */
    public UndoRedoStack() {
        top = null;
        size = 0;
    }

    /**
     * This method is pushing an action on top of the stack
     * Like putting a new plate on top of the pile
     * @param action the action we want to save
     *
     * pre-condition: action should not be null
     * post-condition: action is on top of stack and size go up by 1
     */
    public void push(Action action) {
        Node newNode = new Node(action);
        newNode.next = top;
        top = newNode;
        size++;
    }

    /**
     * This method is taking the top action off the stack
     * Like taking the top plate off the pile
     * @return the action that was on top, or null if stack is empty
     *
     * pre-condition: stack should not be empty for useful result
     * post-condition: top action is removed and size go down by 1
     */
    public Action pop() {
        if (top == null) {
            return null;
        }
        Action data = top.data;
        top = top.next;
        size--;
        return data;
    }

    /**
     * This method is looking at the top action without removing it
     * Like looking at the top plate without taking it off
     * @return the action on top, or null if stack is empty
     *
     * pre-condition: none
     * post-condition: top action is returned but stack is not changed
     */
    public Action peek() {
        if (top == null) {
            return null;
        }
        return top.data;
    }

    /**
     * This method is checking if the stack is empty
     * @return true if no actions in stack, false if there is some
     *
     * pre-condition: none
     * post-condition: true or false is returned
     */
    public boolean isEmpty() {
        return top == null;
    }

    /**
     * This method is telling how many actions is in the stack
     * @return the number of actions
     *
     * pre-condition: none
     * post-condition: the size number is returned
     */
    public int size() {
        return size;
    }

    /**
     * This method is removing all actions from the stack
     * We use this to clear the redo stack when user do a new action
     *
     * pre-condition: none
     * post-condition: stack is empty and size is 0
     */
    public void clear() {
        top = null;
        size = 0;
    }
}
