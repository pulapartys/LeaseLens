# LeaseLens - Project Requirements Document

## Project Overview

LeaseLens is a JavaFX apartment hunting application that helps users find, compare, and track apartments in the Boston area. This document explains how we used the required data structures and concepts in our project.

---

## Core Requirements

| Requirement | How We Met It |
|-------------|---------------|
| Eclipse Java Project | Yes - standard Java project |
| GUI (JavaFX or Swing) | Yes - we used JavaFX. Our Main.java extends Application |

---

## List 1 Topics - We Implemented 3 Out of 4

### 1. Bag

**File:** `src/com/leaselens/datastructures/ApartmentBag.java`

**What is a Bag?**
A bag is like a container where you can put things in and look at them, but you don't care about the order. It's different from a list because items aren't in any particular sequence.

**How We Implemented It:**
We made our own ApartmentBag class that uses an array inside. When the array gets full, we make it bigger by doubling the size. We also have an inner class called BagIterator so we can use for-each loops.

**What Feature Uses It:** Primary Apartment Storage

**What This Feature Does:**
The bag holds ALL the apartments in our app. When a user adds a new apartment, it goes into this bag. When they want to see all their apartments, we go through the bag and display each one. When they delete an apartment, we remove it from the bag.

**Where We Use It in the Code:**

In ApartmentService.java:
```java
// We declare it at the top
private ApartmentBag allApartments;

// We create it in the constructor
allApartments = new ApartmentBag();

// When user adds an apartment
allApartments.add(apartment);

// When user deletes an apartment
allApartments.remove(id);

// When we need to show all apartments or calculate stats
for (Apartment apt : allApartments) {
    // do something with each apartment
}
```

---

### 2. Stacks

**File:** `src/com/leaselens/datastructures/UndoRedoStack.java`

**What is a Stack?**
A stack is like a pile of plates - the last plate you put on top is the first one you take off. This is called LIFO (Last In First Out). You can only add or remove from the top.

**How We Implemented It:**
We made our own UndoRedoStack class using linked nodes. Each node holds an Action and points to the next node below it. We have a "top" pointer that always points to the top of the stack.

**What Feature Uses It:** Undo/Redo

**What This Feature Does:**
We use TWO stacks together to let users undo and redo their actions. When the user does something (like add an apartment), we save that action on the undo stack. If they click undo, we take it off the undo stack and put it on the redo stack, then reverse what they did. If they click redo, we do the opposite.

**How Undo/Redo Works Step by Step:**

| What User Does | Undo Stack | Redo Stack | What Happens |
|----------------|------------|------------|--------------|
| Adds Apartment A | [A] | [] | A is now showing |
| Adds Apartment B | [A, B] | [] | A and B showing |
| Clicks Undo | [A] | [B] | B disappears |
| Clicks Undo | [] | [B, A] | A disappears too |
| Clicks Redo | [A] | [B] | A comes back |
| Adds Apartment C | [A, C] | [] | Redo stack gets cleared |

**Where We Use It in the Code:**

In ApartmentService.java:
```java
// We have two stacks
private UndoRedoStack undoStack;
private UndoRedoStack redoStack;

// When user adds apartment, we save the action
Action action = new Action("ADD", null, apartment.makeCopy());
undoStack.push(action);
redoStack.clear();  // new action means you cant redo old stuff

// When user clicks undo
public String undo() {
    if (undoStack.isEmpty()) {
        return null;
    }
    Action action = undoStack.pop();
    redoStack.push(action);

    // now we reverse the action
    if (action.getType().equals("ADD")) {
        // undo adding = remove it
        allApartments.remove(action.getAfter().getId());
    }
    // ... handle other action types
}

// When user clicks redo
public String redo() {
    if (redoStack.isEmpty()) {
        return null;
    }
    Action action = redoStack.pop();
    undoStack.push(action);

    // now we redo the action
    // ...
}
```

---

### 3. Recursion

**File:** `src/com/leaselens/datastructures/ApartmentSorter.java`

**What is Recursion?**
Recursion is when a method calls itself to solve a smaller version of the same problem. Every recursive method needs a base case (when to stop) and a recursive case (when to call itself again).

