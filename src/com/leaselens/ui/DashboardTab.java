package com.leaselens.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.leaselens.model.Apartment;
import com.leaselens.model.Status;
import com.leaselens.model.UserPreferences;
import com.leaselens.service.ApartmentService;
import com.leaselens.util.ScoreCalculator;
import com.leaselens.datastructures.ApartmentSorter;

/**
 * This class is the Dashboard tab - the home screen of the app
 * It show stats cards, top 3 picks, and budget overview
 *
 * pre-condition: service should not be null
 * post-condition: dashboard tab is created with all sections
 */
public class DashboardTab {

    private ApartmentService service;
    private VBox content;

    // stat labels so we can update them
    private Label totalLabel;
    private Label shortlistedLabel;
    private Label touredLabel;
    private Label rejectedLabel;
    private Label inBudgetLabel;

    // top picks section
    private VBox topPicksBox;

    // budget section
    private Label budgetInfoLabel;

    /**
     * This constructor is making the dashboard tab
     * @param service the apartment service
     *
     * pre-condition: service should not be null
     * post-condition: dashboard is built
     */
    public DashboardTab(ApartmentService service) {
        this.service = service;
        this.content = new VBox(20);
        buildTab();
    }

    /**
     * This method is building all the dashboard sections
     *
     * pre-condition: none
     * post-condition: all sections is added to content
     */
    private void buildTab() {
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f0f2f5;");

        // ---- HEADER ----
        Label header = new Label("Dashboard");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #1a237e;");

        Label subtitle = new Label("Welcome to LeaseLens - Your apartment search organizer");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-text-fill: #666666;");

        VBox headerBox = new VBox(5);
        headerBox.getChildren().addAll(header, subtitle);

        // ---- STAT CARDS ----
        HBox statsRow = buildStatsRow();

        // ---- TOP PICKS SECTION ----
        Label topPicksHeader = new Label("Top 3 Picks");
        topPicksHeader.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        topPicksHeader.setStyle("-fx-text-fill: #1a237e;");

        topPicksBox = new VBox(10);
        topPicksBox.setPadding(new Insets(15));
        topPicksBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );

        // ---- BUDGET OVERVIEW ----
        Label budgetHeader = new Label("Budget Overview");
        budgetHeader.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        budgetHeader.setStyle("-fx-text-fill: #1a237e;");

        budgetInfoLabel = new Label("");
        budgetInfoLabel.setFont(Font.font("Arial", 14));

        VBox budgetBox = new VBox(10);
        budgetBox.setPadding(new Insets(15));
        budgetBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        budgetBox.getChildren().add(budgetInfoLabel);

        // add everything to content
        content.getChildren().addAll(headerBox, statsRow, topPicksHeader, topPicksBox, budgetHeader, budgetBox);

