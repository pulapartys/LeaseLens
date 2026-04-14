package com.leaselens.ui;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import com.leaselens.model.Apartment;
import com.leaselens.model.Status;
import com.leaselens.model.UserPreferences;
import com.leaselens.app.ApartmentManager;
import com.leaselens.calculators.ScoreCalculator;

/**
 * This class is the Dashboard tab - the home screen of the app
 * It show stats cards, top 3 picks using bubble sort, and budget overview
 *
 * pre-condition: service not null
 * post-condition: dashboard is created
 */
public class DashboardTab {

    private ApartmentManager service;
    private VBox content;
    private Label totalLabel;
    private Label shortlistedLabel;
    private Label touredLabel;
    private Label rejectedLabel;
    private Label inBudgetLabel;
    private VBox topPicksBox;
    private Label budgetInfoLabel;

    /**
     * This make the dashboard tab
     * @param service the apartment service
     *
     * pre-condition: service not null
     * post-condition: dashboard is built
     */
    public DashboardTab(ApartmentManager service) {
        this.service = service;
        this.content = new VBox(15);
        buildTab();
    }

    /**
     * This build all the parts of dashboard
     *
     * pre-condition: content not null
     * post-condition: dashboard is fully built
     */
    private void buildTab() {
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f0f2f5;");

        Label header = new Label("Dashboard");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setStyle("-fx-text-fill: #1a237e;");
        Label subtitle = new Label("Overview of your apartment search. Go to 'My Apartments' tab to add listings.");
        subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");

        // stat labels in a simple row
        totalLabel = new Label("0");
        shortlistedLabel = new Label("0");
        touredLabel = new Label("0");
        rejectedLabel = new Label("0");
        inBudgetLabel = new Label("0");

        HBox statsRow = new HBox(20);
        statsRow.setPadding(new Insets(10));
        statsRow.setStyle("-fx-background-color: white; -fx-border-color: #3366cc; -fx-border-width: 0 0 0 4; -fx-background-radius: 5;");
        statsRow.getChildren().addAll(
            makeStatLabel("Total:", totalLabel, "#1a237e"),
            makeStatLabel("Shortlisted:", shortlistedLabel, "#2e7d32"),
            makeStatLabel("Toured:", touredLabel, "#0277bd"),
            makeStatLabel("Rejected:", rejectedLabel, "#c62828"),
            makeStatLabel("In Budget:", inBudgetLabel, "#e65100")
        );

        // top picks section
        Label topHeader = new Label("Top 3 Picks (based on your preferences)");
        topHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        topHeader.setStyle("-fx-text-fill: #2e7d32;");
        topPicksBox = new VBox(8);
        topPicksBox.setPadding(new Insets(10));
        topPicksBox.setStyle("-fx-background-color: white; -fx-border-color: #2e7d32; -fx-border-width: 0 0 0 4; -fx-background-radius: 5;");

        // budget overview
        Label budgetHeader = new Label("Budget Overview");
        budgetHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        budgetHeader.setStyle("-fx-text-fill: #e65100;");
        budgetInfoLabel = new Label("");
        budgetInfoLabel.setFont(Font.font("Arial", 14));
        VBox budgetBox = new VBox(5);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setStyle("-fx-background-color: white; -fx-border-color: #e65100; -fx-border-width: 0 0 0 4; -fx-background-radius: 5;");
        budgetBox.getChildren().add(budgetInfoLabel);

        content.getChildren().addAll(header, subtitle, statsRow, topHeader, topPicksBox, budgetHeader, budgetBox);
        refresh();
    }

