
# LeaseLens — Architecture Document

## 1. Project Overview

**LeaseLens** is a desktop application that helps students and renters organize, compare, and make smart decisions during their apartment search. Instead of juggling multiple websites, spreadsheets, and browser tabs, users store all their apartment options in one place — enriched with real-time data from public APIs.

**Team Size:** 3 students
**Platform:** Desktop (Java)
**Project Type:** Eclipse Java Project — Data Structures Final Project

---

## 2. Problem Statement

When searching for apartments, renters use multiple websites (Zillow, Craigslist, Apartments.com, Facebook Marketplace) and brokers simultaneously. After viewing dozens of listings, it becomes nearly impossible to remember:
- Which apartment had parking?
- Which one was near the T?
- Which ones were already rejected and why?
- What was the rent for that place on Elm Street?

There is no simple, centralized tool for students/renters to **store, compare, track, and rank** apartments they've found across different sources.

---

## 3. Solution

LeaseLens provides a centralized apartment tracking and decision-support tool with:
- Manual apartment entry with rich detail fields
- Automatic data enrichment via free APIs (Walk Score, nearest T stop, geocoding, crime safety data, nearby recreation areas)
- Smart ranking based on user-defined priorities (including safety)
- Visual map view of all saved apartments
- Side-by-side comparison of top choices
- Full undo/redo support for all actions

---

## 4. Target Users

- College students searching for off-campus housing
- Young professionals relocating to a new city
- Renters comparing apartments across multiple sources
- Anyone actively apartment hunting who needs to stay organized

---

## 5. Technology Stack

| Layer              | Technology                     | Purpose                              |
|--------------------|--------------------------------|--------------------------------------|
| Language           | Java 17+                       | Core application logic               |
| IDE                | Eclipse                        | Development environment              |
| GUI Framework      | JavaFX                         | Modern desktop UI with CSS styling   |
| HTTP Client        | java.net.http.HttpClient       | API calls (built into Java 11+)      |
| JSON Parsing       | Gson (com.google.gson)         | Parse API responses                  |
| Data Persistence   | JSON file (local)              | Save/load apartment data to disk     |
| Map Rendering      | OpenStreetMap + JavaFX WebView | Embedded interactive map             |
| Build Tool         | Eclipse Project / Maven        | Dependency management                |

---

## 6. Application Structure — 5 Tabs

### Tab 1: Dashboard
The home screen — a quick overview of everything.

**Features:**
- **Stats Cards:** Total Saved, Shortlisted, Toured, Rejected, Average Rent
- **Top 3 Picks:** Auto-ranked apartments based on user preferences with safety badge showing each apartment's Safety Score from Boston crime data
- **Budget Overview:** Visual indicator showing how many apartments fit within the user's budget

**Data Structures Used:** Bag (iterate all for stats), MergeSort (ranking)

---

### Tab 2: My Apartments
The core data management screen.

**Features:**
- **Sortable Table:** Columns for Name, Address, Rent, Sqft, Beds, Baths, Walk Score, Safety, Parks, Distance to T, Status
  - Sort by any criteria using dropdown (Rent, Sqft, Bedrooms, Walk Score, Safety Score, Distance to T)
  - Sorting implemented using custom MergeSort algorithm
- **Sub-Filter Buttons:** All | New | Shortlisted | Toured | Rejected
  - Each filter returns a separate LinkedList of matching apartments
- **Search Bar:** Type to instantly filter apartments by name or address
  - Powered by HashMap for O(1) lookups
- **Price Range Slider:** Drag to set min-max rent range
  - Filters apartments by iterating through the Bag
- **Row Interactions:**
  - Click a row to expand and view full details (notes, amenities, nearest T stop, Walk Score)
  - Right-click context menu: Edit, Delete, Change Status, Add to Compare
- **Undo / Redo Buttons:** Revert any recent action
  - Powered by Stack (undo stack + redo stack)

**Data Structures Used:** HashMap (search), Bag (price range filter), MergeSort (sort), LinkedList (filtered lists), Stack (undo/redo)