        // load initial data
        refresh();
    }

    /**
     * This method is building the row of stat cards
     * @return HBox with all the stat cards
     *
     * pre-condition: none
     * post-condition: stat cards row is returned
     */
    private HBox buildStatsRow() {
        HBox row = new HBox(15);

        // total apartments card
        totalLabel = new Label("0");
        HBox totalCard = makeStatCard("Total Saved", totalLabel, "#2196f3");

        // shortlisted card
        shortlistedLabel = new Label("0");
        HBox shortlistedCard = makeStatCard("Shortlisted", shortlistedLabel, "#4caf50");

        // toured card
        touredLabel = new Label("0");
        HBox touredCard = makeStatCard("Toured", touredLabel, "#ff9800");

        // rejected card
        rejectedLabel = new Label("0");
        HBox rejectedCard = makeStatCard("Rejected", rejectedLabel, "#f44336");

        // in budget card
        inBudgetLabel = new Label("0");
        HBox inBudgetCard = makeStatCard("In Budget", inBudgetLabel, "#9c27b0");

        row.getChildren().addAll(totalCard, shortlistedCard, touredCard, rejectedCard, inBudgetCard);
        return row;
    }

    /**
     * This method is making one stat card with a title and number
     * @param title the title text like "Total Saved"
     * @param valueLabel the label that show the number
     * @param color the color for the top border
     * @return VBox that look like a card
     *
     * pre-condition: none
     * post-condition: a styled card is returned
     */
    private HBox makeStatCard(String title, Label valueLabel, String color) {
        // left color bar
        Region colorBar = new Region();
        colorBar.setMinWidth(5);
        colorBar.setMaxWidth(5);
        colorBar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 5 0 0 5;");

        // text part with number on top and title below
        VBox textBox = new VBox(2);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setPadding(new Insets(15, 20, 15, 15));

        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setStyle("-fx-text-fill: #888888;");

        textBox.getChildren().addAll(valueLabel, titleLabel);

        HBox card = new HBox();
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);"
        );
        HBox.setHgrow(card, Priority.ALWAYS);
        card.getChildren().addAll(colorBar, textBox);
        return card;
    }

    /**
     * This method is refreshing all dashboard data with latest numbers
     *
     * pre-condition: none
     * post-condition: all labels and sections is updated
     */
    public void refresh() {
        // update stat cards
        totalLabel.setText(String.valueOf(service.getTotalCount()));
        shortlistedLabel.setText(String.valueOf(service.getCountByStatus(Status.SHORTLISTED)));
        touredLabel.setText(String.valueOf(service.getCountByStatus(Status.TOURED)));
        rejectedLabel.setText(String.valueOf(service.getCountByStatus(Status.REJECTED)));

        // count how many apartments fit in budget
        int inBudgetCount = 0;
        double maxBudget = service.getPreferences().getMaxBudget();
        double minBudget = service.getPreferences().getMinBudget();
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            if (apt.getRent() >= minBudget && apt.getRent() <= maxBudget) {
                inBudgetCount = inBudgetCount + 1;
            }
        }
        inBudgetLabel.setText(String.valueOf(inBudgetCount));

        // update top picks
        refreshTopPicks();

        // update budget info
        refreshBudgetInfo();
    }

    /**
     * This method is refreshing the top 3 picks section
     * It calculate scores for all apartments and show best 3
     *
     * pre-condition: none
     * post-condition: top picks section is updated
     */
    private void refreshTopPicks() {
        topPicksBox.getChildren().clear();

        if (service.getTotalCount() == 0) {
            Label emptyLabel = new Label("No apartments added yet. Go to 'My Apartments' tab to add some!");
            emptyLabel.setFont(Font.font("Arial", 14));
            emptyLabel.setStyle("-fx-text-fill: #999999;");
            topPicksBox.getChildren().add(emptyLabel);
            return;
        }

        // get all apartments and calculate scores
        Apartment[] all = service.getAllApartments().toArray();
        UserPreferences prefs = service.getPreferences();

        // find max values for normalizing scores
        double maxRent = 0;
        double maxSqft = 0;
        double maxDist = 0;
        for (int i = 0; i < all.length; i++) {
            if (all[i].getRent() > maxRent) maxRent = all[i].getRent();
            if (all[i].getSqft() > maxSqft) maxSqft = all[i].getSqft();
            if (all[i].getDistanceToT() > maxDist) maxDist = all[i].getDistanceToT();
        }

        // calculate scores and sort
        double[] scores = new double[all.length];
        for (int i = 0; i < all.length; i++) {
            scores[i] = ScoreCalculator.calculateScore(all[i], prefs, maxRent, maxSqft, maxDist);
        }

        // simple bubble sort for scores (small array so its fine)
        for (int i = 0; i < all.length - 1; i++) {
            for (int j = 0; j < all.length - i - 1; j++) {
                if (scores[j] < scores[j + 1]) {
                    // swap scores
                    double tempScore = scores[j];
                    scores[j] = scores[j + 1];
                    scores[j + 1] = tempScore;
                    // swap apartments too
                    Apartment tempApt = all[j];
                    all[j] = all[j + 1];
                    all[j + 1] = tempApt;
                }
            }
        }

        // show top 3 (or less if we dont have 3)
        int count = Math.min(3, all.length);
        for (int i = 0; i < count; i++) {
            HBox pickRow = makeTopPickRow(i + 1, all[i], scores[i]);
            topPicksBox.getChildren().add(pickRow);
        }
    }

    /**
     * This method is making one row for the top picks section
     * @param rank the rank number (1, 2, or 3)
     * @param apartment the apartment
     * @param score the calculated score
     * @return HBox with apartment info
     *
     * pre-condition: apartment should not be null
     * post-condition: a styled row is returned
     */
    private HBox makeTopPickRow(int rank, Apartment apartment, double score) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 8;"
        );

        // rank badge
        Label rankLabel = new Label("#" + rank);
        rankLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        if (rank == 1) {
            rankLabel.setStyle("-fx-text-fill: #ffc107; -fx-min-width: 50;");
        } else if (rank == 2) {
            rankLabel.setStyle("-fx-text-fill: #90a4ae; -fx-min-width: 50;");
        } else {
            rankLabel.setStyle("-fx-text-fill: #cd7f32; -fx-min-width: 50;");
        }

        // apartment name and address
        VBox infoBox = new VBox(3);
        Label nameLabel = new Label(apartment.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: #333333;");

        Label addressLabel = new Label(apartment.getAddress());
        addressLabel.setFont(Font.font("Arial", 12));
        addressLabel.setStyle("-fx-text-fill: #777777;");

        infoBox.getChildren().addAll(nameLabel, addressLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // rent
        Label rentLabel = new Label("$" + String.format("%.0f", apartment.getRent()) + "/mo");
        rentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        rentLabel.setStyle("-fx-text-fill: #1a237e;");

        // check if apartment is over budget
        UserPreferences prefs = service.getPreferences();
        boolean overBudget = apartment.getRent() > prefs.getMaxBudget();

        // match badge or over budget badge
        Label matchLabel;
        if (overBudget) {
            matchLabel = new Label("Over Budget");
            matchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            matchLabel.setPadding(new Insets(5, 12, 5, 12));
            matchLabel.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-background-radius: 15;");
        } else {
            String matchText;
            String matchStyle;
            if (score >= 60) {
                matchText = "Great Match";
                matchStyle = "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 15;";
            } else if (score >= 40) {
                matchText = "Good Match";
                matchStyle = "-fx-background-color: #fff3e0; -fx-text-fill: #e65100; -fx-background-radius: 15;";
            } else {
                matchText = "OK Match";
                matchStyle = "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-background-radius: 15;";
            }
            matchLabel = new Label(matchText);
            matchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            matchLabel.setPadding(new Insets(5, 12, 5, 12));
            matchLabel.setStyle(matchStyle);
        }

        // status badge
        Label statusLabel = new Label(apartment.getStatus().toString());
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        statusLabel.setPadding(new Insets(4, 10, 4, 10));
        statusLabel.setStyle(getStatusStyle(apartment.getStatus()));

        row.getChildren().addAll(rankLabel, infoBox, rentLabel, matchLabel, statusLabel);
        return row;
    }

    /**
     * This method is giving back the style for a status badge
     * @param status the apartment status
     * @return CSS style string for the status
     *
     * pre-condition: status should not be null
     * post-condition: style string is returned
     */
    private String getStatusStyle(Status status) {
        if (status == Status.NEW) {
            return "-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-background-radius: 12;";
        } else if (status == Status.SHORTLISTED) {
            return "-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-background-radius: 12;";
        } else if (status == Status.TOURED) {
            return "-fx-background-color: #fff3e0; -fx-text-fill: #e65100; -fx-background-radius: 12;";
        } else {
            return "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-background-radius: 12;";
        }
    }

    /**
     * This method is refreshing the budget overview section
     *
     * pre-condition: none
     * post-condition: budget info label is updated
     */
    private void refreshBudgetInfo() {
        UserPreferences prefs = service.getPreferences();
        double minBudget = prefs.getMinBudget();
        double maxBudget = prefs.getMaxBudget();

        int total = service.getTotalCount();
        if (total == 0) {
            budgetInfoLabel.setText("No apartments to analyze. Add some apartments first!");
            return;
        }

        // count how many fit in budget
        int fitsInBudget = 0;
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            if (apt.getRent() >= minBudget && apt.getRent() <= maxBudget) {
                fitsInBudget++;
            }
        }

        String text = fitsInBudget + " out of " + total + " apartments fit your budget "
                     + "($" + String.format("%.0f", minBudget) + " - $" + String.format("%.0f", maxBudget) + ").\n"
                     + "Average rent: $" + String.format("%.0f", service.getAverageRent()) + "/month.\n"
                     + "Adjust your budget in the Preferences tab.";
        budgetInfoLabel.setText(text);
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
