/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.speedometer;

import java.io.BufferedInputStream;
import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author omar
 */
public class SpeedAlarm {
    public static final double SPEED_LIMIT = 85; 
    private static boolean isPlaying = false;
    private static Clip clip;
    private static Alert speedAlert = null; // To track the pop-up


    public static void checkSpeed(double speed)
    {
        /* Ensures audio updates run on the JavaFX thread (since `PrimaryController` calls this from UI thread)*/
        Platform.runLater(() -> { 
             /*  Checks if speed exceeds 120 km/h and the alarm isn’t already playing.*/
            if (speed > SPEED_LIMIT && !isPlaying)
            {
                /*Starts the alarm (detailed below)*/
                playAlarm();
                /*Marks the alarm as active.*/
                isPlaying = true;
                showSpeedAlert();
            } 
            else if (speed <= SPEED_LIMIT && isPlaying) /*Checks if speed drops to/below 120 km/h and the alarm is playing*/
            {
                /*Stops the alarm (detailed below)*/
                stopAlarm();
                /*Marks the alarm as inactive*/
                isPlaying = false;
                hideSpeedAlert();
            }
        });
    }

private static void playAlarm()
{
    /*Start a new thread to avoid blocking the JavaFX application thread*/
    new Thread(() -> {
        try {
            /*If the alarm is already playing, do nothing to prevent overlap*/
            if (clip != null && clip.isRunning()) 
            {
                return; // Skip playing again
            }

            /* Attempt to load the audio file as a resource from the classpath.
             *This looks for a file named "mixkit-alert-alarm-1005.wav" in the resources folder
             */
            InputStream resourceStream = SpeedAlarm.class.getResourceAsStream("/mixkit-alert-alarm-1005.wav");

            /*If the file is not found, print an error and exit the method*/
            if (resourceStream == null)
            {
                System.err.println("Audio resource not found!");
                return;
            }

            /* Wrap the raw InputStream in a BufferedInputStream to support mark/reset.
             * This is required by AudioSystem to read the stream correctly
             */
            BufferedInputStream bufferedIn = new BufferedInputStream(resourceStream);

            /*Create an AudioInputStream from the buffered stream (the WAV audio data)*/
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);

            /*Get a Clip instance from the audio system — a Clip is a pre-loaded audio segment that can be started/stopped*/
            clip = AudioSystem.getClip();

            /* Open the clip with the audio input stream — loads the audio data into memory*/
            clip.open(audioInputStream);

            /*Set the clip to loop continuously (will repeat until manually stopped)*/
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            /* Start playing the audio*/
            clip.start();
        } 
        /* Catch any exceptions related to unsupported format, I/O issues, or unavailable audio line*/
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex)
        {
            ex.printStackTrace(); /* Print the error for debugging*/
        }
    }).start(); /*Start the new thread*/
  }


    private static void stopAlarm() 
    { 
        /*Checks if the alarm is playing.*/
        if (clip != null && clip.isRunning())
        {
            /*Stops the audio playback*/
            clip.stop();
            /*Releases audio resources*/
            clip.close();

        }
    }
    private static void showSpeedAlert() 
    {
        if (speedAlert == null) { /*Only create if not already shown*/
            speedAlert = new Alert(AlertType.WARNING);
            speedAlert.setTitle("Speed Warning");
            speedAlert.setHeaderText("Speed Limit Exceeded");
            speedAlert.setContentText("Your speed is above " + SPEED_LIMIT + " km/h!");
            speedAlert.show(); // Show without blocking the app
        }
    }

    private static void hideSpeedAlert() {
        if (speedAlert != null) {
            speedAlert.hide(); /*Hide the pop-up*/
            speedAlert = null; /*Reset to allow a new alert next time*/
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