---

### Tab 3: Compare
Side-by-side apartment comparison for decision-making.

**Features:**
- **Apartment Selector:** Dropdown or checklist to pick 2–3 apartments to compare
- **Comparison Table:** Each apartment is a column, each attribute is a row
  - Rows: Rent, Sqft, Rent per Sqft, Bedrooms, Walk Score, Distance to T, Safety Score, Recreation Areas, Nearest T, Amenities, Parking, Status
- **Color Coding:**
  - Green highlight = best value in that row
  - Red highlight = worst value in that row
- **Winner Summary:** Bottom section showing "Oak Ave wins in 5 out of 8 categories"
- **Notes Display:** Each apartment's personal notes shown at the bottom for quick reference

**Data Structures Used:** LinkedList (selected apartments list), Sorting (rank by each category)

---

### Tab 4: Map View
Visual geographic overview of all apartments.

**Features:**
- **Embedded Map:** OpenStreetMap rendered inside a JavaFX WebView
- **Apartment Pins:** Each apartment displayed as a colored pin
  - Green = Shortlisted
  - Blue = New
  - Yellow = Toured
  - Red = Rejected
- **T Stop Pins:** Toggle MBTA T stop locations on/off
- **Pin Click Popup:** Click any pin to see apartment name, rent, and current status
- **Filter Integration:** Map respects the same filters as My Apartments tab (status, price range)

**Data Structures Used:** HashMap (pin lookup by apartment ID)

---

### Tab 5: Settings & Preferences
Control smart ranking and application settings.

**Features:**
- **Budget Range:** Set minimum and maximum rent thresholds
- **Priority Weight Sliders:** Adjust importance of each factor (must total 100%)
  - Low Rent: ____%
  - Space (Sqft): ____%
  - Proximity to T: ____%
  - Walk Score: ____%
  - Amenity Count: ____%
  - Safety: ____%
- **Save / Load Data:** Export and import apartment data as JSON
- **Data Management:** Clear all data, reset preferences

**Data Structures Used:** Heap (recalculate rankings when weights change)

---

## 7. Data Structures — Implementation Details

### From Left List (2 selected — requirement is 1)

#### 7.1 Bag (Chapters 1, 2, 3)
**Custom Implementation:** `ApartmentBag<T>`

A Bag is an unordered collection that supports add and iteration but not removal by index. We implement it using a resizable array internally.

**Where Used:**
- Master storage of all apartments in the application
- Amenity tag collection for each apartment (parking, laundry, gym, etc.)
- Iterating over all apartments to compute dashboard statistics (average rent, total count, etc.)

**Key Operations:**
| Operation | Method         | Time Complexity |
|-----------|----------------|-----------------|
| Add       | `add(item)`    | O(1) amortized  |
| Size      | `size()`       | O(1)            |
| Contains  | `contains()`   | O(n)            |
| Iterate   | `iterator()`   | O(n)            |
| Is Empty  | `isEmpty()`    | O(1)            |

**Implementation:**
```java
public class ApartmentBag<T> implements Iterable<T> {
    private T[] items;
    private int size;

    public void add(T item) { /* add to array, resize if needed */ }
    public int size() { return size; }
    public boolean contains(T item) { /* linear search */ }
    public Iterator<T> iterator() { /* return custom iterator */ }
}
```

---

#### 7.2 Stack (Chapters 5, 6)
**Custom Implementation:** `UndoRedoStack<T>`

A Stack follows Last-In-First-Out (LIFO) ordering. We implement it using a linked structure internally.

**Where Used:**
- **Undo System:** Every user action (add apartment, delete, change status, edit) is pushed onto the undo stack as an `Action` object. Pressing "Undo" pops the action and reverses it.
- **Redo System:** When an action is undone, it's pushed onto a redo stack. Pressing "Redo" pops from redo and re-applies.
- **Navigation History:** Track which apartment details the user viewed, enabling a "Back" button.

