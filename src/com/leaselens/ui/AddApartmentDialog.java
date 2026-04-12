package com.leaselens.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.leaselens.model.Apartment;
import com.leaselens.model.Status;
import com.leaselens.service.ApartmentService;

/**
 * This class is the popup dialog for adding or editing an apartment
 * It have all the form fields like name, address, rent, amenities etc
 * When user click save it add the apartment and try to get API data
 *
 * pre-condition: service should not be null
 * post-condition: dialog is ready to show
 */
public class AddApartmentDialog {

    private ApartmentService service;
    private Stage dialogStage;
    private boolean saved;

    // form fields - basic info
    private TextField addressField;
    private TextField cityField;
    private TextField neighborhoodField;
    private ComboBox<String> stateCombo;
    private TextField zipField;
    private TextField rentField;
    private TextField sqftField;
    private ComboBox<String> bedroomsCombo;
    private ComboBox<String> bathroomsCombo;

    // amenities checkboxes
    private CheckBox parkingCheck;
    private CheckBox laundryCheck;
    private CheckBox dishwasherCheck;
    private CheckBox acCheck;
    private CheckBox petCheck;
    private CheckBox furnishedCheck;

    // lease details
    private javafx.scene.control.DatePicker availableDatePicker;
    private ComboBox<String> leaseCombo;
    private CheckBox brokerFeeCheck;
    private CheckBox utilitiesCheck;

    // source and notes
    private ComboBox<String> sourceCombo;
    private TextField sourceURLField;
    private TextArea notesArea;

    // status
    private ComboBox<String> statusCombo;
    private Label statusDot;

    // if editing, this is the apartment we editing
    private Apartment editingApartment;

    // error label that show on the form
    private Label errorLabel;


    /**
     * This constructor is making a new dialog for adding apartment
     * @param service the apartment service
     *
     * pre-condition: service should not be null
     * post-condition: dialog is created
     */
    public AddApartmentDialog(ApartmentService service) {
        this.service = service;
        this.saved = false;
        this.editingApartment = null;
    }

    /**
     * This method is showing the dialog for adding a new apartment
     *
     * pre-condition: none
     * post-condition: dialog is showing and waiting for user input
     */
    public void showAdd() {
        this.editingApartment = null;
        buildAndShow("Add New Apartment");
    }

    /**
     * This method is showing the dialog for editing existing apartment
     * @param apartment the apartment to edit
     *
     * pre-condition: apartment should not be null
     * post-condition: dialog is showing with fields filled in
     */
    public void showEdit(Apartment apartment) {
        this.editingApartment = apartment;
        buildAndShow("Edit Apartment");
    }

    /**
     * This method is building the dialog UI and showing it
     * It have address split into street, city, state
     * It have dropdowns for bedrooms, bathrooms, lease
     * It have a rent slider and sqft preset buttons
     * @param title the window title
     *
     * pre-condition: none
     * post-condition: dialog is visible
     */
    private void buildAndShow(String title) {
        dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(25));
        mainBox.setStyle("-fx-background-color: #f8f9fa;");

        // header
        Label headerLabel = new Label(title);
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        headerLabel.setStyle("-fx-text-fill: #1a237e;");

        // ---- BASIC INFO SECTION ----
        Label basicHeader = new Label("Basic Information");
        basicHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        basicHeader.setStyle("-fx-text-fill: #333333;");

        GridPane basicGrid = new GridPane();
        basicGrid.setHgap(15);
        basicGrid.setVgap(12);
        basicGrid.setPadding(new Insets(10));
        basicGrid.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        addressField = makeTextField("e.g. 123 Elm St");
        cityField = makeTextField("e.g. Boston");
        neighborhoodField = makeTextField("e.g. Jamaica Plain");
        zipField = makeTextField("e.g. 02134");
        zipField.setMaxWidth(90);
        rentField = makeTextField("e.g. 2500");
        sqftField = makeTextField("e.g. 650");

        // state dropdown with common states
        stateCombo = new ComboBox<String>();
        stateCombo.getItems().addAll("MA", "CT", "RI", "NH", "NY", "ME", "VT", "NJ", "PA", "CA", "TX", "FL", "IL", "Other");
        stateCombo.setValue("MA");
        stateCombo.setMinWidth(100);
        stateCombo.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");

