
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.Timer;

public final class Pomodoro implements PomodoroInterface {

    public int timeLeft;
    private int workMinutes;
    private int breakMinutes;
    Clip clip;

    private Timer timer;
    private Runnable onTick;

    private boolean isPaused = false;
    private boolean acknowledged = false;
    private boolean inWork = true;
    private Runnable callback;

    public int getWorkMinutes() {
        return timeLeft / 60;
    }

    public int getWorkSeconds() {
        return timeLeft % 60;
    }

    public void playAlarmSound(String path) {

        try {
            URL soundURL = PomodoroGUI.class.getResource(path);
            if (soundURL == null) {
                System.err.println("sound not found.");
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    //takes time in minutes
    public Pomodoro(int workMinutes, int breakMinutes, Runnable onTick) {
        this.workMinutes = workMinutes;
        this.breakMinutes = breakMinutes;
        this.onTick = onTick;
        startWorkTimer();
    }

    public void pause() {
        isPaused = !isPaused;
        System.out.println(isPaused ? "Timer paused\n" : "Timer resumed\n");
    }

    public void acknowledge() {
        isPaused = !isPaused;
        System.out.println("Acknowledged\n");
        clip.stop();

    }

    public void startWorkTimer() {
        inWork = true;
        startTimer(workMinutes * 60); 
    }

    public boolean endOfTimer = false;

    public void startBreakTimer() {
        startTimer(breakMinutes * 60);
        inWork = false;
    }

    private void startTimer(int seconds) {
        timeLeft = seconds;
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isPaused) {
                    timeLeft--;
                    onTick.run();

                    if (timeLeft < 0) {
                        endOfTimer = true;

                        playAlarmSound("morning_alarm.wav");

                        timer.stop();
                        isPaused = true;

                        if (inWork) {
                            endOfTimer = false;
                            startBreakTimer();
                        } else {
                            endOfTimer = false;
                            startWorkTimer();
                        }
                    }
                }
            }
        });
        timer.start();
    }

}