**Key Operations:**
| Operation | Method       | Time Complexity |
|-----------|--------------|-----------------|
| Push      | `push(item)` | O(1)            |
| Pop       | `pop()`      | O(1)            |
| Peek      | `peek()`     | O(1)            |
| Is Empty  | `isEmpty()`  | O(1)            |
| Size      | `size()`     | O(1)            |

**Action Object Design:**
```java
public class Action {
    enum Type { ADD, DELETE, EDIT, STATUS_CHANGE }
    private Type type;
    private Apartment before;  // state before the action
    private Apartment after;   // state after the action
}
```

---

### From Right List (4 selected — requirement is 3)

#### 7.3 Lists (Chapters 10, 11, 12)
**Custom Implementation:** `ApartmentLinkedList<T>`

A doubly-linked list that supports insertion, deletion, and traversal in both directions.

**Where Used:**
- Filtered apartment views (shortlisted, toured, rejected — each is its own LinkedList)
- Maintaining the ordered display list for the table view
- Activity feed on the Dashboard (recent actions list)

**Key Operations:**
| Operation       | Method                | Time Complexity |
|----------------|-----------------------|-----------------|
| Add to end     | `addLast(item)`       | O(1)            |
| Add to front   | `addFirst(item)`      | O(1)            |
| Remove         | `remove(item)`        | O(n)            |
| Get by index   | `get(index)`          | O(n)            |
| Size           | `size()`              | O(1)            |
| Iterate        | `iterator()`          | O(n)            |

---

#### 7.4 Sorting (Chapters 15, 16)
**Custom Implementation:** MergeSort algorithm (not using `Collections.sort()`)

A stable, divide-and-conquer sorting algorithm with guaranteed O(n log n) performance.

**Where Used:**
- Sorting the apartment table by any column: Rent, Sqft, Bedrooms, Walk Score, Distance to T
- Sorting comparison results to determine category winners
- Sorting analytics data for chart display

**Why MergeSort:**
- Stable sort — preserves the relative order of apartments with equal values (e.g., if two apartments have the same rent, their original order is maintained)
- Consistent O(n log n) performance regardless of input data
- Natural fit for linked list sorting

**Implementation:**
```java
public class ApartmentSorter {
    public static <T> void mergeSort(ApartmentLinkedList<T> list, Comparator<T> comparator) {
        // Divide list in half
        // Recursively sort each half
        // Merge the two sorted halves
    }
}
```

**Supported Sort Criteria:**
| Column          | Comparator                    | Order          |
|-----------------|-------------------------------|----------------|
| Rent            | byRentLowToHigh()             | Low → High     |
| Rent            | byRentHighToLow()             | High → Low     |
| Sqft            | bySqftHighToLow()             | High → Low     |
| Bedrooms        | byBedroomsHighToLow()         | High → Low     |
| Walk Score      | byWalkScoreHighToLow()        | High → Low     |
| Safety Score    | bySafetyScoreHighToLow()      | High → Low     |
| Distance to T   | byDistanceToTLowToHigh()      | Low → High     |

---

#### 7.5 Hashing (Chapters 22, 23)
**Custom Implementation:** `ApartmentHashMap<K, V>`

A hash table using separate chaining for collision resolution.

**Where Used:**
- **Search Bar:** O(1) lookup of apartments by name or address as the user types
- **Duplicate Detection:** When adding a new apartment, check if the address already exists to prevent duplicates
- **ID-based Lookup:** Quick retrieval of apartment data by unique ID (used throughout the app)

**Key Operations:**
| Operation  | Method            | Avg Time Complexity |
|-----------|-------------------|---------------------|
| Put       | `put(key, value)` | O(1)                |
| Get       | `get(key)`        | O(1)                |
| Contains  | `containsKey()`   | O(1)                |
| Remove    | `remove(key)`     | O(1)                |
| Keys      | `keySet()`        | O(n)                |

