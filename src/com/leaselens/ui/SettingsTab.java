package com.leaselens.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import com.leaselens.model.UserPreferences;
import com.leaselens.service.ApartmentService;
import com.leaselens.service.DataPersistenceService;

/**
 * This class is the Preferences tab where user can set budget and priority
 * User pick High Medium Low or Not Important for each factor
 * Also have save and load buttons for data persistence
 *
 * pre-condition: service should not be null
 * post-condition: preferences tab is created
 */
public class SettingsTab {

    private ApartmentService service;
    private DataPersistenceService dataService;
    private VBox content;

    // budget fields
    private TextField minBudgetField;
    private TextField maxBudgetField;

    // priority dropdowns for each factor
    private ComboBox<String> rentPriority;
    private ComboBox<String> sqftPriority;
    private ComboBox<String> nearTPriority;
    private ComboBox<String> walkScorePriority;
    private ComboBox<String> amenitiesPriority;
    private ComboBox<String> safetyPriority;

    // status label for feedback
    private Label statusLabel;

    /**
     * This constructor is making the preferences tab
     * @param service the apartment service
     * @param dataService the data persistence service
     *
     * pre-condition: both services should not be null
     * post-condition: tab is built
     */
    public SettingsTab(ApartmentService service, DataPersistenceService dataService) {
        this.service = service;
        this.dataService = dataService;
        this.content = new VBox(20);
        buildTab();
    }

    /**
     * This method is building the preferences tab UI
     *
     * pre-condition: none
     * post-condition: all controls is created
     */
    private void buildTab() {
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f0f2f5;");

        // header
        Label header = new Label("Preferences");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #1a237e;");

        Label subtitle = new Label("Set your budget and adjust how apartments are ranked in Top Picks");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-text-fill: #666;");

        // ---- BUDGET SECTION ----
        Label budgetHeader = new Label("Budget Range");
        budgetHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        budgetHeader.setStyle("-fx-text-fill: #1a237e;");

        HBox budgetBox = new HBox(20);
        budgetBox.setAlignment(Pos.CENTER_LEFT);
        budgetBox.setPadding(new Insets(20));
        budgetBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        UserPreferences prefs = service.getPreferences();

        Label minLabel = new Label("Min Budget: $");
        minLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        minBudgetField = new TextField(String.format("%.0f", prefs.getMinBudget()));
        minBudgetField.setFont(Font.font("Arial", 14));
        minBudgetField.setMaxWidth(100);
        minBudgetField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 8;");

        Label maxLabel = new Label("Max Budget: $");
        maxLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        maxBudgetField = new TextField(String.format("%.0f", prefs.getMaxBudget()));
        maxBudgetField.setFont(Font.font("Arial", 14));
        maxBudgetField.setMaxWidth(100);
        maxBudgetField.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 8;");

        budgetBox.getChildren().addAll(minLabel, minBudgetField, maxLabel, maxBudgetField);

        // ---- RANKING PRIORITIES SECTION ----
        Label weightsHeader = new Label("What Matters Most To You?");
        weightsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        weightsHeader.setStyle("-fx-text-fill: #1a237e;");

        Label weightsSubtitle = new Label("Pick how important each factor is — this decides your Top Picks on the Dashboard");
        weightsSubtitle.setFont(Font.font("Arial", 13));
        weightsSubtitle.setStyle("-fx-text-fill: #666;");

        VBox weightsBox = new VBox(5);
        weightsBox.setPadding(new Insets(20));
        weightsBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        // make the dropdowns
        rentPriority = makePriorityCombo();
        sqftPriority = makePriorityCombo();
        nearTPriority = makePriorityCombo();
        walkScorePriority = makePriorityCombo();
        amenitiesPriority = makePriorityCombo();
        safetyPriority = makePriorityCombo();

        // set them based on saved weights
        rentPriority.setValue(weightToPriority(prefs.getWeightRent()));
        sqftPriority.setValue(weightToPriority(prefs.getWeightSqft()));
        nearTPriority.setValue(weightToPriority(prefs.getWeightNearT()));
        walkScorePriority.setValue(weightToPriority(prefs.getWeightWalkScore()));
        amenitiesPriority.setValue(weightToPriority(prefs.getWeightAmenities()));
        safetyPriority.setValue(weightToPriority(prefs.getWeightSafety()));

        // make a grid with name, description, and dropdown
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);

        // add each row
        addPriorityRow(grid, 0, "Low Rent", "Prefer cheaper apartments", rentPriority);
        addPriorityRow(grid, 1, "Space (Sqft)", "Prefer bigger apartments", sqftPriority);
        addPriorityRow(grid, 2, "Near T Stop", "Close to MBTA transit", nearTPriority);
        addPriorityRow(grid, 3, "Walk Score", "Walkable neighborhood", walkScorePriority);
        addPriorityRow(grid, 4, "Amenities", "Parking, laundry, AC, etc.", amenitiesPriority);
        addPriorityRow(grid, 5, "Safety", "Low crime area", safetyPriority);

        weightsBox.getChildren().add(grid);

