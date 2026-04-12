# LeaseLens — Session Progress Notes

## Project Overview
**LeaseLens** is a JavaFX desktop apartment tracking app built as a Data Structures final project.
Platform: Java 17 + JavaFX 21.0.10 | IDE: Eclipse | OS: macOS

---

## What Is Built and Working

### Application Structure
| File | Role | Status |
|------|------|--------|
| `app/Main.java` | Entry point, launches JavaFX | Working |
| `ui/MainWindow.java` | 5-tab window, tab switching, auto-refresh | Working |
| `ui/DashboardTab.java` | Stats overview (total, shortlisted, toured, rejected, avg rent) | Working |
| `ui/ApartmentsTab.java` | Full apartment table with search, sort, filter, undo/redo | Working |
| `ui/AddApartmentDialog.java` | Add/Edit apartment popup form | Working |
| `ui/CompareTab.java` | Side-by-side apartment comparison | Working |
| `ui/MapTab.java` | Real OpenStreetMap map with apartment pins (JavaFX Canvas) | Working |
| `ui/SettingsTab.java` | User preferences, save/load data | Working |

### Data Structures Used (all custom implementations)
| Class | Type | Used For |
|-------|------|----------|
| `ApartmentBag` | Bag (resizable array) | Master storage of all apartments |
| `UndoRedoStack` | Stack (LIFO) | Undo/redo every user action |
| `ApartmentLinkedList` | Doubly linked list | Filtered views, activity feed |
| `ApartmentSorter` | MergeSort | Sorting table by any column |
| `ApartmentHashMap` | Hash map (separate chaining) | O(1) search by name/address |
| `PlaceFilterQueue` | Queue (FIFO) | Filter nearby places |
| `SearchHistoryDeque` | Deque | Search history for undo |
| `PlacePriorityQueue` | Priority Queue (Heap) | Sort nearby places by distance |

### API Integrations
| API | Purpose | Key Required |
|-----|---------|-------------|
| OpenStreetMap Nominatim | Address → lat/lon geocoding | No |
| Overpass API (OpenStreetMap) | Walk/Transit/Bike scores + amenity counts | No |
| MBTA V3 | Nearest T stop + distance | Yes (free, in `ApiConfig.java`) |

### Model
| Class | Fields |
|-------|--------|
| `Apartment` | name, address, rent, sqft, beds, baths, amenities, lease info, source, notes, lat/lon, walkScore, transitScore, bikeScore, nearestTStop, distanceToT, nearbyFood/Shops/Services/Transit/Leisure/Bike counts, status, dateAdded |
| `Status` (enum) | NEW, SHORTLISTED, TOURED, REJECTED |
| `Action` | type (ADD/DELETE/EDIT/STATUS_CHANGE), before snapshot, after snapshot |
| `UserPreferences` | budget min/max, priority weights |

---

## What Was Fixed and Changed in This Session

### Bug Fix 1 — Eclipse Classpath Error
**Problem:** `.classpath` referenced `javafx-sdk-27` which did not exist on the machine.
**Fix:** Rewrote `.classpath` to use only the installed `javafx-sdk-21.0.10` with relative paths.

### Bug Fix 2 — JavaFX Window Not Appearing on macOS
**Problem:** Eclipse Run Configuration had "Use the -XstartOnFirstThread argument when launching with SWT" checked. This conflicts with JavaFX on macOS and silently prevents the window from showing.
**Fix:** Uncheck that option in Run Configurations → Arguments tab.

### Bug Fix 3 — OpenStreetMap Tiles Returning 403
**Problem:** JavaFX's built-in `Image(url)` sends a generic Java user-agent that OSM blocks.
**Fix:** Added one line to `Main.java` before `launch()`:
```java
System.setProperty("http.agent", "LeaseLens/1.0 StudentProject");
```

### Feature Change — Map View: WebView/HTML → Pure JavaFX Canvas
**Problem:** Original `MapTab.java` used a `WebView` with an embedded HTML page and Leaflet.js (JavaScript). This was not allowed — the project must use only Java and JavaFX.
**Rewrite:** `MapTab.java` completely rebuilt using two stacked `Canvas` nodes:
- `tileCanvas` (bottom) — draws OpenStreetMap tile images fetched as JavaFX `Image` objects
- `markerCanvas` (top) — draws colored circles for each apartment with name labels

