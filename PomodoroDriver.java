
import java.util.Scanner;

/**
 * @author ShaelynnNixon
 *
 */
public class PomodoroDriver {

    public static void main(String args[]) {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter work time in minutes: ");
        int workMinutes = scanner.nextInt();
        System.out.println("Enter break time in minutes: ");
        int breakMinutes = scanner.nextInt();
        System.out.println("Starting timer with " + workMinutes + " minutes of work and " + breakMinutes + " minutes of break.");
        System.out.println("Press enter to start the timer and enter to stop the timer.");
        scanner.nextLine();
        Pomodoro p = new Pomodoro(workMinutes, breakMinutes);

        new Thread(()
                -> {
            while (true) {
                scanner.nextLine();
                p.pause();
            }
        }).start();

       

    }
}
