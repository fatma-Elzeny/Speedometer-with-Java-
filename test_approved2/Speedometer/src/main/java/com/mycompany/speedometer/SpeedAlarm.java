package com.mycompany.speedometer;

import javafx.application.Platform;
import javax.sound.sampled.*;

public class SpeedAlarm {
    private static final int SPEED_LIMIT = 120;
    private static boolean isPlaying = false;
    private static Clip clip;

    public static void checkSpeed(double speed) {
        Platform.runLater(() -> {
            if (speed > SPEED_LIMIT && !isPlaying) {
                playAlarm();
                isPlaying = true;
            } else if (speed <= SPEED_LIMIT && isPlaying) {
                stopAlarm();
                isPlaying = false;
            }
        });
    }

    private static void playAlarm() {
        new Thread(() -> {
            try {
                if (clip != null && clip.isRunning()) {
                    return;
                }
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(SpeedAlarm.class.getResourceAsStream("/alert.wav"));
                clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private static void stopAlarm() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }

    public static void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
        isPlaying = false;
    }
}