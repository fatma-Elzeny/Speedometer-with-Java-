# JavaFX GPS Speedometer Dashboard (Raspberry Pi Edition)

## 📌 Project Overview
This project is a **JavaFX-based Speedometer Dashboard** designed to run on a **Raspberry Pi with Raspbian OS**. It connects to a **UART-based GPS module** (e.g., NEO-7M) via `/dev/ttyS0` to display real-time **speed**, **latitude**, and **longitude**. Key features include:

- A dynamic speedometer gauge powered by the **Medusa library**.
- Live GPS coordinate labels.
- Audio and pop-up alerts when speed exceeds **120 km/h**.
- Remote monitoring capability via **VNC**.

The application is built for simplicity and efficiency, making it suitable for vehicle speed monitoring or outdoor GPS tracking on a Raspberry Pi.

---

## 🚀 Features
- 📡 **Real-time GPS Data**: Reads latitude, longitude, and speed from a GPS module using NMEA `$GPRMC` sentences.
- 🚗 **Live Speed Monitoring**: Displays speed in km/h on a customizable speedometer gauge.
- ⚠ **Over-Speed Alerts**: Triggers an audio alarm and a pop-up warning when speed exceeds 120 km/h.
- 🖥 **Raspberry Pi Compatible**: Runs on Raspbian with VNC for remote GUI access.

---

## 🔍 Main Implementation Points
- **UART GPS Parsing**: Processes `$GPRMC` NMEA frames to extract speed and coordinates.
- **Real-time UI Updates**: Updates the speedometer and labels every 2 seconds using a background thread.
- **Custom Gauge**: Uses Medusa’s `Gauge` for a visually appealing speedometer with colored sections (green: 0-50, yellow: 50-100, red: 100-160).
- **Alert System**: Combines audio playback (`alert.wav`) and a JavaFX `Alert` dialog for over-speed warnings.
- **Clean Shutdown**: Properly closes UART connections and stops audio on app exit.

---

### 📂 Project Structure

```
SpeedometerProject/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com.mycompany.speedometer/
│   │   │   │   ├── App.java              # JavaFX application entry point
│   │   │   │   ├── GPSData.java          # GPS data model and GPSReader manager
│   │   │   │   ├── GPSReader.java        # UART GPS data reader and parser
│   │   │   │   ├── PrimaryController.java # JavaFX UI controller
│   │   │   │   ├── SpeedAlarm.java        # Audio and pop-up alerts for speed limits
│   │   └── resources/
│   │       ├── primary.fxml               # JavaFX UI layout (Scene Builder)
│   │       ├── alert.wav                  # Audio file for speed alert
└── pom.xml                                 # Maven configuration
```

---

## 🧱 Software Architecture

```
+-------------------------+
|   JavaFX Application    |
|-------------------------|
|  App.java               | ← Launches app, sets up GPSReader
|  └─ Loads primary.fxml  |
|                         |
|  ┌───────────────┐      |
|  │PrimaryController│◄────┐
|  │(UI Management) │     │
|  └───────────────┘     ▼
|     ▲                 ┌────────────┐
|     | Updates UI      │ SpeedAlarm │
|     |                 │(Audio/Pop-up)│
|  ┌───────────────┐   └────────────┘
|  │  GPSReader    │         ▲
|  │  (UART Logic) │         |
|  └───────────────┘         |
|         ▲                  |
|     Reads/Writes GPSData  |
|         ▲                  |
|  ┌───────────────┐         |
|  │   GPSData     │◄────────┘
|  │(Data & Manager)│
|  └───────────────┘
|         ▲
|     Serial Comm
+---------┼---------------+
          ▼
   /dev/ttyS0 (GPS Module)
```

---

## 🔌 Hardware Architecture

```
+-------------------------+
|     GPS Module          |
|     (e.g., NEO-7M)      |
|-------------------------|
| TX  ───────► GPIO 15    |
| RX  ◄─────── GPIO 14    |
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
|  JavaFX + Medusa        |
+-------------------------+
```

