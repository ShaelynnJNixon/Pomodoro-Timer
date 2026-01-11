package pomodoro;

public class Task {
    String taskName;
    int minutesWorked;
    int minutesBreak;
    int minutesWork;

    public Task(String taskName, int minutesWorked, int minutesBreak, int minutesWork) {
        this.taskName = taskName;
        this.minutesWorked = minutesWorked;
        this.minutesBreak = minutesBreak;
        this.minutesWork = minutesWork;
    }

    public Task(String taskName, int minutesBreak, int minutesWork) {
        this.taskName = taskName;
        this.minutesBreak = minutesBreak;
        this.minutesWork = minutesWork;
    }
}