**Hash Function Design:**
```java
private int hash(String key) {
    int hashCode = 0;
    for (char c : key.toLowerCase().toCharArray()) {
        hashCode = 31 * hashCode + c;
    }
    return Math.abs(hashCode) % capacity;
}
```

---

## 8. API Integration

### 8.1 MBTA V3 API

**Purpose:** Find the nearest T (subway) stops to any apartment address.

| Detail       | Value                                    |
|-------------|------------------------------------------|
| Base URL    | `https://api-v3.mbta.com`               |
| Auth        | Free API key (register at mbta.com)      |
| Rate Limit  | 20 req/sec (no key), higher with key     |
| Format      | JSON (JSON:API specification)            |
| Cost        | Free                                     |

**Endpoints Used:**

1. **Get all stops:**
   ```
   GET /stops?filter[route_type]=0,1
   ```
   Returns all subway (heavy rail + light rail) stops with lat/lon coordinates.

2. **Calculate nearest stop:**
   Using the Haversine formula in Java, compute the distance from the apartment's coordinates to each T stop and find the closest one.

**Response Data Extracted:**
- Stop name (e.g., "Harvard")
- Stop coordinates (latitude, longitude)
- Route info (e.g., "Red Line")
- Calculated walking distance from apartment

**Haversine Distance Formula:**
```java
public static double haversine(double lat1, double lon1, double lat2, double lon2) {
    final double R = 3958.8; // Earth radius in miles
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
             + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
             * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}
```

---

### 8.2 Overpass API (Walkability Scores)

**Purpose:** Calculate Walk Score, Transit Score, and Bike Score by counting nearby amenities using OpenStreetMap data.

| Detail       | Value                                              |
|-------------|---------------------------------------------------|
| Base URL    | `https://overpass-api.de/api/interpreter`          |
| Auth        | No API key required                                |
| Rate Limit  | Reasonable use (no hard limit)                     |
| Format      | JSON                                               |
| Cost        | Free                                               |

**How It Works:**
1. Query counts all amenities within 700 meters of the apartment (food, shops, services, transit, leisure, bike)
2. Walk Score = weighted sum of food + shops + services + leisure counts (capped at 100)
3. Transit Score = bus stop count × 8 (capped at 100)
4. Bike Score = bike amenities × 10 + walkScore/3 (capped at 100)

**Response Data Extracted:**
- Walk Score (0–100): How walkable is the area
- Transit Score (0–100): Quality of public transit nearby
- Bike Score (0–100): How bikeable is the area
- Nearby amenity counts: food, shops, services, transit stops, leisure, bike parking

---

### 8.3 OpenStreetMap Nominatim (Geocoding)

**Purpose:** Convert apartment addresses to latitude/longitude coordinates for map display and distance calculations.

| Detail       | Value                                              |
|-------------|---------------------------------------------------|
| Base URL    | `https://nominatim.openstreetmap.org/search`      |
| Auth        | No API key required                                |
| Rate Limit  | 1 request/second                                   |
| Format      | JSON                                               |
| Cost        | Free (must include User-Agent header)              |

**Request:**
```
GET /search?q=123+Elm+St+Boston+MA&format=json&limit=1
```

**Response Data Extracted:**
- `lat`: Latitude coordinate
- `lon`: Longitude coordinate
- `display_name`: Full formatted address

**Usage in App:**
1. User enters an apartment address
2. App calls Nominatim to get lat/lon
3. Lat/lon is used to:
   - Place a pin on the Map View
   - Calculate distance to T stops (MBTA API + Haversine)
   - Fetch Walk Score (requires lat/lon)

---

### 8.4 Boston Police Department Open Data (Crime/Safety)

**Purpose:** Get crime incident data near each apartment to calculate a Safety Score (0–100).

| Detail       | Value                                              |
|-------------|---------------------------------------------------|
| Base URL    | `https://data.boston.gov/api/3/action/datastore_search_sql` |
| Auth        | No API key required                                |
| Rate Limit  | No documented limit (city government open data)    |
| Format      | JSON (CKAN API)                                    |
| Cost        | Free (Public Domain - PDDL license)               |