---

## 🔁 Project Flow

1. **App Startup**:
   - `App` creates a `GPSReader` and starts UART communication on `/dev/ttyS0`.
   - Stores the `GPSReader` in `GPSData` for shared access.
   - Loads `primary.fxml` and initializes `PrimaryController`.

2. **GPS Data Reading**:
   - `GPSReader` opens `/dev/ttyS0` (9600 baud) and runs a thread to read NMEA `$GPRMC` sentences.
   - Parses speed (km/h), latitude, and longitude, updating `latestData` in `GPSReader`.

3. **UI Updates**:
   - `PrimaryController` runs a thread to fetch `latestData` from `GPSReader` (via `GPSData`) every 2 seconds.
   - Updates the speedometer gauge and coordinate labels on the JavaFX thread.

4. **Speed Alerts**:
   - If speed > 120 km/h, `SpeedAlarm` plays `alert.wav` in a loop and shows a pop-up warning.
   - When speed drops to ≤ 120 km/h, the audio stops and the pop-up closes.

5. **Shutdown**:
   - `App.stop()` closes the UART connection and stops any active alerts.

---

## 🛠 Dependencies
Update your `pom.xml` with these dependencies:

```xml
<dependencies>
    <!-- JavaFX Modules -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>17.0.1</version>
    </dependency>
    <!-- Medusa Gauge Library -->
    <dependency>
        <groupId>eu.hansolo</groupId>
        <artifactId>medusa</artifactId>
        <version>11.6</version>
    </dependency>
    <!-- jSerialComm for UART -->
    <dependency>
        <groupId>com.fazecast</groupId>
        <artifactId>jSerialComm</artifactId>
        <version>2.9.2</version>
    </dependency>
</dependencies>
```

---

## 🎛 Medusa Library
The **Medusa library**, developed by Hansolo (Gerrit Grunwald), is a powerful open-source Java library for creating customizable gauges and dashboards in JavaFX applications. It provides a wide range of gauge types (e.g., radial, linear) with extensive styling options, making it ideal for real-time data visualization.

### Role in the Project
- **Speedometer Gauge**: Medusa’s `Gauge` class is used in `PrimaryController` to display the vehicle’s speed in km/h. It’s configured with:
  - **Range**: 0 to 160 km/h.
  - **Sections**: Colored zones (green: 0-50, yellow: 50-100, red: 100-160) for quick visual feedback.
  - **Styling**: Red needle, black background, white values, and animated transitions.
- **Real-time Updates**: The gauge updates every 2 seconds with speed data from `GPSReader`, leveraging Medusa’s smooth animation capabilities.

### Why Medusa?
- **Ease of Use**: Simplifies creating professional gauges without building from scratch.
- **Customization**: Offers properties like `needleColor`, `sections`, and `unit` to match the project’s aesthetic.
- **Performance**: Lightweight and optimized for JavaFX, suitable for Raspberry Pi’s limited resources.

### Example Configuration
```java
speedometer.setMinValue(0);
speedometer.setMaxValue(160);
speedometer.setSections(
    new Section(0, 50, Color.GREEN),
    new Section(50, 100, Color.YELLOW),
    new Section(100, 160, Color.RED)
);
speedometer.setNeedleColor(Color.RED);
speedometer.setAnimated(true);
```

