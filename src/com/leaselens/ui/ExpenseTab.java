package com.leaselens.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.leaselens.model.Apartment;
import com.leaselens.service.ApartmentService;
import com.leaselens.datastructures.ExpenseHashMap;

/**
 * This class is the Expense Calculator tab where user can enter monthly
 * expenses like utilities groceries transport etc for each apartment
 * Then it calculate total monthly and yearly cost of living
 *
 * It use a HashMap to store expense category name and dollar amount
 *
 * pre-condition: service should not be null
 * post-condition: expense calculator tab is created
 */
public class ExpenseTab {

    private ApartmentService service;
    private ScrollPane content;
    private VBox mainBox;

    // dropdown to pick apartment
    private ComboBox<String> apartmentCombo;

    // text fields for each expense category
    private TextField utilitiesField;
    private TextField groceriesField;
    private TextField transportField;
    private TextField internetField;
    private TextField insuranceField;
    private TextField otherField;

    // labels for showing results
    private Label rentAmountLabel;
    private Label totalMonthlyLabel;
    private Label totalYearlyLabel;
    private VBox resultsBox;
    private VBox breakdownBox;

    // HashMap to store expenses for the selected apartment
    // key = category name like "Utilities", value = dollar amount
    private ExpenseHashMap expenseMap;

    /**
     * This constructor is making the expense calculator tab
     * @param service the apartment service
     *
     * pre-condition: service should not be null
     * post-condition: tab is built with all fields
     */
    public ExpenseTab(ApartmentService service) {
        this.service = service;
        this.expenseMap = new ExpenseHashMap(50);
        this.mainBox = new VBox(20);
        buildTab();

        // wrap in scroll pane so it scroll if window is small
        ScrollPane scroll = new ScrollPane(mainBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #f0f2f5; -fx-background-color: #f0f2f5;");
        this.content = scroll;
    }

    /**
     * This method is building the expense calculator tab UI
     *
     * pre-condition: none
     * post-condition: all the input fields and result labels is created
     */
    private void buildTab() {
        mainBox.setPadding(new Insets(25));
        mainBox.setStyle("-fx-background-color: #f0f2f5;");

        // header
        Label header = new Label("Expense Calculator");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        header.setStyle("-fx-text-fill: #1a237e;");

        Label subtitle = new Label("Calculate the real monthly cost of living for each apartment");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setStyle("-fx-text-fill: #666;");

        // ---- APARTMENT SELECTION ----
        Label selectLabel = new Label("Select Apartment:");
        selectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        apartmentCombo = new ComboBox<String>();
        apartmentCombo.setMinWidth(300);
        apartmentCombo.setStyle("-fx-font-size: 13px;");
        apartmentCombo.setPromptText("Choose an apartment...");

        // when user picks an apartment, show its rent
        apartmentCombo.setOnAction(e -> loadApartmentRent());

        HBox selectRow = new HBox(15);
        selectRow.setAlignment(Pos.CENTER_LEFT);
        selectRow.setPadding(new Insets(15));
        selectRow.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );
        selectRow.getChildren().addAll(selectLabel, apartmentCombo);

        // ---- EXPENSE INPUT SECTION ----
        Label expenseHeader = new Label("Monthly Expenses");
        expenseHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        expenseHeader.setStyle("-fx-text-fill: #1a237e;");

        Label expenseSubtitle = new Label("Enter how much you expect to spend per month in each category");
        expenseSubtitle.setFont(Font.font("Arial", 13));
        expenseSubtitle.setStyle("-fx-text-fill: #666;");

        VBox expenseBox = new VBox(12);
        expenseBox.setPadding(new Insets(20));
        expenseBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );

        // create input fields for each category
        utilitiesField = new TextField("0");
        groceriesField = new TextField("0");
        transportField = new TextField("0");
        internetField = new TextField("0");
        insuranceField = new TextField("0");
        otherField = new TextField("0");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        // row 0 - utilities
        grid.add(makeFieldLabel("Utilities (electric, gas, water):"), 0, 0);
        grid.add(makeDollarField(utilitiesField), 1, 0);

        // row 1 - groceries
        grid.add(makeFieldLabel("Groceries:"), 0, 1);
        grid.add(makeDollarField(groceriesField), 1, 1);

        // row 2 - transport
        grid.add(makeFieldLabel("Transport (T pass, gas, parking):"), 0, 2);
        grid.add(makeDollarField(transportField), 1, 2);

        // row 3 - internet
        grid.add(makeFieldLabel("Internet / Phone:"), 0, 3);
        grid.add(makeDollarField(internetField), 1, 3);

        // row 4 - insurance
        grid.add(makeFieldLabel("Renter's Insurance:"), 0, 4);
        grid.add(makeDollarField(insuranceField), 1, 4);

        // row 5 - other
        grid.add(makeFieldLabel("Other:"), 0, 5);
        grid.add(makeDollarField(otherField), 1, 5);

        expenseBox.getChildren().add(grid);