**How It Works:**
1. For each apartment, build a SQL query with a bounding box around the apartment's lat/lon (±0.005 degrees, roughly 0.3 miles)
2. The query groups crimes by `OFFENSE_DESCRIPTION` and counts each type
3. Total crime count is used to calculate Safety Score: `100 - min(totalCrimes, 100)`
4. Crime breakdown string is stored (e.g., "Larceny: 15, Assault: 8, Vandalism: 5")

**Request (SQL via URL):**
```
GET /api/3/action/datastore_search_sql?sql=
  SELECT "OFFENSE_DESCRIPTION", COUNT(*) as cnt
  FROM "b973d8cb-eeb2-4e7e-99da-c92938efc9c0"
  WHERE CAST("Lat" AS FLOAT) BETWEEN 42.355 AND 42.365
  AND CAST("Long" AS FLOAT) BETWEEN -71.065 AND -71.055
  GROUP BY "OFFENSE_DESCRIPTION"
  ORDER BY cnt DESC LIMIT 10
```

**Response Data Extracted:**
- `OFFENSE_DESCRIPTION`: Crime type (e.g., "LARCENY", "ASSAULT")
- `cnt`: Number of incidents of that type
- Calculated `safetyScore`: 0–100 (fewer crimes = higher score)
- `crimeBreakdown`: Human-readable string of top offenses

**Where Used in App:**
- **My Apartments Table:** "Safety" column showing score 0–100
- **View Details Popup:** Full crime breakdown with offense types and counts
- **Compare Tab:** Safety Score row with green/red highlighting
- **Dashboard:** Safety badge on Top Picks rows
- **Settings:** Safety weight slider for ranking priority
- **Scoring Algorithm:** Safety score factors into Top Picks ranking

---

### 8.5 Recreation.gov RIDB API (Parks & Recreation)

**Purpose:** Find nearby federal recreation areas, parks, and trails near each apartment.

| Detail       | Value                                              |
|-------------|---------------------------------------------------|
| Base URL    | `https://ridb.recreation.gov/api/v1/recareas`     |
| Auth        | Free API key (register at ridb.recreation.gov)     |
| Rate Limit  | 50 requests/minute                                 |
| Format      | JSON                                               |
| Cost        | Free (US government open data)                     |

**Request:**
```
GET /api/v1/recareas?latitude=42.36&longitude=-71.06&radius=10&limit=5&apikey=YOUR_KEY
```

**Response Data Extracted:**
- `RecAreaName`: Name of the recreation area (e.g., "Boston Harbor Islands National Recreation Area")
- Count of nearby areas within 10 miles
- Comma-separated list of area names

**Where Used in App:**
- **My Apartments Table:** "Parks" column showing count of nearby recreation areas
- **View Details Popup:** List of recreation area names
- **Compare Tab:** "Recreation Areas" row with green/red highlighting

---

## 9. Data Model

### 9.1 Apartment Object

```java
public class Apartment {
    // Basic Info
    private String id;              // Unique identifier (UUID)
    private String name;            // User-given name (e.g., "Elm St Place")
    private String address;         // Full street address
    private double rent;            // Monthly rent
    private double sqft;            // Square footage
    private int bedrooms;
    private int bathrooms;

    // Amenities
    private boolean hasParking;
    private boolean hasLaundry;     // In-unit or building
    private boolean hasDishwasher;
    private boolean hasAC;
    private boolean isPetFriendly;
    private boolean isFurnished;

    // Lease Details
    private String availableDate;
    private int leaseLength;        // In months
    private boolean hasBrokerFee;
    private boolean utilitiesIncluded;

    // Source Tracking
    private String source;          // Zillow, Craigslist, Broker, etc.
    private String sourceURL;
    private String notes;           // Personal notes

    // API-Enriched Data
    private double latitude;
    private double longitude;
    private int walkScore;
    private int transitScore;
    private int bikeScore;
    private String nearestTStop;
    private String nearestTLine;    // Red, Green, Orange, Blue
    private double distanceToT;     // In miles

    // Nearby amenity counts from Overpass API
    private int nearbyFood;
    private int nearbyShops;
    private int nearbyServices;
    private int nearbyTransit;
    private int nearbyLeisure;
    private int nearbyBike;

    // Crime and safety data from Boston Police API
    private int safetyScore;        // 0-100, higher is safer
    private int crimeCount;         // total crimes nearby
    private String crimeBreakdown;  // "Larceny: 15, Assault: 8, ..."

    // Recreation data from Recreation.gov API
    private int recreationCount;    // number of nearby recreation areas
    private String nearbyRecreation; // comma-separated names

    // Status Tracking
    private Status status;          // NEW, SHORTLISTED, TOURED, REJECTED
    private String dateAdded;
}
```

