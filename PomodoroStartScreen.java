
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Screen to greet the user, get the work time and break times from the user,
 * and the task that the use will be working on
 *
 * @author ShaelynnNixon
 */
public class PomodoroStartScreen {

    int screenWidth;
    int screenHeight;

    public PomodoroStartScreen() {
        JFrame frame = new JFrame();
        frame.setLayout(new BoxLayout());
        JPanel taskPanel = new JPanel();
        JPanel workMinutePanel = new JPanel();
        JPanel breakMinutePanel = new JPanel();
        JLabel label = new JLabel();

        label.setText("Welcome to my Pomodoro Timer!");
        frame.add(label);

        JTextField taskField = new JTextField("Enter the name of your task here", 16);
        taskPanel.add(taskField);

        JTextField workMinutesField = new JTextField("Enter the the Minutes you would like to work for: ", 16);
        workMinutePanel.add(workMinutesField);
        JTextField breakMinutesField = new JTextField("Enter the the Minutes you would like to break for: ", 16);
        breakMinutePanel.add(breakMinutesField);

        JButton button = new JButton("Submit");
        taskPanel.add(button);

        frame.add(taskPanel);
        frame.add(workMinutePanel);
        frame.add(breakMinutePanel);
        frame.setSize(500, 500);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(1);
    }

    public static void main(String[] args) {
        new PomodoroStartScreen();
    }

}
