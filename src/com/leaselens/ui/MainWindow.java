package com.leaselens.ui;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import com.leaselens.service.ApartmentService;
import com.leaselens.service.DataPersistenceService;

/**
 * This class is the main window of the application
 * It have the 5 tabs and hold the apartment service that everything share
 *
 * pre-condition: none
 * post-condition: main window is created with all tabs
 */
public class MainWindow {

    private Stage stage;
    private ApartmentService service;
    private DataPersistenceService dataService;
    private TabPane tabPane;

    // tabs
    private DashboardTab dashboardTab;
    private ApartmentsTab apartmentsTab;
    private CompareTab compareTab;
    private ExpenseTab expenseTab;
    private SettingsTab settingsTab;

    /**
     * This constructor is building the main window with all tabs
     * @param stage the JavaFX stage (window)
     *
     * pre-condition: stage should not be null
     * post-condition: window is built but not showing yet
     */
    public MainWindow(Stage stage) {
        this.stage = stage;
        this.service = new ApartmentService();
        this.dataService = new DataPersistenceService();

        // load saved data if any
        dataService.load(service);

        buildUI();
    }

    /**
     * This method is building all the UI parts
     *
     * pre-condition: service should be set up
     * post-condition: all tabs is created and added to window
     */
    private void buildUI() {
        // create the tab pane
        tabPane = new TabPane();
        tabPane.setStyle(
            "-fx-tab-min-height: 40px; " +
            "-fx-tab-max-height: 40px; " +
            "-fx-font-size: 15px;"
        );

        // create each tab
        dashboardTab = new DashboardTab(service);
        apartmentsTab = new ApartmentsTab(service);
        compareTab = new CompareTab(service);
        expenseTab = new ExpenseTab(service);
        settingsTab = new SettingsTab(service, dataService);

        // make tabs
        Tab tab1 = new Tab("  Dashboard  ", dashboardTab.getContent());
        Tab tab2 = new Tab("  My Apartments  ", apartmentsTab.getContent());
        Tab tab3 = new Tab("  Compare  ", compareTab.getContent());
        Tab tab4 = new Tab("  Expenses  ", expenseTab.getContent());
        Tab tab5 = new Tab("  Preferences  ", settingsTab.getContent());

        // dont let user close tabs
        tab1.setClosable(false);
        tab2.setClosable(false);
        tab3.setClosable(false);
        tab4.setClosable(false);
        tab5.setClosable(false);

        tabPane.getTabs().addAll(tab1, tab2, tab3, tab4, tab5);

        // refresh tabs when user switch to them
        tabPane.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldTab, newTab) -> {
                refreshCurrentTab(newTab.getText().trim());
            }
        );

        // set up the main layout
        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setStyle("-fx-background-color: #f0f2f5;");

        Scene scene = new Scene(root, 1250, 850);

        stage.setTitle("LeaseLens - Apartment Search Organizer");
        stage.setScene(scene);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
    }

    /**
     * This method is refreshing the tab that user just switched to
     * @param tabName the name of the tab
     *
     * pre-condition: tabName should be valid tab name
     * post-condition: tab content is refreshed with latest data
     */
    private void refreshCurrentTab(String tabName) {
        if (tabName.equals("Dashboard")) {
            dashboardTab.refresh();
        } else if (tabName.equals("My Apartments")) {
            apartmentsTab.refresh();
        } else if (tabName.equals("Compare")) {
            compareTab.refresh();
        } else if (tabName.equals("Expenses")) {
            expenseTab.refresh();
        }
    }

    /**
     * This method is showing the window on screen
     *
     * pre-condition: buildUI should have been called
     * post-condition: window is visible to user
     */
    public void show() {
        stage.show();
    }
}