        // ---- APPLY BUTTON ----
        Button applyButton = new Button("Apply Preferences");
        applyButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        applyButton.setPadding(new Insets(12, 30, 12, 30));
        applyButton.setStyle(
            "-fx-background-color: #1a237e; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Arial", 14));

        HBox applyRow = new HBox(15);
        applyRow.setAlignment(Pos.CENTER_LEFT);
        applyRow.getChildren().addAll(applyButton, statusLabel);

        // ---- DATA MANAGEMENT SECTION ----
        Label dataHeader = new Label("Data Management");
        dataHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        dataHeader.setStyle("-fx-text-fill: #1a237e;");

        Label dataSubtitle = new Label("Save your apartments and preferences to a file, or load previously saved data");
        dataSubtitle.setFont(Font.font("Arial", 13));
        dataSubtitle.setStyle("-fx-text-fill: #666;");

        VBox dataBox = new VBox(15);
        dataBox.setPadding(new Insets(20));
        dataBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        HBox dataButtonRow = new HBox(15);
        dataButtonRow.setAlignment(Pos.CENTER_LEFT);

        Button saveButton = new Button("Save All Data");
        saveButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        saveButton.setPadding(new Insets(12, 30, 12, 30));
        saveButton.setStyle(
            "-fx-background-color: #4caf50; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        Button loadButton = new Button("Load Saved Data");
        loadButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        loadButton.setPadding(new Insets(12, 30, 12, 30));
        loadButton.setStyle(
            "-fx-background-color: #2196f3; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        Button clearButton = new Button("Clear All Data");
        clearButton.setFont(Font.font("Arial", 14));
        clearButton.setPadding(new Insets(12, 30, 12, 30));
        clearButton.setStyle(
            "-fx-background-color: #f44336; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );

        dataButtonRow.getChildren().addAll(saveButton, loadButton, clearButton);

        Label dataInfoLabel = new Label("Data is saved to data/apartments.json on your computer");
        dataInfoLabel.setFont(Font.font("Arial", 12));
        dataInfoLabel.setStyle("-fx-text-fill: #999;");

        dataBox.getChildren().addAll(dataButtonRow, dataInfoLabel);

        // ---- EVENT HANDLERS ----
        applyButton.setOnAction(e -> applyPreferences());
        saveButton.setOnAction(e -> saveData());
        loadButton.setOnAction(e -> loadData());
        clearButton.setOnAction(e -> showClearConfirmation());

        // add everything
        content.getChildren().addAll(
            header, subtitle,
            budgetHeader, budgetBox,
            weightsHeader, weightsSubtitle, weightsBox,
            applyRow,
            new Separator(),
            dataHeader, dataSubtitle, dataBox
        );
    }

    /**
     * This method is making a dropdown with 4 priority choices
     * @return a ComboBox with High Medium Low Not Important
     *
     * pre-condition: none
     * post-condition: combo box is returned
     */
    private ComboBox<String> makePriorityCombo() {
        ComboBox<String> combo = new ComboBox<String>();
        combo.getItems().addAll("High", "Medium", "Low", "Not Important");
        combo.setValue("Medium");
        combo.setMinWidth(160);
        combo.setStyle("-fx-font-size: 13px;");
        return combo;
    }

    /**
     * This method is adding one row to the priority grid
     * @param grid the grid to add to
     * @param row the row number
     * @param name the factor name
     * @param description short text about what it mean
     * @param combo the dropdown
     *
     * pre-condition: grid should not be null
     * post-condition: row is added
     */
    private void addPriorityRow(GridPane grid, int row, String name,
                                 String description, ComboBox<String> combo) {
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: #333;");
        nameLabel.setMinWidth(130);

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 13));
        descLabel.setStyle("-fx-text-fill: #888;");
        descLabel.setMinWidth(220);

        grid.add(nameLabel, 0, row);
        grid.add(descLabel, 1, row);
        grid.add(combo, 2, row);
    }

    /**
     * This method is turning a saved weight number into a priority word
     * We need this so when we load saved data we can show the right dropdown
     * @param weight the weight between 0 and 1
     * @return the priority word
     *
     * pre-condition: weight should be 0 to 1
     * post-condition: priority string is returned
     */
    private String weightToPriority(double weight) {
        // if weight is basically 0 then not important
        if (weight < 0.01) {
            return "Not Important";
        } else if (weight < 0.12) {
            return "Low";
        } else if (weight < 0.25) {
            return "Medium";
        } else {
            return "High";
        }
    }

    /**
     * This method is turning a priority word into a number
     * High = 3, Medium = 2, Low = 1, Not Important = 0
     * @param priority the priority word
     * @return a number
     *
     * pre-condition: priority is one of the 4 choices
     * post-condition: number is returned
     */
    private double priorityToNumber(String priority) {
        if (priority.equals("High")) {
            return 3.0;
        } else if (priority.equals("Medium")) {
            return 2.0;
        } else if (priority.equals("Low")) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    /**
     * This method is applying preferences when user click apply
     * It turn the dropdown choices into weights that add up to 1.0
     *
     * pre-condition: none
     * post-condition: preferences is saved to service
     */
    private void applyPreferences() {
        UserPreferences prefs = service.getPreferences();

        // set budget
        try {
            prefs.setMinBudget(Double.parseDouble(minBudgetField.getText().trim()));
            prefs.setMaxBudget(Double.parseDouble(maxBudgetField.getText().trim()));
        } catch (Exception e) {
            statusLabel.setText("Error: Budget values must be numbers.");
            statusLabel.setStyle("-fx-text-fill: #c62828;");
            return;
        }

        // turn each dropdown into a number
        double rentNum = priorityToNumber(rentPriority.getValue());
        double sqftNum = priorityToNumber(sqftPriority.getValue());
        double nearTNum = priorityToNumber(nearTPriority.getValue());
        double walkNum = priorityToNumber(walkScorePriority.getValue());
        double amenitiesNum = priorityToNumber(amenitiesPriority.getValue());
        double safetyNum = priorityToNumber(safetyPriority.getValue());

        // add them all up
        double total = rentNum + sqftNum + nearTNum + walkNum + amenitiesNum + safetyNum;

        // make sure at least one thing is picked
        if (total <= 0) {
            statusLabel.setText("Error: At least one factor must not be 'Not Important'.");
            statusLabel.setStyle("-fx-text-fill: #c62828;");
            return;
        }

        // divide each by total so they add up to 1.0
        prefs.setWeightRent(rentNum / total);
        prefs.setWeightSqft(sqftNum / total);
        prefs.setWeightNearT(nearTNum / total);
        prefs.setWeightWalkScore(walkNum / total);
        prefs.setWeightAmenities(amenitiesNum / total);
        prefs.setWeightSafety(safetyNum / total);

        service.setPreferences(prefs);
        statusLabel.setText("Preferences applied! Go to Dashboard to see updated Top Picks.");
        statusLabel.setStyle("-fx-text-fill: #2e7d32;");
    }

    /**
     * This method is saving all data to JSON file
     *
     * pre-condition: none
     * post-condition: data is saved to disk
     */
    private void saveData() {
        applyPreferences(); // apply first, then save
        boolean success = dataService.save(service.getAllApartments(), service.getPreferences());
        if (success) {
            statusLabel.setText("Data saved successfully to data/apartments.json!");
            statusLabel.setStyle("-fx-text-fill: #2e7d32;");
        } else {
            statusLabel.setText("Error saving data. Check console for details.");
            statusLabel.setStyle("-fx-text-fill: #c62828;");
        }
    }

    /**
     * This method is loading data from JSON file
     *
     * pre-condition: none
     * post-condition: data is loaded from disk
     */
    private void loadData() {
        boolean success = dataService.load(service);
        if (success) {
            statusLabel.setText("Data loaded successfully!");
            statusLabel.setStyle("-fx-text-fill: #2e7d32;");

            // update the dropdowns with loaded preferences
            UserPreferences prefs = service.getPreferences();
            minBudgetField.setText(String.format("%.0f", prefs.getMinBudget()));
            maxBudgetField.setText(String.format("%.0f", prefs.getMaxBudget()));
            rentPriority.setValue(weightToPriority(prefs.getWeightRent()));
            sqftPriority.setValue(weightToPriority(prefs.getWeightSqft()));
            nearTPriority.setValue(weightToPriority(prefs.getWeightNearT()));
            walkScorePriority.setValue(weightToPriority(prefs.getWeightWalkScore()));
            amenitiesPriority.setValue(weightToPriority(prefs.getWeightAmenities()));
            safetyPriority.setValue(weightToPriority(prefs.getWeightSafety()));
        } else {
            statusLabel.setText("No saved data found or error loading.");
            statusLabel.setStyle("-fx-text-fill: #ff9800;");
        }
    }

    /**
     * This method is showing confirmation before clearing all data
     *
     * pre-condition: none
     * post-condition: confirmation dialog is showed
     */
    private void showClearConfirmation() {
        Stage confirmStage = new Stage();
        confirmStage.setTitle("Confirm Clear");
        confirmStage.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(20);
        box.setPadding(new Insets(25));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: white;");

        Label message = new Label("Are you sure you want to clear ALL apartment data?\nThis cannot be undone.");
        message.setFont(Font.font("Arial", 14));
        message.setStyle("-fx-text-fill: #333;");

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setFont(Font.font("Arial", 13));
        cancelBtn.setPadding(new Insets(8, 25, 8, 25));
        cancelBtn.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5;");
        cancelBtn.setOnAction(e -> confirmStage.close());

        Button confirmBtn = new Button("Yes, Clear All");
        confirmBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        confirmBtn.setPadding(new Insets(8, 25, 8, 25));
        confirmBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 5;");
        confirmBtn.setOnAction(e -> {
            statusLabel.setText("Please restart the application to clear all data.");
            statusLabel.setStyle("-fx-text-fill: #ff9800;");
            confirmStage.close();
        });

        buttons.getChildren().addAll(cancelBtn, confirmBtn);
        box.getChildren().addAll(message, buttons);

        Scene scene = new Scene(box, 400, 150);
        confirmStage.setScene(scene);
        confirmStage.showAndWait();
    }

    public VBox getContent() {
        return content;
    }
}
