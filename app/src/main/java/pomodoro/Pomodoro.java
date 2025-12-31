package pomodoro;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public final class Pomodoro implements PomodoroInterface {

    public int timeLeft;
    private final int workMinutes;
    private final int breakMinutes;
    Clip clip;

    private Timeline timeLine;
    private final Runnable onTick;

    private boolean isPaused = false;
    private boolean inWork = true;
    private boolean waiting = false;

    //takes time in minutes
    public Pomodoro(int workMinutes, int breakMinutes, Runnable onTick) {
        this.workMinutes = workMinutes;
        this.breakMinutes = breakMinutes;
        this.onTick = onTick;
        startWorkTimer();
    }

    public void pause() {
        if (waiting) return;
        isPaused = !isPaused;
        if (isPaused) {
            timeLine.stop();
        }else{
            timeLine.play();
        }
    }

    public void acknowledge() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }

      if (inWork){
          startBreakTimer();
      }else{
          startWorkTimer();
      }

    }

    public boolean isWaiting(){
        return waiting;
    }

    public void startWorkTimer() {
        inWork = true;
        startTimer(workMinutes* 60);
    }

    public boolean endOfTimer = false;

    public void startBreakTimer() {
        startTimer(breakMinutes * 60);
        inWork = false;
    }


    public int getWorkMinutes() {
        return timeLeft / 60;
    }

    public int getWorkSeconds() {
        return timeLeft % 60;
    }

    public void playAlarmSound(String path) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        try {
            URL soundURL = getClass().getResource(path);
            if (soundURL == null) {
                System.err.println("Sound not found: " + path);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.close();
            }

            clip = AudioSystem.getClip(); // assign to class-level clip
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tick() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        if (timeLeft <= 0) {
            timeLine.stop();
            waiting = true;
            onTick.run();
            playAlarmSound("/sounds/morning_alarm.wav");
            return;
        }

        timeLeft--;
        onTick.run();
    }


    private void startTimer(int seconds) {
        timeLeft = seconds;
        isPaused = false;
        waiting = false;

        if (timeLine != null) {
            timeLine.stop();
        }

        timeLine = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            try {
                tick();
            } catch (UnsupportedAudioFileException ex) {
                throw new RuntimeException(ex);
            } catch (LineUnavailableException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }));
        timeLine.setCycleCount(Timeline.INDEFINITE);
        timeLine.play();
    }


}
