
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public final class Pomodoro implements PomodoroInterface {

    public int timeLeft;
    private int workMinutes;
    private int breakMinutes;

    private Timer timer;
    private Runnable onTick;

    private boolean isPaused = false;
    private boolean inWork = true;

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

    public void startWorkTimer() {
        inWork = true;
        startTimer(workMinutes * 60);
    }

    public void startBreakTimer() {
        inWork = false;
        startTimer(breakMinutes * 60);
    }

    private void startTimer(int seconds) {
        timeLeft = seconds;
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isPaused) {
                    timeLeft--;
                    onTick.run();

                    if (timeLeft < 0){
                        timer.stop();
                        if (inWork){
                            startBreakTimer();
                        }else{
                            startWorkTimer();
                        }
                    }
                }
            }
        });
        timer.start();
    }

}
