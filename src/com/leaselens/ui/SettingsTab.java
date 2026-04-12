package com.leaselens.ui;

import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import com.leaselens.model.UserPreferences;
import com.leaselens.service.ApartmentService;

/**
 * This is the Preferences tab for budget and priority settings
 * Also have save and load buttons for data
 *
 * pre-condition: service not null
 * post-condition: preferences tab is created
 */
public class SettingsTab {

    private ApartmentService service;
    private VBox content;
    private TextField minBudgetField;
    private TextField maxBudgetField;
    private ComboBox<String> rentPriority;
    private ComboBox<String> sqftPriority;
    private ComboBox<String> nearTPriority;
    private ComboBox<String> walkScorePriority;
    private ComboBox<String> amenitiesPriority;
    private ComboBox<String> safetyPriority;
    private Label statusLabel;

    /**
     * This make the settings tab
     * @param service the apartment service
     *
     * pre-condition: service not null
     * post-condition: settings tab is built
     */
    public SettingsTab(ApartmentService service) {
        this.service = service;
        this.content = new VBox(15);
        buildTab();
    }

    /**
     * This build the settings tab layout
     *
     * pre-condition: content not null
     * post-condition: tab is built with all sections
     */
    private void buildTab() {
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f0f2f5;");

        Label header = new Label("Preferences");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setStyle("-fx-text-fill: #1a237e;");
        Label subtitle = new Label("Set your budget and priorities to help rank apartments on the Dashboard.");
        subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");
        subtitle.setWrapText(true);

        // budget section
        Label budgetHeader = new Label("Budget Range");
        budgetHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        budgetHeader.setStyle("-fx-text-fill: #e65100;");
        Label budgetHint = new Label("Enter your minimum and maximum monthly rent budget in dollars.");
        budgetHint.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");

        UserPreferences prefs = service.getPreferences();
        minBudgetField = new TextField(String.format("%.0f", prefs.getMinBudget()));
        minBudgetField.setMaxWidth(100);
        minBudgetField.setPromptText("e.g. 1500");
        maxBudgetField = new TextField(String.format("%.0f", prefs.getMaxBudget()));
        maxBudgetField.setMaxWidth(100);
        maxBudgetField.setPromptText("e.g. 3000");

        HBox budgetBox = new HBox(15);
        budgetBox.setPadding(new Insets(10));
        budgetBox.setAlignment(Pos.CENTER_LEFT);
        budgetBox.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        budgetBox.getChildren().addAll(new Label("Min Budget: $"), minBudgetField,
            new Label("Max Budget: $"), maxBudgetField);

        // priority section
        Label weightsHeader = new Label("What Matters Most To You?");
        weightsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        weightsHeader.setStyle("-fx-text-fill: #3366cc;");
        Label weightsHint = new Label("Set each factor to High, Medium, Low, or Not Important. These affect the Top 3 Picks ranking.");
        weightsHint.setStyle("-fx-text-fill: #888; -fx-font-size: 12;");
        weightsHint.setWrapText(true);

        rentPriority = makePriorityCombo();
        sqftPriority = makePriorityCombo();
        nearTPriority = makePriorityCombo();
        walkScorePriority = makePriorityCombo();
        amenitiesPriority = makePriorityCombo();
        safetyPriority = makePriorityCombo();

        rentPriority.setValue(weightToPriority(prefs.getWeightRent()));
        sqftPriority.setValue(weightToPriority(prefs.getWeightSqft()));
        nearTPriority.setValue(weightToPriority(prefs.getWeightNearT()));
        walkScorePriority.setValue(weightToPriority(prefs.getWeightWalkScore()));
        amenitiesPriority.setValue(weightToPriority(prefs.getWeightAmenities()));
        safetyPriority.setValue(weightToPriority(prefs.getWeightSafety()));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        grid.add(new Label("Low Rent"), 0, 0); grid.add(rentPriority, 1, 0);
        grid.add(new Label("Space (Sqft)"), 0, 1); grid.add(sqftPriority, 1, 1);
        grid.add(new Label("Near T Stop"), 0, 2); grid.add(nearTPriority, 1, 2);
        grid.add(new Label("Walk Score"), 0, 3); grid.add(walkScorePriority, 1, 3);
        grid.add(new Label("Amenities"), 0, 4); grid.add(amenitiesPriority, 1, 4);
        grid.add(new Label("Safety"), 0, 5); grid.add(safetyPriority, 1, 5);

        // buttons
        Button applyBtn = new Button("Apply Preferences");
        applyBtn.setStyle("-fx-background-color: #3366cc; -fx-text-fill: white;");
        applyBtn.setOnAction(e -> applyPreferences());
        statusLabel = new Label("");

        HBox applyRow = new HBox(10);
        applyRow.getChildren().addAll(applyBtn, statusLabel);

        content.getChildren().addAll(header, subtitle,
            budgetHeader, budgetHint, budgetBox,
            weightsHeader, weightsHint, grid, applyRow);
    }

    /**
     * This make a priority combo box
     * @return ComboBox with High Medium Low options
     *
     * pre-condition: none
     * post-condition: combo is returned
     */
    private ComboBox<String> makePriorityCombo() {
        ComboBox<String> c = new ComboBox<String>();
        c.getItems().addAll("High", "Medium", "Low", "Not Important");
        c.setValue("Medium");
        c.setMinWidth(140);
        return c;
    }

    /**
     * This convert a weight number to priority string
     * @param w the weight value
     * @return the priority string
     *
     * pre-condition: none
     * post-condition: return priority string
     */
    private String weightToPriority(double w) {
        if (w < 0.01) return "Not Important";
        if (w < 0.12) return "Low";
        if (w < 0.25) return "Medium";
        return "High";
    }

    /**
     * This convert a priority string to a number
     * @param p the priority string
     * @return the number value
     *
     * pre-condition: p is valid priority
     * post-condition: return number
     */
    private double priorityToNumber(String p) {
        if (p.equals("High")) return 3.0;
        if (p.equals("Medium")) return 2.0;
        if (p.equals("Low")) return 1.0;
        return 0.0;
    }

    /**
     * This apply the user preferences
     *
     * pre-condition: none
     * post-condition: preferences is saved to service
     */
    private void applyPreferences() {
        UserPreferences prefs = service.getPreferences();
        try {
            prefs.setMinBudget(Double.parseDouble(minBudgetField.getText().trim()));
            prefs.setMaxBudget(Double.parseDouble(maxBudgetField.getText().trim()));
        } catch (Exception e) {
            statusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            statusLabel.setText("Error: Budget must be a number (e.g. 1500).");
            return;
        }

        double r = priorityToNumber(rentPriority.getValue());
        double s = priorityToNumber(sqftPriority.getValue());
        double t = priorityToNumber(nearTPriority.getValue());
        double w = priorityToNumber(walkScorePriority.getValue());
        double a = priorityToNumber(amenitiesPriority.getValue());
        double sf = priorityToNumber(safetyPriority.getValue());
        double total = r + s + t + w + a + sf;

        if (total <= 0) {
            statusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            statusLabel.setText("Error: At least one factor must not be 'Not Important'.");
            return;
        }

        prefs.setWeightRent(r / total);
        prefs.setWeightSqft(s / total);
        prefs.setWeightNearT(t / total);
        prefs.setWeightWalkScore(w / total);
        prefs.setWeightAmenities(a / total);
        prefs.setWeightSafety(sf / total);
        service.setPreferences(prefs);
        statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        statusLabel.setText("Preferences applied!");
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