---

#### Recursion in ApartmentSorter.java

**What Feature Uses It:** Sorting Apartments

**What This Feature Does:**
When the user picks a sort option like "Rent: Low to High" or "Walk Score: High to Low", we use merge sort to put the apartments in order. Merge sort works by splitting the list in half over and over until you have single items, then merging them back together in sorted order.

**The Recursive Method:**
```java
private static void mergeSortHelper(Apartment[] arr, Apartment[] temp,
                                     int left, int right,
                                     Comparator<Apartment> comp) {
    // BASE CASE - only one element means its already sorted
    if (left >= right) {
        return;
    }

    int mid = (left + right) / 2;

    // RECURSIVE CASE - sort left half
    mergeSortHelper(arr, temp, left, mid, comp);

    // RECURSIVE CASE - sort right half
    mergeSortHelper(arr, temp, mid + 1, right, comp);

    // merge the two sorted halves together
    merge(arr, temp, left, mid, right, comp);
}
```

**Example of How Merge Sort Works:**
```
Start:    [2500, 1800, 2200, 1500, 3000]

Split:    [2500, 1800] and [2200, 1500, 3000]
Split:    [2500] [1800] and [2200] [1500, 3000]
Split:    [2500] [1800] [2200] [1500] [3000]
          (now everything is single items - base case!)

Merge:    [1800, 2500] and [2200] and [1500, 3000]
Merge:    [1800, 2200, 2500] and [1500, 3000]
Merge:    [1500, 1800, 2200, 2500, 3000]

Done! Sorted from cheapest to most expensive
```

**Where We Use It in the Code:**

In ApartmentService.java:
```java
public Apartment[] getSorted(Comparator<Apartment> comparator) {
    Apartment[] arr = allApartments.toArray();
    ApartmentSorter.mergeSort(arr, comparator);
    return arr;
}
```

The user can sort by:
- Rent (low to high or high to low)
- Square footage
- Number of bedrooms
- Walk score
- Distance to T station
- Safety score

---

## List 2 Topics - We Implemented 4 Out of 9

### 1. Queues, Deques, Priority Queue

**Files:**
- `src/com/leaselens/datastructures/PlaceFilterQueue.java` (Queue)
- `src/com/leaselens/datastructures/SearchHistoryDeque.java` (Deque)
- `src/com/leaselens/datastructures/PlacePriorityQueue.java` (Priority Queue)

**What are these?**
- A **Queue** is like a line at a store - first person in line gets served first (FIFO - First In First Out)
- A **Deque** (double-ended queue) lets you add and remove from both ends
- A **Priority Queue** is like an emergency room - the most urgent patient gets seen first, not whoever came first

**How We Implemented Them:**

For the **Queue**, we used linked nodes with a front and back pointer. New items go to the back, and we remove from the front.

For the **Deque**, we used a doubly linked list where each node has a next AND previous pointer. This lets us add/remove from either end.

For the **Priority Queue**, we used an array-based min-heap. The place with the smallest distance is always at the top. When we add something, we "bubble it up" to the right spot. When we remove, we "bubble down" to fix the heap.

**What Feature Uses It:** View Nearby Places

**What This Feature Does:**
When the user clicks "View Nearby Places" on an apartment, we show restaurants, shops, parks etc. that are close by. The user can filter by category (like "only restaurants") and search by name. They can also undo their searches. All the places are sorted so the closest ones show first.

**How All Three Work Together:**

```
Step 1: We get all nearby places from the API

Step 2: Put ALL places into the Queue (FIFO order)
        filterQueue.enqueue(place)

Step 3: Take each place out of the Queue and check if it matches the filter
        while (!filterQueue.isEmpty()) {
            NearbyPlace place = filterQueue.dequeue();
            if (matchesFilter) {
                priorityQueue.insert(place);  // matching ones go to heap
            }
        }

Step 4: Take places out of the Priority Queue (closest first)
        while (!priorityQueue.isEmpty()) {
            NearbyPlace place = priorityQueue.removeMin();  // closest comes out
            // show this place to user
        }

Step 5: Deque tracks search history for undo
        searchHistory.addLast(searchText);     // save search
        searchHistory.removeLast();            // undo search
```