### 9.2 Enums

```java
public enum Status {
    NEW, SHORTLISTED, TOURED, REJECTED
}

public enum ApartmentSource {
    ZILLOW, CRAIGSLIST, APARTMENTS_COM, FACEBOOK, BROKER, OTHER
}
```

### 9.3 Action Object (for Undo/Redo)

```java
public class Action {
    public enum Type { ADD, DELETE, EDIT, STATUS_CHANGE }

    private Type type;
    private Apartment apartmentBefore;  // Snapshot before action
    private Apartment apartmentAfter;   // Snapshot after action
    private long timestamp;
}
```

### 9.4 User Preferences Object

```java
public class UserPreferences {
    private double minBudget;
    private double maxBudget;
    private double weightRent;       // 0.0 to 1.0, all weights sum to 1.0
    private double weightSqft;
    private double weightNearT;
    private double weightWalkScore;
    private double weightAmenities;
    private double weightSafety;     // weight for safety score from crime data
}
```

---

## 10. Scoring Algorithm (for Top Picks)

Each apartment receives a normalized score based on user-defined weights:

```
Score = (w1 × NormalizedRent) + (w2 × NormalizedSqft) + (w3 × NormalizedNearT)
      + (w4 × NormalizedWalkScore) + (w5 × NormalizedAmenities) + (w6 × SafetyScore)
```

**Normalization:** Each factor is scaled to 0–100 range:
- **Rent:** `100 - (rent / maxRent × 100)` (lower rent = higher score)
- **Sqft:** `(sqft / maxSqft) × 100` (more space = higher score)
- **Near T:** `100 - (distanceToT / maxDistance × 100)` (closer = higher score)
- **Walk Score:** Already 0–100 from Overpass API
- **Amenities:** `(amenityCount / 6) × 100` (6 possible amenities)
- **Safety:** Already 0–100 from Boston Crime API (fewer crimes = higher score)

The top-scoring apartments are sorted and displayed on the Dashboard.

---

## 11. Project Package Structure

