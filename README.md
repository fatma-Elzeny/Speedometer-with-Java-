# Speedometer-with-Java-
# JavaFX GPS Speedometer Dashboard (Raspberry Pi Edition)

## 📌 Project Overview
This project is a **JavaFX-based Speedometer Dashboard** designed to run on **Raspberry Pi with Raspbian**. It integrates with a **UART-based GPS module** (e.g., NEO-7M) to read **real-time speed, latitude, and longitude**. It displays:

- A live speedometer gauge using **Medusa** library.
- GPS coordinates.
- A **Leaflet-based map** displaying current location.
- A red alert when speed exceeds **120 km/h**.

The GUI runs over **VNC**, allowing remote monitoring.

## 🚀 Features
- 📡 **Real-time GPS Data**: Fetches latitude and longitude from a GPS sensor.
- 🚗 **Live Speed Monitoring**: Displays vehicle speed using a speedometer gauge.
- ⚠ **Over-Speed Warning**: Alerts when speed exceeds 120 km/h.
- 🖥 **Runs on Raspberry Pi**: Works over **VNC** for remote monitoring.
---

## 🔍 Main Implementation Points:
- UART serial GPS data parsing (NMEA `$GPRMC`)
- Realtime speed display
- Custom JavaFX Gauge
- Live coordinate labels
- Red alert trigger with sound for speed > 120 km/h
- Google Maps or Leaflet.js map integration
- Clean shutdown and stop hooks

---

## 📊 Project Structure

```
SpeedometerProject/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com.mycompany.speedometer/
│   │   │   │   ├── App.java
│   │   │   │   ├── GPSReader.java
│   │   │   │   ├── GPSData.java
│   │   │   │   ├── PrimaryController.java
│   │   │   │   ├── SpeedAlarm.java
│   │   └── resources/
│   │       ├── primary.fxml
│   │       ├── map.html (for Leaflet map)
└── pom.xml
```

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
       <dependency>
        <groupId>com.fazecast</groupId>
        <artifactId>jSerialComm</artifactId>
        <version>2.9.2</version>
    </dependency>
</dependencies>
```

## 📆 Flow of Execution
1. **App.java**: Starts the JavaFX GUI, loads `primary.fxml`, and sets up controller.
2. **GPSReader**: Initializes and reads from `/dev/ttyS0` UART. Parses `$GPRMC` sentences.
3. **PrimaryController.java**:
   - Gets `GPSData` from `GPSReader`.
   - Updates speedometer gauge.
   - Displays coordinates.
   - Loads the Leaflet map with coordinates.
   - Shows a red alert if speed > 120 km/h.
4. **Leaflet map**: Shown in a WebView. Updated via JavaScript injection.

---

## 📲 Functions Breakdown

### GPSReader.java
- Reads serial UART data.
- Parses NMEA `$GPRMC` lines.
- Converts coordinates.
- Returns `GPSData` with speed, lat, lon, directions.

### PrimaryController.java
- UI logic
- Starts update thread
- Sets labels and gauge values
- Injects lat/lon to map.html
- Handles alerts and stop conditions

### Speedometer (Medusa)
- Customized gauge
- Red section from 120 to 180
- Needle shows speed in km/h

### Map Integration
- `map.html` uses **Leaflet.js**
- JavaFX WebView loads the HTML
- Coordinates injected dynamically

---

## 📅 How to Build & Run

### 1. Install JavaFX + Maven

```bash
sudo apt install openjdk-17-jdk maven
```

### 2. Clone & Install Dependencies
```bash
git clone https://github.com/your/repo.git
cd SpeedometerProject
mvn clean install
```

### 3. Connect GPS to Raspberry Pi
| GPS | RPi Pin |
|-----|---------|
| VCC | 5V      |
| GND | GND     |
| TX  | GPIO15  |
| RX  | GPIO14  |

Enable UART:
```bash
sudo raspi-config
# Interface Options > Serial > No console, Yes UART
sudo reboot
```

### 4. Run the App on RPi
```bash
java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar target/Speedometer-1.0-SNAPSHOT.jar
```

Run with VNC Viewer to view GUI remotely.

---

## 🚫 Alerts and Sound
- When speed exceeds **120 km/h**, red warning and optional alert sound is triggered.
- Add audio logic using JavaFX `MediaPlayer` (optional).

---

## 🌐 Map Integration with Leaflet
- Leaflet HTML is embedded in `map.html`
- Displayed inside `WebView`
- Coordinates are updated dynamically via JavaScript injection

---

## 🎉 Final Outcome
- Realtime Speed and GPS dashboard
- UART Serial GPS parsing
- Leaflet-based location tracking
- Remote viewing via VNC
- Ready for vehicle use case or outdoor GPS monitoring

---

## ✨ Authors
- Developed by: [Fatma Yosry , Mohamed Awadin , Omar Alaa]
- 

---








