package com.leaselens.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leaselens.calculators.HaversineCalculator;

/**
 * This class is calling the MBTA API to find nearest T stops
 * It download all the subway stops and then calculate which one is closest
 *
 * pre-condition: internet connection is needed
 * post-condition: none
 */
public class MBTAService {

    // we save the stops so we dont have to download them every time
    private String[] stopNames;
    private String[] stopLines;
    private double[] stopLats;
    private double[] stopLons;
    private int stopCount;
    private boolean stopsLoaded;

    /**
     * This constructor is making a new MBTA service
     *
     * pre-condition: none
     * post-condition: service is created, stops not loaded yet
     */
    public MBTAService() {
        stopNames = new String[200];
        stopLines = new String[200];
        stopLats = new double[200];
        stopLons = new double[200];
        stopCount = 0;
        stopsLoaded = false;
    }

    /**
     * This method is downloading all subway stops from MBTA API
     * We only do this once and save them for later
     *
     * pre-condition: internet connection and API key needed
     * post-condition: all stops is saved in the arrays
     */
    public void loadStops() {
        if (stopsLoaded) {
            return; // already loaded
        }

        try {
            // route_type 0 = light rail, 1 = heavy rail (subway)
            String url = ApiConfig.MBTA_BASE_URL
                + "/stops?filter[route_type]=0,1"
                + "&api_key=" + ApiConfig.MBTA_API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray data = json.getAsJsonArray("data");

            for (int i = 0; i < data.size(); i++) {
                JsonObject stop = data.get(i).getAsJsonObject();
                JsonObject attributes = stop.getAsJsonObject("attributes");

                String name = attributes.get("name").getAsString();
                double lat = attributes.get("latitude").getAsDouble();
                double lon = attributes.get("longitude").getAsDouble();

                stopNames[stopCount] = name;
                stopLats[stopCount] = lat;
                stopLons[stopCount] = lon;
                stopLines[stopCount] = ""; // we fill this separately if needed
                stopCount++;

                if (stopCount >= 200) break; // safety limit
            }

            stopsLoaded = true;
            System.out.println("Loaded " + stopCount + " MBTA stops");

        } catch (Exception e) {
            System.out.println("Error loading MBTA stops: " + e.getMessage());
        }
    }

    /**
     * This method is finding the nearest stop (bus or subway) to a location
     * First it try the MBTA API location filter which find the closest stop
     * If that dont work it fall back to checking the saved subway stops
     * @param lat latitude of the apartment
     * @param lon longitude of the apartment
     * @return array with [stopName, distance in miles] or null if fail
     *
     * pre-condition: lat/lon should be valid
     * post-condition: nearest stop info is returned
     */
    public String[] findNearestStop(double lat, double lon) {
        // first try: ask the MBTA API to find stops near this location
        // this way we get bus stops too, not just subway
        try {
            String url = ApiConfig.MBTA_BASE_URL
                + "/stops?filter[latitude]=" + lat
                + "&filter[longitude]=" + lon
                + "&filter[radius]=0.02"
                + "&sort=distance"
                + "&page[limit]=1"
                + "&api_key=" + ApiConfig.MBTA_API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray data = json.getAsJsonArray("data");

            // if we got a result, use it
            if (data.size() > 0) {
                JsonObject stop = data.get(0).getAsJsonObject();
                JsonObject attributes = stop.getAsJsonObject("attributes");

                String name = attributes.get("name").getAsString();
                double stopLat = attributes.get("latitude").getAsDouble();
                double stopLon = attributes.get("longitude").getAsDouble();

                // figure out how far it is
                double distance = HaversineCalculator.calculateDistance(
                    lat, lon, stopLat, stopLon);
                distance = Math.round(distance * 100.0) / 100.0;

                System.out.println("Found nearest stop: " + name + " (" + distance + " mi)");
                return new String[]{name, String.valueOf(distance)};
            }
        } catch (Exception e) {
            System.out.println("Location search didnt work: " + e.getMessage());
        }

        // second try: if location search fail, use the saved subway stops
        if (!stopsLoaded) {
            loadStops();
        }

        if (stopCount == 0) {
            return null;
        }

        String nearestName = "";
        double nearestDistance = 999999;

        for (int i = 0; i < stopCount; i++) {
            double distance = HaversineCalculator.calculateDistance(
                lat, lon, stopLats[i], stopLons[i]);

            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestName = stopNames[i];
            }
        }

        // round distance to 2 decimal places
        nearestDistance = Math.round(nearestDistance * 100.0) / 100.0;

