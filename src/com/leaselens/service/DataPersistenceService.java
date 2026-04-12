package com.leaselens.service;

import com.leaselens.model.Apartment;
import com.leaselens.model.UserPreferences;
import com.leaselens.datastructures.ApartmentBag;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;

/**
 * This class is saving and loading apartment data to a JSON file
 * So when user close the app and open again their data is still there
 *
 * pre-condition: none
 * post-condition: none
 */
public class DataPersistenceService {

    private String dataFilePath;
    private Gson gson;

    /**
     * This inner class is what we save to the JSON file
     * It hold all apartments and user preferences together
     */
    private class SaveData {
        Apartment[] apartments;
        UserPreferences preferences;
    }

    /**
     * This constructor is setting up the save file path
     *
     * pre-condition: none
     * post-condition: service is ready to save and load
     */
    public DataPersistenceService() {
        this.dataFilePath = "data/apartments.json";
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * This method is saving all apartments and preferences to JSON file
     * @param apartments the bag of all apartments
     * @param preferences the user preferences
     * @return true if saved, false if error
     *
     * pre-condition: apartments and preferences should not be null
     * post-condition: data is written to the JSON file
     */
    public boolean save(ApartmentBag apartments, UserPreferences preferences) {
        try {
            // make sure data folder exist
            File folder = new File("data");
            if (!folder.exists()) {
                folder.mkdir();
            }

            // put data into save object
            SaveData saveData = new SaveData();
            saveData.apartments = apartments.toArray();
            saveData.preferences = preferences;

            // write to file
            FileWriter writer = new FileWriter(dataFilePath);
            gson.toJson(saveData, writer);
            writer.close();

            System.out.println("Data saved to " + dataFilePath);
            return true;

        } catch (Exception e) {
            System.out.println("Error saving data: " + e.getMessage());
            return false;
        }
    }

    /**
     * This method is loading apartments and preferences from JSON file
     * @param service the apartment service to load data into
     * @return true if loaded, false if file not found or error
     *
     * pre-condition: service should not be null
     * post-condition: apartments is loaded into the service
     */
    public boolean load(ApartmentService service) {
        try {
            File file = new File(dataFilePath);
            if (!file.exists()) {
                System.out.println("No save file found, starting fresh");
                return false;
            }

            FileReader reader = new FileReader(file);
            SaveData saveData = gson.fromJson(reader, SaveData.class);
            reader.close();

            if (saveData != null && saveData.apartments != null) {
                for (int i = 0; i < saveData.apartments.length; i++) {
                    service.addApartment(saveData.apartments[i]);
                }
            }

            if (saveData != null && saveData.preferences != null) {
                service.setPreferences(saveData.preferences);
            }

            System.out.println("Data loaded from " + dataFilePath);
            return true;

        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            return false;
        }
    }
}
