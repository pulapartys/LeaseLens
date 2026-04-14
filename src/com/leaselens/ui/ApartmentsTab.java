package com.leaselens.ui;

import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import com.leaselens.model.Apartment;
import com.leaselens.model.NearbyPlace;
import com.leaselens.model.Status;
import com.leaselens.app.ApartmentManager;
import com.leaselens.datastructures.ApartmentSorter;
import com.leaselens.datastructures.PlacePriorityQueue;
import com.leaselens.datastructures.PlaceFilterQueue;
import com.leaselens.datastructures.SearchHistoryDeque;
import java.util.ArrayList;

/**
 * This is the My Apartments tab - the main table view
 * It show all apartments with search, sort, filter, and price range
 *
 * pre-condition: service not null
 * post-condition: apartments tab is created
 */
public class ApartmentsTab {

    private ApartmentManager service;
    private VBox content;
    private TableView<Apartment> table;
    private ObservableList<Apartment> tableData;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> sortBy;
    private Slider minPriceSlider;
    private Slider maxPriceSlider;
    private Label priceRangeLabel;

    /**
     * This make the apartments tab
     * @param service the apartment service
     *
     * pre-condition: service not null
     * post-condition: tab is built
     */
    public ApartmentsTab(ApartmentManager service) {
        this.service = service;
        this.tableData = FXCollections.observableArrayList();
        this.content = new VBox(10);
        buildTab();
    }