        return new String[]{nearestName, String.valueOf(nearestDistance)};
    }

    /**
     * This method is checking if stops have been loaded
     * @return true if loaded, false if not
     *
     * pre-condition: none
     * post-condition: true or false is returned
     */
    public boolean isStopsLoaded() {
        return stopsLoaded;
    }

    /**
     * This method is telling how many stops we have
     * @return number of stops loaded
     *
     * pre-condition: none
     * post-condition: count is returned
     */
    public int getStopCount() {
        return stopCount;
    }

    /**
     * This method find nearby subway stations (Orange, Red, Blue, Green lines)
     * It ask the MBTA API for train stops near the apartment
     * @param lat the latitude
     * @param lon the longitude
     * @return array of strings like "Park Street (0.3 mi)" or empty if none
     *
     * pre-condition: lat and lon should be valid numbers
     * post-condition: array of station names with distance is returned
     */
    public String[] findNearbySubway(double lat, double lon) {
        // we will store up to 5 results
        String[] temp = new String[5];
        int count = 0;

        try {
            // route_type 0 = light rail (Green line), 1 = heavy rail (Red, Orange, Blue)
            // ask for more so we still get 5 after removing duplicates
            String url = ApiConfig.MBTA_BASE_URL
                + "/stops?filter[latitude]=" + lat
                + "&filter[longitude]=" + lon
                + "&filter[radius]=0.03"
                + "&filter[route_type]=0,1"
                + "&sort=distance"
                + "&page[limit]=10"
                + "&api_key=" + ApiConfig.MBTA_API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray data = json.getAsJsonArray("data");

            // go through each stop the API gave us
            // we skip duplicates because MBTA sometimes give same stop name twice
            for (int i = 0; i < data.size(); i++) {
                if (count >= 5) {
                    break;
                }

                JsonObject stop = data.get(i).getAsJsonObject();
                JsonObject attributes = stop.getAsJsonObject("attributes");

                String name = attributes.get("name").getAsString();
                double sLat = attributes.get("latitude").getAsDouble();
                double sLon = attributes.get("longitude").getAsDouble();

                // check if we already have this stop name
                boolean alreadyHave = false;
                for (int j = 0; j < count; j++) {
                    if (temp[j].startsWith(name + " (")) {
                        alreadyHave = true;
                        break;
                    }
                }
                if (alreadyHave) {
                    continue; // skip this duplicate
                }

                // figure out how far away it is
                double dist = HaversineCalculator.calculateDistance(lat, lon, sLat, sLon);
                dist = Math.round(dist * 100.0) / 100.0;

                temp[count] = name + " (" + dist + " mi)";
                count = count + 1;
            }

        } catch (Exception e) {
            System.out.println("Error finding subway: " + e.getMessage());
        }

        // copy into right size array
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = temp[i];
        }
        return result;
    }

    /**
     * This method find nearby bus stops
     * It ask the MBTA API for bus stops near the apartment
     * @param lat the latitude
     * @param lon the longitude
     * @return array of strings like "Hyde Park Ave @ Walk Hill (0.05 mi)" or empty
     *
     * pre-condition: lat and lon should be valid numbers
     * post-condition: array of bus stop names with distance is returned
     */
    public String[] findNearbyBus(double lat, double lon) {
        // we will store up to 5 results
        String[] temp = new String[5];
        int count = 0;

        try {
            // route_type 3 = bus
            // ask for more so we still get 5 after removing duplicates
            String url = ApiConfig.MBTA_BASE_URL
                + "/stops?filter[latitude]=" + lat
                + "&filter[longitude]=" + lon
                + "&filter[radius]=0.01"
                + "&filter[route_type]=3"
                + "&sort=distance"
                + "&page[limit]=10"
                + "&api_key=" + ApiConfig.MBTA_API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray data = json.getAsJsonArray("data");

            // go through each stop the API gave us
            // we skip duplicates because MBTA sometimes give same stop name twice
            for (int i = 0; i < data.size(); i++) {
                if (count >= 5) {
                    break;
                }

                JsonObject stop = data.get(i).getAsJsonObject();
                JsonObject attributes = stop.getAsJsonObject("attributes");

                String name = attributes.get("name").getAsString();
                double sLat = attributes.get("latitude").getAsDouble();
                double sLon = attributes.get("longitude").getAsDouble();

                // check if we already have this stop name
                boolean alreadyHave = false;
                for (int j = 0; j < count; j++) {
                    if (temp[j].startsWith(name + " (")) {
                        alreadyHave = true;
                        break;
                    }
                }
                if (alreadyHave) {
                    continue; // skip this duplicate
                }

                // figure out how far away it is
                double dist = HaversineCalculator.calculateDistance(lat, lon, sLat, sLon);
                dist = Math.round(dist * 100.0) / 100.0;

                temp[count] = name + " (" + dist + " mi)";
                count = count + 1;
            }

        } catch (Exception e) {
            System.out.println("Error finding bus stops: " + e.getMessage());
        }

        // copy into right size array
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            result[i] = temp[i];
        }
        return result;
    }
}
