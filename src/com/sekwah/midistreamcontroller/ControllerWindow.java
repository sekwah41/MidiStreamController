package com.sekwah.midistreamcontroller;

import com.sekwah.midistreamcontroller.controller.LightData;
import com.sekwah.midistreamcontroller.controller.LightStatus;
import com.sekwah.midistreamcontroller.controller.MidiController;
import com.sekwah.midistreamcontroller.keys.Key;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ControllerWindow extends JFrame {

    private final MidiController midiController;

    private Key[][] keyGrid = new Key[8][8];

    public ControllerWindow() {

        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeFrame();
            }
        });

        this.midiController = new MidiController(this);

        this.midiController.clearLaunchpad();

        this.fillLights(LightData.GREEN_HIGH, LightStatus.STATUS_ON);

        this.setTitle("SekC's Stream Controller");

        this.setSize(300, 60);

        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setLayout(null);

        JLabel keyLabel = new JLabel("No visuals yet, close to stop program.");
        keyLabel.setBounds(10, 3, 280, 20);

        this.add(keyLabel);

        super.setVisible(true);

        this.registerKeys();

        this.showKeys();
    }

    private void registerKeys() {
        this.registerKey(new Key(this.midiController, 1, 1, LightData.YELLOW_HIGH) {
            @Override
            public void run() {

            }
        });

        this.registerKey(new Key(this.midiController, 2, 1, LightData.RED_LOW) {
            @Override
            public void run() {

            }
        });

        this.registerKey(new Key(this.midiController, 3, 1, LightData.RED_LOW) {
            @Override
            public void run() {

            }
        });

        this.registerKey(new Key(this.midiController, 1, 8, LightData.GREEN_HIGH) {
            @Override
            public void run() {

            }
        });

        this.registerKey(new Key(this.midiController, 7, 8, LightData.GREEN_HIGH) {

            private boolean showing = true;

            @Override
            public void run() {
                if(this.showing) {
                    this.showing = false;
                    this.controller.window.setVisible(false);
                    this.controller.setGrid(this.x, this.y, LightData.RED_HIGH, LightStatus.STATUS_ON);
                }
                else {
                    this.showing = true;
                    this.controller.window.setVisible(true);
                    this.controller.setGrid(this.x, this.y, LightData.GREEN_HIGH, LightStatus.STATUS_ON);
                }
            }
        });

        this.registerKey(new Key(this.midiController, 8, 8, LightData.GREEN_HIGH) {

            private boolean showing = true;

            @Override
            public void run() {
                this.controller.window.closeFrame();
            }
        });
    }

    private void registerKey(Key key) {
        this.keyGrid[key.getX() - 1][key.getY() - 1] = key;
    }

    private void showKeys() {
        try {
            for (int y = 1; y <= 8; y++) {
                for (int x = 1; x <= 8; x++) {
                    Key key = this.keyGrid[x - 1][y - 1];
                    if(key == null) {
                        this.midiController.setGrid(x, y, LightData.OFF, LightStatus.STATUS_OFF);
                    }
                    else {
                        this.midiController.setGrid(x, y, key.getDefaultColor(), LightStatus.STATUS_ON);
                    }
                }
                Thread.sleep(50);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void fillLights(LightData color, LightStatus status) {
        try {
            for(int y = 1; y <= 8; y++) {
                for(int x = 1; x <= 8; x++) {
                    this.midiController.setGrid(x, y, color, status);
                }

                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeFrame() {
        this.fillLights(LightData.RED_HIGH, LightStatus.STATUS_ON);
        this.fillLights(LightData.RED_HIGH, LightStatus.STATUS_OFF);
        this.midiController.clearLaunchpad();
        this.dispose();
        System.exit(0);
    }


    public void runKey(int x, int y) {
        Key key = this.keyGrid[x][y];
        if(key != null) {
            key.run();
        }
    }
}