    /**
     * This build the apartments tab with table and buttons
     *
     * pre-condition: content not null
     * post-condition: tab is fully built
     */
    private void buildTab() {
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #f0f2f5;");

        // header row
        Label header = new Label("My Apartments");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setStyle("-fx-text-fill: #1a237e;");
        HBox.setHgrow(header, Priority.ALWAYS);

        Button undoBtn = new Button("Undo");
        undoBtn.setStyle("-fx-background-color: #e0e0e0;");
        Button redoBtn = new Button("Redo");
        redoBtn.setStyle("-fx-background-color: #e0e0e0;");
        Button addBtn = new Button("+ Add Apartment");
        addBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.getChildren().addAll(header, undoBtn, redoBtn, addBtn);

        // search and filter
        searchField = new TextField();
        searchField.setPromptText("Search by neighborhood...");
        searchField.setFont(Font.font("Arial", 13));
        searchField.setMinWidth(250);

        statusFilter = new ComboBox<String>();
        statusFilter.getItems().addAll("All", "NEW", "SHORTLISTED", "TOURED", "REJECTED");
        statusFilter.setValue("All");

        sortBy = new ComboBox<String>();
        sortBy.getItems().addAll("Rent (Low-High)", "Bedrooms", "Walk Score", "Distance to T");
        sortBy.setValue("Rent (Low-High)");

        HBox filterRow = new HBox(10);
        filterRow.setPadding(new Insets(8));
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        filterRow.getChildren().addAll(searchField, new Label("Status:"), statusFilter,
            new Label("Sort:"), sortBy);

        // price range
        minPriceSlider = new Slider(0, 5000, 0);
        minPriceSlider.setMinWidth(150);
        minPriceSlider.setShowTickLabels(true);
        minPriceSlider.setMajorTickUnit(1000);
        maxPriceSlider = new Slider(0, 5000, 5000);
        maxPriceSlider.setMinWidth(150);
        maxPriceSlider.setShowTickLabels(true);
        maxPriceSlider.setMajorTickUnit(1000);
        priceRangeLabel = new Label("$0 - $5000");
        Button applyPriceBtn = new Button("Apply");

        HBox priceRow = new HBox(10);
        priceRow.setPadding(new Insets(5, 8, 5, 8));
        priceRow.setAlignment(Pos.CENTER_LEFT);
        priceRow.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        priceRow.getChildren().addAll(new Label("Price:"), minPriceSlider,
            new Label("to"), maxPriceSlider, priceRangeLabel, applyPriceBtn);

        // table
        table = new TableView<Apartment>();
        VBox.setVgrow(table, Priority.ALWAYS);
        buildTableColumns();
        table.setItems(tableData);
        table.setPlaceholder(new Label("No apartments added yet. Click '+ Add Apartment' to get started!"));

        // action buttons - user must select a row first
        Label actionHint = new Label("Select a row in the table, then use these buttons:");
        actionHint.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #3366cc; -fx-text-fill: white;");
        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
        Button shortlistBtn = new Button("Shortlist");
        shortlistBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        Button touredBtn = new Button("Mark Toured");
        touredBtn.setStyle("-fx-background-color: #0277bd; -fx-text-fill: white;");
        Button rejectBtn = new Button("Reject");
        rejectBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
        HBox actionRow = new HBox(8);
        actionRow.getChildren().addAll(editBtn, deleteBtn, shortlistBtn, touredBtn, rejectBtn);

        // view buttons - show extra info for the selected apartment
        Label viewHint = new Label("View details for selected apartment:");
        viewHint.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        Button viewCrimeBtn = new Button("Crime Data");
        viewCrimeBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
        Button viewTransitBtn = new Button("Transit");
        viewTransitBtn.setStyle("-fx-background-color: #0277bd; -fx-text-fill: white;");
        Button viewNearbyBtn = new Button("Nearby Places");
        viewNearbyBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
        Button viewMapBtn = new Button("View on Map");
        viewMapBtn.setStyle("-fx-background-color: #3366cc; -fx-text-fill: white;");
        Button viewParksBtn = new Button("Parks");
        viewParksBtn.setStyle("-fx-background-color: #e65100; -fx-text-fill: white;");
        HBox viewRow = new HBox(8);
        viewRow.getChildren().addAll(viewCrimeBtn, viewTransitBtn, viewNearbyBtn, viewMapBtn, viewParksBtn);

        // event handlers
        addBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                AddApartmentDialog d = new AddApartmentDialog(service);
                d.showAdd();
                if (d.isSaved()) { refresh(); }
            }
        });
        editBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                Apartment sel = getSelected();
                if (sel != null) {
                    AddApartmentDialog d = new AddApartmentDialog(service);
                    d.showEdit(sel);
                    if (d.isSaved()) { refresh(); }
                }
            }
        });
        deleteBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                Apartment sel = getSelected();
                if (sel != null) { service.removeApartment(sel.getId()); refresh(); }
            }
        });
        shortlistBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { changeStatus(Status.SHORTLISTED); }
        });
        touredBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { changeStatus(Status.TOURED); }
        });
        rejectBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { changeStatus(Status.REJECTED); }
        });
        undoBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { service.undo(); refresh(); }
        });
        redoBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { service.redo(); refresh(); }
        });
        // search filter listener
        searchField.textProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
            public void changed(javafx.beans.value.ObservableValue<? extends String> obs, String o, String n) {
                applyFilters();
            }
        });
        statusFilter.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { applyFilters(); }
        });
        sortBy.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { applyFilters(); }
        });

        // price slider listeners that keep min below max
        minPriceSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            public void changed(javafx.beans.value.ObservableValue<? extends Number> obs, Number o, Number n) {
                // if min goes above max, push max up to match
                if (minPriceSlider.getValue() > maxPriceSlider.getValue()) {
                    maxPriceSlider.setValue(minPriceSlider.getValue());
                }
                updatePriceLabel();
            }
        });
        maxPriceSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            public void changed(javafx.beans.value.ObservableValue<? extends Number> obs, Number o, Number n) {
                // if max goes below min, push min down to match
                if (maxPriceSlider.getValue() < minPriceSlider.getValue()) {
                    minPriceSlider.setValue(maxPriceSlider.getValue());
                }
                updatePriceLabel();
            }
        });
        applyPriceBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { applyFilters(); }
        });

        // view buttons with friendly messages when data is not available
        viewCrimeBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                Apartment s = getSelected();
                if (s != null) { showCrimeData(s); }
            }
        });
        viewTransitBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                Apartment s = getSelected();
                if (s != null) { showTransitData(s); }
            }
        });
        viewMapBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                Apartment s = getSelected();
                if (s != null) {
                    if (s.getLatitude() == 0 && s.getLongitude() == 0) {
                        showNoLocationAlert(s, "View on Map");
                    } else {
                        showMiniMap(s);
                    }
                }
            }
        });
        viewNearbyBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                Apartment s = getSelected();
                if (s != null) {
                    if (s.getLatitude() == 0 && s.getLongitude() == 0) {
                        showNoLocationAlert(s, "Nearby Places");
                    } else {
                        showNearbyPlaces(s);
                    }
                }
            }
        });
        viewParksBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                Apartment s = getSelected();
                if (s != null) { showRecreationData(s); }
            }
        });

        content.getChildren().addAll(headerRow, filterRow, priceRow, table,
            actionHint, actionRow, viewHint, viewRow);
        refresh();
    }

    /**
     * This get the selected apartment from table
     * @return selected apartment or null
     *
     * pre-condition: none
     * post-condition: return selected or null
     */
    private Apartment getSelected() {
        return table.getSelectionModel().getSelectedItem();
    }

    /**
     * This change the status of selected apartment
     * It show a friendly message if the status change is not allowed
     * @param status the new status
     *
     * pre-condition: none
     * post-condition: status is changed if apartment selected and allowed
     */
    private void changeStatus(Status status) {
        Apartment sel = getSelected();
        if (sel == null) {
            return;
        }

        // try to change status
        boolean changed = service.changeStatus(sel.getId(), status);
        if (changed == true) {
            refresh();
        } else {
            // status change was not allowed, show friendly message
            showStatusAlert(sel, status);
        }
    }

    /**
     * This show a popup when a status change is not allowed
     * It tell the user what transitions they can make from current status
     * @param apt the apartment
     * @param triedStatus the status user tried to change to
     *
     * pre-condition: apt not null
     * post-condition: popup is shown
     */
    private void showStatusAlert(Apartment apt, Status triedStatus) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Status Change Not Allowed");

        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #fff3e0;");

        Label headerLbl = new Label("Cannot Change Status");
        headerLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        headerLbl.setStyle("-fx-text-fill: #e65100;");

        Label msgLbl = new Label("You tried to change \"" + apt.getName()
            + "\" from " + apt.getStatus() + " to " + triedStatus + ".\n\n"
            + "This transition is not allowed.\n\n"
            + service.getAllowedStatusMessage(apt.getStatus()));
        msgLbl.setWrapText(true);
        msgLbl.setFont(Font.font("Arial", 13));

        Button okBtn = new Button("OK, Got It");
        okBtn.setStyle("-fx-background-color: #e65100; -fx-text-fill: white; -fx-font-weight: bold;");
        okBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                stage.close();
            }
        });

        box.getChildren().addAll(headerLbl, msgLbl, okBtn);
        stage.setScene(new javafx.scene.Scene(box, 420, 220));
        stage.show();
    }

    /**
     * This show a popup when location data is not available
     * It tell user the address could not be found and what to do
     * @param apt the apartment
     * @param feature the feature that need location like "Map" or "Nearby Places"
     *
     * pre-condition: apt not null
     * post-condition: popup is shown
     */
    private void showNoLocationAlert(Apartment apt, String feature) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Location Not Available");

        VBox box = new VBox(12);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #fce4ec;");

        Label headerLbl = new Label("Location Data Unavailable");
        headerLbl.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        headerLbl.setStyle("-fx-text-fill: #c62828;");

        Label msgLbl = new Label("Cannot show " + feature + " for \"" + apt.getName()
            + "\" because the address could not be found on the map.\n\n"
            + "This can happen if:\n"
            + "  - The street address has a typo\n"
            + "  - The address is too new to be in the map database\n"
            + "  - The geocoding service was temporarily unavailable\n\n"
            + "Try editing the apartment to fix the address, and the system will try to find it again.");
        msgLbl.setWrapText(true);
        msgLbl.setFont(Font.font("Arial", 13));

        Button okBtn = new Button("OK");
        okBtn.setStyle("-fx-background-color: #c62828; -fx-text-fill: white; -fx-font-weight: bold;");
        okBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                stage.close();
            }
        });

        box.getChildren().addAll(headerLbl, msgLbl, okBtn);
        stage.setScene(new javafx.scene.Scene(box, 450, 280));
        stage.show();
    }

    /**
     * This update the price range label text
     *
     * pre-condition: sliders exist
     * post-condition: label is updated
     */
    private void updatePriceLabel() {
        priceRangeLabel.setText("$" + String.format("%.0f", minPriceSlider.getValue())
            + " - $" + String.format("%.0f", maxPriceSlider.getValue()));
    }

    /**
     * This build all the table columns
     *
     * pre-condition: table not null
     * post-condition: columns is added to table
     */
    private void buildTableColumns() {
        TableColumn<Apartment, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(150);

        TableColumn<Apartment, String> addrCol = new TableColumn<>("Address");
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addrCol.setMinWidth(200);

        TableColumn<Apartment, Double> rentCol = new TableColumn<>("Rent");
        rentCol.setCellValueFactory(new PropertyValueFactory<>("rent"));
        rentCol.setMinWidth(80);

        TableColumn<Apartment, Double> sqftCol = new TableColumn<>("Sqft");
        sqftCol.setCellValueFactory(new PropertyValueFactory<>("sqft"));
        sqftCol.setMinWidth(60);

        TableColumn<Apartment, Integer> bedCol = new TableColumn<>("Beds");
        bedCol.setCellValueFactory(new PropertyValueFactory<>("bedrooms"));
        bedCol.setMinWidth(50);

        TableColumn<Apartment, Integer> bathCol = new TableColumn<>("Baths");
        bathCol.setCellValueFactory(new PropertyValueFactory<>("bathrooms"));
        bathCol.setMinWidth(50);

        TableColumn<Apartment, Integer> walkCol = new TableColumn<>("Walk");
        walkCol.setCellValueFactory(new PropertyValueFactory<>("walkScore"));
        walkCol.setMinWidth(60);

        TableColumn<Apartment, Integer> safetyCol = new TableColumn<>("Safety");
        safetyCol.setCellValueFactory(new PropertyValueFactory<>("safetyScore"));
        safetyCol.setMinWidth(60);

        TableColumn<Apartment, Integer> parksCol = new TableColumn<>("Parks");
        parksCol.setCellValueFactory(new PropertyValueFactory<>("recreationCount"));
        parksCol.setMinWidth(50);

        TableColumn<Apartment, Double> distCol = new TableColumn<>("Dist to T");
        distCol.setCellValueFactory(new PropertyValueFactory<>("distanceToT"));
        distCol.setMinWidth(70);

        TableColumn<Apartment, Status> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setMinWidth(90);
        statusCol.setCellFactory(new javafx.util.Callback<TableColumn<Apartment, Status>, TableCell<Apartment, Status>>() {
            public TableCell<Apartment, Status> call(TableColumn<Apartment, Status> col) {
                return new TableCell<Apartment, Status>() {
                    protected void updateItem(Status item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setText(null); setStyle(""); return; }
                        setText(item.toString());
                        if (item == Status.SHORTLISTED) setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                        else if (item == Status.TOURED) setStyle("-fx-text-fill: #0277bd; -fx-font-weight: bold;");
                        else if (item == Status.REJECTED) setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                        else setStyle("-fx-text-fill: #e65100;");
                    }
                };
            }
        });

        TableColumn<Apartment, String> sourceCol = new TableColumn<>("Source");
        sourceCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceCol.setMinWidth(80);

        TableColumn<Apartment, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setMinWidth(120);

        table.getColumns().addAll(nameCol, addrCol, rentCol, sqftCol, bedCol, bathCol,
            walkCol, safetyCol, parksCol, distCol, statusCol, sourceCol, notesCol);
    }

    /**
     * This apply search, filter, sort, and price range to table
     *
     * pre-condition: none
     * post-condition: table data is updated
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
        } else if (sortValue.equals("Bedrooms")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byBedroomsHighToLow());
        } else if (sortValue.equals("Walk Score")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byWalkScoreHighToLow());
        } else if (sortValue.equals("Distance to T")) {
            ApartmentSorter.mergeSort(sortArray, ApartmentSorter.byDistanceToTLowToHigh());
        }

        // add to table
        for (int i = 0; i < sortArray.length; i++) {
            tableData.add(sortArray[i]);
        }
    }

    // ---- POPUP: CRIME DATA ----

    /**
     * This show crime data popup for an apartment
     * @param apt the apartment
     *
     * pre-condition: apt not null
     * post-condition: crime popup is shown
     */
    private void showCrimeData(Apartment apt) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Crime Data - " + apt.getName());

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f0f2f5;");

        Label crimeHeader = new Label("Crime Data - " + apt.getName());
        crimeHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        crimeHeader.setStyle("-fx-text-fill: #1a237e;");
        Label addrLabel = new Label(apt.getAddress());
        addrLabel.setStyle("-fx-text-fill: #666;");
        box.getChildren().addAll(crimeHeader, addrLabel, new Separator());

        Label safetyTitle = new Label("Safety Score");
        safetyTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        safetyTitle.setStyle("-fx-text-fill: #1a237e;");
        String safetyText = "N/A";
        if (apt.getSafetyScore() >= 0) safetyText = apt.getSafetyScore() + " / 100";
        box.getChildren().addAll(safetyTitle, new Label(safetyText));

        Label countTitle = new Label("Crimes Reported Nearby (last 30 days)");
        countTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        countTitle.setStyle("-fx-text-fill: #1a237e;");
        String countText = "N/A";
        if (apt.getCrimeCount() >= 0) countText = String.valueOf(apt.getCrimeCount());
        box.getChildren().addAll(countTitle, new Label("Total: " + countText));

        String breakdown = apt.getCrimeBreakdown();
        if (breakdown != null && !breakdown.isEmpty()) {
            Label offTitle = new Label("Top Offenses");
            offTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            offTitle.setStyle("-fx-text-fill: #1a237e;");
            box.getChildren().add(offTitle);
            String[] offenses = breakdown.split(",");
            for (int i = 0; i < offenses.length; i++) {
                String offense = offenses[i].trim();
                if (!offense.isEmpty()) {
                    box.getChildren().add(new Label("  " + (i + 1) + ".  " + offense));
                }
            }
        } else {
            box.getChildren().add(new Label("No crime data available."));
        }

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #e0e0e0;");
        closeBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                stage.close();
            }
        });
        box.getChildren().add(closeBtn);

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        stage.setScene(new javafx.scene.Scene(sp, 500, 500));
        stage.show();
    }

    // ---- POPUP: TRANSIT DATA ----

    /**
     * This show transit data popup for an apartment
     * @param apt the apartment
     *
     * pre-condition: apt not null
     * post-condition: transit popup is shown
     */
    private void showTransitData(Apartment apt) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Transit Info - " + apt.getName());

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f0f2f5;");

        Label transitHeader = new Label("Transit Info - " + apt.getName());
        transitHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        transitHeader.setStyle("-fx-text-fill: #1a237e;");
        Label addrLabel2 = new Label(apt.getAddress());
        addrLabel2.setStyle("-fx-text-fill: #666;");
        box.getChildren().addAll(transitHeader, addrLabel2, new Separator());

        // subway stops
        Label trainLbl = new Label("Nearby Train Stations");
        trainLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        trainLbl.setStyle("-fx-text-fill: #1a237e;");
        box.getChildren().add(trainLbl);
        addTransitStops(box, apt, true);

        // bus stops
        Label busLbl = new Label("Nearby Bus Stops");
        busLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        busLbl.setStyle("-fx-text-fill: #1a237e;");
        box.getChildren().add(busLbl);
        addTransitStops(box, apt, false);

        // scores
        Label scoresLbl = new Label("Scores");
        scoresLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        scoresLbl.setStyle("-fx-text-fill: #1a237e;");
        String wk = "N/A"; if (apt.getWalkScore() >= 0) wk = String.valueOf(apt.getWalkScore());
        String tr = "N/A"; if (apt.getTransitScore() >= 0) tr = String.valueOf(apt.getTransitScore());
        String bk = "N/A"; if (apt.getBikeScore() >= 0) bk = String.valueOf(apt.getBikeScore());
        box.getChildren().addAll(scoresLbl, new Label("Walk: " + wk + "  |  Transit: " + tr + "  |  Bike: " + bk));

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #e0e0e0;");
        closeBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                stage.close();
            }
        });
        box.getChildren().add(closeBtn);

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #f0f2f5;");
        stage.setScene(new javafx.scene.Scene(sp, 450, 450));
        stage.show();
    }

    /**
     * This add transit stops to the box
     * @param box the VBox to add to
     * @param apt the apartment
     * @param isSubway true for subway, false for bus
     *
     * pre-condition: box and apt not null
     * post-condition: stops is added to box
     */
    private void addTransitStops(VBox box, Apartment apt, boolean isSubway) {
        if (apt.getLatitude() != 0 && apt.getLongitude() != 0) {
            String[] stops;
            if (isSubway) {
                stops = service.getMbtaService().findNearbySubway(apt.getLatitude(), apt.getLongitude());
            } else {
                stops = service.getMbtaService().findNearbyBus(apt.getLatitude(), apt.getLongitude());
            }
            if (stops.length == 0) {
                String type = "train stations";
                if (!isSubway) type = "bus stops";
                box.getChildren().add(new Label("  No " + type + " nearby"));
            } else {
                for (int i = 0; i < stops.length; i++) {
                    box.getChildren().add(new Label("  " + stops[i]));
                }
            }
        } else {
            box.getChildren().add(new Label("  No location data available"));
        }
    }

    // ---- POPUP: RECREATION DATA ----

    /**
     * This show recreation data popup for an apartment
     * @param apt the apartment
     *
     * pre-condition: apt not null
     * post-condition: recreation popup is shown
     */
    private void showRecreationData(Apartment apt) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Parks - " + apt.getName());

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f0f2f5;");

        Label parksHeader = new Label("Nearby Parks - " + apt.getName());
        parksHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        parksHeader.setStyle("-fx-text-fill: #1a237e;");
        box.getChildren().addAll(parksHeader, new Separator());

        Label countTitle = new Label("Recreation Areas Found");
        countTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        countTitle.setStyle("-fx-text-fill: #1a237e;");
        String countText = "N/A";
        if (apt.getRecreationCount() >= 0) countText = String.valueOf(apt.getRecreationCount());
        box.getChildren().addAll(countTitle, new Label("Total: " + countText));

        Label listTitle = new Label("Parks & Recreation");
        listTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        listTitle.setStyle("-fx-text-fill: #1a237e;");
        box.getChildren().add(listTitle);

        String recNames = apt.getNearbyRecreation();
        if (recNames != null && !recNames.isEmpty()) {
            String[] parts = recNames.split(",");
            for (int i = 0; i < parts.length; i++) {
                String name = parts[i].trim();
                if (!name.isEmpty()) {
                    box.getChildren().add(new Label("  " + (i + 1) + ".  " + name));
                }
            }
        } else {
            box.getChildren().add(new Label("No recreation data available."));
        }

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #e0e0e0;");
        closeBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                stage.close();
            }
        });
        box.getChildren().add(closeBtn);

        ScrollPane sp = new ScrollPane(box);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #f0f2f5;");
        stage.setScene(new javafx.scene.Scene(sp, 450, 400));
        stage.show();
    }

    // ---- POPUP: MINI MAP (kept same - uses OSM tile API) ----

    /**
     * This show a mini map popup with the apartment location
     * It use OpenStreetMap tiles on a Canvas with zoom and drag
     * @param apt the apartment
     *
     * pre-condition: apt has valid coordinates
     * post-condition: map popup is shown
     */
    private void showMiniMap(Apartment apt) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Map - " + apt.getName());

        double lat = apt.getLatitude();
        double lon = apt.getLongitude();
        int tileSize = 256;
        int[] currentZoom = {16};
        double[] cLat = {lat};
        double[] cLon = {lon};

        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(600, 450);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // this draw the map tiles and apartment pin
        Runnable drawMap = new Runnable() {
            public void run() {
                int zoom = currentZoom[0];
                gc.setFill(javafx.scene.paint.Color.web("#b8d4e8"));
                gc.fillRect(0, 0, 600, 450);

                double centerTX = (cLon[0] + 180.0) / 360.0 * Math.pow(2, zoom);
                double cLatRad = Math.toRadians(cLat[0]);
                double centerTY = (1.0 - Math.log(Math.tan(cLatRad) + 1.0 / Math.cos(cLatRad)) / Math.PI) / 2.0 * Math.pow(2, zoom);

                int startTX = (int) Math.floor(centerTX - 300.0 / tileSize);
                int startTY = (int) Math.floor(centerTY - 225.0 / tileSize);
                double startPX = (startTX - centerTX + 300.0 / tileSize) * tileSize;
                double startPY = (startTY - centerTY + 225.0 / tileSize) * tileSize;
                int across = (int) Math.ceil(600.0 / tileSize) + 2;
                int down = (int) Math.ceil(450.0 / tileSize) + 2;
                int maxTile = (int) Math.pow(2, zoom);

                double pinTX = (lon + 180.0) / 360.0 * Math.pow(2, zoom);
                double pinLatRad = Math.toRadians(lat);
                double pinTY = (1.0 - Math.log(Math.tan(pinLatRad) + 1.0 / Math.cos(pinLatRad)) / Math.PI) / 2.0 * Math.pow(2, zoom);
                double pinX = (pinTX - centerTX) * tileSize + 300;
                double pinY = (pinTY - centerTY) * tileSize + 225;

                for (int tx = 0; tx < across; tx++) {
                    for (int ty = 0; ty < down; ty++) {
                        int tX = ((startTX + tx) % maxTile + maxTile) % maxTile;
                        int tY = startTY + ty;
                        if (tY < 0 || tY >= maxTile) continue;

                        double px = startPX + tx * tileSize;
                        double py = startPY + ty * tileSize;
                        String url = "https://tile.openstreetmap.org/" + zoom + "/" + tX + "/" + tY + ".png";
                        javafx.scene.image.Image tile = new javafx.scene.image.Image(url, tileSize, tileSize, false, false, true);

                        final double fpx = px;
                        final double fpy = py;
                        final double savedPinX = pinX;
                        final double savedPinY = pinY;

                        tile.progressProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
                            public void changed(javafx.beans.value.ObservableValue<? extends Number> obs, Number ov, Number nv) {
                                if (nv.doubleValue() >= 1.0 && !tile.isError()) {
                                    javafx.application.Platform.runLater(new Runnable() {
                                        public void run() {
                                            gc.drawImage(tile, fpx, fpy, tileSize, tileSize);
                                            drawPin(gc, savedPinX, savedPinY, apt.getName());
                                        }
                                    });
                                }
                            }
                        });
                        if (tile.getProgress() >= 1.0 && !tile.isError()) {
                            gc.drawImage(tile, px, py, tileSize, tileSize);
                        }
                    }
                }
                drawPin(gc, pinX, pinY, apt.getName());
            }
        };

        drawMap.run();

        Button zoomIn = new Button("+");
        zoomIn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                if (currentZoom[0] < 19) { currentZoom[0]++; drawMap.run(); }
            }
        });
        Button zoomOut = new Button("-");
        zoomOut.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                if (currentZoom[0] > 5) { currentZoom[0]--; drawMap.run(); }
            }
        });
        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                currentZoom[0] = 16; cLat[0] = lat; cLon[0] = lon; drawMap.run();
            }
        });

        VBox zoomBtns = new VBox(3);
        zoomBtns.getChildren().addAll(zoomIn, zoomOut, resetBtn);
        zoomBtns.setPadding(new Insets(5));

        javafx.scene.layout.StackPane mapPane = new javafx.scene.layout.StackPane();
        mapPane.getChildren().addAll(canvas, zoomBtns);
        javafx.scene.layout.StackPane.setAlignment(zoomBtns, javafx.geometry.Pos.TOP_LEFT);

        Label helpLabel = new Label("Use + / - to zoom | Click Reset to go back");
        VBox mapBox = new VBox(5);
        mapBox.setPadding(new Insets(5));
        mapBox.getChildren().addAll(mapPane, new Label(apt.getAddress()), helpLabel);

        stage.setScene(new javafx.scene.Scene(mapBox, 620, 520));
        stage.show();
    }

    /**
     * This draw a pin on the map canvas
     * @param gc the graphics context
     * @param x the x position
     * @param y the y position
     * @param name the apartment name
     *
     * pre-condition: gc not null
     * post-condition: pin is drawn
     */
    private void drawPin(javafx.scene.canvas.GraphicsContext gc, double x, double y, String name) {
        gc.setFill(javafx.scene.paint.Color.web("#f44336"));
        gc.fillOval(x - 10, y - 10, 20, 20);
        gc.setStroke(javafx.scene.paint.Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeOval(x - 10, y - 10, 20, 20);
        gc.setFill(javafx.scene.paint.Color.web("#1a237e"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText(name, x + 15, y + 5);
    }

    // ---- POPUP: NEARBY PLACES (uses Overpass API + data structures) ----

    /**
     * This show nearby places popup using Overpass API
     * It use Queue for filtering, PriorityQueue for sorting, Deque for search history
     * @param apt the apartment
     *
     * pre-condition: apt has valid coordinates
     * post-condition: nearby places popup is shown
     */
    private void showNearbyPlaces(Apartment apt) {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        stage.setTitle("Nearby Places - " + apt.getName());

        double lat = apt.getLatitude();
        double lon = apt.getLongitude();
        java.util.ArrayList<NearbyPlace> allPlaces = new java.util.ArrayList<NearbyPlace>();
        SearchHistoryDeque searchHistory = new SearchHistoryDeque();

        VBox mainBox = new VBox(12);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #f0f2f5;");

        String cardStyle = "-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12;";

        Label nearbyHeader = new Label("Nearby Places");
        nearbyHeader.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        nearbyHeader.setStyle("-fx-text-fill: #1a237e;");
        Label nearbyAddr = new Label(apt.getName() + "  —  " + apt.getAddress());
        nearbyAddr.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        nearbyAddr.setWrapText(true);

        // search and undo card
        TextField placeSearch = new TextField();
        placeSearch.setPromptText("Type a place name and press Enter to search...");
        Button undoBtn = new Button("Undo Search");
        undoBtn.setStyle("-fx-background-color: #e0e0e0;");
        HBox searchRow = new HBox(8);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(placeSearch, Priority.ALWAYS);
        searchRow.getChildren().addAll(placeSearch, undoBtn);

        Label historyLabel = new Label("Search history: empty (Deque)");
        historyLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");

        VBox searchCard = new VBox(8);
        searchCard.setStyle(cardStyle);
        searchCard.getChildren().addAll(searchRow, historyLabel);

        // category checkboxes card
        CheckBox allCheck = new CheckBox("All"); allCheck.setSelected(true);
        CheckBox foodCheck = new CheckBox("Food"); foodCheck.setSelected(true);
        CheckBox shopCheck = new CheckBox("Shops"); shopCheck.setSelected(true);
        CheckBox serviceCheck = new CheckBox("Services"); serviceCheck.setSelected(true);
        CheckBox leisureCheck = new CheckBox("Leisure"); leisureCheck.setSelected(true);

        HBox checkRow = new HBox(15);
        checkRow.setAlignment(Pos.CENTER_LEFT);
        checkRow.getChildren().addAll(allCheck, foodCheck, shopCheck, serviceCheck, leisureCheck);

        Label filterHint = new Label("Filter by category or search by name. Results sorted by distance (closest first).");
        filterHint.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
        filterHint.setWrapText(true);

        VBox filterCard = new VBox(8);
        filterCard.setStyle(cardStyle);
        filterCard.getChildren().addAll(checkRow, filterHint);

        Label resultsCountLabel = new Label("");
        resultsCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        resultsCountLabel.setStyle("-fx-text-fill: #333;");
        Label dsLabel = new Label("Data Structures: Queue (filter) + PriorityQueue (sort by distance) + Deque (search history)");
        dsLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10;");
        dsLabel.setWrapText(true);

        VBox resultsBox = new VBox(6);
        Label loadingLabel = new Label("Loading nearby places from Overpass API... Please wait.");
        loadingLabel.setStyle("-fx-text-fill: #0277bd; -fx-font-size: 13;");
        resultsBox.getChildren().add(loadingLabel);

        placeSearch.setDisable(true);
        undoBtn.setDisable(true);

        mainBox.getChildren().addAll(nearbyHeader, nearbyAddr, searchCard, filterCard, resultsCountLabel, dsLabel, resultsBox);

        ScrollPane sp = new ScrollPane(mainBox);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: #f0f2f5;");
        stage.setScene(new javafx.scene.Scene(sp, 600, 550));
        stage.show();

        // filter using Queue and PriorityQueue
        Runnable applyFilter = new Runnable() {
            public void run() {
                resultsBox.getChildren().clear();
                String search = placeSearch.getText().toLowerCase().trim();

                // step 1: put all places into filter queue
                PlaceFilterQueue filterQueue = new PlaceFilterQueue();
                for (int i = 0; i < allPlaces.size(); i++) {
                    filterQueue.enqueue(allPlaces.get(i));
                }

                // step 2: dequeue each, check if it match, put in priority queue
                PlacePriorityQueue pq = new PlacePriorityQueue();
                while (!filterQueue.isEmpty()) {
                    NearbyPlace place = filterQueue.dequeue();
                    boolean catOk = false;
                    if (place.getCategory().equals("food") && foodCheck.isSelected()) catOk = true;
                    if (place.getCategory().equals("shop") && shopCheck.isSelected()) catOk = true;
                    if (place.getCategory().equals("service") && serviceCheck.isSelected()) catOk = true;
                    if (place.getCategory().equals("leisure") && leisureCheck.isSelected()) catOk = true;

                    boolean nameOk = search.isEmpty() || place.getName().toLowerCase().contains(search);
                    if (catOk && nameOk) pq.insert(place);
                }

                // step 3: removeMin from priority queue (closest first)
                // then group by category for display
                int matchCount = pq.size();
                if (pq.isEmpty()) {
                    resultsBox.getChildren().add(new Label("No places found."));
                }

                ArrayList<NearbyPlace> foodList = new ArrayList<NearbyPlace>();
                ArrayList<NearbyPlace> shopList = new ArrayList<NearbyPlace>();
                ArrayList<NearbyPlace> serviceList = new ArrayList<NearbyPlace>();
                ArrayList<NearbyPlace> leisureList = new ArrayList<NearbyPlace>();

                while (!pq.isEmpty()) {
                    NearbyPlace p = pq.removeMin();
                    if (p.getCategory().equals("food")) foodList.add(p);
                    else if (p.getCategory().equals("shop")) shopList.add(p);
                    else if (p.getCategory().equals("service")) serviceList.add(p);
                    else leisureList.add(p);
                }

                // show each category with heading
                addCategorySection(resultsBox, "Food", foodList);
                addCategorySection(resultsBox, "Shops", shopList);
                addCategorySection(resultsBox, "Services", serviceList);
                addCategorySection(resultsBox, "Leisure", leisureList);

                resultsCountLabel.setText("Showing " + matchCount + " of " + allPlaces.size() + " places");
            }
        };

        // search handlers
        placeSearch.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                String text = placeSearch.getText().trim();
                if (!text.isEmpty()) {
                    searchHistory.addLast(text);
                    historyLabel.setText("Searches: " + searchHistory.size() + " | Last: \"" + searchHistory.peekLast() + "\" (Deque)");
                }
                applyFilter.run();
            }
        });
        placeSearch.textProperty().addListener(new javafx.beans.value.ChangeListener<String>() {
            public void changed(javafx.beans.value.ObservableValue<? extends String> obs, String o, String n) {
                applyFilter.run();
            }
        });

        undoBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                if (!searchHistory.isEmpty()) {
                    searchHistory.removeLast();
                    if (!searchHistory.isEmpty()) {
                        placeSearch.setText(searchHistory.peekLast());
                        historyLabel.setText("Searches: " + searchHistory.size() + " | Last: \"" + searchHistory.peekLast() + "\" (Deque)");
                    } else {
                        placeSearch.setText("");
                        historyLabel.setText("Search history: empty (Deque)");
                    }
                }
            }
        });

        allCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                boolean on = allCheck.isSelected();
                foodCheck.setSelected(on); shopCheck.setSelected(on);
                serviceCheck.setSelected(on); leisureCheck.setSelected(on);
                applyFilter.run();
            }
        });
        foodCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { applyFilter.run(); }
        });
        shopCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { applyFilter.run(); }
        });
        serviceCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { applyFilter.run(); }
        });
        leisureCheck.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) { applyFilter.run(); }
        });

        // fetch nearby places from Overpass API in background
        Thread fetchThread = new Thread(new Runnable() {
            public void run() {
                try {
                    String query = "[out:json][timeout:15];"
                        + "(node[\"amenity\"](around:500," + lat + "," + lon + ");"
                        + "node[\"shop\"](around:500," + lat + "," + lon + ");"
                        + "node[\"leisure\"](around:500," + lat + "," + lon + "););"
                        + "out body;";
                    String encoded = java.net.URLEncoder.encode(query, "UTF-8");

                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                    String[] servers = { com.leaselens.api.ApiConfig.OVERPASS_BASE_URL, com.leaselens.api.ApiConfig.OVERPASS_BACKUP_URL };
                    String responseBody = null;

                    for (int s = 0; s < servers.length; s++) {
                        String url = servers[s] + "?data=" + encoded;
                        System.out.println("Trying Overpass server: " + servers[s]);
                        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(url))
                            .header("User-Agent", "LeaseLens/1.0 StudentProject")
                            .build();
                        try {
                            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                            String body = resp.body().trim();
                            if (body.startsWith("{")) { responseBody = body; break; }
                            else System.out.println("Overpass returned non-JSON, trying next...");
                        } catch (Exception err) { System.out.println("Overpass error: " + err.getMessage()); }
                    }

                    if (responseBody == null) throw new Exception("Overpass API is busy.");

                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(responseBody).getAsJsonObject();
                    com.google.gson.JsonArray elements = json.getAsJsonArray("elements");

                    for (int i = 0; i < elements.size(); i++) {
                        com.google.gson.JsonObject el = elements.get(i).getAsJsonObject();
                        if (!el.has("tags")) continue;
                        com.google.gson.JsonObject tags = el.getAsJsonObject("tags");

                        String name = "";
                        if (tags.has("name")) name = tags.get("name").getAsString();
                        if (name.isEmpty()) continue;

                        double pLat = 0;
                        double pLon = 0;
                        if (el.has("lat")) pLat = el.get("lat").getAsDouble();
                        if (el.has("lon")) pLon = el.get("lon").getAsDouble();
                        double dist = calculateDistance(lat, lon, pLat, pLon);

                        String amenity = "";
                        String shop = "";
                        String leisure = "";
                        if (tags.has("amenity")) amenity = tags.get("amenity").getAsString();
                        if (tags.has("shop")) shop = tags.get("shop").getAsString();
                        if (tags.has("leisure")) leisure = tags.get("leisure").getAsString();

                        String type = "";
                        String category = "";
                        if (amenity.equals("restaurant") || amenity.equals("cafe") || amenity.equals("bar")
                            || amenity.equals("fast_food") || amenity.equals("bakery") || amenity.equals("pub")) {
                            type = amenity; category = "food";
                        } else if (!shop.isEmpty()) { type = shop; category = "shop"; }
                        else if (!leisure.isEmpty()) { type = leisure; category = "leisure"; }
                        else if (!amenity.isEmpty()) { type = amenity; category = "service"; }
                        else continue;

                        allPlaces.add(new NearbyPlace(name, type, category, pLat, pLon, dist));
                    }
                    System.out.println("Loaded " + allPlaces.size() + " nearby places");

                    javafx.application.Platform.runLater(new Runnable() {
                        public void run() {
                            placeSearch.setDisable(false);
                            undoBtn.setDisable(false);
                            applyFilter.run();
                        }
                    });
                } catch (Exception e) {
                    final String errorMsg = e.getMessage();
                    javafx.application.Platform.runLater(new Runnable() {
                        public void run() {
                            resultsBox.getChildren().clear();
                            resultsBox.getChildren().add(new Label("Error: " + errorMsg));
                        }
                    });
                }
            }
        });
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    /**
     * This add a category section with heading and places to the box
     * @param box the VBox to add to
     * @param title the heading text
     * @param places the list of places in this category
     *
     * pre-condition: box not null
     * post-condition: heading and places is added if list not empty
     */
    private void addCategorySection(VBox box, String title, ArrayList<NearbyPlace> places) {
        if (places.isEmpty()) return;
        Label heading = new Label(title);
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        heading.setStyle("-fx-text-fill: #1a237e;");
        heading.setPadding(new Insets(8, 0, 2, 0));
        box.getChildren().add(heading);
        for (int i = 0; i < places.size(); i++) {
            NearbyPlace p = places.get(i);
            box.getChildren().add(new Label("  " + (i + 1) + ".  " + p.getName() + " - " + (int) Math.round(p.getDistance()) + "m"));
        }
    }

    /**
     * This calculate distance between two coordinates in meters
     * @param lat1 first latitude
     * @param lon1 first longitude
     * @param lat2 second latitude
     * @param lon2 second longitude
     * @return distance in meters
     *
     * pre-condition: valid coordinates
     * post-condition: return distance
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    /**
     * This refresh the table data
     *
     * pre-condition: none
     * post-condition: table is refreshed
     */
    public void refresh() { applyFilters(); }

    /**
     * This return the content for this tab
     * @return VBox content
     *
     * pre-condition: buildTab was called
     * post-condition: content is returned
     */
    public VBox getContent() { return content; }
}
