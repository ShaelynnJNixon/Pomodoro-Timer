
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
        scanner.close();
        Pomodoro p = new Pomodoro(workMinutes, breakMinutes);
    }
}
