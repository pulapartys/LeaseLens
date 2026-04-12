package com.leaselens.ui;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import com.leaselens.model.Apartment;
import com.leaselens.service.ApartmentService;
import com.leaselens.datastructures.ExpenseHashMap;

/**
 * This is the Expense Calculator tab
 * User enter monthly expenses and it calculate total cost
 * It use a HashMap to store expense categories
 *
 * pre-condition: service not null
 * post-condition: expense tab is created
 */
public class ExpenseTab {

    private ApartmentService service;
    private ScrollPane content;
    private VBox mainBox;
    private ComboBox<String> apartmentCombo;
    private TextField utilitiesField;
    private TextField groceriesField;
    private TextField transportField;
    private TextField internetField;
    private TextField insuranceField;
    private TextField otherField;
    private VBox resultsBox;
    private ExpenseHashMap expenseMap;

    /**
     * This make the expense calculator tab
     * @param service the apartment service
     *
     * pre-condition: service not null
     * post-condition: expense tab is built
     */
    public ExpenseTab(ApartmentService service) {
        this.service = service;
        this.expenseMap = new ExpenseHashMap(50);
        this.mainBox = new VBox(15);
        buildTab();
        ScrollPane scroll = new ScrollPane(mainBox);
        scroll.setFitToWidth(true);
        this.content = scroll;
    }

