# Speedometer-with-Java-
# Speedometer Dashboard

## 📌 Project Overview
This project is a **JavaFX-based Speedometer Dashboard** designed to run on **Raspberry Pi (Raspbian OS)**. It displays **real-time speed, latitude, and longitude** using a **GPS module**. If the speed exceeds 120 km/h, a **red warning message** appears and sound alert.

## 🚀 Features
- 📡 **Real-time GPS Data**: Fetches latitude and longitude from a GPS sensor.
- 🚗 **Live Speed Monitoring**: Displays vehicle speed using a speedometer gauge.
- ⚠ **Over-Speed Warning**: Alerts when speed exceeds 120 km/h.
- 🖥 **Runs on Raspberry Pi**: Works over **VNC** for remote monitoring.

---

## 🛠 Dependencies
Ensure you have the following dependencies in your **Maven `pom.xml`**:

```xml
<dependencies>
    <!-- JavaFX Modules -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21</version>
    </dependency>
    <!-- Medusa Gauge Library -->
     <dependency>
            <groupId>eu.hansolo</groupId>
            <artifactId>medusa</artifactId>
            <version>21.0.9</version>
     </dependency>
</dependencies>
```




