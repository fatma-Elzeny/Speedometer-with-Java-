/***
App.java :
 * Role : The Entry Point of the JavaFX Application.
 * 
 * Startup (App Launches)
 * Where: App.main() and App.start()
 * What Happens: The app begins execution, initializes the GPS connection, and loads the UI.
 */
package com.mycompany.speedometer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import static javafx.application.Application.launch;

/*
  1.1- Initializes the app.
*/

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        /**
         * Creates the single GPSReader instance that will talk to the GPS device.
        */
        GPSReader reader = new GPSReader(); 
        /**
         * Starts the UART connection and spawns a thread to read GPS data.
         */
        reader.setupUART(); 
        /**
         * Stores the GPSReader in GPSData’s static reader field so PrimaryController can access it later.
         */
        GPSData.setReader(reader); 
        /***
         *  Creates a JavaFX Scene by loading the primary.fxml file (via loadFXML), 
         *   which links to PrimaryController.
         */
        scene = new Scene(loadFXML("primary"), 640, 480);
        /**
         * Assigns the scene to the main window (Stage).
         */
        stage.setScene(scene);
        /**
         * Sets the window title.
         */
        stage.setTitle("Speedometer Demo");
        /**
         * Displays the window, by triggering PrimaryController.initialize().
         */
        stage.show();
    }
    /***
     *   Where: App.stop()
     *   What Happens: The app cleans up by stopping the GPS reader and alarm.
     */
       @Override
    public void stop() {
        // Retrieves the GPSReader from GPSData.
        GPSReader reader = GPSData.getReader(); 
        // Ensures the reader exists.
        if (reader != null) {
            // Stops the GPS reading thread
            reader.stop(); // Stop the GPSReader
        }
        SpeedAlarm.stop();
    }
    
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }
    /***
         *  1.3.1- Loads the UI.
         *
    */
    /**
     * 
     * @param fxml
     * @return Loads the FXML, instantiates PrimaryController, and returns the UI root node.
     * @throws IOException 
     */
    private static Parent loadFXML(String fxml) throws IOException {
        /**
         * Creates an FXMLLoader to load primary.fxml from resources.
         */
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    /***
     * Role: Entry point for the Java application.
     * @param args 
     */
    public static void main(String[] args) {
        /**
         * launch() - Calls JavaFX’s Application.launch(), which creates an App instance and invokes start().
         */
        launch();
    }
}