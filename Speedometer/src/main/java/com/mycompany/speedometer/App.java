/* App.java */
package com.mycompany.speedometer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private GPSReader reader;

    @Override
    public void start(Stage stage) throws IOException {
        reader = new GPSReader();
        reader.setupUART();
        GPSData.setReader(reader);

        // Load FXML from resources
        java.net.URL fxmlUrl = getClass().getResource("/com/mycompany/speedometer/primary.fxml");
        if (fxmlUrl == null) {
            throw new IOException("Cannot find primary.fxml at /com/mycompany/speedometer/primary.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        scene = new Scene(root, 1000, 1000);
        stage.setScene(scene);
        stage.setTitle("Speedometer Demo");

        PrimaryController controller = loader.getController();
        stage.setOnCloseRequest(event -> {
            controller.stop();
            stop();
        });

        stage.show();
    }

    @Override
    public void stop() {
        if (reader != null) {
            reader.stop();
        }
        // SpeedAlarm.stop(); // Uncomment if implemented
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        java.net.URL url = App.class.getResource("/com/mycompany/speedometer/" + fxml + ".fxml");
        if (url == null) {
            throw new IOException("Cannot find " + fxml + ".fxml");
        }
        FXMLLoader fxmlLoader = new FXMLLoader(url);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