    /**
     * This make a stat label with title and value
     * @param title the stat name
     * @param valueLabel the number label
     * @return HBox with title and value
     *
     * pre-condition: none
     * post-condition: label is returned
     */
    private HBox makeStatLabel(String title, Label valueLabel, String color) {
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 13));
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    /**
     * This refresh all dashboard data
     *
     * pre-condition: none
     * post-condition: all numbers is updated
     */
    public void refresh() {
        totalLabel.setText(String.valueOf(service.getTotalCount()));
        shortlistedLabel.setText(String.valueOf(service.getCountByStatus(Status.SHORTLISTED)));
        touredLabel.setText(String.valueOf(service.getCountByStatus(Status.TOURED)));
        rejectedLabel.setText(String.valueOf(service.getCountByStatus(Status.REJECTED)));

        int inBudget = 0;
        double maxB = service.getPreferences().getMaxBudget();
        double minB = service.getPreferences().getMinBudget();
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            if (apt.getRent() >= minB && apt.getRent() <= maxB) {
                inBudget = inBudget + 1;
            }
        }
        inBudgetLabel.setText(String.valueOf(inBudget));
        refreshTopPicks();
        refreshBudgetInfo();
    }

    /**
     * This refresh the top 3 picks using bubble sort
     *
     * pre-condition: service not null
     * post-condition: top picks is updated
     */
    private void refreshTopPicks() {
        topPicksBox.getChildren().clear();
        if (service.getTotalCount() == 0) {
            topPicksBox.getChildren().add(new Label("No apartments added yet. Add some in the 'My Apartments' tab to see your top picks here."));
            return;
        }

        Apartment[] all = service.getAllApartments().toArray();
        UserPreferences prefs = service.getPreferences();

        // find max values for normalizing
        double maxRent = 0;
        double maxSqft = 0;
        double maxDist = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i].getRent() > maxRent) maxRent = all[i].getRent();
            if (all[i].getSqft() > maxSqft) maxSqft = all[i].getSqft();
            if (all[i].getDistanceToT() > maxDist) maxDist = all[i].getDistanceToT();
        }

        // calculate scores
        double[] scores = new double[all.length];
        for (int i = 0; i < all.length; i++) {
            scores[i] = ScoreCalculator.calculateScore(all[i], prefs, maxRent, maxSqft, maxDist);
        }

        // bubble sort by score highest first
        for (int i = 0; i < all.length - 1; i++) {
            for (int j = 0; j < all.length - i - 1; j++) {
                if (scores[j] < scores[j + 1]) {
                    double tempScore = scores[j];
                    scores[j] = scores[j + 1];
                    scores[j + 1] = tempScore;
                    Apartment tempApt = all[j];
                    all[j] = all[j + 1];
                    all[j + 1] = tempApt;
                }
            }
        }

        // show top 3
        int count = Math.min(3, all.length);
        for (int i = 0; i < count; i++) {
            String text = "#" + (i + 1) + "  " + all[i].getName()
                + " - $" + String.format("%.0f", all[i].getRent()) + "/mo"
                + "  (Score: " + String.format("%.0f", scores[i]) + ")";
            String[] rankColors = {"#b8860b", "#757575", "#8d6e63"};
            Label pickLabel = new Label(text);
            pickLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            pickLabel.setStyle("-fx-text-fill: " + rankColors[i] + ";");
            topPicksBox.getChildren().add(pickLabel);
        }
    }

    /**
     * This refresh the budget info section
     *
     * pre-condition: service not null
     * post-condition: budget label is updated
     */
    private void refreshBudgetInfo() {
        UserPreferences prefs = service.getPreferences();
        int total = service.getTotalCount();
        if (total == 0) {
            budgetInfoLabel.setText("No apartments to analyze.");
            return;
        }
        int fits = 0;
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            if (apt.getRent() >= prefs.getMinBudget() && apt.getRent() <= prefs.getMaxBudget()) {
                fits = fits + 1;
            }
        }
        budgetInfoLabel.setText(fits + " out of " + total + " apartments fit your budget "
            + "($" + String.format("%.0f", prefs.getMinBudget()) + " - $"
            + String.format("%.0f", prefs.getMaxBudget()) + ").\n"
            + "Average rent: $" + String.format("%.0f", service.getAverageRent()) + "/month.");
    }

    /**
     * This return the content for this tab
     * @return VBox content
     *
     * pre-condition: buildTab was called
     * post-condition: content is returned
     */
    public VBox getContent() {
        return content;
    }
}