Tile math implemented in pure Java:
- `lonToTileDouble(lon)` — convert longitude to OSM tile X coordinate
- `latToTileDouble(lat)` — convert latitude to OSM tile Y coordinate
- `lonToPixelX(lon, width)` — convert longitude to canvas pixel X
- `latToPixelY(lat, height)` — convert latitude to canvas pixel Y

### Feature Added — Map Zoom
- Scroll wheel on the map to zoom in/out (zoom range: 5–18)
- On-screen `+` / `-` overlay buttons in the top-left corner of the map
- Zoom re-fetches and redraws all tiles at the new zoom level

### Feature Added — "View on Map" Cross-Tab Navigation
- "View on Map" button added to the action row in `ApartmentsTab`
- Clicking it: centers the map on the selected apartment, zooms to level 15, and switches to the Map View tab
- Connected via `apartmentsTab.setMapNavigator(mapTab, switchCallback)` called in `MainWindow`

### API Change — Walk Score → Overpass API
**Problem:** Walk Score API was broken/unavailable.
**Replacement:** `WalkScoreService.java` completely rewritten to use the free Overpass API (OpenStreetMap) — no API key needed.

How the new scoring works:
- Queries all amenities within 700 meters of the apartment
- Counts: food (restaurants, cafes, bars), shops (supermarkets, convenience), services (pharmacy, bank, hospital, school), leisure (parks, gyms), transit (bus stops, subway entrances), bike (bike parking/rental)
- Walk Score = food×4(max 35) + shops×4(max 30) + services×3(max 20) + leisure×3(max 15), capped at 100
- Transit Score = transitCount × 8, capped at 100
- Bike Score = bikeCount×10 + walkScore/3, capped at 100

### Feature Added — Nearby Amenity Counts in View Details
The breakdown counts (Food, Shops, Services, Transit Stops, Leisure, Bike) that come back from Overpass API are now:
- Stored on the `Apartment` object as: `nearbyFood`, `nearbyShops`, `nearbyServices`, `nearbyTransit`, `nearbyLeisure`, `nearbyBike`
- Shown in the "View Details" popup under Scores section:
  ```
  Nearby (within 700m): Food: 5   Shops: 1   Services: 4   Transit Stops: 11   Leisure: 2   Bike: 0
  ```

---

## Apartment Tab Features Summary

| Feature | How It Works |
|---------|-------------|
| Search bar | HashMap O(1) lookup by name/address as you type |
| Status filter dropdown | Filters by NEW/SHORTLISTED/TOURED/REJECTED |
| Sort dropdown | MergeSort by rent, sqft, bedrooms, walk score, distance to T |
| Price range sliders | Filters apartments by iterating through the Bag |
| + Add Apartment | Opens dialog, geocodes address, fetches all API data |
| Edit | Re-opens dialog with existing values |
| Delete | Removes from all data structures |
| Shortlist / Toured / Reject buttons | One-click status change |
| Undo / Redo | Reverts/reapplies last action using stack |
| View Details | Popup showing all fields including scores and nearby amenity counts |
| View on Map | Switches to Map tab, centers and zooms on that apartment's location |

---

## Current API Keys

Stored in `src/com/leaselens/api/ApiConfig.java`:
- `MBTA_API_KEY` — free key from api-v3.mbta.com (already in file)
- `MBTA_BASE_URL` — `https://api-v3.mbta.com`
- `OVERPASS_BASE_URL` — `https://overpass-api.de/api/interpreter` (no key needed)
- `NOMINATIM_BASE_URL` — `https://nominatim.openstreetmap.org/search` (no key needed)

---

## Pending Work (To Do Tomorrow)

- [ ] Any remaining UI polish or flow improvements
- [ ] Any additional features still planned

---

## How to Run the App (for Teammates)

### What You Need Before Starting
- **Eclipse IDE** installed on your computer
- **Java 17 or higher** installed
- The full **LeaseLens project folder** (shared via GitHub or zip) — make sure the `lib/` folder is included, it contains JavaFX and Gson