**Where We Use It in the Code:**

In ApartmentsTab.java (the showNearbyPlaces method):
```java
// Create the search history deque
SearchHistoryDeque searchHistory = new SearchHistoryDeque();

// Put all places into the filter queue
PlaceFilterQueue filterQueue = new PlaceFilterQueue();
for (int i = 0; i < allPlaces.size(); i++) {
    filterQueue.enqueue(allPlaces.get(i));
}

// Create priority queue for sorting by distance
PlacePriorityQueue priorityQueue = new PlacePriorityQueue(100);

// Dequeue each place and check if it matches filter
while (!filterQueue.isEmpty()) {
    NearbyPlace place = filterQueue.dequeue();
    if (matchesCategory && matchesSearch) {
        priorityQueue.insert(place);  // add to heap
    }
}

// Remove from priority queue to display closest first
while (!priorityQueue.isEmpty()) {
    NearbyPlace place = priorityQueue.removeMin();
    // display this place
}

// When user searches, save to deque
searchHistory.addLast(text);

// When user clicks undo, remove last search
searchHistory.removeLast();
searchField.setText(searchHistory.peekLast());
```

---

### 2. Sorting

**File:** `src/com/leaselens/datastructures/ApartmentSorter.java`

**What is Sorting?**
Sorting means putting things in order - like arranging apartments from cheapest to most expensive, or from highest walk score to lowest.

**How We Implemented It:**
We used merge sort, which is a divide-and-conquer algorithm. It splits the array in half over and over until you have single items, then merges them back in sorted order. We also wrote different comparators so the user can pick how to sort.

**What Feature Uses It:** Sort Dropdown

**What This Feature Does:**
In the apartments list, theres a dropdown that lets the user pick how to sort. They can sort by rent, square footage, bedrooms, walk score, safety score, or distance to the T station.

**Where We Use It in the Code:**

In ApartmentsTab.java:
```java
Apartment[] sortArray = results.toArray(new Apartment[0]);
String sortValue = sortBy.getValue();

if (sortValue.equals("Rent (Low-High)")) {
    ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byRentLowToHigh());
} else if (sortValue.equals("Rent (High-Low)")) {
    ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byRentHighToLow());
} else if (sortValue.equals("Sqft (Large-Small)")) {
    ApartmentSorter.mergeSort(sortArray, ApartmentSorter.bySqftHighToLow());
} else if (sortValue.equals("Walk Score")) {
    ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byWalkScoreHighToLow());
} else if (sortValue.equals("Safety Score")) {
    ApartmentSorter.mergeSort(sortArray, ApartmentSorter.bySafetyScoreHighToLow());
} else if (sortValue.equals("Distance to T")) {
    ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byDistanceToTLowToHigh());
}
```

(Note: The recursive merge sort code is already shown in the List 1 Recursion section above)

---

### 3. Hashing

**File:** `src/com/leaselens/datastructures/ApartmentHashMap.java`

**What is Hashing?**
Hashing is a way to store things so you can find them really fast. Instead of looking through everything one by one, we use a math formula (hash function) to figure out exactly where something is stored.

**How We Implemented It:**
We made our own hash map with an array of linked lists (this is called separate chaining). When we want to store an apartment, we run the key through our hash function to get an index. If two things end up at the same index (collision), we chain them together in a linked list.

Our hash function:
```java
private int hash(String key) {
    int hashCode = 0;
    for (int i = 0; i < key.length(); i++) {
        char c = key.charAt(i);
        hashCode = 31 * hashCode + c;
    }
    return hashCode % capacity;
}
```

**What Feature Uses It:** Search Bar

**What This Feature Does:**
When the user types in the search bar, we quickly find all apartments that match. We store each apartment with THREE keys - its ID, name, and address. So if the user searches "Boston" it will find apartments with "Boston" in their name or address.

**Where We Use It in the Code:**

