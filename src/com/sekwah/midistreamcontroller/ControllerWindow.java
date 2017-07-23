package com.sekwah.midistreamcontroller;

import com.sekwah.midistreamcontroller.controller.LightData;
import com.sekwah.midistreamcontroller.controller.LightStatus;
import com.sekwah.midistreamcontroller.controller.MidiController;
import com.sekwah.midistreamcontroller.keys.Key;
import com.sekwah.midistreamcontroller.keys.SceneKey;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ControllerWindow extends JFrame {

    private final MidiController midiController;

    private Key[][] keyGrid = new Key[8][8];

    private long currentTime = 0;

    private int sceneCount = 3;

    private boolean streaming = false;

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

        for(int i = 1; i <= sceneCount; i++) {
            LightData color;
            if(i == 1) {
                color = LightData.YELLOW_HIGH;
            }
            else {
                color = LightData.RED_LOW;
            }
            this.registerKey(new SceneKey(this.midiController, i, 1, color, i) {
                @Override
                public void run() {
                    selectScene(this.sceneId);
                    this.runKeys(KeyEvent.VK_NUMPAD0 + this.sceneId);
                }
            });
        }

        this.registerKey(new Key(this.midiController, 1, 8, LightData.GREEN_HIGH) {

            @Override
            public void run() {
                if(streaming) {
                    streaming = false;
                    controller.setGrid(this.getX(), this.getY(), LightData.GREEN_HIGH, LightStatus.STATUS_ON);
                    controller.setGrid(8, 8, LightData.RED_HIGH, LightStatus.STATUS_ON);
                    this.runKeys(KeyEvent.VK_ALT, KeyEvent.VK_SUBTRACT);
                }
                else {
                    streaming = true;
                    controller.setGrid(this.getX(), this.getY(), LightData.RED_HIGH, LightStatus.STATUS_ON);
                    controller.setGrid(8, 8, LightData.RED_HIGH, LightStatus.STATUS_OFF);
                    this.runKeys(KeyEvent.VK_ALT, KeyEvent.VK_ADD);
                }
            }
        });

        // Mic audio
        this.registerKey(new Key(this.midiController, 4, 8, LightData.GREEN_HIGH) {

            private boolean muted = true;

            @Override
            public void run() {
                if(this.muted) {
                    this.muted = false;
                    this.runKeys(KeyEvent.VK_NUMPAD6);
                    controller.setGrid(this.x, this.y, LightData.RED_HIGH, LightStatus.STATUS_ON);
                }
                else {
                    this.muted = true;
                    this.runKeys(KeyEvent.VK_NUMPAD9);
                    this.controller.setGrid(this.x, this.y, LightData.GREEN_HIGH, LightStatus.STATUS_ON);
                }
            }
        });

        // Desktop audio
        this.registerKey(new Key(this.midiController, 5, 8, LightData.GREEN_HIGH) {

            private boolean muted = true;

            @Override
            public void run() {
                if(this.muted) {
                    this.muted = false;
                    this.runKeys(KeyEvent.VK_NUMPAD7);
                    controller.setGrid(this.x, this.y, LightData.RED_HIGH, LightStatus.STATUS_ON);
                }
                else {
                    this.muted = true;
                    this.runKeys(KeyEvent.VK_NUMPAD8);
                    this.controller.setGrid(this.x, this.y, LightData.GREEN_HIGH, LightStatus.STATUS_ON);
                }
            }
        });

        this.registerKey(new Key(this.midiController, 7, 8, LightData.GREEN_HIGH) {

            private boolean showing = true;

            @Override
            public void run() {
                if(this.showing) {
                    this.showing = false;
                    setVisible(false);
                    controller.setGrid(this.x, this.y, LightData.RED_HIGH, LightStatus.STATUS_ON);
                }
                else {
                    this.showing = true;
                    setVisible(true);
                    this.controller.setGrid(this.x, this.y, LightData.GREEN_HIGH, LightStatus.STATUS_ON);
                }
            }
        });

        this.registerKey(new Key(this.midiController, 8, 8, LightData.RED_HIGH) {

            @Override
            public void run() {
                if(streaming) {
                    return;
                }
                closeFrame();
            }
        });
    }

    /**
     * Can only do maximum of 8 atm due to how that is coded though i wont need more really...
     */
    public void selectScene(int value) {
        for(int i = 1; i <= sceneCount; i++) {
            LightData color;
            if(i == value) {
                color = LightData.YELLOW_HIGH;
            }
            else {
                color = LightData.RED_LOW;
            }
            this.midiController.setGrid(i, 1, color, LightStatus.STATUS_ON);
        }
    }

    private void registerKey(Key key) {
        this.keyGrid[key.getX() - 1][key.getY() - 1] = key;
    }

    private void showKeys() {
        try {
            for (int y = 1; y <= 8; y++) {
                this.currentTime = System.currentTimeMillis();
                for (int x = 1; x <= 8; x++) {
                    Key key = this.keyGrid[x - 1][y - 1];
                    if(key == null) {
                        this.midiController.setGrid(x, y, LightData.OFF, LightStatus.STATUS_OFF);
                    }
                    else {
                        this.midiController.setGrid(x, y, key.getDefaultColor(), LightStatus.STATUS_ON);
                    }
                }
                Thread.sleep(this.delayWithTimeDifference(50));
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private long delayWithTimeDifference(int delay) {
        long diff = this.currentTime - System.currentTimeMillis() + delay;
        return diff > 0 ? diff : 0;
    }

    private void fillLights(LightData color, LightStatus status) {
        try {
            for(int y = 1; y <= 8; y++) {
                this.currentTime = System.currentTimeMillis();
                for(int x = 1; x <= 8; x++) {
                    this.midiController.setGrid(x, y, color, status);
                }

                Thread.sleep(this.delayWithTimeDifference(50));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeFrame() {
        this.closeLights(LightData.RED_HIGH, LightStatus.STATUS_ON);
        this.closeLightsRev(LightData.RED_HIGH, LightStatus.STATUS_OFF);
        this.midiController.clearLaunchpad();
        this.dispose();
        System.exit(0);
    }

    private void closeLightsRev(LightData color, LightStatus status) {
        try {
            for (int i = 7; i >= 0; i--) {
                this.closeLightFrame(i, color, status);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeLights(LightData color, LightStatus status) {
        try {
            for (int i = 0; i < 8; i++) {
                this.closeLightFrame(i, color, status);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeLightFrame(int i, LightData color, LightStatus status) throws InterruptedException {
        this.currentTime = System.currentTimeMillis();
        for (int x = 8; x >= 8 - i; x--) {
            this.midiController.setGrid(x, 8 - i, color, status);
        }
        for (int y = 8; y >= 8 - i; y--) {
            this.midiController.setGrid(8 - i, y, color, status);
        }
        Thread.sleep(this.delayWithTimeDifference(50));
    }


    public void runKey(int x, int y) {
        if(x < 0 || x > 15 || y < 0 || y > 15) {
            return;
        }
        Key key = this.keyGrid[x][y];
        if(key != null) {
            key.run();
        }
    }
}
