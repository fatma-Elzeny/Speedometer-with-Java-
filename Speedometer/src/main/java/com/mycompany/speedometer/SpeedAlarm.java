/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.speedometer;

import java.io.File;
import java.io.IOException;
import javafx.application.Platform;
import javax.sound.sampled.*;

/**
 *
 * @author omar
 */
public class SpeedAlarm
{
    private static final int SPEED_LIMIT = 120; 
    private static boolean isPlaying = false;
    private static Clip clip;

    public static void checkSpeed(double speed)
    {
        Platform.runLater(() -> {  // Ensure speed check happens on JavaFX thread
            if (speed > SPEED_LIMIT && !isPlaying)
            {
                playAlarm();
                isPlaying = true;
            } 
            else if (speed <= SPEED_LIMIT && isPlaying)
            {
                stopAlarm();
                isPlaying = false;
            }
        });
    }

    private static void playAlarm()
    {
        new Thread(() -> {  // Run audio in a separate thread
            try {
                if (clip != null && clip.isRunning()) {
                    return; // Prevent duplicate alarms
                }

                File audioFile = new File("/home/omar/Downloads/mixkit-alert-alarm-1005.wav");
                javax.sound.sampled.AudioInputStream audioInputStream = javax.sound.sampled.AudioSystem.getAudioInputStream(audioFile);
                clip = javax.sound.sampled.AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.loop(javax.sound.sampled.Clip.LOOP_CONTINUOUSLY);
                clip.start();

              } 
            catch (javax.sound.sampled.UnsupportedAudioFileException | IOException | javax.sound.sampled.LineUnavailableException ex)
            {
                ex.printStackTrace();
            }
        }).start();
    }

    private static void stopAlarm() 
    {
        if (clip != null && clip.isRunning())
        {
            clip.stop();
            clip.close();

        }
    }
}

