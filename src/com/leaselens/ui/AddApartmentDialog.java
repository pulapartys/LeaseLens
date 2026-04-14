package com.leaselens.ui;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import com.leaselens.model.Apartment;
import com.leaselens.model.Status;
import com.leaselens.app.ApartmentManager;
import java.util.ArrayList;

/**
 * This is the popup dialog for adding or editing an apartment
 * It have all the form fields like address, rent, amenities etc
 *
 * pre-condition: service not null
 * post-condition: dialog is ready to show
 */
public class AddApartmentDialog {

    private ApartmentManager service;
    private Stage dialogStage;
    private boolean saved;
    private Apartment editingApartment;
    private Label errorLabel;

    // form fields
    private TextField addressField;
    private TextField cityField;
    private TextField neighborhoodField;
    private ComboBox<String> stateCombo;
    private TextField zipField;
    private TextField rentField;
    private TextField sqftField;
    private ComboBox<String> bedroomsCombo;
    private ComboBox<String> bathroomsCombo;
    private CheckBox parkingCheck;
    private CheckBox laundryCheck;
    private CheckBox dishwasherCheck;
    private CheckBox acCheck;
    private CheckBox petCheck;
    private CheckBox furnishedCheck;
    private javafx.scene.control.DatePicker availableDatePicker;
    private ComboBox<String> leaseCombo;
    private CheckBox brokerFeeCheck;
    private CheckBox utilitiesCheck;
    private ComboBox<String> sourceCombo;
    private TextField sourceURLField;
    private TextArea notesArea;
    private ComboBox<String> statusCombo;

    /**
     * This make the dialog
     * @param service the apartment service
     *
     * pre-condition: service not null
     * post-condition: dialog is ready
     */
    public AddApartmentDialog(ApartmentManager service) {
        this.service = service;
        this.saved = false;
        this.editingApartment = null;
    }

    /**
     * This show dialog for adding new apartment
     *
     * pre-condition: none
     * post-condition: add dialog is shown
     */
    public void showAdd() {
        this.editingApartment = null;
        buildAndShow("Add New Apartment");
    }

    /**
     * This show dialog for editing existing apartment
     * @param apartment the apartment to edit
     *
     * pre-condition: apartment not null
     * post-condition: edit dialog is shown with filled fields
     */
    public void showEdit(Apartment apartment) {
        this.editingApartment = apartment;
        buildAndShow("Edit Apartment");
    }

    /**
     * This build and show the dialog with all form fields
     * @param title the dialog title
     *
     * pre-condition: none
     * post-condition: dialog is shown
     */
    private void buildAndShow(String title) {
        dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.APPLICATION_MODAL);

