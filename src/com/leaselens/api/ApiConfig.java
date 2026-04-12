package com.leaselens.api;

/**
 * This class is storing the API keys and URLs for all the APIs we use
 * You need to get your own free API keys and put them here
 *
 * pre-condition: none
 * post-condition: none
 */
public class ApiConfig {

    // MBTA API - get free key at https://api-v3.mbta.com
    public static final String MBTA_API_KEY = "ad02952139dc40a3a2b46ec01b1a3ee3";
    public static final String MBTA_BASE_URL = "https://api-v3.mbta.com";

    // Overpass API (OpenStreetMap) - no key needed, counts nearby amenities for walkability
    public static final String OVERPASS_BASE_URL = "https://overpass-api.de/api/interpreter";
    // backup overpass server in case main one is busy
    public static final String OVERPASS_BACKUP_URL = "https://overpass.kumi.systems/api/interpreter";

    // OpenStreetMap Nominatim - no key needed
    public static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/search";

    // Boston Crime Open Data API - no key needed, completely free
    public static final String BOSTON_CRIME_BASE_URL = "https://data.boston.gov/api/3/action/datastore_search_sql";

    // Recreation.gov RIDB API - get free key at https://ridb.recreation.gov
    public static final String RECREATION_BASE_URL = "https://ridb.recreation.gov/api/v1/recareas";
    public static final String RECREATION_API_KEY = "40180ca8-b753-4bd1-9fed-6606eaca6984";
}
