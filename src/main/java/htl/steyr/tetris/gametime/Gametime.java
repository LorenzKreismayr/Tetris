package htl.steyr.tetris.gametime;

import javafx.application.Platform;
import javafx.scene.control.Label;

public class Gametime {

        private int totalSeconds = 0;
        private boolean running = false;
        private Thread timerThread;
        private Label gametimeLabel;

        public Gametime(Label gametimeLabel) {
            this.gametimeLabel = gametimeLabel;
        }

        public void start() {
            running = true;
            timerThread = new Thread(() -> {
                try {
                    while (running) {
                        Thread.sleep(1000);
                        totalSeconds++;
                        updateLabel();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            timerThread.setDaemon(true);
            timerThread.start();
        }

        public void stop() {
            running = false;
            if (timerThread != null) {
                timerThread.interrupt();
            }
        }

        public void reset() {
            stop();
            totalSeconds = 0;
            updateLabel();
        }

        private void updateLabel() {
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;
            String formatted = String.format("%02d:%02d", minutes, seconds);
            Platform.runLater(() -> {
                gametimeLabel.setText(formatted);
            });
        }

        public int getTotalSeconds() {
            return totalSeconds;
        }
    }

