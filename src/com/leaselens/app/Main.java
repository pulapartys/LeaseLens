package com.leaselens.app;

import javafx.application.Application;
import javafx.stage.Stage;
import com.leaselens.ui.MainWindow;

/**
 * This is the main class that is starting the LeaseLens application
 * When user run the program this class is called first
 *
 * pre-condition: JavaFX need to be installed on the computer
 * post-condition: the application window is showing on screen
 */
public class Main extends Application {

    /**
     * This method is starting the JavaFX window
     * @param primaryStage the main window that JavaFX is giving us
     *
     * pre-condition: primaryStage should not be null
     * post-condition: the main window is showed with all the tabs
     */
    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow(primaryStage);
        mainWindow.show();
    }

    /**
     * This is the main method that is running first
     * @param args command line arguments
     *
     * pre-condition: no special arguments is needed
     * post-condition: the app is launched
     */
    public static void main(String[] args) {
        // for the map API we set User-Agent so OpenStreetMap tile server does not block our map requests
        System.setProperty("http.agent", "LeaseLens/1.0 StudentProject");
        launch(args);
    }
}