        // ---- CALCULATE BUTTON ----
        Button calculateButton = new Button("Calculate Total Cost");
        calculateButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        calculateButton.setPadding(new Insets(12, 30, 12, 30));
        calculateButton.setStyle(
            "-fx-background-color: #1a237e; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );
        calculateButton.setOnAction(e -> calculateExpenses());

        // ---- RESULTS SECTION ----
        resultsBox = new VBox(15);
        resultsBox.setPadding(new Insets(20));
        resultsBox.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);"
        );
        resultsBox.setVisible(false);

        // add everything to main box
        mainBox.getChildren().addAll(
            header, subtitle,
            selectRow,
            expenseHeader, expenseSubtitle, expenseBox,
            calculateButton,
            resultsBox
        );
    }

    /**
     * This method is making a label for the expense input fields
     * @param text the label text
     * @return the styled label
     *
     * pre-condition: text should not be null
     * post-condition: label is returned
     */
    private Label makeFieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        label.setStyle("-fx-text-fill: #333;");
        label.setMinWidth(250);
        return label;
    }

    /**
     * This method is wrapping a text field with a dollar sign in front
     * @param field the text field to wrap
     * @return HBox with dollar sign and text field
     *
     * pre-condition: field should not be null
     * post-condition: styled HBox is returned
     */
    private HBox makeDollarField(TextField field) {
        field.setFont(Font.font("Arial", 13));
        field.setMaxWidth(120);
        field.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 8;");

        Label dollar = new Label("$");
        dollar.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        dollar.setStyle("-fx-text-fill: #666;");

        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(dollar, field);
        return box;
    }

    /**
     * This method is loading the rent for the selected apartment
     * So we can show it in the results
     *
     * pre-condition: an apartment should be selected in the combo box
     * post-condition: rent label is updated
     */
    private void loadApartmentRent() {
        // clear old results when switching apartments
        resultsBox.setVisible(false);
    }

    /**
     * This method is doing the expense calculation
     * It put all expenses into the HashMap then add them up with rent
     *
     * pre-condition: user should have selected an apartment and entered numbers
     * post-condition: results is displayed with breakdown
     */
    private void calculateExpenses() {
        // check if apartment is selected
        String selected = apartmentCombo.getValue();
        if (selected == null || selected.isEmpty()) {
            resultsBox.getChildren().clear();
            Label errorLabel = new Label("Please select an apartment first!");
            errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            errorLabel.setStyle("-fx-text-fill: #c62828;");
            resultsBox.getChildren().add(errorLabel);
            resultsBox.setVisible(true);
            return;
        }

        // find the apartment
        Apartment apartment = findSelectedApartment(selected);
        if (apartment == null) {
            return;
        }

        // clear the HashMap and put new values in
        expenseMap.clear();

        // try to read each field and put into HashMap
        try {
            double utilities = parseField(utilitiesField);
            double groceries = parseField(groceriesField);
            double transport = parseField(transportField);
            double internet = parseField(internetField);
            double insurance = parseField(insuranceField);
            double other = parseField(otherField);

            // put each expense into the HashMap
            expenseMap.put("Utilities", utilities);
            expenseMap.put("Groceries", groceries);
            expenseMap.put("Transport", transport);
            expenseMap.put("Internet / Phone", internet);
            expenseMap.put("Renter's Insurance", insurance);
            expenseMap.put("Other", other);

        } catch (Exception e) {
            resultsBox.getChildren().clear();
            Label errorLabel = new Label("Error: Please enter valid numbers in all fields!");
            errorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            errorLabel.setStyle("-fx-text-fill: #c62828;");
            resultsBox.getChildren().add(errorLabel);
            resultsBox.setVisible(true);
            return;
        }

        // calculate total expenses from the HashMap
        double totalExpenses = 0;
        for (String category : expenseMap.getAllKeys()) {
            totalExpenses = totalExpenses + expenseMap.get(category);
        }

        double rent = apartment.getRent();
        double totalMonthly = rent + totalExpenses;
        double totalYearly = totalMonthly * 12;

        // show results
        showResults(apartment.getName(), rent, totalExpenses, totalMonthly, totalYearly);
    }

    /**
     * This method is parsing a text field to get the dollar amount
     * If field is empty or blank it return 0
     * @param field the text field to parse
     * @return the dollar amount as double
     *
     * pre-condition: field should not be null
     * post-condition: a number is returned
     */
    private double parseField(TextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(text);
    }

    /**
     * This method is finding the apartment object from the combo box text
     * @param comboText the text from the combo box
     * @return the apartment object or null
     *
     * pre-condition: comboText should not be null
     * post-condition: apartment is returned if found
     */
    private Apartment findSelectedApartment(String comboText) {
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " - $" + String.format("%.0f", apt.getRent()) + "/mo";
            if (display.equals(comboText)) {
                return apt;
            }
        }
        return null;
    }

    /**
     * This method is showing the calculation results in a nice format
     * It show a breakdown of each expense from the HashMap plus totals
     * @param aptName the apartment name
     * @param rent the monthly rent
     * @param totalExpenses total of all expenses
     * @param totalMonthly rent plus expenses
     * @param totalYearly monthly times 12
     *
     * pre-condition: all values should be calculated already
     * post-condition: results section is visible with all numbers
     */
    private void showResults(String aptName, double rent, double totalExpenses,
                             double totalMonthly, double totalYearly) {
        resultsBox.getChildren().clear();

        // results header
        Label resultsHeader = new Label("Cost Breakdown for: " + aptName);
        resultsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        resultsHeader.setStyle("-fx-text-fill: #1a237e;");

        // breakdown grid
        GridPane breakdownGrid = new GridPane();
        breakdownGrid.setHgap(30);
        breakdownGrid.setVgap(8);

        // rent row
        int row = 0;
        breakdownGrid.add(makeBreakdownLabel("Rent:"), 0, row);
        breakdownGrid.add(makeBreakdownValue("$" + String.format("%.2f", rent)), 1, row);
        row++;

        // add separator
        Separator sep1 = new Separator();
        breakdownGrid.add(sep1, 0, row, 2, 1);
        row++;

        // loop through the HashMap to show each expense
        for (String category : expenseMap.getAllKeys()) {
            double amount = expenseMap.get(category);
            if (amount > 0) {
                breakdownGrid.add(makeBreakdownLabel(category + ":"), 0, row);
                breakdownGrid.add(makeBreakdownValue("$" + String.format("%.2f", amount)), 1, row);
                row++;
            }
        }

        // separator before totals
        Separator sep2 = new Separator();
        breakdownGrid.add(sep2, 0, row, 2, 1);
        row++;

        // total expenses row
        Label expLabel = new Label("Total Expenses:");
        expLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        expLabel.setStyle("-fx-text-fill: #333;");
        Label expValue = new Label("$" + String.format("%.2f", totalExpenses));
        expValue.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        expValue.setStyle("-fx-text-fill: #333;");
        breakdownGrid.add(expLabel, 0, row);
        breakdownGrid.add(expValue, 1, row);
        row++;

        // total monthly card
        HBox monthlyCard = makeTotalCard(
            "Total Monthly Cost",
            "$" + String.format("%.2f", totalMonthly),
            "#1a237e"
        );

        // total yearly card
        HBox yearlyCard = makeTotalCard(
            "Total Yearly Cost",
            "$" + String.format("%.2f", totalYearly),
            "#4caf50"
        );

        HBox totalCards = new HBox(20);
        totalCards.getChildren().addAll(monthlyCard, yearlyCard);

        resultsBox.getChildren().addAll(resultsHeader, breakdownGrid, totalCards);
        resultsBox.setVisible(true);
    }

    /**
     * This method is making a label for the breakdown section
     * @param text the label text
     * @return a styled label
     *
     * pre-condition: none
     * post-condition: label is returned
     */
    private Label makeBreakdownLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 14));
        label.setStyle("-fx-text-fill: #555;");
        label.setMinWidth(200);
        return label;
    }

    /**
     * This method is making a value label for the breakdown section
     * @param text the value text like "$150.00"
     * @return a styled label
     *
     * pre-condition: none
     * post-condition: label is returned
     */
    private Label makeBreakdownValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", 14));
        label.setStyle("-fx-text-fill: #333;");
        return label;
    }

    /**
     * This method is making a big colored card for showing total cost
     * @param title the card title like "Total Monthly Cost"
     * @param amount the dollar amount text
     * @param color the accent color
     * @return a styled HBox card
     *
     * pre-condition: none
     * post-condition: card is returned
     */
    private HBox makeTotalCard(String title, String amount, String color) {
        VBox cardContent = new VBox(5);
        cardContent.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setStyle("-fx-text-fill: #666;");

        Label amountLabel = new Label(amount);
        amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        amountLabel.setStyle("-fx-text-fill: " + color + ";");

        cardContent.getChildren().addAll(titleLabel, amountLabel);

        HBox card = new HBox();
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 40, 20, 40));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: " + color + "; " +
            "-fx-border-radius: 10; " +
            "-fx-border-width: 2;"
        );
        card.getChildren().add(cardContent);
        return card;
    }

    /**
     * This method is refreshing the apartment dropdown list
     * Called when user switch to this tab
     *
     * pre-condition: none
     * post-condition: combo box have the latest apartments
     */
    public void refresh() {
        String previousSelection = apartmentCombo.getValue();
        apartmentCombo.getItems().clear();

        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " - $" + String.format("%.0f", apt.getRent()) + "/mo";
            apartmentCombo.getItems().add(display);
        }

        // try to keep the old selection
        if (previousSelection != null && apartmentCombo.getItems().contains(previousSelection)) {
            apartmentCombo.setValue(previousSelection);
        }
    }

    /**
     * This method is returning the tab content
     * @return the scroll pane with all the content
     *
     * pre-condition: none
     * post-condition: content is returned
     */
    public ScrollPane getContent() {
        return content;
    }
}
