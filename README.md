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

### 📂 Project Structure

```
SpeedometerProject/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com.mycompany.speedometer/
│   │   │   │   ├── App.java              # Main JavaFX launcher
│   │   │   │   ├── GPSReader.java        # Reads and parses GPS data
│   │   │   │   ├── GPSData.java          # Data model for GPS values
│   │   │   │   ├── PrimaryController.java # JavaFX controller
│   │   │   │   ├── SpeedAlarm.java        # Sound alerts for speed limits
│   │   └── resources/
│   │       ├── primary.fxml               # UI layout file (JavaFX Scene Builder)
│   │       ├── map.html (for Leaflet map)
└── pom.xml
```

---

## 🧱 Software Architecture

```
+-------------------------+
|   JavaFX Application    |
|-------------------------|
|  App.java               | ← Main entry point
|  └─ Loads primary.fxml  |
|                         |
|  ┌───────────────┐      |
|  │Controller     │◄─────┐
|  │(Dashboard)    │      │
|  └───────────────┘      │
|     ▲                  ▼
|  ┌───────────────┐   ┌────────────┐
|  │  GPSReader    │   │ SpeedAlarm │
|  │  (UART logic) │   │ (Sound/FX) │
|  └───────────────┘   └────────────┘
|         ▲
|     Reads GPS
|         ▲
|     Serial Comm
+---------┼---------------+
          ▼
   /dev/ttyS0 on Raspberry Pi
```

---

## 🔌 Hardware Architecture

```
+-------------------------+
|     GPS Module (e.g.,   |
|     NEO-6M or u-blox)   |
|-------------------------|
| TX ───────► GPIO 15     |
| RX ◄─────── GPIO 14     |
| VCC ─────── 5V          |
| GND ─────── GND         |
+-------------------------+
           │
           ▼
+-------------------------+
|   Raspberry Pi 3/4      |
|-------------------------|
|  Raspbian OS + VNC      |
|  Java 17 + Maven        |
|  JavaFX + Leaflet Map   |
+-------------------------+
```

---

## 🔁 Project Flow

1. **UART Setup**: `GPSReader` opens `/dev/ttyS0`, sets baud rate (9600), and starts reading NMEA frames.
2. **Data Parsing**: Parses `$GPRMC` frames to extract speed, latitude, longitude.
3. **Data Update**: The parsed `GPSData` is shared with `DashboardController`.
4. **JavaFX UI Update**: GUI elements like Gauge, Labels, and Map are updated on the main thread.
5. **Speed Alarm**: If speed > 120 km/h, a red warning and sound alert are triggered.
6. **Map Integration**: Leaflet map updates current location using the GPS coordinates.

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
1. **`App`**
   - **Role**: The entry point of the JavaFX application. It initializes the app, sets up the GPS connection, loads the UI, and cleans up when the app closes.
   - **Key Variables**:
     - `scene: Scene` (static) - Holds the JavaFX UI scene.

2. **`GPSData`**
   - **Role**: A dual-purpose class. It acts as an immutable data holder for GPS information (speed, latitude, longitude, etc.) and statically manages the single `GPSReader` instance.
   - **Key Variables**:
     - Instance fields (`speedKmh`, `latitude`, etc.) - Store specific GPS data points.
     - `reader: GPSReader` (static) - Holds the single `GPSReader` instance shared across the app.

3. **`GPSReader`**
   - **Role**: Handles communication with the GPS device via UART, parses `$GPRMC` NMEA sentences, and updates the latest GPS data.
   - **Key Variables**:
     - `serialPort: SerialPort` - Manages the UART connection.
     - `running: boolean` (volatile) - Controls the reading thread.
     - `latestData: GPSData` - Stores the most recent parsed GPS data.

4. **`PrimaryController`**
   - **Role**: Manages the UI (speedometer, labels) and updates it with GPS data in a separate thread. It also triggers the speed alarm.
   - **Key Variables**:
     - `speedometer: Gauge`, `latitudeLabel: Label`, etc. (FXML) - UI components.
     - `running: boolean` (volatile) - Controls the update thread.

5. **`SpeedAlarm`**
   - **Role**: Plays an audio alarm when speed exceeds a limit and stops it when below.
   - **Key Variables**:
     - `SPEED_LIMIT: int` (static final) - Threshold for alarm (120 km/h).
     - `isPlaying: boolean` (static) - Tracks alarm state.
     - `clip: Clip` (static) - Audio clip for the alarm.

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


---








