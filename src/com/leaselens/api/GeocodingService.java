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
 * This class is calling the OpenStreetMap API to turn address into coordinates
 * We need latitude and longitude for the map and for finding nearest T stop
 *
 * pre-condition: internet connection is needed
 * post-condition: none
 */
public class GeocodingService {

    /**
     * This method is getting the latitude and longitude for an address
     * It try structured query first (more accurate) then free text as backup
     * @param address the street address to look up
     * @return array with [latitude, longitude] or null if it fail
     *
     * pre-condition: address should not be empty
     * post-condition: coordinates is returned or null if error
     */
    public double[] getCoordinates(String address) {
        // first try: use structured query by splitting the address into parts
        // this is more accurate because nominatim wont confuse street names
        // with neighborhood names (like "Hyde Park Ave" vs "Hyde Park" neighborhood)
        System.out.println("Geocoding try 1 (structured): " + address);
        double[] result = callNominatimStructured(address);
        if (result != null) {
            return result;
        }

        // second try: use the full address as free text
        System.out.println("Geocoding try 2 (free text): " + address);
        result = callNominatim(address);
        if (result != null) {
            return result;
        }

        // third try: simplify the address and try free text again
        String simpler = makeSimpler(address);
        if (!simpler.equals(address)) {
            System.out.println("Geocoding try 3 (simplified): " + simpler);
            result = callNominatim(simpler);
            if (result != null) {
                return result;
            }
        }

        System.out.println("Geocoding failed for: " + address);
        return null;
    }

    /**
     * This method is calling Nominatim with structured fields
     * Instead of one big search string, we tell it what is the street,
     * what is the city, what is the state, and what is the zip code
     * This way it dont get confused by street names like "Hyde Park Ave"
     * @param address the full address string
     * @return array with [lat, lon] or null if not found
     *
     * pre-condition: address should have at least street and city
     * post-condition: coordinates or null
     */
    private double[] callNominatimStructured(String address) {
        try {
            // split the address by comma
            String[] parts = address.split(", ");

            String street = "";
            String city = "";
            String state = "";
            String zip = "";

            if (parts.length == 4) {
                // format: "street, neighborhood, city, state zip"
                street = parts[0].trim();
                // skip parts[1] which is the neighborhood
                city = parts[2].trim();
                // parts[3] is like "MA 02130"
                String lastPart = parts[3].trim();
                String[] stateZip = lastPart.split(" ");
                if (stateZip.length >= 1) {
                    state = stateZip[0].trim();
                }
                if (stateZip.length >= 2) {
                    zip = stateZip[1].trim();
                }
            } else if (parts.length == 3) {
                // format: "street, city, state zip"
                street = parts[0].trim();
                city = parts[1].trim();
                // parts[2] is like "MA 02130"
                String lastPart = parts[2].trim();
                String[] stateZip = lastPart.split(" ");
                if (stateZip.length >= 1) {
                    state = stateZip[0].trim();
                }
                if (stateZip.length >= 2) {
                    zip = stateZip[1].trim();
                }
            } else {
                // dont know this format, skip structured query
                return null;
            }

            // build the structured query URL
            // this tells nominatim exactly which part is the street vs city
            String url = ApiConfig.NOMINATIM_BASE_URL + "?"
                + "street=" + URLEncoder.encode(street, "UTF-8")
                + "&city=" + URLEncoder.encode(city, "UTF-8");

            // add state if we have it
            if (!state.isEmpty()) {
                url = url + "&state=" + URLEncoder.encode(state, "UTF-8");
            }

            // add zip code if we have it
            if (!zip.isEmpty()) {
                url = url + "&postalcode=" + URLEncoder.encode(zip, "UTF-8");
            }

            url = url + "&country=US&format=json&limit=1";

            System.out.println("Structured URL: street=" + street + " city=" + city
                + " state=" + state + " zip=" + zip);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "LeaseLens-StudentProject")
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonArray results = JsonParser.parseString(response.body()).getAsJsonArray();

            if (results.size() > 0) {
                JsonObject first = results.get(0).getAsJsonObject();
                double lat = Double.parseDouble(first.get("lat").getAsString());
                double lon = Double.parseDouble(first.get("lon").getAsString());
                System.out.println("Structured query found: " + lat + ", " + lon);
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            System.out.println("Structured geocoding error: " + e.getMessage());
        }
        return null;
    }

    /**
     * This method is calling the Nominatim API with one address as free text
     * @param address the address to look up
     * @return array with [lat, lon] or null if not found
     *
     * pre-condition: address should not be empty
     * post-condition: coordinates or null
     */
    private double[] callNominatim(String address) {
        try {
            String encoded = URLEncoder.encode(address, "UTF-8");
            String url = ApiConfig.NOMINATIM_BASE_URL + "?q=" + encoded + "&format=json&limit=1";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "LeaseLens-StudentProject")
                .build();

            HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

            JsonArray results = JsonParser.parseString(response.body()).getAsJsonArray();

            if (results.size() > 0) {
                JsonObject first = results.get(0).getAsJsonObject();
                double lat = Double.parseDouble(first.get("lat").getAsString());
                double lon = Double.parseDouble(first.get("lon").getAsString());
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            System.out.println("Error getting coordinates: " + e.getMessage());
        }
        return null;
    }

    /**
     * This method make a simpler address by keeping just the street
     * and the last part which is usually the state and zip
     * Example: "164 Hyde Park Ave, Jamaica Plain, Boston, MA 02130"
     * becomes: "164 Hyde Park Ave, Boston, MA 02130"
     * @param address the full address
     * @return simpler address string
     *
     * pre-condition: address should not be empty
     * post-condition: simpler address is returned
     */
    private String makeSimpler(String address) {
        String[] parts = address.split(", ");

        // if 4 parts like [street, neighborhood, city, state]
        // skip the neighborhood and keep the rest
        if (parts.length == 4) {
            return parts[0] + ", " + parts[2] + ", " + parts[3];
        }

        // if 3 parts just return as is
        return address;
    }
}
