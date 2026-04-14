package com.leaselens.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This class is calling the Recreation.gov API to find parks and recreation areas
 * near an apartment location. It need a free API key from Recreation.gov
 *
 * pre-condition: internet connection is needed and API key is set in ApiConfig
 * post-condition: none
 */
public class RecreationService {

    /**
     * This method is finding nearby recreation areas for a location
     * It search within 10 miles and return up to 5 results
     * @param lat the latitude of the apartment
     * @param lon the longitude of the apartment
     * @return array where [0] is count as string and [1] is comma separated names, or null if fail
     *
     * pre-condition: lat and lon should be valid coordinates
     * post-condition: recreation data is returned or null if error
     */
    public String[] getNearbyRecreation(double lat, double lon) {
        try {
            // build the URL with latitude, longitude, radius, and API key
            String url = ApiConfig.RECREATION_BASE_URL
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&radius=10"
                + "&limit=5"
                + "&apikey=" + ApiConfig.RECREATION_API_KEY;

            // make the request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "LeaseLens/1.0 StudentProject")
                .header("accept", "application/json")
                .timeout(java.time.Duration.ofSeconds(10))
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            // parse the JSON response
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray recData = json.getAsJsonArray("RECDATA");

            // loop through results and build names list
            int count = 0;
            String namesList = "";

            for (int i = 0; i < recData.size(); i++) {
                JsonObject area = recData.get(i).getAsJsonObject();
                String areaName = area.get("RecAreaName").getAsString();

                if (i > 0) {
                    namesList = namesList + ", ";
                }
                namesList = namesList + areaName;
                count++;
            }

            System.out.println("Recreation areas near (" + lat + "," + lon + "): count=" + count + " -> " + namesList);

            return new String[]{String.valueOf(count), namesList};

        } catch (Exception e) {
            System.out.println("Error getting recreation data: " + e.getMessage());
        }

        return null;
    }
}
