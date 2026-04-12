package com.leaselens.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.leaselens.model.Apartment;
import com.leaselens.service.ApartmentService;

/**
 * This class is the Compare tab where user can pick 2 or 3 apartments
 * and see them side by side with green and red colors showing best and worst
 *
 * pre-condition: service should not be null
 * post-condition: compare tab is created
 */
public class CompareTab {

    private ApartmentService service;
    private VBox content;

    private ComboBox<String> apartment1Combo;
    private ComboBox<String> apartment2Combo;
    private ComboBox<String> apartment3Combo;
    private VBox resultsBox;

    /**
     * This constructor is making the compare tab
     * @param service the apartment service
     *
     * pre-condition: service should not be null
     * post-condition: tab is built
     */
    public CompareTab(ApartmentService service) {
        this.service = service;
        this.content = new VBox(20);
        buildTab();
    }

    /**
     * This method is building the compare tab UI
     *
     * pre-condition: none
     * post-condition: all parts is created
     */
    private void buildTab() {
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f0f2f5;");

        // header
        Label header = new Label("Compare Apartments");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #1a237e;");

        Label subtitle = new Label("Select 2 or 3 apartments to compare side by side");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-text-fill: #666;");

        // selection row
        HBox selectRow = new HBox(15);
        selectRow.setAlignment(Pos.CENTER_LEFT);
        selectRow.setPadding(new Insets(15));
        selectRow.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        Label apt1Label = new Label("Apartment 1:");
        apt1Label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        apartment1Combo = new ComboBox<String>();
        apartment1Combo.setMinWidth(200);

        Label apt2Label = new Label("Apartment 2:");
        apt2Label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        apartment2Combo = new ComboBox<String>();
        apartment2Combo.setMinWidth(200);

        Label apt3Label = new Label("Apartment 3 (optional):");
        apt3Label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        apartment3Combo = new ComboBox<String>();
        apartment3Combo.setMinWidth(200);

        Button compareButton = new Button("Compare");
        compareButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        compareButton.setPadding(new Insets(10, 25, 10, 25));
        compareButton.setStyle(
            "-fx-background-color: #1a237e; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        selectRow.getChildren().addAll(apt1Label, apartment1Combo, apt2Label, apartment2Combo,
                                        apt3Label, apartment3Combo, compareButton);

        // results area
        resultsBox = new VBox(0);
        VBox.setVgrow(resultsBox, Priority.ALWAYS);

        // placeholder message
        Label placeholderLabel = new Label("Select apartments above and click Compare to see results");
        placeholderLabel.setFont(Font.font("Arial", 15));
        placeholderLabel.setStyle("-fx-text-fill: #999;");
        placeholderLabel.setPadding(new Insets(50));
        resultsBox.getChildren().add(placeholderLabel);
        resultsBox.setAlignment(Pos.CENTER);

        // compare button action
        compareButton.setOnAction(e -> doCompare());

        ScrollPane scrollPane = new ScrollPane(resultsBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f2f5;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        content.getChildren().addAll(header, subtitle, selectRow, scrollPane);

        refresh();
    }

    /**
     * This method is doing the comparison and showing results
     *
     * pre-condition: at least 2 apartments should be selected
     * post-condition: comparison table is shown
     */
    private void doCompare() {
        resultsBox.getChildren().clear();
        resultsBox.setAlignment(Pos.TOP_LEFT);

        String name1 = apartment1Combo.getValue();
        String name2 = apartment2Combo.getValue();
        String name3 = apartment3Combo.getValue();

        if (name1 == null || name2 == null) {
            Label errorLabel = new Label("Please select at least 2 apartments to compare.");
            errorLabel.setFont(Font.font("Arial", 14));
            errorLabel.setStyle("-fx-text-fill: #c62828;");
            resultsBox.getChildren().add(errorLabel);
            return;
        }

        // find the apartments by searching
        Apartment apt1 = findByName(name1);
        Apartment apt2 = findByName(name2);
        Apartment apt3 = null;
        if (name3 != null && !name3.equals("None")) {
            apt3 = findByName(name3);
        }

        if (apt1 == null || apt2 == null) {
            Label errorLabel = new Label("Could not find selected apartments.");
            errorLabel.setFont(Font.font("Arial", 14));
            errorLabel.setStyle("-fx-text-fill: #c62828;");
            resultsBox.getChildren().add(errorLabel);
            return;
        }

        // build comparison grid
        GridPane grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setPadding(new Insets(15));
        grid.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        int colCount = apt3 != null ? 4 : 3;

        // header row
        addGridCell(grid, "Feature", 0, 0, true, "#f5f5f5", "#333");
        addGridCell(grid, apt1.getName(), 1, 0, true, "#e3f2fd", "#1565c0");
        addGridCell(grid, apt2.getName(), 2, 0, true, "#e8f5e9", "#2e7d32");
        if (apt3 != null) {
            addGridCell(grid, apt3.getName(), 3, 0, true, "#fff3e0", "#e65100");
        }

        // data rows
        int row = 1;

        // rent
        double[] rents = apt3 != null ?
            new double[]{apt1.getRent(), apt2.getRent(), apt3.getRent()} :
            new double[]{apt1.getRent(), apt2.getRent()};
        int bestRent = findMinIndex(rents);
        int worstRent = findMaxIndex(rents);

        addGridCell(grid, "Rent", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, "$" + String.format("%.0f", apt1.getRent()), 1, row, 0, bestRent, worstRent);
        addCompareCell(grid, "$" + String.format("%.0f", apt2.getRent()), 2, row, 1, bestRent, worstRent);
        if (apt3 != null) addCompareCell(grid, "$" + String.format("%.0f", apt3.getRent()), 3, row, 2, bestRent, worstRent);
        row++;

        // sqft (higher is better)
        double[] sqfts = apt3 != null ?
            new double[]{apt1.getSqft(), apt2.getSqft(), apt3.getSqft()} :
            new double[]{apt1.getSqft(), apt2.getSqft()};
        int bestSqft = findMaxIndex(sqfts);
        int worstSqft = findMinIndex(sqfts);

        addGridCell(grid, "Sqft", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, String.valueOf(apt1.getSqft()), 1, row, 0, bestSqft, worstSqft);
        addCompareCell(grid, String.valueOf(apt2.getSqft()), 2, row, 1, bestSqft, worstSqft);
        if (apt3 != null) addCompareCell(grid, String.valueOf(apt3.getSqft()), 3, row, 2, bestSqft, worstSqft);
        row++;

        // rent per sqft (lower is better)
        double rps1 = apt1.getSqft() > 0 ? apt1.getRent() / apt1.getSqft() : 0;
        double rps2 = apt2.getSqft() > 0 ? apt2.getRent() / apt2.getSqft() : 0;
        double rps3 = apt3 != null && apt3.getSqft() > 0 ? apt3.getRent() / apt3.getSqft() : 0;
        double[] rps = apt3 != null ? new double[]{rps1, rps2, rps3} : new double[]{rps1, rps2};
        int bestRps = findMinIndex(rps);
        int worstRps = findMaxIndex(rps);

        addGridCell(grid, "Rent/Sqft", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, "$" + String.format("%.2f", rps1), 1, row, 0, bestRps, worstRps);
        addCompareCell(grid, "$" + String.format("%.2f", rps2), 2, row, 1, bestRps, worstRps);
        if (apt3 != null) addCompareCell(grid, "$" + String.format("%.2f", rps3), 3, row, 2, bestRps, worstRps);
        row++;

        // bedrooms (higher is better)
        double[] beds = apt3 != null ?
            new double[]{apt1.getBedrooms(), apt2.getBedrooms(), apt3.getBedrooms()} :
            new double[]{apt1.getBedrooms(), apt2.getBedrooms()};
        int bestBeds = findMaxIndex(beds);
        int worstBeds = findMinIndex(beds);

        addGridCell(grid, "Bedrooms", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, String.valueOf(apt1.getBedrooms()), 1, row, 0, bestBeds, worstBeds);
        addCompareCell(grid, String.valueOf(apt2.getBedrooms()), 2, row, 1, bestBeds, worstBeds);
        if (apt3 != null) addCompareCell(grid, String.valueOf(apt3.getBedrooms()), 3, row, 2, bestBeds, worstBeds);
        row++;

        // walk score (higher is better)
        double[] walks = apt3 != null ?
            new double[]{apt1.getWalkScore(), apt2.getWalkScore(), apt3.getWalkScore()} :
            new double[]{apt1.getWalkScore(), apt2.getWalkScore()};
        int bestWalk = findMaxIndex(walks);
        int worstWalk = findMinIndex(walks);

        addGridCell(grid, "Walk Score", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, formatScore(apt1.getWalkScore()), 1, row, 0, bestWalk, worstWalk);
        addCompareCell(grid, formatScore(apt2.getWalkScore()), 2, row, 1, bestWalk, worstWalk);
        if (apt3 != null) addCompareCell(grid, formatScore(apt3.getWalkScore()), 3, row, 2, bestWalk, worstWalk);
        row++;

        // distance to T (lower is better)
        double[] dists = apt3 != null ?
            new double[]{apt1.getDistanceToT(), apt2.getDistanceToT(), apt3.getDistanceToT()} :
            new double[]{apt1.getDistanceToT(), apt2.getDistanceToT()};
        int bestDist = findMinIndex(dists);
        int worstDist = findMaxIndex(dists);

        addGridCell(grid, "Distance to T", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, formatDist(apt1.getDistanceToT()), 1, row, 0, bestDist, worstDist);
        addCompareCell(grid, formatDist(apt2.getDistanceToT()), 2, row, 1, bestDist, worstDist);
        if (apt3 != null) addCompareCell(grid, formatDist(apt3.getDistanceToT()), 3, row, 2, bestDist, worstDist);
        row++;

        // safety score (higher is better)
        double[] safeties = apt3 != null ?
            new double[]{apt1.getSafetyScore(), apt2.getSafetyScore(), apt3.getSafetyScore()} :
            new double[]{apt1.getSafetyScore(), apt2.getSafetyScore()};
        int bestSafety = findMaxIndex(safeties);
        int worstSafety = findMinIndex(safeties);

        addGridCell(grid, "Safety Score", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, formatScore(apt1.getSafetyScore()), 1, row, 0, bestSafety, worstSafety);
        addCompareCell(grid, formatScore(apt2.getSafetyScore()), 2, row, 1, bestSafety, worstSafety);
        if (apt3 != null) addCompareCell(grid, formatScore(apt3.getSafetyScore()), 3, row, 2, bestSafety, worstSafety);
        row++;

        // recreation areas (higher is better)
        double[] recs = apt3 != null ?
            new double[]{apt1.getRecreationCount(), apt2.getRecreationCount(), apt3.getRecreationCount()} :
            new double[]{apt1.getRecreationCount(), apt2.getRecreationCount()};
        int bestRec = findMaxIndex(recs);
        int worstRec = findMinIndex(recs);

        addGridCell(grid, "Recreation Areas", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, formatScore(apt1.getRecreationCount()), 1, row, 0, bestRec, worstRec);
        addCompareCell(grid, formatScore(apt2.getRecreationCount()), 2, row, 1, bestRec, worstRec);
        if (apt3 != null) addCompareCell(grid, formatScore(apt3.getRecreationCount()), 3, row, 2, bestRec, worstRec);
        row++;

        // nearest T stop (no color)
        addGridCell(grid, "Nearest T", 0, row, true, "#f5f5f5", "#333");
        addGridCell(grid, apt1.getNearestTStop().isEmpty() ? "N/A" : apt1.getNearestTStop(), 1, row, false, "white", "#333");
        addGridCell(grid, apt2.getNearestTStop().isEmpty() ? "N/A" : apt2.getNearestTStop(), 2, row, false, "white", "#333");
        if (apt3 != null) addGridCell(grid, apt3.getNearestTStop().isEmpty() ? "N/A" : apt3.getNearestTStop(), 3, row, false, "white", "#333");
        row++;

        // amenities count (higher is better)
        double[] amenities = apt3 != null ?
            new double[]{apt1.countAmenities(), apt2.countAmenities(), apt3.countAmenities()} :
            new double[]{apt1.countAmenities(), apt2.countAmenities()};
        int bestAm = findMaxIndex(amenities);
        int worstAm = findMinIndex(amenities);

        addGridCell(grid, "Amenities", 0, row, true, "#f5f5f5", "#333");
        addCompareCell(grid, apt1.countAmenities() + " / 6", 1, row, 0, bestAm, worstAm);
        addCompareCell(grid, apt2.countAmenities() + " / 6", 2, row, 1, bestAm, worstAm);
        if (apt3 != null) addCompareCell(grid, apt3.countAmenities() + " / 6", 3, row, 2, bestAm, worstAm);
        row++;

        // parking (no color)
        addGridCell(grid, "Parking", 0, row, true, "#f5f5f5", "#333");
        addGridCell(grid, apt1.getHasParking() ? "Yes" : "No", 1, row, false, "white", "#333");
        addGridCell(grid, apt2.getHasParking() ? "Yes" : "No", 2, row, false, "white", "#333");
        if (apt3 != null) addGridCell(grid, apt3.getHasParking() ? "Yes" : "No", 3, row, false, "white", "#333");
        row++;

        // status
        addGridCell(grid, "Status", 0, row, true, "#f5f5f5", "#333");
        addGridCell(grid, apt1.getStatus().toString(), 1, row, false, "white", "#333");
        addGridCell(grid, apt2.getStatus().toString(), 2, row, false, "white", "#333");
        if (apt3 != null) addGridCell(grid, apt3.getStatus().toString(), 3, row, false, "white", "#333");
        row++;

        // winner summary
        int wins1 = 0, wins2 = 0, wins3 = 0;
        // count who won the most categories from bestRent, bestSqft, bestRps, bestBeds, bestWalk, bestDist, bestAm
        int[] bests = {bestRent, bestSqft, bestRps, bestBeds, bestWalk, bestDist, bestSafety, bestRec, bestAm};
        for (int b : bests) {
            if (b == 0) wins1++;
            if (b == 1) wins2++;
            if (b == 2) wins3++;
        }

        String winner = "";
        if (wins1 >= wins2 && wins1 >= wins3) winner = apt1.getName();
        else if (wins2 >= wins1 && wins2 >= wins3) winner = apt2.getName();
        else if (apt3 != null) winner = apt3.getName();

        Label winnerLabel = new Label("Winner: " + winner + "  (" +
            apt1.getName() + ": " + wins1 + " wins, " +
            apt2.getName() + ": " + wins2 + " wins" +
            (apt3 != null ? ", " + apt3.getName() + ": " + wins3 + " wins" : "") + ")");
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        winnerLabel.setStyle("-fx-text-fill: #1a237e;");
        winnerLabel.setPadding(new Insets(15, 0, 0, 0));

        resultsBox.getChildren().addAll(grid, winnerLabel);
    }

    /**
     * This method is adding a cell to the comparison grid
     * @param grid the grid pane
     * @param text the text to show
     * @param col column number
     * @param row row number
     * @param bold if text should be bold
     * @param bgColor background color
     * @param textColor text color
     *
     * pre-condition: grid should not be null
     * post-condition: cell is added to grid
     */
    private void addGridCell(GridPane grid, String text, int col, int row, boolean bold, String bgColor, String textColor) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, 14));
        label.setPadding(new Insets(12, 20, 12, 20));
        label.setMinWidth(160);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setStyle(
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: " + textColor + "; " +
            "-fx-border-color: #eee; " +
            "-fx-border-width: 0 0 1 0;"
        );
        grid.add(label, col, row);
    }

    /**
     * This method is adding a colored comparison cell (green=best, red=worst)
     * @param grid the grid
     * @param text the value text
     * @param col column
     * @param row row
     * @param myIndex which apartment this is (0, 1, or 2)
     * @param bestIndex which index is best
     * @param worstIndex which index is worst
     *
     * pre-condition: grid should not be null
     * post-condition: colored cell is added
     */
    private void addCompareCell(GridPane grid, String text, int col, int row, int myIndex, int bestIndex, int worstIndex) {
        String bgColor = "white";
        String textColor = "#333";

        if (myIndex == bestIndex) {
            bgColor = "#e8f5e9";
            textColor = "#2e7d32";
        } else if (myIndex == worstIndex) {
            bgColor = "#ffebee";
            textColor = "#c62828";
        }

        addGridCell(grid, text, col, row, false, bgColor, textColor);
    }

    /**
     * This method find the index of minimum value in array
     * @param values the array of doubles
     * @return index of the smallest value
     *
     * pre-condition: array should not be empty
     * post-condition: index is returned
     */
    private int findMinIndex(double[] values) {
        int minIdx = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] >= 0 && (values[i] < values[minIdx] || values[minIdx] < 0)) {
                minIdx = i;
            }
        }
        return minIdx;
    }

    /**
     * This method find the index of maximum value in array
     * @param values the array of doubles
     * @return index of the biggest value
     *
     * pre-condition: array should not be empty
     * post-condition: index is returned
     */
    private int findMaxIndex(double[] values) {
        int maxIdx = 0;
        for (int i = 1; i < values.length; i++) {
            if (values[i] > values[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    /**
     * This method is finding an apartment by its display name
     * @param name the name shown in combo box
     * @return the apartment or null
     *
     * pre-condition: name should not be null
     * post-condition: apartment is returned or null
     */
    private Apartment findByName(String name) {
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " ($" + String.format("%.0f", apt.getRent()) + ")";
            if (display.equals(name)) {
                return apt;
            }
        }
        return null;
    }

    private String formatScore(int score) {
        return score >= 0 ? String.valueOf(score) : "N/A";
    }

    private String formatDist(double dist) {
        return dist >= 0 ? dist + " mi" : "N/A";
    }

    /**
     * This method is refreshing the combo boxes with latest apartment list
     *
     * pre-condition: none
     * post-condition: combo boxes is updated
     */
    public void refresh() {
        apartment1Combo.getItems().clear();
        apartment2Combo.getItems().clear();
        apartment3Combo.getItems().clear();
        apartment3Combo.getItems().add("None");

        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " ($" + String.format("%.0f", apt.getRent()) + ")";
            apartment1Combo.getItems().add(display);
            apartment2Combo.getItems().add(display);
            apartment3Combo.getItems().add(display);
        }

        apartment3Combo.setValue("None");
    }

    public VBox getContent() {
        return content;
    }
}
