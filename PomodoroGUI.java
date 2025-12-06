
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class PomodoroGUI extends JFrame {

    private Pomodoro p;
    private JLabel label = new JLabel();

    public PomodoroGUI() {
        setTitle("Pomodoro Timer");
        setSize(700, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        label.setFont(label.getFont().deriveFont(48f));
        add(label);
        p = new Pomodoro(25, 5, () -> updateLabel());
        setVisible(true);

    }

    private void updateLabel() {
        int minutes = p.timeLeft / 60;
        int seconds = p.timeLeft % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(()-> new PomodoroGUI());
    }
}
