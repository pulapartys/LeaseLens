package com.leaselens.ui;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import com.leaselens.model.Apartment;
import com.leaselens.app.ApartmentManager;

/**
 * This is the Compare tab where user pick 2 or 3 apartments
 * and see them side by side with green and red colors
 *
 * pre-condition: service not null
 * post-condition: compare tab is created
 */
public class CompareTab {

    private ApartmentManager service;
    private VBox content;
    private ComboBox<String> combo1;
    private ComboBox<String> combo2;
    private ComboBox<String> combo3;
    private VBox resultsBox;
    private boolean isRefreshing;

    /**
     * This make the compare tab
     * @param service the apartment service
     *
     * pre-condition: service not null
     * post-condition: compare tab is built
     */
    public CompareTab(ApartmentManager service) {
        this.service = service;
        this.content = new VBox(15);
        buildTab();
    }

    /**
     * This build the compare tab layout
     *
     * pre-condition: content not null
     * post-condition: tab is built with combos and results area
     */
    private void buildTab() {
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f0f2f5;");

        Label header = new Label("Compare Apartments");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setStyle("-fx-text-fill: #1a237e;");

        combo1 = new ComboBox<String>();
        combo1.setPromptText("Pick first apartment...");
        combo2 = new ComboBox<String>();
        combo2.setPromptText("Pick second apartment...");
        combo3 = new ComboBox<String>();
        combo3.setPromptText("Optional third...");

        // when user pick something in any combo, update other combos
        // so they cant pick the same apartment twice
        combo1.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                updateComboChoices();
            }
        });
        combo2.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                updateComboChoices();
            }
        });
        combo3.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                updateComboChoices();
            }
        });

        Button compareBtn = new Button("Compare");
        compareBtn.setStyle("-fx-background-color: #3366cc; -fx-text-fill: white;");
        compareBtn.setOnAction(new javafx.event.EventHandler<javafx.event.ActionEvent>() {
            public void handle(javafx.event.ActionEvent e) {
                doCompare();
            }
        });

        HBox selectRow = new HBox(10);
        selectRow.setPadding(new Insets(10));
        selectRow.setAlignment(Pos.CENTER_LEFT);
        selectRow.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        selectRow.getChildren().addAll(
            new Label("Apt 1:"), combo1,
            new Label("Apt 2:"), combo2,
            new Label("Apt 3 (optional):"), combo3, compareBtn);

        Label hint = new Label("Pick at least 2 apartments from the dropdowns above, then click Compare. Green = best value, Red = worst.");
        hint.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        hint.setWrapText(true);

        resultsBox = new VBox(10);
        resultsBox.setPadding(new Insets(10));
        resultsBox.getChildren().add(hint);

        ScrollPane scroll = new ScrollPane(resultsBox);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        content.getChildren().addAll(header, selectRow, scroll);
        refresh();
    }

    /**
     * This do the comparison between selected apartments
     * It build a grid with green for best and red for worst
     *
     * pre-condition: at least 2 apartments selected
     * post-condition: results grid is shown
     */
    private void doCompare() {
        resultsBox.getChildren().clear();

        String n1 = combo1.getValue();
        String n2 = combo2.getValue();
        String n3 = combo3.getValue();

        if (n1 == null || n2 == null) {
            Label errLabel = new Label("Please select at least 2 apartments.");
            errLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            resultsBox.getChildren().add(errLabel);
            return;
        }

        // safety check - make sure user did not pick same apartment
        if (n1.equals(n2)) {
            Label errLabel = new Label("Apartment 1 and Apartment 2 are the same. Please pick different apartments to compare.");
            errLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            errLabel.setWrapText(true);
            resultsBox.getChildren().add(errLabel);
            return;
        }
        if (n3 != null && !n3.equals("None")) {
            if (n3.equals(n1) || n3.equals(n2)) {
                Label errLabel = new Label("Apartment 3 is the same as another selection. Please pick a different apartment or choose 'None'.");
                errLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
                errLabel.setWrapText(true);
                resultsBox.getChildren().add(errLabel);
                return;
            }
        }

        Apartment a1 = findByName(n1);
        Apartment a2 = findByName(n2);
        Apartment a3 = null;
        if (n3 != null && !n3.equals("None")) {
            a3 = findByName(n3);
        }
        if (a1 == null || a2 == null) {
            resultsBox.getChildren().add(new Label("Could not find selected apartments."));
            return;
        }

        // put apartments in array so we can loop
        Apartment[] apts;
        if (a3 != null) {
            apts = new Apartment[]{a1, a2, a3};
        } else {
            apts = new Apartment[]{a1, a2};
        }

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: white;");

        // header row
        addCell(grid, "Feature", 0, 0, true, "#f5f5f5", "#333");
        for (int i = 0; i < apts.length; i++) {
            addCell(grid, apts[i].getName(), i + 1, 0, true, "#f5f5f5", "#333");
        }

        int row = 1;
        int[] wins = {0, 0, 0};

        // rent (lower better)
        row = addRow(grid, row, "Rent", apts, wins, true, "rent");
        // sqft (higher better)
        row = addRow(grid, row, "Sqft", apts, wins, false, "sqft");
        // rent per sqft (lower better)
        row = addRow(grid, row, "Rent/Sqft", apts, wins, true, "rps");
        // bedrooms (higher better)
        row = addRow(grid, row, "Bedrooms", apts, wins, false, "beds");
        // walk score (higher better)
        row = addRow(grid, row, "Walk Score", apts, wins, false, "walk");
        // distance to T (lower better)
        row = addRow(grid, row, "Distance to T", apts, wins, true, "dist");
        // safety (higher better)
        row = addRow(grid, row, "Safety Score", apts, wins, false, "safety");
        // recreation (higher better)
        row = addRow(grid, row, "Recreation", apts, wins, false, "rec");

        // nearest T stop (no color)
        addCell(grid, "Nearest T", 0, row, true, "#f5f5f5", "#333");
        for (int i = 0; i < apts.length; i++) {
            String stop = apts[i].getNearestTStop();
            if (stop.isEmpty()) stop = "N/A";
            addCell(grid, stop, i + 1, row, false, "white", "#333");
        }
        row++;

        // amenities (higher better)
        row = addRow(grid, row, "Amenities", apts, wins, false, "amen");

        // parking (no color)
        addCell(grid, "Parking", 0, row, true, "#f5f5f5", "#333");
        for (int i = 0; i < apts.length; i++) {
            String parkText = "No";
            if (apts[i].getHasParking()) parkText = "Yes";
            addCell(grid, parkText, i + 1, row, false, "white", "#333");
        }
        row++;

        // status (no color)
        addCell(grid, "Status", 0, row, true, "#f5f5f5", "#333");
        for (int i = 0; i < apts.length; i++) {
            addCell(grid, apts[i].getStatus().toString(), i + 1, row, false, "white", "#333");
        }

        // find winner
        int bestIdx = 0;
        for (int i = 1; i < apts.length; i++) {
            if (wins[i] >= wins[bestIdx]) bestIdx = i;
        }
        String winText = "Winner: " + apts[bestIdx].getName() + "  (";
        for (int i = 0; i < apts.length; i++) {
            if (i > 0) winText = winText + ", ";
            winText = winText + apts[i].getName() + ": " + wins[i] + " wins";
        }
        winText = winText + ")";
        Label winLabel = new Label(winText);
        winLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        winLabel.setPadding(new Insets(10));
        winLabel.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 5;");

        resultsBox.getChildren().addAll(grid, winLabel);
    }

    /**
     * This add one comparison row and get value from apartment
     * It figure out which apartment is best and worst for this field
     * @param grid the grid to add to
     * @param row the current row number
     * @param label the row label
     * @param apts the apartments array
     * @param wins the win counter array
     * @param lowerBetter true if lower value is better
     * @param field which field to compare
     * @return the next row number
     *
     * pre-condition: grid and apts not null
     * post-condition: row is added with green and red colors
     */
    private int addRow(GridPane grid, int row, String label, Apartment[] apts,
            int[] wins, boolean lowerBetter, String field) {

        double[] vals = new double[apts.length];
        String[] texts = new String[apts.length];

        for (int i = 0; i < apts.length; i++) {
            if (field.equals("rent")) {
                vals[i] = apts[i].getRent();
                texts[i] = "$" + String.format("%.0f", apts[i].getRent());
            } else if (field.equals("sqft")) {
                vals[i] = apts[i].getSqft();
                texts[i] = String.valueOf((int) apts[i].getSqft());
            } else if (field.equals("rps")) {
                if (apts[i].getSqft() > 0) {
                    vals[i] = apts[i].getRent() / apts[i].getSqft();
                } else {
                    vals[i] = 0;
                }
                texts[i] = "$" + String.format("%.2f", vals[i]);
            } else if (field.equals("beds")) {
                vals[i] = apts[i].getBedrooms();
                texts[i] = String.valueOf(apts[i].getBedrooms());
            } else if (field.equals("walk")) {
                vals[i] = apts[i].getWalkScore();
                texts[i] = fmtScore(apts[i].getWalkScore());
            } else if (field.equals("dist")) {
                vals[i] = apts[i].getDistanceToT();
                texts[i] = fmtDist(apts[i].getDistanceToT());
            } else if (field.equals("safety")) {
                vals[i] = apts[i].getSafetyScore();
                texts[i] = fmtScore(apts[i].getSafetyScore());
            } else if (field.equals("rec")) {
                vals[i] = apts[i].getRecreationCount();
                texts[i] = fmtScore(apts[i].getRecreationCount());
            } else if (field.equals("amen")) {
                vals[i] = apts[i].countAmenities();
                texts[i] = apts[i].countAmenities() + " / 6";
            }
        }

        // find best and worst
        int bestIdx;
        int worstIdx;
        if (lowerBetter) {
            bestIdx = findMinIdx(vals);
            worstIdx = findMaxIdx(vals);
        } else {
            bestIdx = findMaxIdx(vals);
            worstIdx = findMinIdx(vals);
        }
        wins[bestIdx] = wins[bestIdx] + 1;

        addCell(grid, label, 0, row, true, "#f5f5f5", "#333");
        for (int i = 0; i < texts.length; i++) {
            String bg = "white";
            String fg = "#333";
            if (i == bestIdx) { bg = "#e8f5e9"; fg = "#2e7d32"; }
            else if (i == worstIdx) { bg = "#ffebee"; fg = "#c62828"; }
            addCell(grid, texts[i], i + 1, row, false, bg, fg);
        }
        return row + 1;
    }

    /**
     * This add one cell to the grid
     * @param grid the grid
     * @param text the text to show
     * @param col column number
     * @param row row number
     * @param bold if text should be bold
     * @param bg background color
     * @param fg text color
     *
     * pre-condition: grid not null
     * post-condition: cell is added
     */
    private void addCell(GridPane grid, String text, int col, int row,
            boolean bold, String bg, String fg) {
        Label l = new Label(text);
        FontWeight weight = FontWeight.NORMAL;
        if (bold) weight = FontWeight.BOLD;
        l.setFont(Font.font("Arial", weight, 13));
        l.setPadding(new Insets(8, 15, 8, 15));
        l.setMinWidth(130);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg
            + "; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        grid.add(l, col, row);
    }

    /**
     * This find the index of smallest value in array
     * @param v the array of values
     * @return index of minimum
     *
     * pre-condition: v not empty
     * post-condition: return index of min value
     */
    private int findMinIdx(double[] v) {
        int idx = 0;
        for (int i = 1; i < v.length; i++) {
            if (v[i] >= 0 && (v[i] < v[idx] || v[idx] < 0)) idx = i;
        }
        return idx;
    }

    /**
     * This find the index of biggest value in array
     * @param v the array of values
     * @return index of maximum
     *
     * pre-condition: v not empty
     * post-condition: return index of max value
     */
    private int findMaxIdx(double[] v) {
        int idx = 0;
        for (int i = 1; i < v.length; i++) {
            if (v[i] > v[idx]) idx = i;
        }
        return idx;
    }

    /**
     * This find apartment by the combo box display name
     * @param name the display string
     * @return the apartment or null
     *
     * pre-condition: name not null
     * post-condition: return apartment or null
     */
    private Apartment findByName(String name) {
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " ($" + String.format("%.0f", apt.getRent()) + ")";
            if (display.equals(name)) return apt;
        }
        return null;
    }

    /**
     * This format a score value for display
     * @param s the score
     * @return formatted string or N/A
     *
     * pre-condition: none
     * post-condition: return formatted string
     */
    private String fmtScore(int s) {
        if (s >= 0) return String.valueOf(s);
        return "N/A";
    }

    /**
     * This format a distance value for display
     * @param d the distance
     * @return formatted string or N/A
     *
     * pre-condition: none
     * post-condition: return formatted string
     */
    private String fmtDist(double d) {
        if (d >= 0) return d + " mi";
        return "N/A";
    }

    /**
     * This update the combo boxes so user cant pick same apartment twice
     * When user pick something in one combo, other combos hide that choice
     * We save what user picked, rebuild the lists, then put the picks back
     *
     * pre-condition: none
     * post-condition: combos only show apartments that is not picked in other combos
     */
    private void updateComboChoices() {
        // dont run if we are already updating or refreshing
        // because clearing items fires setOnAction which calls this again
        if (isRefreshing) {
            return;
        }

        // turn on flag so this method dont run again while we are inside it
        isRefreshing = true;

        // save what user already picked
        String pick1 = combo1.getValue();
        String pick2 = combo2.getValue();
        String pick3 = combo3.getValue();

        // get all apartment names
        java.util.ArrayList<String> allNames = new java.util.ArrayList<String>();
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " ($" + String.format("%.0f", apt.getRent()) + ")";
            allNames.add(display);
        }

        // close dropdowns first so JavaFX dont crash
        combo1.hide();
        combo2.hide();
        combo3.hide();

        // rebuild combo1 - show everything except what combo2 and combo3 picked
        combo1.getItems().clear();
        for (int i = 0; i < allNames.size(); i++) {
            String name = allNames.get(i);
            boolean taken = false;
            if (name.equals(pick2)) { taken = true; }
            if (name.equals(pick3) && !"None".equals(pick3)) { taken = true; }
            if (taken == false) {
                combo1.getItems().add(name);
            }
        }

        // rebuild combo2 - show everything except what combo1 and combo3 picked
        combo2.getItems().clear();
        for (int i = 0; i < allNames.size(); i++) {
            String name = allNames.get(i);
            boolean taken = false;
            if (name.equals(pick1)) { taken = true; }
            if (name.equals(pick3) && !"None".equals(pick3)) { taken = true; }
            if (taken == false) {
                combo2.getItems().add(name);
            }
        }

        // rebuild combo3 - show "None" plus everything except what combo1 and combo2 picked
        combo3.getItems().clear();
        combo3.getItems().add("None");
        for (int i = 0; i < allNames.size(); i++) {
            String name = allNames.get(i);
            boolean taken = false;
            if (name.equals(pick1)) { taken = true; }
            if (name.equals(pick2)) { taken = true; }
            if (taken == false) {
                combo3.getItems().add(name);
            }
        }

        // put the saved picks back
        if (pick1 != null && combo1.getItems().contains(pick1)) {
            combo1.setValue(pick1);
        }
        if (pick2 != null && combo2.getItems().contains(pick2)) {
            combo2.setValue(pick2);
        }
        if (pick3 != null && combo3.getItems().contains(pick3)) {
            combo3.setValue(pick3);
        } else {
            combo3.setValue("None");
        }

        // turn off flag so next user pick works
        isRefreshing = false;
    }

    /**
     * This refresh the combo boxes with current apartments
     * It reset all picks and show all apartments in all combos
     *
     * pre-condition: none
     * post-condition: combos is updated with all apartments
     */
    public void refresh() {
        // turn on flag so updateComboChoices does not run during refresh
        isRefreshing = true;

        // close dropdowns first so JavaFX dont crash
        combo1.hide();
        combo2.hide();
        combo3.hide();

        combo1.getItems().clear();
        combo2.getItems().clear();
        combo3.getItems().clear();
        combo3.getItems().add("None");
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " ($" + String.format("%.0f", apt.getRent()) + ")";
            combo1.getItems().add(display);
            combo2.getItems().add(display);
            combo3.getItems().add(display);
        }
        combo1.setValue(null);
        combo2.setValue(null);
        combo3.setValue("None");

        // turn off flag so user picks work again
        isRefreshing = false;
    }

    /**
     * This return the content for this tab
     * @return VBox content
     *
     * pre-condition: buildTab was called
     * post-condition: content is returned
     */
    public VBox getContent() { return content; }
}
