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
 * This class is calling the Boston crime data API to find how safe an area is
 * It look at crimes near the apartment and give a safety score
 * No API key is needed because Boston open data is free for everyone
 *
 * pre-condition: internet connection is needed
 * post-condition: none
 */
public class CrimeDataService {

    // we save the last breakdown so caller can get it after calling getCrimeData
    private String lastBreakdown;

    /**
     * This method is getting crime data near a location
     * It use a bounding box around the lat/lon (about 0.3 miles)
     * Then it count crimes by type and calculate a safety score
     * @param lat the latitude of the apartment
     * @param lon the longitude of the apartment
     * @return array with [safetyScore, crimeCount] or null if it fail
     *
     * pre-condition: lat and lon should be valid Boston coordinates
     * post-condition: crime data is returned or null if error
     */
    public int[] getCrimeData(double lat, double lon) {
        try {
            // build the SQL query for Boston crime data
            // we look at crimes within about 0.3 miles (0.005 degrees)
            // the resource ID is for "Crime Incident Reports 2023 to Present"
            String sql = "SELECT \"OFFENSE_DESCRIPTION\", COUNT(*) as cnt "
                + "FROM \"b973d8cb-eeb2-4e7e-99da-c92938efc9c0\" "
                + "WHERE \"Lat\" IS NOT NULL "
                + "AND CAST(\"Lat\" AS FLOAT) > " + (lat - 0.005) + " "
                + "AND CAST(\"Lat\" AS FLOAT) < " + (lat + 0.005) + " "
                + "AND CAST(\"Long\" AS FLOAT) > " + (lon - 0.005) + " "
                + "AND CAST(\"Long\" AS FLOAT) < " + (lon + 0.005) + " "
                + "GROUP BY \"OFFENSE_DESCRIPTION\" "
                + "ORDER BY cnt DESC "
                + "LIMIT 10";

            // encode the SQL for the URL
            String encodedSql = URLEncoder.encode(sql, "UTF-8");
            String url = ApiConfig.BOSTON_CRIME_BASE_URL + "?sql=" + encodedSql;

            // make the request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "LeaseLens/1.0 StudentProject")
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            // parse the JSON response
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonObject result = json.getAsJsonObject("result");
            JsonArray records = result.getAsJsonArray("records");

            // count total crimes and build breakdown string
            int totalCrimes = 0;
            String breakdown = "";

            for (int i = 0; i < records.size(); i++) {
                JsonObject record = records.get(i).getAsJsonObject();
                String offense = record.get("OFFENSE_DESCRIPTION").getAsString();
                int count = record.get("cnt").getAsInt();

                totalCrimes = totalCrimes + count;

                // add to breakdown string
                if (i > 0) {
                    breakdown = breakdown + ", ";
                }
                breakdown = breakdown + offense + ": " + count;
            }

            // calculate safety score - fewer crimes means safer
            // more crimes = lower score, less crimes = higher score
            double rawScore = 100.0 / (1.0 + (double) totalCrimes / 50.0);
            int safetyScore = (int) Math.round(rawScore);

            // save breakdown so caller can get it
            lastBreakdown = breakdown;

            System.out.println("Crime data for (" + lat + "," + lon + "): total=" + totalCrimes + " safety=" + safetyScore);

            return new int[]{safetyScore, totalCrimes};

        } catch (Exception e) {
            System.out.println("Error getting crime data: " + e.getMessage());
        }

        return null;
    }

    /**
     * This method is giving back the last crime breakdown string
     * Call this after calling getCrimeData to get the breakdown
     * @return string like "Larceny: 15, Assault: 8, ..."
     *
     * pre-condition: getCrimeData should be called first
     * post-condition: breakdown string is returned
     */
    public String getLastBreakdown() {
        return lastBreakdown;
    }
}