---

### Step 1 — Open the Project in Eclipse

1. Open Eclipse
2. Go to **File → Open Projects from File System...**
3. Click **Directory...** and select the `LeaseLens` folder
4. Click **Finish**

The project will appear in the **Package Explorer** on the left side.

---

### Step 2 — Fix the Run Configuration (macOS only — do this once)

> This step is only needed on Mac. Skip if you are on Windows.

If you run the app and nothing appears on screen, this is why.

1. Click **Run → Run Configurations...**
2. On the left, find **Java Application → Main** (or create one if it does not exist)
3. Click the **Arguments** tab at the top
4. Scroll down to find **"Use the -XstartOnFirstThread argument when launching with SWT"**
5. Make sure that checkbox is **UNCHECKED** (unticked)
6. Click **Apply**, then **Close**

---

### Step 3 — Run the App

1. In the Package Explorer, expand `src → com.leaselens.app`
2. Right-click on **Main.java**
3. Click **Run As → Java Application**
4. The LeaseLens window should open

---

### If You See a "javafx" Error or Red X on the Project

The project uses JavaFX which is already included in the `lib/` folder. If Eclipse still shows errors:

1. Right-click the project in Package Explorer
2. Click **Build Path → Configure Build Path...**
3. Go to the **Libraries** tab
4. Make sure you can see entries for `javafx.controls.jar`, `javafx.graphics.jar`, etc. under `lib/javafx-sdk-21.0.10/`
5. If they are missing, click **Add JARs...**, navigate to `lib/javafx-sdk-21.0.10/lib/`, and add all the `.jar` files there
6. Also add `lib/gson-2.13.2.jar`
7. Click **Apply and Close**
8. Right-click project → **Refresh**, then try running again

---

### If the Map Does Not Load / Shows a Blue Screen

The map loads tiles from the internet (OpenStreetMap). Make sure:
- You have an active internet connection
- Wait a few seconds after the app opens — tiles load asynchronously
- Click **Refresh Map** button in the Map View tab to reload pins

---

### Normal App Behavior

- When you **add an apartment**, the app automatically fetches coordinates, walk/transit/bike scores, and nearest T stop — this takes 2–5 seconds per apartment (internet required)
- All your data is **saved automatically** to `data/apartments.json` in the project folder
- Next time you open the app, your apartments are loaded back automatically

---

## Project File Tree (Current)
```
LeaseLens/
├── src/com/leaselens/
│   ├── app/
│   │   └── Main.java
│   ├── model/
│   │   ├── Apartment.java
│   │   ├── Action.java
│   │   ├── Status.java
│   │   └── UserPreferences.java
│   ├── datastructures/
│   │   ├── ApartmentBag.java
│   │   ├── ApartmentHashMap.java
│   │   ├── ApartmentLinkedList.java
│   │   ├── ApartmentSorter.java
│   │   ├── PlaceFilterQueue.java
│   │   ├── SearchHistoryDeque.java
│   │   ├── PlacePriorityQueue.java
│   │   └── UndoRedoStack.java
│   ├── api/
│   │   ├── ApiConfig.java
│   │   ├── GeocodingService.java
│   │   ├── MBTAService.java
│   │   └── WalkScoreService.java          (uses Overpass, not Walk Score)
│   ├── service/
│   │   ├── ApartmentService.java
│   │   └── DataPersistenceService.java
│   ├── ui/
│   │   ├── MainWindow.java
│   │   ├── DashboardTab.java
│   │   ├── ApartmentsTab.java
│   │   ├── AddApartmentDialog.java
│   │   ├── CompareTab.java
│   │   ├── MapTab.java                    (pure JavaFX Canvas, no HTML)
│   │   └── SettingsTab.java
│   └── util/
│       ├── HaversineCalculator.java
│       └── ScoreCalculator.java
├── lib/
│   ├── javafx-sdk-21.0.10/
│   └── gson-2.13.2.jar
├── .classpath
├── ARCHITECTURE.md                        (original design document)
└── PROGRESS.md                            (this file)
```
