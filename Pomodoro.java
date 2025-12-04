
import java.util.Timer;
import java.util.TimerTask;

public final class Pomodoro implements PomodoroInterface{

    int timeLeft;
    int workMinutes;
    int breakMinutes;

    //takes time in minutes
    public Pomodoro(int workMinutes, int breakMinutes) {
        this.workMinutes = workMinutes;
        this.breakMinutes = breakMinutes;
        startWorkTimer();
    }

    public void startWorkTimer() {
        timeLeft = workMinutes * 60;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("Work minutes remaining: " + (timeLeft / 60) + " Seconds Remaining: " + (timeLeft % 60) + "\n");
                timeLeft--;
                if (timeLeft < 0) {
                    timer.cancel();
                    startBreakTimer();
                }
            }
        }, 0, 1000);
    }

    public void startBreakTimer() {
        timeLeft = breakMinutes * 60;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("Break minutes remaining: " + (timeLeft / 60) + " Seconds Remaining: " + (timeLeft % 60) + "\n");
                timeLeft--;
                if (timeLeft < 0) {
                    timer.cancel();
                    startWorkTimer();
                }
            }
        }, 1, 1000);
    }

}
