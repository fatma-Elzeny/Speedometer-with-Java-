module com.mycompany.speedometer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop; 


    opens com.mycompany.speedometer to javafx.fxml;
    exports com.mycompany.speedometer;
    requires eu.hansolo.medusa;
    requires java.base;
    requires com.fazecast.jSerialComm;
    requires javafx.web;

}
