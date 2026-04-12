package com.leaselens.ui;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import com.leaselens.service.ApartmentService;
import com.leaselens.service.DataPersistenceService;

/**
 * This is the main window that hold all the tabs
 *
 * pre-condition: none
 * post-condition: main window is created with 5 tabs
 */
public class MainWindow {

    private Stage stage;
    private ApartmentService service;
    private DataPersistenceService dataService;
    private DashboardTab dashboardTab;
    private ApartmentsTab apartmentsTab;
    private CompareTab compareTab;
    private ExpenseTab expenseTab;
    private SettingsTab settingsTab;

    /**
     * This build the main window
     * @param stage the JavaFX window
     *
     * pre-condition: stage not null
     * post-condition: window is built
     */
    public MainWindow(Stage stage) {
        this.stage = stage;
        this.service = new ApartmentService();
        this.dataService = new DataPersistenceService();
        dataService.load(service);
        buildUI();
    }

    /**
     * This build all the UI parts
     *
     * pre-condition: service is ready
     * post-condition: all tabs is created
     */
    private void buildUI() {
        TabPane tabPane = new TabPane();

        dashboardTab = new DashboardTab(service);
        apartmentsTab = new ApartmentsTab(service);
        compareTab = new CompareTab(service);
        expenseTab = new ExpenseTab(service);
        settingsTab = new SettingsTab(service);

        Tab tab1 = new Tab("Dashboard", dashboardTab.getContent());
        Tab tab2 = new Tab("My Apartments", apartmentsTab.getContent());
        Tab tab3 = new Tab("Compare", compareTab.getContent());
        Tab tab4 = new Tab("Expenses", expenseTab.getContent());
        Tab tab5 = new Tab("Preferences", settingsTab.getContent());
        tab1.setClosable(false);
        tab2.setClosable(false);
        tab3.setClosable(false);
        tab4.setClosable(false);
        tab5.setClosable(false);

        tabPane.getTabs().addAll(tab1, tab2, tab3, tab4, tab5);

        // refresh when user switch tab
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            String name = newTab.getText();
            if (name.equals("Dashboard")) dashboardTab.refresh();
            else if (name.equals("My Apartments")) apartmentsTab.refresh();
            else if (name.equals("Compare")) compareTab.refresh();
            else if (name.equals("Expenses")) expenseTab.refresh();
        });

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("LeaseLens - Apartment Search Organizer");
        stage.setScene(scene);
    }

    /**
     * This show the window on screen
     *
     * pre-condition: buildUI was called
     * post-condition: window is visible
     */
    public void show() {
        stage.show();
    }
}