In ApartmentService.java:
```java
// We create the hash map
private ApartmentHashMap searchMap;
searchMap = new ApartmentHashMap(100);

// When adding an apartment, we store it 3 times with different keys
searchMap.put(apartment.getId(), apartment);
searchMap.put(apartment.getName(), apartment);
searchMap.put(apartment.getAddress(), apartment);

// When user searches
public ArrayList<Apartment> search(String query) {
    return searchMap.search(query);
}
```

In ApartmentsTab.java:
```java
// When user types in search bar and hits enter
if (!searchText.isEmpty()) {
    results = service.search(searchText);
}
```

---

### 4. Heap

**File:** `src/com/leaselens/datastructures/PlacePriorityQueue.java`

**What is a Heap?**
A heap is a special tree structure where the parent is always smaller (min-heap) or larger (max-heap) than its children. We use an array to store it, where for any node at index i, its children are at 2i+1 and 2i+2.

**How We Implemented It:**
Our PlacePriorityQueue uses a min-heap internally. The place with the smallest distance is always at index 0 (the root). When we add a new place, we put it at the end and "bubble up" until the heap property is restored. When we remove the minimum, we move the last item to the root and "bubble down".

```java
// Bubble up - move a node up if its smaller than parent
private void bubbleUp(int index) {
    while (index > 0) {
        int parentIndex = (index - 1) / 2;
        if (heap[index].getDistance() < heap[parentIndex].getDistance()) {
            // swap with parent
            NearbyPlace temp = heap[index];
            heap[index] = heap[parentIndex];
            heap[parentIndex] = temp;
            index = parentIndex;
        } else {
            break;
        }
    }
}

// Bubble down - move a node down if its bigger than children
private void bubbleDown(int index) {
    while (true) {
        int leftChild = 2 * index + 1;
        int rightChild = 2 * index + 2;
        int smallest = index;

        if (leftChild < size && heap[leftChild].getDistance() < heap[smallest].getDistance()) {
            smallest = leftChild;
        }
        if (rightChild < size && heap[rightChild].getDistance() < heap[smallest].getDistance()) {
            smallest = rightChild;
        }

        if (smallest != index) {
            // swap and continue
            NearbyPlace temp = heap[index];
            heap[index] = heap[smallest];
            heap[smallest] = temp;
            index = smallest;
        } else {
            break;
        }
    }
}
```

**What Feature Uses It:** View Nearby Places (sorting by distance)

**What This Feature Does:**
When showing nearby places, we want the closest ones to appear first. The heap lets us do this efficiently. Every time we call removeMin(), we get the next closest place.

**Where We Use It in the Code:**

In ApartmentsTab.java:
```java
// Create priority queue (which uses heap internally)
PlacePriorityQueue priorityQueue = new PlacePriorityQueue(100);

// Insert places - heap maintains order automatically
priorityQueue.insert(place);

// Remove places in order of distance (closest first)
while (!priorityQueue.isEmpty()) {
    NearbyPlace place = priorityQueue.removeMin();
    // this gives us closest place first, then next closest, etc.
}
```

---

## Summary

**List 1 Topics - We implemented 3 (only 1 required):**

| Topic | What We Made | What It Does In The App |
|-------|--------------|-------------------------|
| Bag | ApartmentBag.java | Stores all the apartments |
| Stacks | UndoRedoStack.java | Lets users undo and redo their actions |
| Recursion | ApartmentSorter.java | Sorting apartments using merge sort |

**List 2 Topics - We implemented 4 (only 3 required):**

| Topic | What We Made | What It Does In The App |
|-------|--------------|-------------------------|
| Queue, Deque, Priority Queue | PlaceFilterQueue.java, SearchHistoryDeque.java, PlacePriorityQueue.java | View nearby places with filtering and search history |
| Sorting | ApartmentSorter.java | Sort apartments by rent, sqft, walk score, etc. |
| Hashing | ApartmentHashMap.java | Search bar to find apartments by name or address |
| Heap | PlacePriorityQueue.java | Sort nearby places by distance (closest first) |

All of these are our own implementations - we didn't use Java's built-in ArrayList, HashMap, PriorityQueue, or Collections.sort(). We wrote everything from scratch with our own inner classes for nodes, entries, and iterators.
