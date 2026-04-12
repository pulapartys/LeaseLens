package com.leaselens.api;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class is using the Overpass API (OpenStreetMap) to calculate walkability scores
 * It count nearby amenities like restaurants, shops, transit stops, and bike parking
 * Then it calculate walk score, transit score, and bike score from those counts
 * No API key is needed - Overpass API is completely free
 * If the main server is busy it try a backup server
 *
 * pre-condition: internet connection is needed
 * post-condition: none
 */
public class WalkScoreService {

    /**
     * This method is getting walkability, transit, and bike scores for a location
     * It use Overpass API to count nearby amenities within 700 meters
     * If the main server is busy it try a backup server
     * @param address the street address (not used but kept for compatibility)
     * @param lat the latitude of the location
     * @param lon the longitude of the location
     * @return array with [walkScore, transitScore, bikeScore] from 0 to 100, or null if fail
     *
     * pre-condition: lat and lon should be valid coordinates
     * post-condition: scores is returned or null if error
     */
    public int[] getScores(String address, double lat, double lon) {
        // build the Overpass query to find all amenities within 700 meters
        // we ask for amenity nodes, leisure nodes, shops, bus stops, and bike parking
        String query = "[out:json][timeout:15];"
            + "(node[\"amenity\"](around:700," + lat + "," + lon + ");"
            + "node[\"leisure\"](around:700," + lat + "," + lon + ");"
            + "node[\"shop\"](around:700," + lat + "," + lon + ");"
            + "node[\"highway\"=\"bus_stop\"](around:700," + lat + "," + lon + "););"
            + "out tags;";

        // try the main overpass server first
        System.out.println("Trying main Overpass server...");
        int[] result = callOverpass(ApiConfig.OVERPASS_BASE_URL, query, lat, lon);
        if (result != null) {
            return result;
        }

        // if main server failed (busy or error), try the backup server
        System.out.println("Main server didnt work, trying backup Overpass server...");
        result = callOverpass(ApiConfig.OVERPASS_BACKUP_URL, query, lat, lon);
        if (result != null) {
            return result;
        }

        System.out.println("Both Overpass servers failed for (" + lat + "," + lon + ")");
        return null;
    }

    /**
     * This method is calling one Overpass server with the query
     * It count all the amenities and calculate the scores
     * @param serverUrl the overpass server URL to use
     * @param query the overpass query
     * @param lat the latitude (for logging)
     * @param lon the longitude (for logging)
     * @return array with scores or null if it fail
     *
     * pre-condition: serverUrl and query should not be empty
     * post-condition: scores array or null
     */
    private int[] callOverpass(String serverUrl, String query, double lat, double lon) {
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String url = serverUrl + "?data=" + encodedQuery;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "LeaseLens/1.0 StudentProject INFO6205")
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            // check if the server gave us real JSON or an error page
            String body = response.body().trim();
            if (!body.startsWith("{")) {
                // the server gave us HTML or something else, not JSON
                System.out.println("Overpass server returned non-JSON: " + serverUrl);
                return null;
            }

            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            JsonArray elements = json.getAsJsonArray("elements");

            // count each type of amenity
            int foodCount = 0;       // restaurants, cafes, bars
            int shopCount = 0;       // supermarkets, convenience stores, shops
            int serviceCount = 0;    // pharmacies, banks, hospitals, schools
            int leisureCount = 0;    // parks, playgrounds
            int transitCount = 0;    // bus stops, train stations
            int bikeCount = 0;       // bike parking, bike rental

            for (int i = 0; i < elements.size(); i++) {
                JsonObject element = elements.get(i).getAsJsonObject();

                // skip elements without tags
                if (!element.has("tags")) continue;

                JsonObject tags = element.getAsJsonObject("tags");

                String amenity = "";
                String leisure = "";
                String highway = "";

                if (tags.has("amenity")) amenity = tags.get("amenity").getAsString();
                if (tags.has("leisure")) leisure = tags.get("leisure").getAsString();
                if (tags.has("highway")) highway = tags.get("highway").getAsString();

                // check if it is a food place
                if (amenity.equals("restaurant") || amenity.equals("cafe")
                    || amenity.equals("bar") || amenity.equals("fast_food")
                    || amenity.equals("bakery") || amenity.equals("pub")
                    || amenity.equals("biergarten") || amenity.equals("food_court")) {
                    foodCount = foodCount + 1;
                }

                // check if it is a shop or grocery place
                if (tags.has("shop") || amenity.equals("supermarket")
                    || amenity.equals("convenience") || amenity.equals("marketplace")) {
                    shopCount = shopCount + 1;
                }

                // check if it is a service (health, finance, education)
                if (amenity.equals("pharmacy") || amenity.equals("bank")
                    || amenity.equals("hospital") || amenity.equals("clinic")
                    || amenity.equals("school") || amenity.equals("library")
                    || amenity.equals("post_office") || amenity.equals("dentist")
                    || amenity.equals("doctors")) {
                    serviceCount = serviceCount + 1;
                }

                // check if it is a leisure or park place
                if (leisure.equals("park") || leisure.equals("playground")
                    || leisure.equals("fitness_centre") || leisure.equals("sports_centre")
                    || amenity.equals("cinema") || amenity.equals("theatre")) {
                    leisureCount = leisureCount + 1;
                }

                // check if it is a transit stop or station
                if (highway.equals("bus_stop") || amenity.equals("bus_station")
                    || amenity.equals("ferry_terminal") || amenity.equals("taxi")
                    || amenity.equals("subway_entrance")) {
                    transitCount = transitCount + 1;
                }

                // check if it is bike-related
                if (amenity.equals("bicycle_parking") || amenity.equals("bicycle_rental")) {
                    bikeCount = bikeCount + 1;
                }
            }

            // calculate walk score (0 to 100)
            // more food, shops, services, and parks = higher walk score
            int walkScore = Math.min(foodCount * 4, 35)
                          + Math.min(shopCount * 4, 30)
                          + Math.min(serviceCount * 3, 20)
                          + Math.min(leisureCount * 3, 15);
            walkScore = Math.min(walkScore, 100);

            // calculate transit score (0 to 100)
            // each bus stop adds 8 points, stations add more from MBTA
            int transitScore = Math.min(transitCount * 8, 100);

            // calculate bike score (0 to 100)
            // based on bike amenities and how walkable the area is
            int bikeScore = Math.min(bikeCount * 10 + walkScore / 3, 100);

            System.out.println("Overpass scores for (" + lat + "," + lon + "): "
                + "walk=" + walkScore + " transit=" + transitScore + " bike=" + bikeScore
                + " (food=" + foodCount + " shops=" + shopCount
                + " services=" + serviceCount + " transit=" + transitCount + ")");

            return new int[]{walkScore, transitScore, bikeScore,
                             foodCount, shopCount, serviceCount, transitCount, leisureCount, bikeCount};

        } catch (Exception e) {
            System.out.println("Error from Overpass server " + serverUrl + ": " + e.getMessage());
        }

        return null;
    }
}
