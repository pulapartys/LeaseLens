package com.leaselens.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.CheckBox;
import com.leaselens.model.Apartment;
import com.leaselens.model.NearbyPlace;
import com.leaselens.model.Status;
import com.leaselens.service.ApartmentService;
import com.leaselens.datastructures.ApartmentSorter;
import com.leaselens.datastructures.PlacePriorityQueue;
import com.leaselens.datastructures.PlaceFilterQueue;
import com.leaselens.datastructures.SearchHistoryDeque;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * This class is the My Apartments tab - the main table view
 * It show all apartments in a table with search, sort, filter, and price range
 * This is the most important tab in the whole application
 *
 * pre-condition: service should not be null
 * post-condition: apartments tab is created with table and controls
 */
public class ApartmentsTab {

    private ApartmentService service;
    private VBox content;

    // for "View on Map" navigation - set by MainWindow
    private MapTab mapTab;
    private Runnable switchToMapTab;

    // table
    private TableView<Apartment> table;
    private ObservableList<Apartment> tableData;

    // controls
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> sortBy;
    private Slider minPriceSlider;
    private Slider maxPriceSlider;
    private Label priceRangeLabel;

    /**
     * This constructor is making the apartments tab
     * @param service the apartment service
     *
     * pre-condition: service should not be null
     * post-condition: tab is built with table and all controls
     */
    public ApartmentsTab(ApartmentService service) {
        this.service = service;
        this.tableData = FXCollections.observableArrayList();
        this.content = new VBox(15);
        buildTab();
    }

