package com.leaselens.datastructures;

import com.leaselens.model.Action;

/**
 * This interface is for Stack data structure
 * Stack is like pile of plates - last one in is first one out (LIFO)
 */
public interface StackInterface {

    /**
     * This method is putting action on top of stack
     * @param action the action we want to add
     *
     * pre-condition: action should not be null
     * post-condition: action is on top and size go up by 1
     */
    public abstract void push(Action action);

    /**
     * This method is removing and giving back top action
     * @return the action from top, null if stack empty
     *
     * pre-condition: none
     * post-condition: top action is removed and size go down by 1
     */
    public abstract Action pop();

    /**
     * This method is looking at top action but not removing
     * @return the action on top, null if stack empty
     *
     * pre-condition: none
     * post-condition: stack is not changed
     */
    public abstract Action peek();

    /**
     * This method is checking if stack is empty
     * @return true if empty, false if has items
     *
     * pre-condition: none
     * post-condition: stack is not changed
     */
    public abstract boolean isEmpty();

    /**
     * This method is removing all actions from stack
     *
     * pre-condition: none
     * post-condition: stack is empty and size is 0
     */
    public abstract void clear();
}
