/**
 * 
 * GUI and Driver class for Pomodoro.java
 * 
 * @author ShaelynnNixon
 */
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.*;

public class PomodoroGUI {

    private Pomodoro p;
    private JLabel label = new JLabel();
    JFrame frame;
    JPanel EOTPanel;
    int height = 800;
    int width = 700;

    public PomodoroGUI() {
        p = new Pomodoro(1, 1, () -> updateLabel());
        frame = new JFrame("Pomodoro Timer");
        JPanel panel = new JPanel();
        frame.setLayout(new FlowLayout());

        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Font labelFont = new Font("Helvetica", Font.BOLD, 48);
        label.setFont(labelFont);
        label.setAlignmentY(height / 2);
        label.setText(String.format("%02d:%02d", p.timeLeft / 60, p.timeLeft % 60));
        frame.add(label);

        JButton toggleButton = new JButton("Pause");

        toggleButton.addActionListener(e -> p.pause());
        frame.add(toggleButton);

        //end of timer panel
        EOTPanel = new JPanel();
        EOTPanel.setLayout(new FlowLayout());

        JButton EOTbutton = new JButton("OKAY ! ");
        EOTPanel.add(EOTbutton);

        EOTbutton.addActionListener(e -> p.acknowledge());
        EOTbutton.addActionListener(e -> EOTPanel.setVisible(false));
        EOTPanel.setVisible(false);
        EOTbutton.setVisible(true);
        frame.add(EOTPanel);

        frame.setVisible(true);

    }

    private void updateLabel() {
        SwingUtilities.invokeLater(() -> {
            if (p.timeLeft > 0){
            int minutes = p.timeLeft / 60;
            int seconds = p.timeLeft % 60;
            label.setText(String.format("%02d:%02d", minutes, seconds));
            }

            
            else{
                
                EOTPanel.setVisible(true);
                label.setText(String.format("%02d:%02d", 0, 0));
                frame.repaint();
                frame.revalidate();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PomodoroGUI());
    }
}