        // bedrooms dropdown - no more typing numbers
        bedroomsCombo = new ComboBox<String>();
        bedroomsCombo.getItems().addAll("Studio (0)", "1", "2", "3", "4", "5+");
        bedroomsCombo.setValue("1");
        bedroomsCombo.setMinWidth(140);
        bedroomsCombo.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");

        // bathrooms dropdown - no more typing numbers
        bathroomsCombo = new ComboBox<String>();
        bathroomsCombo.getItems().addAll("1", "2", "3", "4", "5");
        bathroomsCombo.setValue("1");
        bathroomsCombo.setMinWidth(140);
        bathroomsCombo.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");

        // row 0: street address (full width)
        basicGrid.add(makeLabel("Street Address:"), 0, 0);
        basicGrid.add(addressField, 1, 0);
        GridPane.setColumnSpan(addressField, 3);

        // row 1: city and state
        basicGrid.add(makeLabel("City:"), 0, 1);
        basicGrid.add(cityField, 1, 1);
        basicGrid.add(makeLabel("State:"), 2, 1);
        basicGrid.add(stateCombo, 3, 1);

        // row 2: neighborhood and zip code
        basicGrid.add(makeLabel("Neighborhood:"), 0, 2);
        basicGrid.add(neighborhoodField, 1, 2);
        basicGrid.add(makeLabel("Zip Code:"), 2, 2);
        basicGrid.add(zipField, 3, 2);

        // row 3: rent and sqft
        basicGrid.add(makeLabel("Monthly Rent ($):"), 0, 3);
        basicGrid.add(rentField, 1, 3);
        basicGrid.add(makeLabel("Size (sqft):"), 2, 3);
        basicGrid.add(sqftField, 3, 3);

        // row 4: bedrooms and bathrooms dropdowns
        basicGrid.add(makeLabel("Bedrooms:"), 0, 4);
        basicGrid.add(bedroomsCombo, 1, 4);
        basicGrid.add(makeLabel("Bathrooms:"), 2, 4);
        basicGrid.add(bathroomsCombo, 3, 4);

        // ---- AMENITIES SECTION ----
        Label amenitiesHeader = new Label("Amenities");
        amenitiesHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        amenitiesHeader.setStyle("-fx-text-fill: #333333;");

        HBox amenitiesBox = new HBox(18);
        amenitiesBox.setPadding(new Insets(12));
        amenitiesBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        parkingCheck = new CheckBox("\uD83C\uDD7F Parking Available");
        laundryCheck = new CheckBox("\uD83E\uDDFA In-Unit Laundry");
        dishwasherCheck = new CheckBox("\uD83C\uDF7D Dishwasher");
        acCheck = new CheckBox("\u2744 Air Conditioning");
        petCheck = new CheckBox("\uD83D\uDC3E Pets Allowed");
        furnishedCheck = new CheckBox("\uD83D\uDECB Comes Furnished");

        parkingCheck.setFont(Font.font("Arial", 13));
        laundryCheck.setFont(Font.font("Arial", 13));
        dishwasherCheck.setFont(Font.font("Arial", 13));
        acCheck.setFont(Font.font("Arial", 13));
        petCheck.setFont(Font.font("Arial", 13));
        furnishedCheck.setFont(Font.font("Arial", 13));

        // put amenities in two rows so they fit better
        HBox amenitiesRow1 = new HBox(18);
        amenitiesRow1.getChildren().addAll(parkingCheck, laundryCheck, dishwasherCheck);
        HBox amenitiesRow2 = new HBox(18);
        amenitiesRow2.getChildren().addAll(acCheck, petCheck, furnishedCheck);