```
LeaseLens/
├── src/
│   └── com/leaselens/
│       ├── app/
│       │   └── Main.java                      # Application entry point (JavaFX Application)
│       │
│       ├── model/                             # Data models
│       │   ├── Apartment.java                 # Apartment data object (all fields + getters/setters)
│       │   ├── Action.java                    # Undo/redo action object (type + before/after snapshots)
│       │   ├── Status.java                    # Enum: NEW, SHORTLISTED, TOURED, REJECTED
│       │   └── UserPreferences.java           # Budget + priority weights (including safety)
│       │
│       ├── datastructures/                    # Custom data structure implementations
│       │   ├── ApartmentBag.java              # Bag (resizable array)
│       │   ├── UndoRedoStack.java             # Stack (linked-node based, LIFO)
│       │   ├── ApartmentLinkedList.java       # Doubly linked list
│       │   ├── ApartmentHashMap.java          # Hash map (separate chaining, polynomial hash)
│       │   ├── PlaceFilterQueue.java          # Queue (linked-node based, FIFO)
│       │   ├── SearchHistoryDeque.java        # Deque (doubly linked)
│       │   ├── PlacePriorityQueue.java        # Priority queue (min-heap)
│       │   └── ApartmentSorter.java           # MergeSort + comparators
│       │
│       ├── api/                               # API integration layer
│       │   ├── ApiConfig.java                 # API keys and base URLs (all 5 APIs)
│       │   ├── GeocodingService.java          # OpenStreetMap Nominatim geocoding
│       │   ├── WalkScoreService.java          # Overpass API walkability scores
│       │   ├── MBTAService.java               # MBTA V3 nearest T stop finder
│       │   ├── CrimeDataService.java          # Boston PD crime data + safety score
│       │   └── RecreationService.java         # Recreation.gov nearby parks
│       │
│       ├── service/                           # Business logic
│       │   ├── ApartmentService.java          # CRUD + data structure management + API enrichment
│       │   └── DataPersistenceService.java    # JSON save/load (Gson)
│       │
│       ├── ui/                                # JavaFX UI
│       │   ├── MainWindow.java                # Main window with 5-tab navigation
│       │   ├── DashboardTab.java              # Tab 1: Stats, Top Picks (with safety badge), Budget
│       │   ├── ApartmentsTab.java             # Tab 2: Table, search, sort, filter, details popup
│       │   ├── CompareTab.java                # Tab 3: Side-by-side comparison with green/red
│       │   ├── MapTab.java                    # Tab 4: OpenStreetMap with colored pins
│       │   ├── SettingsTab.java               # Tab 5: Budget, 6 weight sliders, save/load
│       │   └── AddApartmentDialog.java        # Popup form for adding/editing apartments
│       │
│       └── util/                              # Utility classes
│           ├── HaversineCalculator.java       # Haversine distance formula
│           └── ScoreCalculator.java           # Weighted apartment scoring (6 factors)
│
├── lib/
│   ├── gson-2.13.2.jar                        # JSON parsing library
│   └── javafx-sdk-21.0.10/                    # JavaFX SDK
│
├── data/
│   └── apartments.json                        # Persisted apartment data (auto-created)
│
├── ARCHITECTURE.md                            # This document
└── PROGRESS.md                                # Development progress tracking
```

---

## 12. Data Flow Diagrams

### 12.1 Adding an Apartment
```
User fills form → Validate input
                      │
                      ▼
               Create Apartment object
                      │
                      ├→ ApartmentBag.add(apartment)           [Master storage]
                      ├→ ApartmentHashMap.put(id, apartment)    [Search index]
                      ├→ UndoStack.push(ADD action)             [Undo history]
                      │
                      ▼
               Enrich with API data (background)
                      │
                      ├→ Step 1: GeocodingService.getCoordinates(address) → lat/lon
                      ├→ Step 2: WalkScoreService.getScores(lat, lon) → walk/transit/bike scores + amenity counts
                      ├→ Step 3: MBTAService.findNearestStop(lat, lon) → T stop name + distance
                      ├→ Step 4: CrimeDataService.getCrimeData(lat, lon) → safety score + crime breakdown
                      ├→ Step 5: RecreationService.getNearbyRecreation(lat, lon) → park count + names
                      │
                      ▼
               Refresh all UI tabs
```

### 12.2 Searching for an Apartment
```
User types in search bar
         │
         ▼
SearchService.search(query)
         │
         ├→ ApartmentHashMap.get(query)     [Exact match by name/address]
         ├→ ApartmentHashMap.keySet()        [Partial match filtering]
         │
         ▼
Return matching apartments → Update table view
```

### 12.3 Filtering by Price Range
```
User drags price slider → min: $1200, max: $1800
         │
         ▼
ApartmentService.filterByPriceRange(1200, 1800)
         │
         ▼
Iterates through ApartmentBag, returns matching apartments → Update table view
```

### 12.4 Sorting the Table
```
User clicks column header (e.g., "Rent")
         │
         ▼
ApartmentSorter.mergeSort(list, rentComparator)
         │
         ▼
Sorted list → Refresh table view
```