    /**
     * This method is building the whole tab UI
     *
     * pre-condition: none
     * post-condition: all controls and table is created
     */
    private void buildTab() {
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f0f2f5;");

        // ---- HEADER ROW ----
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label header = new Label("My Apartments");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #1a237e;");
        HBox.setHgrow(header, Priority.ALWAYS);

        // undo / redo buttons
        Button undoButton = new Button("Undo");
        undoButton.setFont(Font.font("Arial", 12));
        undoButton.setPadding(new Insets(8, 15, 8, 15));
        undoButton.setStyle(
            "-fx-background-color: #ff9800; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );

        Button redoButton = new Button("Redo");
        redoButton.setFont(Font.font("Arial", 12));
        redoButton.setPadding(new Insets(8, 15, 8, 15));
        redoButton.setStyle(
            "-fx-background-color: #ff9800; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );

        Button addButton = new Button("+ Add Apartment");
        addButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        addButton.setPadding(new Insets(10, 25, 10, 25));
        addButton.setStyle(
            "-fx-background-color: #1a237e; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        headerRow.getChildren().addAll(header, undoButton, redoButton, addButton);

        // ---- SEARCH AND FILTER ROW ----
        HBox filterRow = new HBox(12);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setPadding(new Insets(12));
        filterRow.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        // search bar
        searchField = new TextField();
        searchField.setPromptText("Search by name or address...");
        searchField.setFont(Font.font("Arial", 13));
        searchField.setMinWidth(250);
        searchField.setStyle(
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-border-color: #ddd; " +
            "-fx-padding: 8;"
        );

        // status filter
        Label filterLabel = new Label("Status:");
        filterLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        statusFilter = new ComboBox<String>();
        statusFilter.getItems().addAll("All", "NEW", "SHORTLISTED", "TOURED", "REJECTED");
        statusFilter.setValue("All");
        statusFilter.setMinWidth(130);

        // sort by
        Label sortLabel = new Label("Sort by:");
        sortLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        sortBy = new ComboBox<String>();
        sortBy.getItems().addAll("Rent (Low-High)", "Rent (High-Low)", "Sqft (Large-Small)",
                                  "Bedrooms", "Walk Score", "Safety Score", "Distance to T");
        sortBy.setValue("Rent (Low-High)");
        sortBy.setMinWidth(160);

        filterRow.getChildren().addAll(searchField, filterLabel, statusFilter, sortLabel, sortBy);

        // ---- PRICE RANGE ROW ----
        HBox priceRow = new HBox(12);
        priceRow.setAlignment(Pos.CENTER_LEFT);
        priceRow.setPadding(new Insets(10, 12, 10, 12));
        priceRow.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        Label priceLabel = new Label("Price Range:");
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        minPriceSlider = new Slider(0, 5000, 0);
        minPriceSlider.setMinWidth(200);
        minPriceSlider.setShowTickLabels(true);
        minPriceSlider.setMajorTickUnit(1000);

        Label toLabel = new Label(" to ");
        toLabel.setFont(Font.font("Arial", 13));

        maxPriceSlider = new Slider(0, 5000, 5000);
        maxPriceSlider.setMinWidth(200);
        maxPriceSlider.setShowTickLabels(true);
        maxPriceSlider.setMajorTickUnit(1000);

        priceRangeLabel = new Label("$0 - $5000");
        priceRangeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        priceRangeLabel.setStyle("-fx-text-fill: #1a237e;");
        priceRangeLabel.setMinWidth(120);

        Button applyPriceButton = new Button("Apply");
        applyPriceButton.setFont(Font.font("Arial", 12));
        applyPriceButton.setPadding(new Insets(6, 15, 6, 15));
        applyPriceButton.setStyle(
            "-fx-background-color: #00796b; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );

        priceRow.getChildren().addAll(priceLabel, minPriceSlider, toLabel, maxPriceSlider, priceRangeLabel, applyPriceButton);

        // ---- TABLE ----
        table = new TableView<Apartment>();
        table.setStyle(
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8;"
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        // make columns
        TableColumn<Apartment, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(150);

        TableColumn<Apartment, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressCol.setMinWidth(200);

        TableColumn<Apartment, Double> rentCol = new TableColumn<>("Rent");
        rentCol.setCellValueFactory(new PropertyValueFactory<>("rent"));
        rentCol.setMinWidth(90);
        rentCol.setCellFactory(col -> new TableCell<Apartment, Double>() {
            @Override
            protected void updateItem(Double rent, boolean empty) {
                super.updateItem(rent, empty);
                if (empty || rent == null) {
                    setText(null);
                } else {
                    setText("$" + String.format("%.0f", rent));
                }
            }
        });

        TableColumn<Apartment, Double> sqftCol = new TableColumn<>("Sqft");
        sqftCol.setCellValueFactory(new PropertyValueFactory<>("sqft"));
        sqftCol.setMinWidth(70);

        TableColumn<Apartment, Integer> bedCol = new TableColumn<>("Beds");
        bedCol.setCellValueFactory(new PropertyValueFactory<>("bedrooms"));
        bedCol.setMinWidth(50);

        TableColumn<Apartment, Integer> bathCol = new TableColumn<>("Baths");
        bathCol.setCellValueFactory(new PropertyValueFactory<>("bathrooms"));
        bathCol.setMinWidth(50);

        TableColumn<Apartment, Integer> walkCol = new TableColumn<>("Walk Score");
        walkCol.setCellValueFactory(new PropertyValueFactory<>("walkScore"));
        walkCol.setMinWidth(85);
        walkCol.setCellFactory(col -> new TableCell<Apartment, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                } else if (score < 0) {
                    setText("N/A");
                } else {
                    setText(String.valueOf(score));
                }
            }
        });

        TableColumn<Apartment, Integer> safetyCol = new TableColumn<>("Safety");
        safetyCol.setCellValueFactory(new PropertyValueFactory<>("safetyScore"));
        safetyCol.setMinWidth(70);
        safetyCol.setCellFactory(col -> new TableCell<Apartment, Integer>() {
            @Override
            protected void updateItem(Integer score, boolean empty) {
                super.updateItem(score, empty);
                if (empty || score == null) {
                    setText(null);
                } else if (score < 0) {
                    setText("N/A");
                } else {
                    setText(String.valueOf(score));
                }
            }
        });

        TableColumn<Apartment, Integer> parksCol = new TableColumn<>("Parks");
        parksCol.setCellValueFactory(new PropertyValueFactory<>("recreationCount"));
        parksCol.setMinWidth(60);
        parksCol.setCellFactory(col -> new TableCell<Apartment, Integer>() {
            @Override
            protected void updateItem(Integer count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) {
                    setText(null);
                } else if (count < 0) {
                    setText("N/A");
                } else {
                    setText(String.valueOf(count));
                }
            }
        });

        TableColumn<Apartment, Double> distCol = new TableColumn<>("Dist to T");
        distCol.setCellValueFactory(new PropertyValueFactory<>("distanceToT"));
        distCol.setMinWidth(80);
        distCol.setCellFactory(col -> new TableCell<Apartment, Double>() {
            @Override
            protected void updateItem(Double dist, boolean empty) {
                super.updateItem(dist, empty);
                if (empty || dist == null) {
                    setText(null);
                } else if (dist < 0) {
                    setText("N/A");
                } else {
                    setText(dist + " mi");
                }
            }
        });

        TableColumn<Apartment, Status> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setMinWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Apartment, Status>() {
            @Override
            protected void updateItem(Status status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toString());
                    setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    if (status == Status.NEW) {
                        setStyle("-fx-text-fill: #1565c0;");
                    } else if (status == Status.SHORTLISTED) {
                        setStyle("-fx-text-fill: #2e7d32;");
                    } else if (status == Status.TOURED) {
                        setStyle("-fx-text-fill: #e65100;");
                    } else {
                        setStyle("-fx-text-fill: #c62828;");
                    }
                }
            }
        });

        table.getColumns().add(nameCol);
        table.getColumns().add(addressCol);
        table.getColumns().add(rentCol);
        table.getColumns().add(sqftCol);
        table.getColumns().add(bedCol);
        table.getColumns().add(bathCol);
        table.getColumns().add(walkCol);
        table.getColumns().add(safetyCol);
        table.getColumns().add(parksCol);
        table.getColumns().add(distCol);
        table.getColumns().add(statusCol);
        table.setItems(tableData);
        table.setPlaceholder(new Label("No apartments added yet. Click '+ Add Apartment' to get started!"));

        // ---- ROW 1: edit, delete, status buttons ----
        HBox actionRow = new HBox(10);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setPadding(new Insets(5, 0, 0, 0));

        Button editButton = new Button("Edit");
        editButton.setFont(Font.font("Arial", 12));
        editButton.setPadding(new Insets(7, 15, 7, 15));
        editButton.setStyle("-fx-background-color: #00796b; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        Button deleteButton = new Button("Delete");
        deleteButton.setFont(Font.font("Arial", 12));
        deleteButton.setPadding(new Insets(7, 15, 7, 15));
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        Button shortlistBtn = new Button("Shortlist");
        shortlistBtn.setFont(Font.font("Arial", 12));
        shortlistBtn.setPadding(new Insets(7, 15, 7, 15));
        shortlistBtn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        Button touredBtn = new Button("Mark Toured");
        touredBtn.setFont(Font.font("Arial", 12));
        touredBtn.setPadding(new Insets(7, 15, 7, 15));
        touredBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        Button rejectBtn = new Button("Reject");
        rejectBtn.setFont(Font.font("Arial", 12));
        rejectBtn.setPadding(new Insets(7, 15, 7, 15));
        rejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        actionRow.getChildren().addAll(editButton, deleteButton, shortlistBtn, touredBtn, rejectBtn);

        // ---- ROW 2: view buttons for crime, transit, nearby, map, parks ----
        HBox viewRow = new HBox(10);
        viewRow.setAlignment(Pos.CENTER_LEFT);

        Button viewCrimeBtn = new Button("View Crime Data");
        viewCrimeBtn.setFont(Font.font("Arial", 12));
        viewCrimeBtn.setPadding(new Insets(7, 15, 7, 15));
        viewCrimeBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        Button viewTransitBtn = new Button("View Transit");
        viewTransitBtn.setFont(Font.font("Arial", 12));
        viewTransitBtn.setPadding(new Insets(7, 15, 7, 15));
        viewTransitBtn.setStyle("-fx-background-color: #0277bd; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        Button viewNearbyBtn = new Button("View Nearby Places");
        viewNearbyBtn.setFont(Font.font("Arial", 12));
        viewNearbyBtn.setPadding(new Insets(7, 15, 7, 15));
        viewNearbyBtn.setStyle("-fx-background-color: #00796b; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        Button viewOnMapBtn = new Button("View on Map");
        viewOnMapBtn.setFont(Font.font("Arial", 12));
        viewOnMapBtn.setPadding(new Insets(7, 15, 7, 15));
        viewOnMapBtn.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        Button viewRecBtn = new Button("View Parks");
        viewRecBtn.setFont(Font.font("Arial", 12));
        viewRecBtn.setPadding(new Insets(7, 15, 7, 15));
        viewRecBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");

        viewRow.getChildren().addAll(viewCrimeBtn, viewTransitBtn, viewNearbyBtn, viewOnMapBtn, viewRecBtn);

        // view crime data button
        viewCrimeBtn.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showCrimeData(selected);
            }
        });

        // view transit button
        viewTransitBtn.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showTransitData(selected);
            }
        });

        // view on map button
        viewOnMapBtn.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getLatitude() != 0 || selected.getLongitude() != 0) {
                    showMiniMap(selected);
                }
            }
        });

        // view nearby places button
        viewNearbyBtn.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (selected.getLatitude() != 0 || selected.getLongitude() != 0) {
                    showNearbyPlaces(selected);
                }
            }
        });

        // view parks button
        viewRecBtn.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showRecreationData(selected);
            }
        });

        // ---- SET UP EVENT HANDLERS ----

        // add button
        addButton.setOnAction(e -> {
            AddApartmentDialog dialog = new AddApartmentDialog(service);
            dialog.showAdd();
            if (dialog.isSaved()) {
                refresh();
            }
        });

        // edit button
        editButton.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                AddApartmentDialog dialog = new AddApartmentDialog(service);
                dialog.showEdit(selected);
                if (dialog.isSaved()) {
                    refresh();
                }
            }
        });

        // delete button
        deleteButton.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.removeApartment(selected.getId());
                refresh();
            }
        });

        // status buttons
        shortlistBtn.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.changeStatus(selected.getId(), Status.SHORTLISTED);
                refresh();
            }
        });

        touredBtn.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.changeStatus(selected.getId(), Status.TOURED);
                refresh();
            }
        });

        rejectBtn.setOnAction(e -> {
            Apartment selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                service.changeStatus(selected.getId(), Status.REJECTED);
                refresh();
            }
        });

        // undo / redo
        undoButton.setOnAction(e -> {
            service.undo();
            refresh();
        });

        redoButton.setOnAction(e -> {
            service.redo();
            refresh();
        });

        // search - filter as user types
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            applyFilters();
        });

        // status filter
        statusFilter.setOnAction(e -> applyFilters());

        // sort by
        sortBy.setOnAction(e -> applyFilters());

        // price sliders update label
        minPriceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            priceRangeLabel.setText("$" + String.format("%.0f", minPriceSlider.getValue())
                + " - $" + String.format("%.0f", maxPriceSlider.getValue()));
        });
        maxPriceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            priceRangeLabel.setText("$" + String.format("%.0f", minPriceSlider.getValue())
                + " - $" + String.format("%.0f", maxPriceSlider.getValue()));
        });

        // apply price filter button
        applyPriceButton.setOnAction(e -> applyFilters());

        // add everything to content
        content.getChildren().addAll(headerRow, filterRow, priceRow, table, actionRow, viewRow);

        // load initial data
        refresh();
    }

    /**
     * This method is applying all filters (search, status, price, sort) and updating table
     *
     * pre-condition: none
     * post-condition: table show filtered and sorted results
     */
    private void applyFilters() {
        tableData.clear();

        // step 1: get apartments based on search or price range
        ArrayList<Apartment> results;
        String searchText = searchField.getText().trim();
        double minPrice = minPriceSlider.getValue();
        double maxPrice = maxPriceSlider.getValue();

        if (!searchText.isEmpty()) {
            // use hash map search
            results = service.search(searchText);
        } else if (minPrice > 0 || maxPrice < 5000) {
            // filter by price range
            results = service.filterByPriceRange(minPrice, maxPrice);
        } else {
            // show all
            results = new ArrayList<Apartment>();
            for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
                Apartment apt = service.getAllApartments().get(i);
                results.add(apt);
            }
        }

        // step 2: filter by status
        String statusValue = statusFilter.getValue();
        if (!statusValue.equals("All")) {
            ArrayList<Apartment> statusFiltered = new ArrayList<Apartment>();
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).getStatus().toString().equals(statusValue)) {
                    statusFiltered.add(results.get(i));
                }
            }
            results = statusFiltered;
        }

        // step 3: sort using our merge sort
        Apartment[] sortArray = results.toArray(new Apartment[0]);
        String sortValue = sortBy.getValue();

        if (sortValue.equals("Rent (Low-High)")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byRentLowToHigh());
        } else if (sortValue.equals("Rent (High-Low)")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byRentHighToLow());
        } else if (sortValue.equals("Sqft (Large-Small)")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.bySqftHighToLow());
        } else if (sortValue.equals("Bedrooms")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byBedroomsHighToLow());
        } else if (sortValue.equals("Walk Score")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byWalkScoreHighToLow());
        } else if (sortValue.equals("Safety Score")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.bySafetyScoreHighToLow());
        } else if (sortValue.equals("Distance to T")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byDistanceToTLowToHigh());
        }

        // add to table
        for (int i = 0; i < sortArray.length; i++) {
            tableData.add(sortArray[i]);
        }
    }

    /**
     * This method is showing a detail popup for an apartment
     * @param apartment the apartment to show details for
     *
     * pre-condition: apartment should not be null
     * post-condition: detail window is showed
     */
    private void showDetails(Apartment apartment) {
        javafx.stage.Stage detailStage = new javafx.stage.Stage();
        detailStage.setTitle("Apartment Details - " + apartment.getName());

        VBox box = new VBox(12);
        box.setPadding(new Insets(25));
        box.setStyle("-fx-background-color: white;");

        // name header
        Label nameLabel = new Label(apartment.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        nameLabel.setStyle("-fx-text-fill: #1a237e;");

        Label addressLabel = new Label(apartment.getAddress());
        addressLabel.setFont(Font.font("Arial", 14));
        addressLabel.setStyle("-fx-text-fill: #666;");

        // basic info
        Label basicInfo = new Label(
            "Rent: $" + String.format("%.0f", apartment.getRent()) + "/mo    " +
            "Sqft: " + apartment.getSqft() + "    " +
            "Beds: " + apartment.getBedrooms() + "    " +
            "Baths: " + apartment.getBathrooms()
        );
        basicInfo.setFont(Font.font("Arial", 14));

        // amenities
        String amenities = "Amenities: ";
        if (apartment.getHasParking()) amenities += "Parking, ";
        if (apartment.getHasLaundry()) amenities += "Laundry, ";
        if (apartment.getHasDishwasher()) amenities += "Dishwasher, ";
        if (apartment.getHasAC()) amenities += "AC, ";
        if (apartment.getPetFriendly()) amenities += "Pet Friendly, ";
        if (apartment.getFurnished()) amenities += "Furnished, ";
        if (amenities.equals("Amenities: ")) amenities += "None";
        if (amenities.endsWith(", ")) amenities = amenities.substring(0, amenities.length() - 2);

        Label amenitiesLabel = new Label(amenities);
        amenitiesLabel.setFont(Font.font("Arial", 13));

        // API data
        Label apiHeader = new Label("Scores & Transit");
        apiHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        apiHeader.setStyle("-fx-text-fill: #1a237e;");

        String walkText = apartment.getWalkScore() >= 0 ? String.valueOf(apartment.getWalkScore()) : "N/A";
        String transitText = apartment.getTransitScore() >= 0 ? String.valueOf(apartment.getTransitScore()) : "N/A";
        String bikeText = apartment.getBikeScore() >= 0 ? String.valueOf(apartment.getBikeScore()) : "N/A";
        String tStop = apartment.getNearestTStop().isEmpty() ? "N/A" : apartment.getNearestTStop();
        String tDist = apartment.getDistanceToT() >= 0 ? apartment.getDistanceToT() + " mi" : "N/A";

        Label scoresLabel = new Label(
            "Walk Score: " + walkText + "    " +
            "Transit Score: " + transitText + "    " +
            "Bike Score: " + bikeText
        );
        scoresLabel.setFont(Font.font("Arial", 14));

        // nearby amenity counts
        String nearbyText;
        if (apartment.getNearbyFood() >= 0) {
            nearbyText = "Food: " + apartment.getNearbyFood()
                + "   Shops: " + apartment.getNearbyShops()
                + "   Services: " + apartment.getNearbyServices()
                + "   Transit Stops: " + apartment.getNearbyTransit()
                + "   Leisure: " + apartment.getNearbyLeisure()
                + "   Bike: " + apartment.getNearbyBike();
        } else {
            nearbyText = "Nearby amenity counts not available (add apartment to fetch)";
        }
        Label nearbyLabel = new Label("Nearby (within 700m): " + nearbyText);
        nearbyLabel.setFont(Font.font("Arial", 13));
        nearbyLabel.setWrapText(true);

        // safety and crime info
        String safetyText;
        if (apartment.getSafetyScore() >= 0) {
            safetyText = "Safety Score: " + apartment.getSafetyScore()
                + "   Crimes Nearby: " + apartment.getCrimeCount()
                + "\nTop Offenses: " + apartment.getCrimeBreakdown();
        } else {
            safetyText = "Safety data not available";
        }
        Label safetyLabel = new Label(safetyText);
        safetyLabel.setFont(Font.font("Arial", 13));
        safetyLabel.setWrapText(true);

        // recreation info
        String recText;
        if (apartment.getRecreationCount() >= 0) {
            recText = "Nearby Recreation Areas (" + apartment.getRecreationCount() + "): "
                + apartment.getNearbyRecreation();
        } else {
            recText = "Recreation data not available";
        }
        Label recLabel = new Label(recText);
        recLabel.setFont(Font.font("Arial", 13));
        recLabel.setWrapText(true);

        Label tLabel = new Label("Nearest T Stop: " + tStop + " (" + tDist + ")");
        tLabel.setFont(Font.font("Arial", 14));

        // lease info
        Label leaseInfo = new Label(
            "Source: " + apartment.getSource() + "    " +
            "Available: " + apartment.getAvailableDate() + "    " +
            "Lease: " + apartment.getLeaseLength() + " months"
        );
        leaseInfo.setFont(Font.font("Arial", 13));

        // notes
        Label notesHeader = new Label("Notes:");
        notesHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label notesLabel = new Label(apartment.getNotes().isEmpty() ? "No notes" : apartment.getNotes());
        notesLabel.setFont(Font.font("Arial", 13));
        notesLabel.setWrapText(true);

        // status
        Label statusLabel = new Label("Status: " + apartment.getStatus().toString());
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        // buttons row
        HBox detailButtons = new HBox(10);
        detailButtons.setAlignment(Pos.CENTER_LEFT);
        detailButtons.setPadding(new Insets(10, 0, 0, 0));

        // view on map button - open a small map window for this apartment
        Button mapButton = new Button("View on Map");
        mapButton.setFont(Font.font("Arial", 13));
        mapButton.setPadding(new Insets(8, 20, 8, 20));
        mapButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        mapButton.setOnAction(e -> {
            if (apartment.getLatitude() != 0 || apartment.getLongitude() != 0) {
                showMiniMap(apartment);
            }
        });

        // view nearby places button - show nearby restaurants, shops, etc
        Button nearbyButton = new Button("View Nearby Places");
        nearbyButton.setFont(Font.font("Arial", 13));
        nearbyButton.setPadding(new Insets(8, 20, 8, 20));
        nearbyButton.setStyle("-fx-background-color: #00796b; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        nearbyButton.setOnAction(e -> {
            if (apartment.getLatitude() != 0 || apartment.getLongitude() != 0) {
                showNearbyPlaces(apartment);
            }
        });

        // close button
        Button closeButton = new Button("Close");
        closeButton.setFont(Font.font("Arial", 13));
        closeButton.setPadding(new Insets(8, 25, 8, 25));
        closeButton.setStyle("-fx-background-color: #1a237e; -fx-text-fill: white; -fx-background-radius: 5;");
        closeButton.setOnAction(e -> detailStage.close());

        detailButtons.getChildren().addAll(mapButton, nearbyButton, closeButton);

        box.getChildren().addAll(nameLabel, addressLabel, basicInfo, amenitiesLabel,
            apiHeader, scoresLabel, nearbyLabel, safetyLabel, recLabel, tLabel, leaseInfo, notesHeader, notesLabel, statusLabel, detailButtons);

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);

        javafx.scene.Scene scene = new javafx.scene.Scene(sp, 600, 500);
        detailStage.setScene(scene);
        detailStage.show();
    }

    /**
     * This method is showing crime data for an apartment in a popup
     * It show the safety score, crime count, and top offenses from Boston PD data
     * @param apartment the apartment to show crime data for
     *
     * pre-condition: apartment should not be null
     * post-condition: crime data popup is opened
     */
    private void showCrimeData(Apartment apartment) {
        javafx.stage.Stage crimeStage = new javafx.stage.Stage();
        crimeStage.setTitle("Crime Data - " + apartment.getName());

        VBox box = new VBox(12);
        box.setPadding(new Insets(25));
        box.setStyle("-fx-background-color: white;");

        Label header = new Label("Crime Data - " + apartment.getName());
        header.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        header.setStyle("-fx-text-fill: #c62828;");

        Label addrLabel = new Label(apartment.getAddress());
        addrLabel.setFont(Font.font("Arial", 13));
        addrLabel.setStyle("-fx-text-fill: #666;");

        // safety score
        String safetyText = "N/A";
        if (apartment.getSafetyScore() >= 0) {
            safetyText = String.valueOf(apartment.getSafetyScore()) + " / 100";
        }
        Label safetyLabel = new Label("Safety Score: " + safetyText);
        safetyLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        if (apartment.getSafetyScore() >= 70) {
            safetyLabel.setStyle("-fx-text-fill: #2e7d32;");
        } else if (apartment.getSafetyScore() >= 40) {
            safetyLabel.setStyle("-fx-text-fill: #ff9800;");
        } else {
            safetyLabel.setStyle("-fx-text-fill: #c62828;");
        }

        // crime count
        String countText = "N/A";
        if (apartment.getCrimeCount() >= 0) {
            countText = String.valueOf(apartment.getCrimeCount());
        }
        Label countLabel = new Label("Crimes Reported Nearby: " + countText);
        countLabel.setFont(Font.font("Arial", 14));

        // crime breakdown - show each offense on its own row
        Label breakdownHeader = new Label("Top Offenses:");
        breakdownHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        breakdownHeader.setStyle("-fx-text-fill: #333;");

        VBox offenseList = new VBox(6);
        offenseList.setPadding(new Insets(5, 0, 5, 10));

        String breakdown = apartment.getCrimeBreakdown();
        if (breakdown == null || breakdown.isEmpty()) {
            Label noDataLabel = new Label("No crime data available. Add the apartment to fetch data.");
            noDataLabel.setFont(Font.font("Arial", 13));
            noDataLabel.setStyle("-fx-text-fill: #999;");
            offenseList.getChildren().add(noDataLabel);
        } else {
            // split by comma to get each offense
            String[] offenses = breakdown.split(",");
            for (int i = 0; i < offenses.length; i++) {
                String offense = offenses[i].trim();
                if (offense.isEmpty()) continue;

                // make a row with colored background
                HBox offenseRow = new HBox(10);
                offenseRow.setPadding(new Insets(6, 12, 6, 12));
                offenseRow.setAlignment(Pos.CENTER_LEFT);

                // alternate row colors
                if (i % 2 == 0) {
                    offenseRow.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 5;");
                } else {
                    offenseRow.setStyle("-fx-background-color: #fff3e0; -fx-background-radius: 5;");
                }

                // split offense name and count (like "ASSAULT: 51")
                String offenseName = offense;
                String offenseCount = "";
                int colonIndex = offense.lastIndexOf(":");
                if (colonIndex >= 0) {
                    offenseName = offense.substring(0, colonIndex).trim();
                    offenseCount = offense.substring(colonIndex + 1).trim();
                }

                // rank number
                Label rankLabel = new Label("#" + (i + 1));
                rankLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                rankLabel.setStyle("-fx-text-fill: #c62828; -fx-min-width: 30;");

                // offense name
                Label nameLabel = new Label(offenseName);
                nameLabel.setFont(Font.font("Arial", 13));
                nameLabel.setStyle("-fx-text-fill: #333;");
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                // count badge
                Label countBadge = new Label(offenseCount);
                countBadge.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                countBadge.setPadding(new Insets(2, 8, 2, 8));
                countBadge.setStyle(
                    "-fx-background-color: #c62828; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 10;"
                );

                offenseRow.getChildren().addAll(rankLabel, nameLabel, countBadge);
                offenseList.getChildren().add(offenseRow);
            }
        }

        Button closeBtn = new Button("Close");
        closeBtn.setFont(Font.font("Arial", 13));
        closeBtn.setPadding(new Insets(8, 25, 8, 25));
        closeBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-background-radius: 5;");
        closeBtn.setOnAction(e -> crimeStage.close());

        box.getChildren().addAll(header, addrLabel, safetyLabel, countLabel, breakdownHeader, offenseList, closeBtn);

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: white;");

        javafx.scene.Scene scene = new javafx.scene.Scene(sp, 550, 450);
        crimeStage.setScene(scene);
        crimeStage.show();
    }

    /**
     * This method is showing transit data for an apartment in a popup
     * It show the nearest T stop, distance, walk score, transit score, bike score
     * @param apartment the apartment to show transit data for
     *
     * pre-condition: apartment should not be null
     * post-condition: transit data popup is opened
     */
    private void showTransitData(Apartment apartment) {
        javafx.stage.Stage transitStage = new javafx.stage.Stage();
        transitStage.setTitle("Transit Info - " + apartment.getName());

        VBox box = new VBox(12);
        box.setPadding(new Insets(25));
        box.setStyle("-fx-background-color: white;");

        Label header = new Label("Transit Info - " + apartment.getName());
        header.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        header.setStyle("-fx-text-fill: #0277bd;");

        Label addrLabel = new Label(apartment.getAddress());
        addrLabel.setFont(Font.font("Arial", 13));
        addrLabel.setStyle("-fx-text-fill: #666;");

        // ---- NEARBY SUBWAY STATIONS ----
        Label subwayHeader = new Label("Nearby Train Stations");
        subwayHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        subwayHeader.setStyle("-fx-text-fill: #e65100;");

        VBox subwayBox = new VBox(4);
        if (apartment.getLatitude() != 0 && apartment.getLongitude() != 0) {
            String[] subwayStops = service.getMbtaService().findNearbySubway(
                apartment.getLatitude(), apartment.getLongitude());

            if (subwayStops.length == 0) {
                Label none = new Label("  No train stations nearby");
                none.setFont(Font.font("Arial", 13));
                none.setStyle("-fx-text-fill: #999;");
                subwayBox.getChildren().add(none);
            } else {
                for (int i = 0; i < subwayStops.length; i++) {
                    Label stopLabel = new Label("  " + subwayStops[i]);
                    stopLabel.setFont(Font.font("Arial", 13));
                    subwayBox.getChildren().add(stopLabel);
                }
            }
        } else {
            Label noData = new Label("  No location data available");
            noData.setFont(Font.font("Arial", 13));
            noData.setStyle("-fx-text-fill: #999;");
            subwayBox.getChildren().add(noData);
        }

        // ---- NEARBY BUS STOPS ----
        Label busHeader = new Label("Nearby Bus Stops");
        busHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        busHeader.setStyle("-fx-text-fill: #1565c0;");

        VBox busBox = new VBox(4);
        if (apartment.getLatitude() != 0 && apartment.getLongitude() != 0) {
            String[] busStops = service.getMbtaService().findNearbyBus(
                apartment.getLatitude(), apartment.getLongitude());

            if (busStops.length == 0) {
                Label none = new Label("  No bus stops nearby");
                none.setFont(Font.font("Arial", 13));
                none.setStyle("-fx-text-fill: #999;");
                busBox.getChildren().add(none);
            } else {
                for (int i = 0; i < busStops.length; i++) {
                    Label stopLabel = new Label("  " + busStops[i]);
                    stopLabel.setFont(Font.font("Arial", 13));
                    busBox.getChildren().add(stopLabel);
                }
            }
        } else {
            Label noData = new Label("  No location data available");
            noData.setFont(Font.font("Arial", 13));
            noData.setStyle("-fx-text-fill: #999;");
            busBox.getChildren().add(noData);
        }

        // ---- SCORES ----
        Label scoresHeader = new Label("Scores");
        scoresHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        scoresHeader.setStyle("-fx-text-fill: #2e7d32;");

        String walkText = apartment.getWalkScore() >= 0 ? String.valueOf(apartment.getWalkScore()) : "N/A";
        String transitText = apartment.getTransitScore() >= 0 ? String.valueOf(apartment.getTransitScore()) : "N/A";
        String bikeText = apartment.getBikeScore() >= 0 ? String.valueOf(apartment.getBikeScore()) : "N/A";

        Label walkLabel = new Label("  Walk Score: " + walkText);
        walkLabel.setFont(Font.font("Arial", 14));
        Label transitLabel = new Label("  Transit Score: " + transitText);
        transitLabel.setFont(Font.font("Arial", 14));
        Label bikeLabel = new Label("  Bike Score: " + bikeText);
        bikeLabel.setFont(Font.font("Arial", 14));

        Button closeBtn = new Button("Close");
        closeBtn.setFont(Font.font("Arial", 13));
        closeBtn.setPadding(new Insets(8, 25, 8, 25));
        closeBtn.setStyle("-fx-background-color: #0277bd; -fx-text-fill: white; -fx-background-radius: 5;");
        closeBtn.setOnAction(e -> transitStage.close());

        box.getChildren().addAll(
            header, addrLabel,
            subwayHeader, subwayBox,
            busHeader, busBox,
            scoresHeader, walkLabel, transitLabel, bikeLabel,
            closeBtn
        );

        javafx.scene.Scene scene = new javafx.scene.Scene(box, 480, 500);
        transitStage.setScene(scene);
        transitStage.show();
    }

    /**
     * This method is showing recreation / parks data for an apartment in a popup
     * It show nearby recreation areas from Recreation.gov
     * @param apartment the apartment to show parks data for
     *
     * pre-condition: apartment should not be null
     * post-condition: recreation data popup is opened
     */
    private void showRecreationData(Apartment apartment) {
        javafx.stage.Stage recStage = new javafx.stage.Stage();
        recStage.setTitle("Nearby Parks - " + apartment.getName());

        VBox box = new VBox(12);
        box.setPadding(new Insets(25));
        box.setStyle("-fx-background-color: white;");

        Label header = new Label("Nearby Parks & Recreation - " + apartment.getName());
        header.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        header.setStyle("-fx-text-fill: #2e7d32;");

        Label addrLabel = new Label(apartment.getAddress());
        addrLabel.setFont(Font.font("Arial", 13));
        addrLabel.setStyle("-fx-text-fill: #666;");

        // recreation count
        String countText = "N/A";
        if (apartment.getRecreationCount() >= 0) {
            countText = String.valueOf(apartment.getRecreationCount());
        }
        Label countLabel = new Label("Recreation Areas Found: " + countText);
        countLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        countLabel.setStyle("-fx-text-fill: #2e7d32;");

        // list of recreation areas
        Label listHeader = new Label("Nearby Recreation Areas:");
        listHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        listHeader.setStyle("-fx-text-fill: #333;");

        String recNames = apartment.getNearbyRecreation();
        if (recNames == null || recNames.isEmpty()) {
            recNames = "No recreation data available. Add the apartment to fetch data.";
        }

        // split by comma and show each one on its own line
        VBox listBox = new VBox(5);
        listBox.setPadding(new Insets(5, 0, 5, 15));
        String[] parts = recNames.split(",");
        for (int i = 0; i < parts.length; i++) {
            String parkName = parts[i].trim();
            if (!parkName.isEmpty()) {
                Label parkLabel = new Label("- " + parkName);
                parkLabel.setFont(Font.font("Arial", 13));
                parkLabel.setStyle("-fx-text-fill: #333;");
                listBox.getChildren().add(parkLabel);
            }
        }

        Button closeBtn = new Button("Close");
        closeBtn.setFont(Font.font("Arial", 13));
        closeBtn.setPadding(new Insets(8, 25, 8, 25));
        closeBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 5;");
        closeBtn.setOnAction(e -> recStage.close());

        box.getChildren().addAll(header, addrLabel, countLabel, listHeader, listBox, closeBtn);

        javafx.scene.Scene scene = new javafx.scene.Scene(box, 500, 350);
        recStage.setScene(scene);
        recStage.show();
    }

    /**
     * This method is showing a small map window centered on one apartment
     * It use OpenStreetMap tiles and draw a red pin for the apartment
     * @param apartment the apartment to show on map
     *
     * pre-condition: apartment should have coordinates
     * post-condition: map window is opened
     */
    /**
     * This method is showing a map popup with zoom and drag controls
     * User can zoom in and out with buttons or scroll wheel
     * User can drag the map to see things around the address
     * @param apartment the apartment to show on map
     *
     * pre-condition: apartment should have latitude and longitude
     * post-condition: map window is opened with controls
     */
    private void showMiniMap(Apartment apartment) {
        javafx.stage.Stage mapStage = new javafx.stage.Stage();
        mapStage.setTitle("Map - " + apartment.getName());

        double lat = apartment.getLatitude();
        double lon = apartment.getLongitude();
        int tileSize = 256;

        // we use arrays so we can change the values inside button clicks
        // because java dont let us change normal variables inside lambdas
        int[] currentZoom = {16};
        double[] centerLat = {lat};
        double[] centerLon = {lon};

        // these are for when user is dragging the map
        double[] dragStartX = {0};
        double[] dragStartY = {0};
        double[] dragStartLat = {0};
        double[] dragStartLon = {0};

        // make a canvas for the map
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(600, 450);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // this thing draws all the map tiles on the canvas
        // we call it every time zoom or center changes
        Runnable drawMap = new Runnable() {
            public void run() {
                int zoom = currentZoom[0];
                double cLat = centerLat[0];
                double cLon = centerLon[0];

                // fill background with water color
                gc.setFill(javafx.scene.paint.Color.web("#b8d4e8"));
                gc.fillRect(0, 0, 600, 450);

                // calculate center tile position
                double centerTileX = (cLon + 180.0) / 360.0 * Math.pow(2, zoom);
                double cLatRad = Math.toRadians(cLat);
                double centerTileY = (1.0 - Math.log(Math.tan(cLatRad) + 1.0 / Math.cos(cLatRad)) / Math.PI) / 2.0 * Math.pow(2, zoom);

                // figure out which tiles to draw
                int startTileX = (int) Math.floor(centerTileX - 600.0 / 2.0 / tileSize);
                int startTileY = (int) Math.floor(centerTileY - 450.0 / 2.0 / tileSize);
                double startPixelX = (startTileX - centerTileX + 600.0 / 2.0 / tileSize) * tileSize;
                double startPixelY = (startTileY - centerTileY + 450.0 / 2.0 / tileSize) * tileSize;

                int tilesAcross = (int) Math.ceil(600.0 / tileSize) + 2;
                int tilesDown = (int) Math.ceil(450.0 / tileSize) + 2;
                int maxTile = (int) Math.pow(2, zoom);

                // figure out where the apartment pin should be on screen
                double pinTileX = (lon + 180.0) / 360.0 * Math.pow(2, zoom);
                double pinLatRad = Math.toRadians(lat);
                double pinTileY = (1.0 - Math.log(Math.tan(pinLatRad) + 1.0 / Math.cos(pinLatRad)) / Math.PI) / 2.0 * Math.pow(2, zoom);
                double pinScreenX = (pinTileX - centerTileX) * tileSize + 300;
                double pinScreenY = (pinTileY - centerTileY) * tileSize + 225;

                // load and draw each tile
                for (int tx = 0; tx < tilesAcross; tx++) {
                    for (int ty = 0; ty < tilesDown; ty++) {
                        int tileX = ((startTileX + tx) % maxTile + maxTile) % maxTile;
                        int tileY = startTileY + ty;
                        if (tileY < 0 || tileY >= maxTile) {
                            continue;
                        }

                        double px = startPixelX + tx * tileSize;
                        double py = startPixelY + ty * tileSize;

                        String url = "https://tile.openstreetmap.org/" + zoom + "/" + tileX + "/" + tileY + ".png";
                        javafx.scene.image.Image tile = new javafx.scene.image.Image(url, tileSize, tileSize, false, false, true);

                        final double fpx = px;
                        final double fpy = py;
                        final double savedPinX = pinScreenX;
                        final double savedPinY = pinScreenY;

                        tile.progressProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal.doubleValue() >= 1.0 && !tile.isError()) {
                                javafx.application.Platform.runLater(() -> {
                                    gc.drawImage(tile, fpx, fpy, tileSize, tileSize);
                                    // draw the apartment pin on top after each tile loads
                                    gc.setFill(javafx.scene.paint.Color.web("#f44336"));
                                    gc.fillOval(savedPinX - 10, savedPinY - 10, 20, 20);
                                    gc.setStroke(javafx.scene.paint.Color.WHITE);
                                    gc.setLineWidth(3);
                                    gc.strokeOval(savedPinX - 10, savedPinY - 10, 20, 20);
                                    gc.setFill(javafx.scene.paint.Color.web("#1a237e"));
                                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                                    gc.fillText(apartment.getName(), savedPinX + 15, savedPinY + 5);
                                });
                            }
                        });

                        if (tile.getProgress() >= 1.0 && !tile.isError()) {
                            gc.drawImage(tile, px, py, tileSize, tileSize);
                        }
                    }
                }

                // draw pin right away too
                gc.setFill(javafx.scene.paint.Color.web("#f44336"));
                gc.fillOval(pinScreenX - 10, pinScreenY - 10, 20, 20);
                gc.setStroke(javafx.scene.paint.Color.WHITE);
                gc.setLineWidth(3);
                gc.strokeOval(pinScreenX - 10, pinScreenY - 10, 20, 20);
                gc.setFill(javafx.scene.paint.Color.web("#1a237e"));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                gc.fillText(apartment.getName(), pinScreenX + 15, pinScreenY + 5);
            }
        };

        // draw the map the first time
        drawMap.run();

        // make zoom in button
        Button zoomInBtn = new Button("+");
        zoomInBtn.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-min-width: 40; "
            + "-fx-min-height: 40; -fx-background-color: white; -fx-border-color: #999; "
            + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        zoomInBtn.setOnAction(e -> {
            if (currentZoom[0] < 19) {
                currentZoom[0] = currentZoom[0] + 1;
                drawMap.run();
            }
        });

        // make zoom out button
        Button zoomOutBtn = new Button("-");
        zoomOutBtn.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-min-width: 40; "
            + "-fx-min-height: 40; -fx-background-color: white; -fx-border-color: #999; "
            + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        zoomOutBtn.setOnAction(e -> {
            if (currentZoom[0] > 5) {
                currentZoom[0] = currentZoom[0] - 1;
                drawMap.run();
            }
        });

        // make reset button to go back to apartment location
        Button resetBtn = new Button("Reset");
        resetBtn.setStyle("-fx-font-size: 11; -fx-min-width: 40; -fx-min-height: 30; "
            + "-fx-background-color: white; -fx-border-color: #999; "
            + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        resetBtn.setOnAction(e -> {
            currentZoom[0] = 16;
            centerLat[0] = lat;
            centerLon[0] = lon;
            drawMap.run();
        });

        // put zoom buttons in a box on top left of map
        VBox zoomButtons = new VBox(5);
        zoomButtons.getChildren().addAll(zoomInBtn, zoomOutBtn, resetBtn);
        zoomButtons.setPadding(new Insets(10));
        zoomButtons.setMaxWidth(50);
        zoomButtons.setMaxHeight(130);

        // when user scrolls mouse wheel on canvas, zoom in or out
        canvas.setOnScroll(scrollEvent -> {
            if (scrollEvent.getDeltaY() > 0) {
                // scroll up means zoom in
                if (currentZoom[0] < 19) {
                    currentZoom[0] = currentZoom[0] + 1;
                    drawMap.run();
                }
            } else {
                // scroll down means zoom out
                if (currentZoom[0] > 5) {
                    currentZoom[0] = currentZoom[0] - 1;
                    drawMap.run();
                }
            }
        });

        // when user press mouse down, save where they started dragging
        canvas.setOnMousePressed(mouseEvent -> {
            dragStartX[0] = mouseEvent.getX();
            dragStartY[0] = mouseEvent.getY();
            dragStartLat[0] = centerLat[0];
            dragStartLon[0] = centerLon[0];
        });

        // when user let go of mouse, move the map to the new position
        canvas.setOnMouseReleased(mouseEvent -> {
            double dx = mouseEvent.getX() - dragStartX[0];
            double dy = mouseEvent.getY() - dragStartY[0];

            // only move if they dragged more than 5 pixels
            // so clicking doesnt accidentally move the map
            if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                int zoom = currentZoom[0];

                // figure out where the old center was in tile coordinates
                double oldCenterTileX = (dragStartLon[0] + 180.0) / 360.0 * Math.pow(2, zoom);
                double oldLatRad = Math.toRadians(dragStartLat[0]);
                double oldCenterTileY = (1.0 - Math.log(Math.tan(oldLatRad) + 1.0 / Math.cos(oldLatRad)) / Math.PI) / 2.0 * Math.pow(2, zoom);

                // move center by how much they dragged (opposite direction)
                double newCenterTileX = oldCenterTileX - dx / tileSize;
                double newCenterTileY = oldCenterTileY - dy / tileSize;

                // convert tile position back to longitude
                centerLon[0] = newCenterTileX / Math.pow(2, zoom) * 360.0 - 180.0;

                // convert tile position back to latitude (inverse mercator math)
                double n = Math.PI - 2.0 * Math.PI * newCenterTileY / Math.pow(2, zoom);
                centerLat[0] = Math.toDegrees(Math.atan(Math.sinh(n)));

                drawMap.run();
            }
        });

        // use a StackPane to put zoom buttons on top of the canvas
        javafx.scene.layout.StackPane mapPane = new javafx.scene.layout.StackPane();
        mapPane.getChildren().addAll(canvas, zoomButtons);
        javafx.scene.layout.StackPane.setAlignment(zoomButtons, javafx.geometry.Pos.TOP_LEFT);

        // add address label at bottom
        VBox mapBox = new VBox(10);
        mapBox.setPadding(new Insets(10));
        mapBox.setStyle("-fx-background-color: white;");

        Label addrLabel = new Label(apartment.getAddress());
        addrLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        addrLabel.setStyle("-fx-text-fill: #1a237e;");

        Label coordLabel = new Label("Coordinates: " + lat + ", " + lon);
        coordLabel.setFont(Font.font("Arial", 12));
        coordLabel.setStyle("-fx-text-fill: #666;");

        Label helpLabel = new Label("Scroll to zoom | Drag to move | Click Reset to go back");
        helpLabel.setFont(Font.font("Arial", 11));
        helpLabel.setStyle("-fx-text-fill: #999;");

        mapBox.getChildren().addAll(mapPane, addrLabel, coordLabel, helpLabel);

        javafx.scene.Scene scene = new javafx.scene.Scene(mapBox, 620, 560);
        mapStage.setScene(scene);
        mapStage.show();
    }

    /**
     * This method is showing a window with nearby places for an apartment
     * It use Queue, Deque, and PriorityQueue to filter and sort places
     * User can search by name, filter by category, and undo searches
     * Places are sorted by distance from apartment (closest first)
     * @param apartment the apartment to find nearby places for
     *
     * pre-condition: apartment should have coordinates
     * post-condition: nearby places window is opened with filter controls
     */
    private void showNearbyPlaces(Apartment apartment) {
        javafx.stage.Stage placesStage = new javafx.stage.Stage();
        placesStage.setTitle("Nearby Places - " + apartment.getName());

        double lat = apartment.getLatitude();
        double lon = apartment.getLongitude();

        // this list hold ALL places from the API
        // we use it to re-filter when user changes search or checkboxes
        java.util.ArrayList<NearbyPlace> allPlaces = new java.util.ArrayList<NearbyPlace>();

        // the search history deque saves what user searched for
        // when they click undo it goes back to previous search
        SearchHistoryDeque searchHistory = new SearchHistoryDeque();

        // main layout
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: white;");

        // header
        Label header = new Label("Nearby Places - " + apartment.getName());
        header.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        header.setStyle("-fx-text-fill: #1a237e;");

        Label addrLabel = new Label(apartment.getAddress());
        addrLabel.setFont(Font.font("Arial", 13));
        addrLabel.setStyle("-fx-text-fill: #666;");

        // search box and undo button in one row
        TextField searchField = new TextField();
        searchField.setPromptText("Search places by name... (press Enter to save search)");
        searchField.setPrefWidth(320);

        Button undoBtn = new Button("Undo Search");
        undoBtn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; "
            + "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 5 10 5 10;");

        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        searchRow.getChildren().addAll(searchField, undoBtn);

        // label that show search history info
        Label historyLabel = new Label("Search history: empty (Deque)");
        historyLabel.setFont(Font.font("Arial", 11));
        historyLabel.setStyle("-fx-text-fill: #999;");

        // category checkboxes so user can pick what types to show
        // "All" checkbox is checked by default and it check all the others
        CheckBox allCheck = new CheckBox("All");
        allCheck.setSelected(true);
        CheckBox foodCheck = new CheckBox("Food & Drinks");
        foodCheck.setSelected(true);
        CheckBox shopCheck = new CheckBox("Shops & Stores");
        shopCheck.setSelected(true);
        CheckBox serviceCheck = new CheckBox("Services");
        serviceCheck.setSelected(true);
        CheckBox leisureCheck = new CheckBox("Leisure & Parks");
        leisureCheck.setSelected(true);

        HBox checkboxRow = new HBox(15);
        checkboxRow.setAlignment(Pos.CENTER_LEFT);
        checkboxRow.getChildren().addAll(allCheck, foodCheck, shopCheck, serviceCheck, leisureCheck);

        // label showing how many results and what data structures used
        Label resultsCountLabel = new Label("");
        resultsCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        resultsCountLabel.setStyle("-fx-text-fill: #1a237e;");

        Label dsLabel = new Label("Using: Queue (filter pipeline) + PriorityQueue (sort by distance) + Deque (search history)");
        dsLabel.setFont(Font.font("Arial", 10));
        dsLabel.setStyle("-fx-text-fill: #aaa;");

        // the box where filtered results will show up
        VBox resultsBox = new VBox(4);
        resultsBox.setPadding(new Insets(5, 0, 5, 0));

        // loading label shown until API finishes
        Label loadingLabel = new Label("Loading nearby places...");
        loadingLabel.setFont(Font.font("Arial", 14));
        loadingLabel.setStyle("-fx-text-fill: #999;");
        resultsBox.getChildren().add(loadingLabel);

        // disable filter controls until data loads
        searchField.setDisable(true);
        undoBtn.setDisable(true);
        foodCheck.setDisable(true);
        shopCheck.setDisable(true);
        serviceCheck.setDisable(true);
        leisureCheck.setDisable(true);

        mainBox.getChildren().addAll(header, addrLabel, searchRow, historyLabel,
            checkboxRow, resultsCountLabel, dsLabel, resultsBox);

        ScrollPane sp = new ScrollPane(mainBox);
        sp.setFitToWidth(true);

        javafx.scene.Scene scene = new javafx.scene.Scene(sp, 650, 550);
        placesStage.setScene(scene);
        placesStage.show();

        // this runnable filters and displays places using Queue and PriorityQueue
        // step 1: put all places into PlaceFilterQueue (FIFO queue)
        // step 2: dequeue each place, check if it match the search and category
        // step 3: matching places go into PlacePriorityQueue (sorted by distance)
        // step 4: removeMin from priority queue to display closest places first
        Runnable applyFilter = new Runnable() {
            public void run() {
                resultsBox.getChildren().clear();

                String searchText = searchField.getText().toLowerCase().trim();

                // STEP 1: put all places into the filter queue (FIFO)
                PlaceFilterQueue filterQueue = new PlaceFilterQueue();
                for (int i = 0; i < allPlaces.size(); i++) {
                    filterQueue.enqueue(allPlaces.get(i));
                }

                // STEP 2: dequeue each place and check if it match the filter
                // if it match, put it in the priority queue
                PlacePriorityQueue priorityQueue = new PlacePriorityQueue();
                int queueProcessed = 0;

                while (!filterQueue.isEmpty()) {
                    NearbyPlace place = filterQueue.dequeue();
                    queueProcessed = queueProcessed + 1;

                    // check if the category checkbox is turned on
                    boolean categoryOk = false;
                    if (place.getCategory().equals("food") && foodCheck.isSelected()) {
                        categoryOk = true;
                    }
                    if (place.getCategory().equals("shop") && shopCheck.isSelected()) {
                        categoryOk = true;
                    }
                    if (place.getCategory().equals("service") && serviceCheck.isSelected()) {
                        categoryOk = true;
                    }
                    if (place.getCategory().equals("leisure") && leisureCheck.isSelected()) {
                        categoryOk = true;
                    }

                    // check if the name contain the search text
                    boolean nameOk = false;
                    if (searchText.isEmpty()) {
                        nameOk = true;
                    } else if (place.getName().toLowerCase().contains(searchText)) {
                        nameOk = true;
                    }

                    // if both category and name match, add to priority queue
                    if (categoryOk && nameOk) {
                        priorityQueue.insert(place);
                    }
                }

                // STEP 3: remove from priority queue (closest first) and show them
                int matchCount = priorityQueue.size();

                if (priorityQueue.isEmpty()) {
                    Label noResults = new Label("No places found matching your filter.");
                    noResults.setFont(Font.font("Arial", 13));
                    noResults.setStyle("-fx-text-fill: #999;");
                    resultsBox.getChildren().add(noResults);
                }

                int rowNumber = 0;
                while (!priorityQueue.isEmpty()) {
                    NearbyPlace place = priorityQueue.removeMin();
                    rowNumber = rowNumber + 1;

                    // pick a color based on category
                    String color = "#333";
                    String categoryName = "";
                    if (place.getCategory().equals("food")) {
                        color = "#f44336";
                        categoryName = "Food";
                    } else if (place.getCategory().equals("shop")) {
                        color = "#2196f3";
                        categoryName = "Shop";
                    } else if (place.getCategory().equals("service")) {
                        color = "#ff9800";
                        categoryName = "Service";
                    } else if (place.getCategory().equals("leisure")) {
                        color = "#4caf50";
                        categoryName = "Leisure";
                    }

                    int distRounded = (int) Math.round(place.getDistance());

                    // make a nice row for this place
                    HBox placeRow = new HBox(10);
                    placeRow.setAlignment(Pos.CENTER_LEFT);
                    placeRow.setPadding(new Insets(5, 10, 5, 10));

                    // alternate row colors so it look nice
                    if (rowNumber % 2 == 0) {
                        placeRow.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5;");
                    } else {
                        placeRow.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
                    }

                    // number label
                    Label numLabel = new Label(rowNumber + ".");
                    numLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    numLabel.setStyle("-fx-text-fill: #999;");
                    numLabel.setMinWidth(25);

                    // category badge with color
                    Label badge = new Label(categoryName);
                    badge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                    badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                        + "-fx-padding: 2 6 2 6; -fx-background-radius: 10;");
                    badge.setMinWidth(55);

                    // place name and type
                    Label nameLabel = new Label(place.getName() + " (" + place.getType() + ")");
                    nameLabel.setFont(Font.font("Arial", 13));
                    nameLabel.setStyle("-fx-text-fill: #333;");

                    // distance from apartment
                    Label distLabel = new Label(distRounded + "m");
                    distLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    distLabel.setStyle("-fx-text-fill: #666;");
                    distLabel.setMinWidth(50);

                    placeRow.getChildren().addAll(numLabel, badge, nameLabel, distLabel);
                    resultsBox.getChildren().add(placeRow);
                }

                resultsCountLabel.setText("Showing " + matchCount + " of " + allPlaces.size()
                    + " places (sorted by distance, closest first)");
            }
        };

        // when user press Enter in search field, save to history deque and filter
        searchField.setOnAction(e -> {
            String text = searchField.getText().trim();
            if (!text.isEmpty()) {
                searchHistory.addLast(text);
                historyLabel.setText("Searches saved: " + searchHistory.size()
                    + " | Last: \"" + searchHistory.peekLast() + "\" (Deque)");
            }
            applyFilter.run();
        });

        // also filter live when user types (but dont save to history until Enter)
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilter.run();
        });

        // undo button removes the last search from the deque
        undoBtn.setOnAction(e -> {
            if (!searchHistory.isEmpty()) {
                searchHistory.removeLast();
                // set search field to the previous search, or empty if no more history
                if (!searchHistory.isEmpty()) {
                    searchField.setText(searchHistory.peekLast());
                    historyLabel.setText("Searches saved: " + searchHistory.size()
                        + " | Last: \"" + searchHistory.peekLast() + "\" (Deque)");
                } else {
                    searchField.setText("");
                    historyLabel.setText("Search history: empty (Deque)");
                }
            }
        });

        // when user click a checkbox, re-filter the results
        // we need to add applyFilter to the existing checkbox handlers
        // when "All" checkbox is clicked, check or uncheck all the others
        allCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent event) {
                boolean isAll = allCheck.isSelected();
                foodCheck.setSelected(isAll);
                shopCheck.setSelected(isAll);
                serviceCheck.setSelected(isAll);
                leisureCheck.setSelected(isAll);
                applyFilter.run();
            }
        });

        // when food checkbox is clicked
        foodCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent event) {
                if (!foodCheck.isSelected()) {
                    allCheck.setSelected(false);
                } else if (foodCheck.isSelected() && shopCheck.isSelected() && serviceCheck.isSelected() && leisureCheck.isSelected()) {
                    allCheck.setSelected(true);
                }
                applyFilter.run();
            }
        });

        // when shop checkbox is clicked
        shopCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent event) {
                if (!shopCheck.isSelected()) {
                    allCheck.setSelected(false);
                } else if (foodCheck.isSelected() && shopCheck.isSelected() && serviceCheck.isSelected() && leisureCheck.isSelected()) {
                    allCheck.setSelected(true);
                }
                applyFilter.run();
            }
        });

        // when service checkbox is clicked
        serviceCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent event) {
                if (!serviceCheck.isSelected()) {
                    allCheck.setSelected(false);
                } else if (foodCheck.isSelected() && shopCheck.isSelected() && serviceCheck.isSelected() && leisureCheck.isSelected()) {
                    allCheck.setSelected(true);
                }
                applyFilter.run();
            }
        });

        // when leisure checkbox is clicked
        leisureCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent event) {
                if (!leisureCheck.isSelected()) {
                    allCheck.setSelected(false);
                } else if (foodCheck.isSelected() && shopCheck.isSelected() && serviceCheck.isSelected() && leisureCheck.isSelected()) {
                    allCheck.setSelected(true);
                }
                applyFilter.run();
            }
        });

        // fetch nearby places in background thread from Overpass API
        Thread fetchThread = new Thread(new Runnable() {
            public void run() {
                try {
                    // build Overpass query - use "out body" so we get lat and lon
                    String query = "[out:json][timeout:15];"
                        + "(node[\"amenity\"](around:500," + lat + "," + lon + ");"
                        + "node[\"shop\"](around:500," + lat + "," + lon + ");"
                        + "node[\"leisure\"](around:500," + lat + "," + lon + "););"
                        + "out body;";

                    String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");

                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

                    // try main server first, then backup if it fails
                    String[] servers = {
                        com.leaselens.api.ApiConfig.OVERPASS_BASE_URL,
                        com.leaselens.api.ApiConfig.OVERPASS_BACKUP_URL
                    };

                    String responseBody = null;

                    for (int s = 0; s < servers.length; s++) {
                        String url = servers[s] + "?data=" + encodedQuery;
                        System.out.println("Trying Overpass server: " + servers[s]);

                        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(url))
                            .header("User-Agent", "LeaseLens/1.0 StudentProject")
                            .build();

                        try {
                            java.net.http.HttpResponse<String> response = client.send(request,
                                java.net.http.HttpResponse.BodyHandlers.ofString());

                            String body = response.body().trim();
                            if (body.startsWith("{")) {
                                responseBody = body;
                                break; // got good JSON, stop trying
                            } else {
                                System.out.println("Overpass server returned non-JSON, trying next...");
                            }
                        } catch (Exception serverError) {
                            System.out.println("Overpass server error: " + serverError.getMessage());
                        }
                    }

                    if (responseBody == null) {
                        throw new Exception("Overpass API is busy right now. Please try again in a minute.");
                    }

                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(responseBody).getAsJsonObject();
                    com.google.gson.JsonArray elements = json.getAsJsonArray("elements");

                    // go through each element and make a NearbyPlace object
                    for (int i = 0; i < elements.size(); i++) {
                        com.google.gson.JsonObject element = elements.get(i).getAsJsonObject();
                        if (!element.has("tags")) {
                            continue;
                        }
                        com.google.gson.JsonObject tags = element.getAsJsonObject("tags");

                        // get the name of the place
                        String name = "";
                        if (tags.has("name")) {
                            name = tags.get("name").getAsString();
                        }
                        if (name.isEmpty()) {
                            continue; // skip places with no name
                        }

                        // get lat and lon of this place
                        double placeLat = 0;
                        double placeLon = 0;
                        if (element.has("lat") && element.has("lon")) {
                            placeLat = element.get("lat").getAsDouble();
                            placeLon = element.get("lon").getAsDouble();
                        }

                        // calculate how far this place is from the apartment
                        double distance = calculateDistance(lat, lon, placeLat, placeLon);

                        // figure out what type and category this place is
                        String amenity = "";
                        String shop = "";
                        String leisure = "";
                        if (tags.has("amenity")) {
                            amenity = tags.get("amenity").getAsString();
                        }
                        if (tags.has("shop")) {
                            shop = tags.get("shop").getAsString();
                        }
                        if (tags.has("leisure")) {
                            leisure = tags.get("leisure").getAsString();
                        }

                        String type = "";
                        String category = "";

                        if (amenity.equals("restaurant") || amenity.equals("cafe")
                            || amenity.equals("bar") || amenity.equals("fast_food")
                            || amenity.equals("bakery") || amenity.equals("pub")) {
                            type = amenity;
                            category = "food";
                        } else if (!shop.isEmpty()) {
                            type = shop;
                            category = "shop";
                        } else if (!leisure.isEmpty()) {
                            type = leisure;
                            category = "leisure";
                        } else if (!amenity.isEmpty()) {
                            type = amenity;
                            category = "service";
                        } else {
                            continue; // skip if we cant figure out category
                        }

                        NearbyPlace place = new NearbyPlace(name, type, category,
                            placeLat, placeLon, distance);
                        allPlaces.add(place);
                    }

                    System.out.println("Loaded " + allPlaces.size() + " nearby places");

                    // update UI on JavaFX thread - enable controls and show results
                    javafx.application.Platform.runLater(new Runnable() {
                        public void run() {
                            // turn on the filter controls now that data is loaded
                            searchField.setDisable(false);
                            undoBtn.setDisable(false);
                            foodCheck.setDisable(false);
                            shopCheck.setDisable(false);
                            serviceCheck.setDisable(false);
                            leisureCheck.setDisable(false);

                            // run the filter to show all places
                            applyFilter.run();
                        }
                    });

                } catch (Exception e) {
                    javafx.application.Platform.runLater(new Runnable() {
                        public void run() {
                            resultsBox.getChildren().clear();
                            Label errorLabel = new Label("Error loading nearby places: " + e.getMessage());
                            errorLabel.setStyle("-fx-text-fill: #c62828;");
                            resultsBox.getChildren().add(errorLabel);
                        }
                    });
                }
            }
        });
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    /**
     * This method is calculating the distance between two places on earth
     * It use the haversine formula which is math for distance on a sphere
     * @param lat1 latitude of first place
     * @param lon1 longitude of first place
     * @param lat2 latitude of second place
     * @param lon2 longitude of second place
     * @return distance in meters
     *
     * pre-condition: coordinates should be valid numbers
     * post-condition: distance in meters is returned
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000; // radius of earth in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    /**
     * This method is setting the map navigator so the "View on Map" button can work
     * @param mapTab the MapTab instance to center on
     * @param switchToMap a Runnable that switches to the map tab
     *
     * pre-condition: mapTab and switchToMap should not be null
     * post-condition: View on Map button is connected to the map tab
     */
    public void setMapNavigator(MapTab mapTab, Runnable switchToMap) {
        this.mapTab = mapTab;
        this.switchToMapTab = switchToMap;
    }

    /**
     * This method is refreshing the table with latest data
     *
     * pre-condition: none
     * post-condition: table is updated
     */
    public void refresh() {
        applyFilters();
    }

    /**
     * This method is giving back the content of this tab
     * @return the VBox content
     *
     * pre-condition: none
     * post-condition: content is returned
     */
    public VBox getContent() {
        return content;
    }
}