    /**
     * This build the expense tab layout
     *
     * pre-condition: mainBox not null
     * post-condition: tab is built with form and results
     */
    private void buildTab() {
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: #f0f2f5;");

        Label header = new Label("Expense Calculator");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        header.setStyle("-fx-text-fill: #1a237e;");
        Label subtitle = new Label("Estimate your total monthly and yearly living costs. Pick an apartment, enter your expenses, and click Calculate.");
        subtitle.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");
        subtitle.setWrapText(true);

        // apartment selector
        apartmentCombo = new ComboBox<String>();
        apartmentCombo.setMinWidth(300);
        apartmentCombo.setPromptText("Choose an apartment...");

        HBox selectRow = new HBox(10);
        selectRow.setPadding(new Insets(10));
        selectRow.setAlignment(Pos.CENTER_LEFT);
        selectRow.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        selectRow.getChildren().addAll(new Label("Select Apartment:"), apartmentCombo);

        // expense fields - enter dollar amounts, leave blank or 0 if not applicable
        utilitiesField = new TextField();
        utilitiesField.setPromptText("0");
        groceriesField = new TextField();
        groceriesField.setPromptText("0");
        transportField = new TextField();
        transportField.setPromptText("0");
        internetField = new TextField();
        internetField.setPromptText("0");
        insuranceField = new TextField();
        insuranceField.setPromptText("0");
        otherField = new TextField();
        otherField.setPromptText("0");

        Label expHeader = new Label("Monthly Expenses (enter dollar amounts, leave blank if none)");
        expHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        expHeader.setStyle("-fx-text-fill: #3366cc;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        grid.setStyle("-fx-background-color: white; -fx-background-radius: 5;");
        addExpenseRow(grid, 0, "Utilities (electric, gas, water):", utilitiesField);
        addExpenseRow(grid, 1, "Groceries:", groceriesField);
        addExpenseRow(grid, 2, "Transport (T pass, gas, parking):", transportField);
        addExpenseRow(grid, 3, "Internet / Phone:", internetField);
        addExpenseRow(grid, 4, "Renter's Insurance:", insuranceField);
        addExpenseRow(grid, 5, "Other:", otherField);

        Button calcBtn = new Button("Calculate Total Cost");
        calcBtn.setStyle("-fx-background-color: #3366cc; -fx-text-fill: white;");
        calcBtn.setOnAction(e -> calculateExpenses());

        resultsBox = new VBox(10);
        resultsBox.setPadding(new Insets(10));
        resultsBox.setStyle("-fx-background-color: white; -fx-border-color: #2e7d32; -fx-border-width: 0 0 0 4; -fx-background-radius: 5;");
        resultsBox.setVisible(false);

        mainBox.getChildren().addAll(header, subtitle, selectRow, expHeader, grid, calcBtn, resultsBox);
    }

    /**
     * This add one expense row to the grid
     * @param grid the grid pane
     * @param row the row number
     * @param label the expense category name
     * @param field the text field for amount
     *
     * pre-condition: grid not null
     * post-condition: row is added to grid
     */
    private void addExpenseRow(GridPane grid, int row, String label, TextField field) {
        Label l = new Label(label);
        l.setFont(Font.font("Arial", 13));
        l.setMinWidth(250);
        field.setMaxWidth(120);
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(new Label("$"), field);
        grid.add(l, 0, row);
        grid.add(box, 1, row);
    }

    /**
     * This calculate all expenses and show results
     * It use the ExpenseHashMap to store categories
     *
     * pre-condition: apartment is selected
     * post-condition: results is shown with totals
     */
    private void calculateExpenses() {
        String selected = apartmentCombo.getValue();
        if (selected == null || selected.isEmpty()) {
            resultsBox.getChildren().clear();
            resultsBox.getChildren().add(new Label("Please select an apartment first!"));
            resultsBox.setVisible(true);
            return;
        }

        Apartment apt = findApartment(selected);
        if (apt == null) return;

        // store expenses in HashMap
        expenseMap.clear();
        try {
            expenseMap.put("Utilities", parseField(utilitiesField));
            expenseMap.put("Groceries", parseField(groceriesField));
            expenseMap.put("Transport", parseField(transportField));
            expenseMap.put("Internet / Phone", parseField(internetField));
            expenseMap.put("Renter's Insurance", parseField(insuranceField));
            expenseMap.put("Other", parseField(otherField));
        } catch (Exception e) {
            resultsBox.getChildren().clear();
            resultsBox.getChildren().add(new Label("Error: Please enter valid numbers!"));
            resultsBox.setVisible(true);
            return;
        }

        // add up all expenses from HashMap
        double totalExp = 0;
        for (String cat : expenseMap.getAllKeys()) {
            totalExp = totalExp + expenseMap.get(cat);
        }

        double rent = apt.getRent();
        double monthly = rent + totalExp;
        double yearly = monthly * 12;

        // show results
        resultsBox.getChildren().clear();
        Label rHeader = new Label("Cost Breakdown for: " + apt.getName());
        rHeader.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        rHeader.setStyle("-fx-text-fill: #2e7d32;");

        String info = "Rent: $" + String.format("%.2f", rent) + "\n";
        for (String cat : expenseMap.getAllKeys()) {
            double amt = expenseMap.get(cat);
            if (amt > 0) {
                info = info + cat + ": $" + String.format("%.2f", amt) + "\n";
            }
        }
        info = info + "\nTotal Expenses: $" + String.format("%.2f", totalExp);
        info = info + "\nTotal Monthly Cost: $" + String.format("%.2f", monthly);
        info = info + "\nTotal Yearly Cost: $" + String.format("%.2f", yearly);

        Label infoLabel = new Label(info);
        infoLabel.setFont(Font.font("Arial", 14));
        resultsBox.getChildren().addAll(rHeader, infoLabel);
        resultsBox.setVisible(true);
    }

    /**
     * This parse a text field to get a number
     * @param field the text field
     * @return the number value
     *
     * pre-condition: field not null
     * post-condition: return the number or 0
     */
    private double parseField(TextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) return 0;
        return Double.parseDouble(text);
    }

    /**
     * This find apartment by combo box text
     * @param comboText the display string
     * @return the apartment or null
     *
     * pre-condition: comboText not null
     * post-condition: return apartment or null
     */
    private Apartment findApartment(String comboText) {
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " - $" + String.format("%.0f", apt.getRent()) + "/mo";
            if (display.equals(comboText)) return apt;
        }
        return null;
    }

    /**
     * This refresh the apartment combo box
     *
     * pre-condition: none
     * post-condition: combo is updated
     */
    public void refresh() {
        String prev = apartmentCombo.getValue();
        apartmentCombo.getItems().clear();
        for (int i = 0; i < service.getAllApartments().getCurrentSize(); i++) {
            Apartment apt = service.getAllApartments().get(i);
            String display = apt.getName() + " - $" + String.format("%.0f", apt.getRent()) + "/mo";
            apartmentCombo.getItems().add(display);
        }
        if (prev != null && apartmentCombo.getItems().contains(prev)) {
            apartmentCombo.setValue(prev);
        }
    }

    /**
     * This return the content for this tab
     * @return ScrollPane content
     *
     * pre-condition: buildTab was called
     * post-condition: content is returned
     */
    public ScrollPane getContent() { return content; }
}