        VBox amenitiesContent = new VBox(8);
        amenitiesContent.setPadding(new Insets(12));
        amenitiesContent.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );
        amenitiesContent.getChildren().addAll(amenitiesRow1, amenitiesRow2);

        // ---- LEASE & SOURCE SECTION ----
        Label leaseHeader = new Label("Lease Details & Source");
        leaseHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        leaseHeader.setStyle("-fx-text-fill: #333333;");

        GridPane leaseGrid = new GridPane();
        leaseGrid.setHgap(15);
        leaseGrid.setVgap(12);
        leaseGrid.setPadding(new Insets(10));
        leaseGrid.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        availableDatePicker = new javafx.scene.control.DatePicker();
        availableDatePicker.setPromptText("When can you move in?");
        availableDatePicker.setMinWidth(180);
        availableDatePicker.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");

        // lease duration dropdown instead of typing
        leaseCombo = new ComboBox<String>();
        leaseCombo.getItems().addAll("3 months", "6 months", "9 months", "12 months (1 year)", "18 months", "24 months (2 years)");
        leaseCombo.setValue("12 months (1 year)");
        leaseCombo.setMinWidth(180);
        leaseCombo.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");

        brokerFeeCheck = new CheckBox("Broker Fee Required (extra fee paid to agent)");
        utilitiesCheck = new CheckBox("Utilities Included (heat, water, electric in rent)");
        brokerFeeCheck.setFont(Font.font("Arial", 13));
        utilitiesCheck.setFont(Font.font("Arial", 13));

        sourceCombo = new ComboBox<String>();
        sourceCombo.getItems().addAll("Zillow", "Trulia", "Redfin", "Craigslist",
            "Apartments.com", "Facebook", "Broker", "Walk-in", "Other");
        sourceCombo.setValue("Zillow");
        sourceCombo.setMinWidth(180);
        sourceCombo.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");

        sourceURLField = makeTextField("Paste the listing URL here");

        // status dropdown with a colored dot next to it
        statusCombo = new ComboBox<String>();
        statusCombo.getItems().addAll("NEW", "SHORTLISTED", "TOURED", "REJECTED");
        statusCombo.setValue("NEW");
        statusCombo.setMinWidth(140);
        statusCombo.setStyle("-fx-font-family: Arial; -fx-font-size: 13;");

        statusDot = new Label("\u25CF");
        statusDot.setFont(Font.font("Arial", 18));
        statusDot.setStyle("-fx-text-fill: #4caf50;");

        // when status changes, update the dot color
        statusCombo.setOnAction(e -> {
            String val = statusCombo.getValue();
            if (val.equals("NEW")) {
                statusDot.setStyle("-fx-text-fill: #4caf50;");
            } else if (val.equals("SHORTLISTED")) {
                statusDot.setStyle("-fx-text-fill: #2196f3;");
            } else if (val.equals("TOURED")) {
                statusDot.setStyle("-fx-text-fill: #ff9800;");
            } else if (val.equals("REJECTED")) {
                statusDot.setStyle("-fx-text-fill: #f44336;");
            }
        });

        // put status combo and dot together
        HBox statusBox = new HBox(5);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusBox.getChildren().addAll(statusDot, statusCombo);

        leaseGrid.add(makeLabel("Move-in Date:"), 0, 0);
        leaseGrid.add(availableDatePicker, 1, 0);
        leaseGrid.add(makeLabel("Lease Duration:"), 2, 0);
        leaseGrid.add(leaseCombo, 3, 0);
        leaseGrid.add(brokerFeeCheck, 0, 1);
        GridPane.setColumnSpan(brokerFeeCheck, 2);
        leaseGrid.add(utilitiesCheck, 2, 1);
        GridPane.setColumnSpan(utilitiesCheck, 2);
        leaseGrid.add(makeLabel("Current Status:"), 0, 2);
        leaseGrid.add(statusBox, 1, 2);
        leaseGrid.add(makeLabel("Found On:"), 0, 3);
        leaseGrid.add(sourceCombo, 1, 3);
        leaseGrid.add(makeLabel("Listing Link:"), 2, 3);
        leaseGrid.add(sourceURLField, 3, 3);

        // ---- NOTES SECTION ----
        Label notesHeader = new Label("Personal Notes");
        notesHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        notesHeader.setStyle("-fx-text-fill: #333333;");

        notesArea = new TextArea();
        notesArea.setPromptText("Your personal notes... e.g. Nice natural light, close to campus, noisy street at night, good water pressure, friendly neighbors");
        notesArea.setPrefRowCount(5);
        notesArea.setFont(Font.font("Arial", 13));
        notesArea.setStyle(
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: #ddd;"
        );

        // ---- BUTTONS ----
        HBox buttonRow = new HBox(15);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Cancel");
        cancelButton.setFont(Font.font("Arial", 14));
        cancelButton.setPadding(new Insets(10, 30, 10, 30));
        cancelButton.setStyle(
            "-fx-background-color: #e0e0e0; " +
            "-fx-text-fill: #333333; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        Button saveButton = new Button("Save Apartment");
        saveButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        saveButton.setPadding(new Insets(10, 30, 10, 30));
        saveButton.setStyle(
            "-fx-background-color: #1a237e; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        cancelButton.setOnAction(e -> dialogStage.close());
        saveButton.setOnAction(e -> handleSave());

        buttonRow.getChildren().addAll(cancelButton, saveButton);

        // error label - show validation errors here
        errorLabel = new Label("");
        errorLabel.setFont(Font.font("Arial", 13));
        errorLabel.setStyle("-fx-text-fill: #c62828;");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);

        // put it all in a scroll pane in case screen is small
        mainBox.getChildren().addAll(
            headerLabel,
            basicHeader, basicGrid,
            amenitiesHeader, amenitiesContent,
            leaseHeader, leaseGrid,
            notesHeader, notesArea,
            errorLabel,
            buttonRow
        );

        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f8f9fa;");

        // if editing, fill in the existing values
        if (editingApartment != null) {
            fillFieldsForEdit();
        }

        Scene scene = new Scene(scrollPane, 780, 700);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    /**
     * This method is filling the form fields with existing apartment data for editing
     * It try to split the saved address into street, city, state parts
     *
     * pre-condition: editingApartment should not be null
     * post-condition: all fields is filled with existing data
     */
    private void fillFieldsForEdit() {
        // try to split saved address into street, neighborhood, city, state, zip
        String savedAddress = editingApartment.getAddress();
        if (savedAddress != null && !savedAddress.isEmpty()) {
            String[] parts = savedAddress.split(", ");
            if (parts.length == 4) {
                // has neighborhood: "street, neighborhood, city, state zip"
                addressField.setText(parts[0]);
                neighborhoodField.setText(parts[1]);
                cityField.setText(parts[2]);
                String statePart = parts[3];
                if (statePart.contains(" ")) {
                    String[] stateZip = statePart.split(" ");
                    statePart = stateZip[0];
                    zipField.setText(stateZip[1]);
                }
                stateCombo.setValue(statePart);
            } else if (parts.length == 3) {
                // no neighborhood: "street, city, state zip"
                addressField.setText(parts[0]);
                cityField.setText(parts[1]);
                String statePart = parts[2];
                if (statePart.contains(" ")) {
                    String[] stateZip = statePart.split(" ");
                    statePart = stateZip[0];
                    zipField.setText(stateZip[1]);
                }
                stateCombo.setValue(statePart);
            } else if (parts.length == 2) {
                addressField.setText(parts[0]);
                cityField.setText(parts[1]);
            } else {
                addressField.setText(savedAddress);
            }
        }

        rentField.setText(String.valueOf((int) editingApartment.getRent()));
        sqftField.setText(String.valueOf((int) editingApartment.getSqft()));

        // set bedrooms dropdown
        int beds = editingApartment.getBedrooms();
        if (beds == 0) {
            bedroomsCombo.setValue("Studio (0)");
        } else if (beds >= 5) {
            bedroomsCombo.setValue("5+");
        } else {
            bedroomsCombo.setValue(String.valueOf(beds));
        }

        // set bathrooms dropdown
        int baths = editingApartment.getBathrooms();
        if (baths >= 5) {
            bathroomsCombo.setValue("5");
        } else if (baths >= 1) {
            bathroomsCombo.setValue(String.valueOf(baths));
        } else {
            bathroomsCombo.setValue("1");
        }

        parkingCheck.setSelected(editingApartment.getHasParking());
        laundryCheck.setSelected(editingApartment.getHasLaundry());
        dishwasherCheck.setSelected(editingApartment.getHasDishwasher());
        acCheck.setSelected(editingApartment.getHasAC());
        petCheck.setSelected(editingApartment.getPetFriendly());
        furnishedCheck.setSelected(editingApartment.getFurnished());

        // try to set the date picker from the saved date string
        String savedDate = editingApartment.getAvailableDate();
        if (savedDate != null && !savedDate.isEmpty()) {
            try {
                availableDatePicker.setValue(java.time.LocalDate.parse(savedDate));
            } catch (Exception e) {
                // if date cant be parsed, just leave it empty
            }
        }

        // set lease duration dropdown
        int lease = editingApartment.getLeaseLength();
        if (lease == 3) {
            leaseCombo.setValue("3 months");
        } else if (lease == 6) {
            leaseCombo.setValue("6 months");
        } else if (lease == 9) {
            leaseCombo.setValue("9 months");
        } else if (lease == 12) {
            leaseCombo.setValue("12 months (1 year)");
        } else if (lease == 18) {
            leaseCombo.setValue("18 months");
        } else if (lease == 24) {
            leaseCombo.setValue("24 months (2 years)");
        } else {
            leaseCombo.setValue("12 months (1 year)");
        }

        brokerFeeCheck.setSelected(editingApartment.getBrokerFee());
        utilitiesCheck.setSelected(editingApartment.getUtilitiesIncluded());
        if (editingApartment.getSource() != null && !editingApartment.getSource().isEmpty()) {
            sourceCombo.setValue(editingApartment.getSource());
        }
        sourceURLField.setText(editingApartment.getSourceURL());
        notesArea.setText(editingApartment.getNotes());
        statusCombo.setValue(editingApartment.getStatus().toString());

        // update status dot color
        String statusVal = statusCombo.getValue();
        if (statusVal.equals("NEW")) {
            statusDot.setStyle("-fx-text-fill: #4caf50;");
        } else if (statusVal.equals("SHORTLISTED")) {
            statusDot.setStyle("-fx-text-fill: #2196f3;");
        } else if (statusVal.equals("TOURED")) {
            statusDot.setStyle("-fx-text-fill: #ff9800;");
        } else if (statusVal.equals("REJECTED")) {
            statusDot.setStyle("-fx-text-fill: #f44336;");
        }
    }

    /**
     * This method is handling when user click the save button
     * It validate the input and combine street + city + state into one address
     * Bedrooms, bathrooms, and lease dont need validation because they are dropdowns
     *
     * pre-condition: form fields should have some values
     * post-condition: apartment is saved and dialog is closed, or error is showed
     */
    private void handleSave() {
        // clear old error first
        errorLabel.setVisible(false);
        errorLabel.setText("");

        // reset all text field borders to normal
        String normalBorder = "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ddd; -fx-padding: 8;";
        String errorBorder = "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #f44336; -fx-border-width: 2; -fx-padding: 8;";
        addressField.setStyle(normalBorder);
        cityField.setStyle(normalBorder);
        neighborhoodField.setStyle(normalBorder);
        rentField.setStyle(normalBorder);
        sqftField.setStyle(normalBorder);

        // get the text from fields
        String street = addressField.getText().trim();
        String city = cityField.getText().trim();
        String state = stateCombo.getValue();
        String rentText = rentField.getText().trim();
        String sqftText = sqftField.getText().trim();

        // --- VALIDATE STREET ADDRESS ---
        if (street.isEmpty()) {
            showValidationError("Street address is required. Please enter the street address.", addressField, errorBorder);
            return;
        }
        if (street.length() < 5) {
            showValidationError("Street address is too short. Please enter a full street address.", addressField, errorBorder);
            return;
        }

        // --- VALIDATE CITY ---
        if (city.isEmpty()) {
            showValidationError("City is required. Please enter the city name.", cityField, errorBorder);
            return;
        }
        if (city.length() < 2) {
            showValidationError("City name is too short.", cityField, errorBorder);
            return;
        }

        // --- VALIDATE NEIGHBORHOOD ---
        String neighborhood = neighborhoodField.getText().trim();
        if (neighborhood.isEmpty()) {
            showValidationError("Neighborhood is required. This helps us find the correct location on the map.", neighborhoodField, errorBorder);
            return;
        }
        if (neighborhood.length() < 2) {
            showValidationError("Neighborhood name is too short.", neighborhoodField, errorBorder);
            return;
        }

        // capitalize each word so the address looks nice and APIs work better
        // like "hyde park ave" becomes "Hyde Park Ave"
        street = capitalizeWords(street);
        neighborhood = capitalizeWords(neighborhood);
        city = capitalizeWords(city);

        // combine street, neighborhood, city, state, zip into one full address
        String zip = zipField.getText().trim();

        String address = street + ", " + neighborhood + ", " + city + ", " + state;
        if (!zip.isEmpty()) {
            address = address + " " + zip;
        }

        // --- VALIDATE RENT ---
        if (rentText.isEmpty()) {
            showValidationError("Rent is required. Please enter the monthly rent or use the slider.", rentField, errorBorder);
            return;
        }
        double rent = 0;
        try {
            rent = Double.parseDouble(rentText);
        } catch (Exception e) {
            showValidationError("Rent must be a number. Example: 2500", rentField, errorBorder);
            return;
        }
        if (rent <= 0) {
            showValidationError("Rent must be more than $0.", rentField, errorBorder);
            return;
        }
        if (rent > 50000) {
            showValidationError("Rent seems too high. Please enter a value under $50,000.", rentField, errorBorder);
            return;
        }

        // --- VALIDATE SQFT ---
        double sqft = 0;
        if (!sqftText.isEmpty()) {
            try {
                sqft = Double.parseDouble(sqftText);
            } catch (Exception e) {
                showValidationError("Size must be a number. Example: 650", sqftField, errorBorder);
                return;
            }
            if (sqft < 0) {
                showValidationError("Size cannot be negative.", sqftField, errorBorder);
                return;
            }
            if (sqft > 50000) {
                showValidationError("Size seems too high. Please enter a value under 50,000.", sqftField, errorBorder);
                return;
            }
        }

        // --- READ BEDROOMS FROM DROPDOWN (no validation needed) ---
        int bedrooms = 0;
        String bedVal = bedroomsCombo.getValue();
        if (bedVal.equals("Studio (0)")) {
            bedrooms = 0;
        } else if (bedVal.equals("5+")) {
            bedrooms = 5;
        } else {
            bedrooms = Integer.parseInt(bedVal);
        }

        // --- READ BATHROOMS FROM DROPDOWN (no validation needed) ---
        int bathrooms = Integer.parseInt(bathroomsCombo.getValue());

        // --- READ LEASE FROM DROPDOWN (no validation needed) ---
        int leaseLength = 12;
        String leaseVal = leaseCombo.getValue();
        if (leaseVal.equals("3 months")) {
            leaseLength = 3;
        } else if (leaseVal.equals("6 months")) {
            leaseLength = 6;
        } else if (leaseVal.equals("9 months")) {
            leaseLength = 9;
        } else if (leaseVal.equals("12 months (1 year)")) {
            leaseLength = 12;
        } else if (leaseVal.equals("18 months")) {
            leaseLength = 18;
        } else if (leaseVal.equals("24 months (2 years)")) {
            leaseLength = 24;
        }

        // --- GET DATE ---
        String dateText = "";
        if (availableDatePicker.getValue() != null) {
            dateText = availableDatePicker.getValue().toString();
        }

        // --- ALL VALIDATIONS PASSED ---

        if (editingApartment != null) {
            // editing existing apartment
            Apartment updated = editingApartment.makeCopy();
            updated.setName(street);
            updated.setAddress(address);
            updated.setRent(rent);
            updated.setSqft(sqft);
            updated.setBedrooms(bedrooms);
            updated.setBathrooms(bathrooms);
            updated.setHasParking(parkingCheck.isSelected());
            updated.setHasLaundry(laundryCheck.isSelected());
            updated.setHasDishwasher(dishwasherCheck.isSelected());
            updated.setHasAC(acCheck.isSelected());
            updated.setPetFriendly(petCheck.isSelected());
            updated.setFurnished(furnishedCheck.isSelected());
            updated.setAvailableDate(dateText);
            updated.setLeaseLength(leaseLength);
            updated.setBrokerFee(brokerFeeCheck.isSelected());
            updated.setUtilitiesIncluded(utilitiesCheck.isSelected());
            updated.setSource(sourceCombo.getValue());
            updated.setSourceURL(sourceURLField.getText().trim());
            updated.setNotes(notesArea.getText().trim());
            updated.setStatus(Status.valueOf(statusCombo.getValue()));

            service.updateApartment(updated);
        } else {
            // adding new apartment
            Apartment apartment = new Apartment(street, address, rent, sqft, bedrooms, bathrooms);
            apartment.setHasParking(parkingCheck.isSelected());
            apartment.setHasLaundry(laundryCheck.isSelected());
            apartment.setHasDishwasher(dishwasherCheck.isSelected());
            apartment.setHasAC(acCheck.isSelected());
            apartment.setPetFriendly(petCheck.isSelected());
            apartment.setFurnished(furnishedCheck.isSelected());
            apartment.setAvailableDate(dateText);
            apartment.setLeaseLength(leaseLength);
            apartment.setBrokerFee(brokerFeeCheck.isSelected());
            apartment.setUtilitiesIncluded(utilitiesCheck.isSelected());
            apartment.setSource(sourceCombo.getValue());
            apartment.setSourceURL(sourceURLField.getText().trim());
            apartment.setNotes(notesArea.getText().trim());
            apartment.setStatus(Status.valueOf(statusCombo.getValue()));

            // try to get API data in background
            service.enrichWithApiData(apartment);
            service.addApartment(apartment);
        }

        saved = true;
        dialogStage.close();
    }

    /**
     * This method is showing an error message to the user
     * @param message the error message to show
     *
     * pre-condition: message should not be null
     * post-condition: error dialog is showed
     */
    private void showError(String message) {
        Stage errorStage = new Stage();
        errorStage.setTitle("Error");
        errorStage.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: white;");

        Label errorLabel = new Label(message);
        errorLabel.setFont(Font.font("Arial", 14));
        errorLabel.setStyle("-fx-text-fill: #c62828;");

        Button okButton = new Button("OK");
        okButton.setFont(Font.font("Arial", 13));
        okButton.setPadding(new Insets(8, 25, 8, 25));
        okButton.setStyle(
            "-fx-background-color: #1a237e; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5;"
        );
        okButton.setOnAction(e -> errorStage.close());

        box.getChildren().addAll(errorLabel, okButton);

        Scene scene = new Scene(box, 400, 120);
        errorStage.setScene(scene);
        errorStage.showAndWait();
    }

    /**
     * This method is showing a validation error on the form
     * It set the error label text and make the bad field border red
     * @param message the error message to show
     * @param badField the text field that have the bad input
     * @param errorBorder the css style for red border
     *
     * pre-condition: message and badField should not be null
     * post-condition: error is showed and field border is red
     */
    private void showValidationError(String message, TextField badField, String errorBorder) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        badField.setStyle(errorBorder);
        badField.requestFocus();
    }

    /**
     * This method is making a styled text field
     * @param prompt the placeholder text
     * @return a styled TextField
     *
     * pre-condition: none
     * post-condition: text field is returned
     */
    private TextField makeTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setFont(Font.font("Arial", 13));
        field.setMinWidth(180);
        field.setStyle(
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-border-color: #ddd; " +
            "-fx-padding: 8;"
        );
        return field;
    }

    /**
     * This method is making a styled label for the form
     * @param text the label text
     * @return a styled Label
     *
     * pre-condition: none
     * post-condition: label is returned
     */
    private Label makeLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        label.setStyle("-fx-text-fill: #555555;");
        return label;
    }

    /**
     * This method is telling if user saved the apartment
     * @return true if saved, false if cancelled
     *
     * pre-condition: none
     * post-condition: true or false is returned
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * This method is capitalizing the first letter of each word
     * Like "jamaica plain" becomes "Jamaica Plain"
     * This is important so the address look right for the APIs
     * @param text the text to capitalize
     * @return text with each word capitalized
     *
     * pre-condition: text should not be null
     * post-condition: capitalized text is returned
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split(" ");
        String result = "";

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.length() > 0) {
                // make first letter uppercase and rest lowercase
                String first = word.substring(0, 1).toUpperCase();
                String rest = word.substring(1);
                word = first + rest;
            }
            if (i > 0) {
                result = result + " ";
            }
            result = result + word;
        }

        return result;
    }
}