### 12.5 Undo/Redo
```
User clicks "Undo"
         │
         ▼
Action action = undoStack.pop()
         │
         ├→ If ADD: remove apartment from all data structures
         ├→ If DELETE: re-add apartment to all data structures
         ├→ If EDIT: restore apartment to "before" state
         ├→ If STATUS_CHANGE: revert status to previous value
         │
         ▼
redoStack.push(action)
Refresh UI
```

---

## 13. UI Design Guidelines

### Color Scheme
| Element          | Color     | Hex       |
|-----------------|-----------|-----------|
| Primary          | Navy Blue | #1a237e   |
| Secondary        | Teal      | #00796b   |
| Background       | Light Gray| #f5f5f5   |
| Card Background  | White     | #ffffff   |
| Shortlisted      | Green     | #4caf50   |
| Toured           | Amber     | #ff9800   |
| Rejected         | Red       | #f44336   |
| New              | Blue      | #2196f3   |

### Typography
- Headers: Bold, 18–24px
- Body: Regular, 14px
- Table: Regular, 13px
- Monospace for rent values and scores

### Layout Principles
- Tab-based navigation at the top
- Cards for dashboard stats
- Consistent padding and spacing
- Responsive table with alternating row colors
- Status badges with rounded corners and color coding
- Clean forms with labels above fields

---

## 14. Data Persistence

### Save Format: JSON
All apartment data is saved to `data/apartments.json` on every change and loaded on app startup.

**Sample JSON:**
```json
{
  "apartments": [
    {
      "id": "a1b2c3d4",
      "name": "Elm Street Studio",
      "address": "123 Elm St, Boston, MA 02134",
      "rent": 1400.0,
      "sqft": 650,
      "bedrooms": 1,
      "bathrooms": 1,
      "hasParking": true,
      "hasLaundry": false,
      "isPetFriendly": true,
      "source": "ZILLOW",
      "notes": "Nice natural light, noisy street",
      "latitude": 42.3601,
      "longitude": -71.0589,
      "walkScore": 89,
      "transitScore": 76,
      "nearestTStop": "Harvard",
      "nearestTLine": "Red Line",
      "distanceToT": 0.3,
      "status": "SHORTLISTED",
      "dateAdded": "2026-03-20"
    }
  ],
  "preferences": {
    "minBudget": 1000,
    "maxBudget": 2000,
    "weightRent": 0.35,
    "weightSqft": 0.20,
    "weightNearT": 0.25,
    "weightWalkScore": 0.15,
    "weightAmenities": 0.05
  }
}
```

---

## 15. API Keys Required

Before running the application, users need to obtain free API keys:

| API              | Where to Register                     | Key Required? | Cost  |
|-----------------|---------------------------------------|---------------|-------|
| MBTA V3         | https://api-v3.mbta.com               | Yes (free)    | Free  |
| Overpass (Walk Score) | No registration needed          | No            | Free  |
| Nominatim       | No registration needed                | No            | Free  |
| Boston PD Crime | No registration needed                | No            | Free  |
| Recreation.gov  | https://ridb.recreation.gov           | Yes (free)    | Free  |

Keys are stored in `src/com/leaselens/api/ApiConfig.java`.

---

## 16. Summary

| Aspect                  | Detail                                                    |
|------------------------|-----------------------------------------------------------|
| **App Name**           | LeaseLens                                                  |
| **Type**               | Desktop application (JavaFX)                               |
| **Purpose**            | Apartment search organizer and decision tool               |
| **Data Structures (7)**| Bag, Stack, Queue, Deque, Priority Queue (Heap), HashMap, MergeSort |
| **APIs (5)**           | MBTA V3, Overpass (Walk Score), Nominatim, Boston PD Crime, Recreation.gov |
| **Tabs (5)**           | Dashboard, My Apartments, Compare, Map View, Settings      |
| **Persistence**        | Local JSON file (Gson)                                     |
| **Key Features**       | Smart ranking (6 factors), undo/redo, search, sort, price filter, safety score, nearby parks, map view, side-by-side comparison |