        String cardStyle = "-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 15;";

        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(25));
        mainBox.setStyle("-fx-background-color: #f0f2f5;");

        Label headerLabel = new Label(title);
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        headerLabel.setStyle("-fx-text-fill: #1a237e;");
        Label requiredHint = new Label("* = required field. Fill in at least the address, city, neighborhood, and rent.");
        requiredHint.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12;");

        // ---- BASIC INFO CARD ----
        addressField = new TextField();
        addressField.setPromptText("e.g. 123 Elm St");
        addressField.setPrefWidth(400);
        cityField = new TextField();
        cityField.setPromptText("e.g. Boston");
        cityField.setPrefWidth(180);
        neighborhoodField = new TextField();
        neighborhoodField.setPromptText("e.g. Jamaica Plain");
        neighborhoodField.setPrefWidth(180);
        zipField = new TextField();
        zipField.setPromptText("e.g. 02134");
        zipField.setPrefWidth(80);
        rentField = new TextField();
        rentField.setPromptText("e.g. 2500");
        rentField.setPrefWidth(120);
        sqftField = new TextField();
        sqftField.setPromptText("e.g. 650");
        sqftField.setPrefWidth(120);

        stateCombo = new ComboBox<String>();
        stateCombo.getItems().addAll("MA", "CT", "RI", "NH", "NY", "ME", "VT", "NJ", "PA", "CA", "TX", "FL", "IL", "Other");
        stateCombo.setValue("MA");
        stateCombo.setPrefWidth(80);

        bedroomsCombo = new ComboBox<String>();
        bedroomsCombo.getItems().addAll("Studio (0)", "1", "2", "3", "4", "5+");
        bedroomsCombo.setValue("1");
        bedroomsCombo.setPrefWidth(120);

        bathroomsCombo = new ComboBox<String>();
        bathroomsCombo.getItems().addAll("1", "2", "3", "4", "5");
        bathroomsCombo.setValue("1");
        bathroomsCombo.setPrefWidth(80);

        GridPane basicGrid = new GridPane();
        basicGrid.setHgap(12);
        basicGrid.setVgap(12);
        // row 0: street address full width
        basicGrid.add(new Label("Street Address *"), 0, 0);
        basicGrid.add(addressField, 1, 0);
        GridPane.setColumnSpan(addressField, 3);
        // row 1: city, state
        basicGrid.add(new Label("City *"), 0, 1);
        basicGrid.add(cityField, 1, 1);
        basicGrid.add(new Label("State"), 2, 1);
        basicGrid.add(stateCombo, 3, 1);
        // row 2: neighborhood, zip
        basicGrid.add(new Label("Neighborhood *"), 0, 2);
        basicGrid.add(neighborhoodField, 1, 2);
        basicGrid.add(new Label("Zip Code"), 2, 2);
        basicGrid.add(zipField, 3, 2);
        // row 3: rent, sqft
        basicGrid.add(new Label("Monthly Rent ($) *"), 0, 3);
        basicGrid.add(rentField, 1, 3);
        basicGrid.add(new Label("Size (sqft)"), 2, 3);
        basicGrid.add(sqftField, 3, 3);
        // row 4: beds, baths
        basicGrid.add(new Label("Bedrooms"), 0, 4);
        basicGrid.add(bedroomsCombo, 1, 4);
        basicGrid.add(new Label("Bathrooms"), 2, 4);
        basicGrid.add(bathroomsCombo, 3, 4);

        Label basicLbl = new Label("Location & Details");
        basicLbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        basicLbl.setStyle("-fx-text-fill: #1a237e;");

        VBox basicCard = new VBox(10);
        basicCard.setStyle(cardStyle);
        basicCard.getChildren().addAll(basicLbl, basicGrid);

        // ---- AMENITIES CARD ----
        parkingCheck = new CheckBox("Parking");
        laundryCheck = new CheckBox("Laundry");
        dishwasherCheck = new CheckBox("Dishwasher");
        acCheck = new CheckBox("AC");
        petCheck = new CheckBox("Pets Allowed");
        furnishedCheck = new CheckBox("Furnished");

        // two rows of three for easier reading
        GridPane amenGrid = new GridPane();
        amenGrid.setHgap(25);
        amenGrid.setVgap(10);
        amenGrid.add(parkingCheck, 0, 0);
        amenGrid.add(laundryCheck, 1, 0);
        amenGrid.add(dishwasherCheck, 2, 0);
        amenGrid.add(acCheck, 0, 1);
        amenGrid.add(petCheck, 1, 1);
        amenGrid.add(furnishedCheck, 2, 1);

        Label amenLbl = new Label("Amenities (check all that apply)");
        amenLbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        amenLbl.setStyle("-fx-text-fill: #1a237e;");

        VBox amenCard = new VBox(10);
        amenCard.setStyle(cardStyle);
        amenCard.getChildren().addAll(amenLbl, amenGrid);

        // ---- LEASE DETAILS CARD ----
        availableDatePicker = new javafx.scene.control.DatePicker();
        availableDatePicker.setPromptText("Pick a date");
        availableDatePicker.setPrefWidth(160);

        leaseCombo = new ComboBox<String>();
        leaseCombo.getItems().addAll("3 months", "6 months", "9 months", "12 months (1 year)", "18 months", "24 months (2 years)");
        leaseCombo.setValue("12 months (1 year)");
        leaseCombo.setPrefWidth(180);

        brokerFeeCheck = new CheckBox("Broker Fee Required");
        utilitiesCheck = new CheckBox("Utilities Included");

        statusCombo = new ComboBox<String>();
        statusCombo.getItems().addAll("NEW", "SHORTLISTED", "TOURED", "REJECTED");
        statusCombo.setValue("NEW");
        statusCombo.setPrefWidth(140);

        sourceCombo = new ComboBox<String>();
        sourceCombo.getItems().addAll("Zillow", "Trulia", "Redfin", "Craigslist", "Apartments.com", "Facebook", "Broker", "Walk-in", "Other");
        sourceCombo.setValue("Zillow");
        sourceCombo.setPrefWidth(140);

        sourceURLField = new TextField();
        sourceURLField.setPromptText("e.g. https://zillow.com/...");
        sourceURLField.setPrefWidth(300);

        GridPane leaseGrid = new GridPane();
        leaseGrid.setHgap(12);
        leaseGrid.setVgap(12);
        leaseGrid.add(new Label("Move-in Date"), 0, 0);
        leaseGrid.add(availableDatePicker, 1, 0);
        leaseGrid.add(new Label("Lease Length"), 2, 0);
        leaseGrid.add(leaseCombo, 3, 0);
        leaseGrid.add(brokerFeeCheck, 0, 1);
        GridPane.setColumnSpan(brokerFeeCheck, 2);
        leaseGrid.add(utilitiesCheck, 2, 1);
        GridPane.setColumnSpan(utilitiesCheck, 2);
        leaseGrid.add(new Label("Status"), 0, 2);
        leaseGrid.add(statusCombo, 1, 2);
        leaseGrid.add(new Label("Source"), 2, 2);
        leaseGrid.add(sourceCombo, 3, 2);
        leaseGrid.add(new Label("Listing URL"), 0, 3);
        leaseGrid.add(sourceURLField, 1, 3);
        GridPane.setColumnSpan(sourceURLField, 3);

        Label leaseLbl = new Label("Lease & Listing Info");
        leaseLbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        leaseLbl.setStyle("-fx-text-fill: #1a237e;");

        VBox leaseCard = new VBox(10);
        leaseCard.setStyle(cardStyle);
        leaseCard.getChildren().addAll(leaseLbl, leaseGrid);

        // ---- NOTES CARD ----
        notesArea = new TextArea();
        notesArea.setPromptText("Any extra details, e.g. 'Nice view, noisy street, landlord responsive'");
        notesArea.setPrefRowCount(3);

        Label notesLbl = new Label("Notes (optional)");
        notesLbl.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        notesLbl.setStyle("-fx-text-fill: #1a237e;");

        VBox notesCard = new VBox(10);
        notesCard.setStyle(cardStyle);
        notesCard.getChildren().addAll(notesLbl, notesArea);

        // ---- ERROR AND BUTTONS ----
        errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 13;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setStyle("-fx-background-color: #e0e0e0;");
        cancelBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                dialogStage.close();
            }
        });
        Button saveBtn = new Button("Save Apartment");
        saveBtn.setPrefWidth(150);
        saveBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        saveBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                handleSave();
            }
        });

        HBox btnRow = new HBox(15);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(5, 0, 0, 0));
        btnRow.getChildren().addAll(errorLabel, cancelBtn, saveBtn);
        HBox.setHgrow(errorLabel, Priority.ALWAYS);

        mainBox.getChildren().addAll(headerLabel, requiredHint,
            basicCard, amenCard, leaseCard, notesCard, btnRow);

        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f2f5;");

        if (editingApartment != null) fillFieldsForEdit();

        dialogStage.setScene(new Scene(scrollPane, 720, 680));
        dialogStage.showAndWait();
    }

    /**
     * This fill the form fields with existing apartment data for editing
     *
     * pre-condition: editingApartment not null
     * post-condition: all fields is filled
     */
    private void fillFieldsForEdit() {
        String addr = editingApartment.getAddress();
        if (addr != null && !addr.isEmpty()) {
            String[] parts = addr.split(", ");
            if (parts.length == 4) {
                addressField.setText(parts[0]);
                neighborhoodField.setText(parts[1]);
                cityField.setText(parts[2]);
                parseStateZip(parts[3]);
            } else if (parts.length == 3) {
                addressField.setText(parts[0]);
                cityField.setText(parts[1]);
                parseStateZip(parts[2]);
            } else if (parts.length == 2) {
                addressField.setText(parts[0]);
                cityField.setText(parts[1]);
            } else {
                addressField.setText(addr);
            }
        }

        rentField.setText(String.valueOf((int) editingApartment.getRent()));
        sqftField.setText(String.valueOf((int) editingApartment.getSqft()));

        int beds = editingApartment.getBedrooms();
        if (beds == 0) bedroomsCombo.setValue("Studio (0)");
        else if (beds >= 5) bedroomsCombo.setValue("5+");
        else bedroomsCombo.setValue(String.valueOf(beds));

        int baths = editingApartment.getBathrooms();
        if (baths >= 5) bathroomsCombo.setValue("5");
        else if (baths >= 1) bathroomsCombo.setValue(String.valueOf(baths));

        parkingCheck.setSelected(editingApartment.getHasParking());
        laundryCheck.setSelected(editingApartment.getHasLaundry());
        dishwasherCheck.setSelected(editingApartment.getHasDishwasher());
        acCheck.setSelected(editingApartment.getHasAC());
        petCheck.setSelected(editingApartment.getPetFriendly());
        furnishedCheck.setSelected(editingApartment.getFurnished());

        String savedDate = editingApartment.getAvailableDate();
        if (savedDate != null && !savedDate.isEmpty()) {
            try { availableDatePicker.setValue(java.time.LocalDate.parse(savedDate)); }
            catch (Exception e) { /* skip if cant parse */ }
        }

        int lease = editingApartment.getLeaseLength();
        if (lease == 3) leaseCombo.setValue("3 months");
        else if (lease == 6) leaseCombo.setValue("6 months");
        else if (lease == 9) leaseCombo.setValue("9 months");
        else if (lease == 18) leaseCombo.setValue("18 months");
        else if (lease == 24) leaseCombo.setValue("24 months (2 years)");
        else leaseCombo.setValue("12 months (1 year)");

        brokerFeeCheck.setSelected(editingApartment.getBrokerFee());
        utilitiesCheck.setSelected(editingApartment.getUtilitiesIncluded());
        if (editingApartment.getSource() != null && !editingApartment.getSource().isEmpty()) {
            sourceCombo.setValue(editingApartment.getSource());
        }
        sourceURLField.setText(editingApartment.getSourceURL());
        notesArea.setText(editingApartment.getNotes());
        statusCombo.setValue(editingApartment.getStatus().toString());
    }

    /**
     * This parse state and zip from a string like "MA 02134"
     * @param part the string to parse
     *
     * pre-condition: part not null
     * post-condition: state and zip fields is set
     */
    private void parseStateZip(String part) {
        if (part.contains(" ")) {
            String[] sz = part.split(" ");
            stateCombo.setValue(sz[0]);
            zipField.setText(sz[1]);
        } else {
            stateCombo.setValue(part);
        }
    }

    /**
     * This handle the save button click and validate all fields
     * It check all errors at once so user can fix everything in one go
     *
     * pre-condition: form fields exist
     * post-condition: apartment is saved if valid
     */
    private void handleSave() {
        errorLabel.setVisible(false);

        String street = addressField.getText().trim();
        String city = cityField.getText().trim();
        String neighborhood = neighborhoodField.getText().trim();
        String rentText = rentField.getText().trim();
        String sqftText = sqftField.getText().trim();
        String zip = zipField.getText().trim();

        // collect all errors instead of stopping at first one
        ArrayList<String> errors = new ArrayList<String>();

        // check street address
        if (street.isEmpty() || street.length() < 5) {
            errors.add("Street address is required (at least 5 characters).");
        }

        // check city
        if (city.isEmpty() || city.length() < 2) {
            errors.add("City is required (at least 2 characters).");
        }

        // check neighborhood
        if (neighborhood.isEmpty() || neighborhood.length() < 2) {
            errors.add("Neighborhood is required (at least 2 characters).");
        }

        // check rent
        double rent = 0;
        boolean rentOk = false;
        if (rentText.isEmpty()) {
            errors.add("Monthly rent is required.");
        } else {
            try {
                rent = Double.parseDouble(rentText);
                rentOk = true;
            } catch (Exception e) {
                errors.add("Rent must be a number.");
            }
        }
        if (rentOk == true && (rent < 1000 || rent > 50000)) {
            errors.add("Rent must be between $1,000 and $50,000.");
        }

        // check sqft if user typed something
        double sqft = 0;
        boolean sqftOk = false;
        if (!sqftText.isEmpty()) {
            try {
                sqft = Double.parseDouble(sqftText);
                sqftOk = true;
            } catch (Exception e) {
                errors.add("Size (sqft) must be a number.");
            }
        }
        if (sqftOk == true && sqft <= 0) {
            errors.add("Size (sqft) must be greater than 0.");
        }

        // check zip code if user typed something
        if (!zip.isEmpty()) {
            if (zip.length() != 5) {
                errors.add("Zip code must be exactly 5 digits (e.g. 02134).");
            } else {
                // check every letter is a number
                boolean allDigits = true;
                for (int i = 0; i < zip.length(); i++) {
                    char c = zip.charAt(i);
                    if (c < '0' || c > '9') {
                        allDigits = false;
                    }
                }
                if (allDigits == false) {
                    errors.add("Zip code must be only numbers (e.g. 02134).");
                }
            }
        }

        // if there is any errors, show them all at once
        if (errors.size() > 0) {
            String allErrors = "";
            for (int i = 0; i < errors.size(); i++) {
                if (i > 0) {
                    allErrors = allErrors + "\n";
                }
                allErrors = allErrors + "- " + errors.get(i);
            }
            showError(allErrors);
            return;
        }

        // all good, now capitalize and build address
        street = capitalizeWords(street);
        neighborhood = capitalizeWords(neighborhood);
        city = capitalizeWords(city);

        String address = street + ", " + neighborhood + ", " + city + ", " + stateCombo.getValue();
        if (!zip.isEmpty()) {
            address = address + " " + zip;
        }

        int bedrooms = readBedrooms();
        int bathrooms = Integer.parseInt(bathroomsCombo.getValue());
        int leaseLength = readLeaseLength();
        String dateText = "";
        if (availableDatePicker.getValue() != null) {
            dateText = availableDatePicker.getValue().toString();
        }

        // save the apartment and get API warnings
        String apiWarnings = "";

        if (editingApartment != null) {
            // editing existing apartment
            Apartment updated = editingApartment.makeCopy();
            updated.setName(street);
            updated.setAddress(address);
            updated.setNeighborhood(neighborhood);
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

            // if address changed, clear old API data so stale values dont stay
            if (!updated.getAddress().equals(editingApartment.getAddress())) {
                updated.setLatitude(0);
                updated.setLongitude(0);
                updated.setWalkScore(-1);
                updated.setTransitScore(-1);
                updated.setBikeScore(-1);
                updated.setNearbyFood(-1);
                updated.setNearbyShops(-1);
                updated.setNearbyServices(-1);
                updated.setNearbyTransit(-1);
                updated.setNearbyLeisure(-1);
                updated.setNearbyBike(-1);
                updated.setNearestTStop("");
                updated.setDistanceToT(-1);
                updated.setSafetyScore(-1);
                updated.setCrimeCount(-1);
                updated.setCrimeBreakdown("");
                updated.setRecreationCount(-1);
                updated.setNearbyRecreation("");
            }
            apiWarnings = service.enrichWithApiData(updated);
            service.updateApartment(updated);
        } else {
            // adding new apartment
            Apartment apartment = new Apartment(street, address, rent, sqft, bedrooms, bathrooms);
            apartment.setNeighborhood(neighborhood);
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

            // try to get API data
            apiWarnings = service.enrichWithApiData(apartment);
            service.addApartment(apartment);
        }

        saved = true;
        dialogStage.close();

        // if any API had problems, show a warning popup
        // check if geocoding failed (address not found) or just other APIs failed
        if (apiWarnings.length() > 0) {
            boolean addressBad = apiWarnings.startsWith("Could not find this address");
            if (addressBad) {
                showAddressNotFoundPopup(apiWarnings);
            } else {
                showApiWarningPopup(apiWarnings);
            }
        }
    }

    /**
     * This show a popup when address was found but some APIs had trouble
     * Like walk score or crime data or transit was down
     * The apartment is saved and address is good, just some extra data missing
     * @param warnings the warning messages from APIs
     *
     * pre-condition: warnings not empty, geocoding was ok
     * post-condition: yellow popup is shown
     */
    private void showApiWarningPopup(String warnings) {
        Stage warningStage = new Stage();
        warningStage.setTitle("Heads Up - Some Data Unavailable");
        warningStage.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #fff8e1;");

        Label headerLabel = new Label("Apartment saved, but some data is missing");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        headerLabel.setStyle("-fx-text-fill: #e65100;");

        Label subLabel = new Label("Your address was found on the map, "
            + "but we had trouble getting some extra data:");
        subLabel.setFont(Font.font("Arial", 13));
        subLabel.setWrapText(true);
        subLabel.setStyle("-fx-text-fill: #555;");

        Label warnLabel = new Label(warnings);
        warnLabel.setWrapText(true);
        warnLabel.setFont(Font.font("Arial", 12));
        warnLabel.setStyle("-fx-text-fill: #c62828;");

        Label tipLabel = new Label("Tip: You can edit the apartment later to retry. "
            + "If problem keeps happening, the service might be temporarily down.");
        tipLabel.setWrapText(true);
        tipLabel.setFont(Font.font("Arial", 12));
        tipLabel.setStyle("-fx-text-fill: #666;");

        Button okBtn = new Button("Got It");
        okBtn.setStyle("-fx-background-color: #e65100; -fx-text-fill: white; -fx-font-weight: bold;");
        okBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                warningStage.close();
            }
        });

        box.getChildren().addAll(headerLabel, subLabel, warnLabel, tipLabel, okBtn);

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #fff8e1;");
        warningStage.setScene(new Scene(sp, 500, 300));
        warningStage.show();
    }

    /**
     * This show a serious popup when address could not be found on the map
     * It tell user the apartment is saved but address is probably wrong
     * No walk score, crime, transit, or parks data is available
     * @param warnings the warning message from geocoding failure
     *
     * pre-condition: warnings not empty, geocoding failed
     * post-condition: red popup is shown telling user to fix address
     */
    private void showAddressNotFoundPopup(String warnings) {
        Stage warningStage = new Stage();
        warningStage.setTitle("Address Not Found");
        warningStage.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #fce4ec;");

        Label headerLabel = new Label("Warning: Address Could Not Be Found");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        headerLabel.setStyle("-fx-text-fill: #c62828;");

        Label subLabel = new Label("Your apartment was saved, but the address "
            + "could not be located on the map. This means NO extra data "
            + "(Walk Score, Crime, Transit, Parks) is available.");
        subLabel.setFont(Font.font("Arial", 13));
        subLabel.setWrapText(true);
        subLabel.setStyle("-fx-text-fill: #555;");

        Label reasonLabel = new Label("This can happen if:\n"
            + "  - The street address has a typo\n"
            + "  - The city or zip code is wrong\n"
            + "  - The address is too new to be in the map database\n"
            + "  - Your internet connection is down");
        reasonLabel.setFont(Font.font("Arial", 12));
        reasonLabel.setWrapText(true);
        reasonLabel.setStyle("-fx-text-fill: #555;");

        Label fixLabel = new Label("What to do: Edit the apartment, "
            + "double-check the address, and save again. "
            + "The system will try to find it again.");
        fixLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        fixLabel.setWrapText(true);
        fixLabel.setStyle("-fx-text-fill: #c62828;");

        Button okBtn = new Button("OK, I Will Check");
        okBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        okBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                warningStage.close();
            }
        });

        box.getChildren().addAll(headerLabel, subLabel, reasonLabel, fixLabel, okBtn);

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #fce4ec;");
        warningStage.setScene(new Scene(sp, 500, 350));
        warningStage.show();
    }


    /**
     * This read the bedrooms combo value
     * @return number of bedrooms
     *
     * pre-condition: bedroomsCombo has value
     * post-condition: return number
     */
    private int readBedrooms() {
        String val = bedroomsCombo.getValue();
        if (val.equals("Studio (0)")) return 0;
        if (val.equals("5+")) return 5;
        return Integer.parseInt(val);
    }

    /**
     * This read the lease length combo value
     * @return number of months
     *
     * pre-condition: leaseCombo has value
     * post-condition: return number of months
     */
    private int readLeaseLength() {
        String val = leaseCombo.getValue();
        if (val.equals("3 months")) return 3;
        if (val.equals("6 months")) return 6;
        if (val.equals("9 months")) return 9;
        if (val.equals("18 months")) return 18;
        if (val.equals("24 months (2 years)")) return 24;
        return 12;
    }

    /**
     * This show an error message
     * @param msg the error message
     *
     * pre-condition: none
     * post-condition: error label is visible with message
     */
    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    /**
     * This capitalize the first letter of each word
     * @param text the text to capitalize
     * @return capitalized text
     *
     * pre-condition: none
     * post-condition: return capitalized text
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.split(" ");
        String result = "";
        for (int i = 0; i < words.length; i++) {
            String w = words[i];
            if (w.length() > 0) {
                w = w.substring(0, 1).toUpperCase() + w.substring(1);
            }
            if (i > 0) result = result + " ";
            result = result + w;
        }
        return result;
    }

    /**
     * This return if the apartment was saved
     * @return true if saved
     *
     * pre-condition: none
     * post-condition: return saved status
     */
    public boolean isSaved() { return saved; }
}