For more details, visit the [Medusa GitHub repository](https://github.com/HanSolo/Medusa).

---

## 📆 Flow of Execution
1. **`App.java`**:
   - Launches the JavaFX app, initializes `GPSReader`, and loads `primary.fxml`.
2. **`GPSReader.java`**:
   - Opens `/dev/ttyS0`, reads NMEA data in a thread, and updates `latestData`.
3. **`GPSData.java`**:
   - Stores parsed GPS data and provides access to the `GPSReader`.
4. **`PrimaryController.java`**:
   - Configures the speedometer.
   - Updates UI with speed and coordinates every 2 seconds.
   - Triggers `SpeedAlarm` when speed exceeds 120 km/h.
5. **`SpeedAlarm.java`**:
   - Plays an audio alert and shows a pop-up when speed > 120 km/h.
   - Stops both when speed ≤ 120 km/h.

---

## 📲 Functions Breakdown
1. **`App`**
   - **Role**: Launches the app, sets up `GPSReader`, loads the UI, and handles shutdown.
   - **Key Variables**: `scene` (static) - Manages the JavaFX UI.

2. **`GPSData`**
   - **Role**: Holds GPS data (speed, coordinates) and statically manages the `GPSReader`.
   - **Key Variables**: 
     - Instance: `speedKmh`, `latitude`, etc. - GPS data points.
     - Static: `reader` - Shared `GPSReader` instance.

3. **`GPSReader`**
   - **Role**: Reads UART data, parses `$GPRMC`, and updates `latestData`.
   - **Key Variables**: 
     - `serialPort` - UART connection.
     - `running` (volatile) - Thread control.
     - `latestData` - Latest GPS data.

4. **`PrimaryController`**
   - **Role**: Manages UI updates and triggers alerts.
   - **Key Variables**: 
     - `speedometer`, `latitudeLabel`, etc. (FXML) - UI elements.
     - `running` (volatile) - Update thread control.

5. **`SpeedAlarm`**
   - **Role**: Handles audio and pop-up alerts for speed > 120 km/h.
   - **Key Variables**: 
     - `SPEED_LIMIT` (static final) - 120 km/h threshold.
     - `isPlaying` (static) - Tracks alert state.
     - `clip` (static) - Audio playback.
     - `speedAlert` (static) - Pop-up dialog.

---

## 📅 How to Build & Run

### 1. Install Java with JavaFX
- Use **SDKMAN!** to install Java 17 with JavaFX:
```bash
curl -s "https://get.sdkman.io" | bash
# Open a new terminal
sdk install java 17.0.1.fx-zulu
java -version  # Should show OpenJDK 17 with JavaFX
sudo apt install maven
```

### 2. Clone & Install Dependencies
```bash
git clone <your-repo-url>  # Replace with your actual repo URL
cd SpeedometerProject
mvn clean install
```

### 3. Connect GPS to Raspberry Pi
| GPS Pin | RPi Pin  |
|---------|----------|
| VCC     | 5V       |
| GND     | GND      |
| TX      | GPIO 15 (RXD) |
| RX      | GPIO 14 (TXD) |

Enable UART and VNC:
```bash
sudo raspi-config
# Interface Options > Serial Port > No console, Yes UART
# Interface Options > VNC > Yes
sudo reboot
```

### 4. Run the App on Raspberry Pi
```bash
cd SpeedometerProject
sudo mvn javafx:run
```

### 5. View GUI Remotely with VNC
- Find the Raspberry Pi’s IP:
```bash
ifconfig  # Look for wlan0 or eth0 IP, e.g., 192.168.1.100
```
- On your remote device (e.g., laptop):
```bash
vncviewer <rpi-ip>  # e.g., vncviewer 192.168.1.100
```

---

## 🚫 Alerts
- **Trigger**: Speed > 120 km/h.
- **Audio**: Plays `alert.wav` in a loop (stored in `src/main/resources/`).
- **Pop-up**: Displays a JavaFX `Alert` with "Speed Limit Exceeded" message.
- **Stop**: Audio and pop-up stop when speed ≤ 120 km/h.

---

## 🎉 Final Outcome
- Real-time speedometer and GPS coordinate display.
- UART-based GPS parsing (`$GPRMC`).
- Audio and visual alerts for over-speed conditions.
- Remote monitoring via VNC on Raspberry Pi.
- Ideal for vehicle dashboards or GPS tracking applications.

---

## ✨ Authors
- Developed by: [Fatma Yosry, Mohamed Awadin, Omar Alaa]
---
