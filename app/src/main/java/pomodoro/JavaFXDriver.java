/**
 * GUI driver class for pomodoro.Pomodoro.java
 *
 * @author ShaelynnJNixon
 */
package pomodoro;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.sql.*;


public class JavaFXDriver extends Application {
    Stage window;
    Label timeLabel;
    Pomodoro p;
    Button pauseButton;
    Button startButton;
    Button acknowledgeButton;
    static String taskName;
    Label taskLabel;
    static int minutesWorked = 0;
    Button saveButton;
    Scene scene2;
    boolean paused = false;
    int workMinutes = 45;
    int breakMinutes = 5;
    Scene scene1;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        window = primaryStage;
        TextField workField = new TextField();
        workField.setPromptText("Work minutes");

        TextField breakField = new TextField();
        breakField.setPromptText("Break minutes");

        TextField taskField = new TextField();
        taskField.setPromptText("Task name");

        Label errorLabel = new Label();

        startButton = new Button("Start");

        VBox layout1 = new VBox(15);
        layout1.setPadding(new Insets(30));
        layout1.setAlignment(Pos.CENTER);

        scene1 = new Scene(layout1, 500, 500);

        //scene 2

        timeLabel = new Label("00:00");
        taskLabel = new Label("Task name: ");
        timeLabel.setId("timer-label");
        pauseButton = new Button("Pause");
        acknowledgeButton = new Button("Acknowledge");
        acknowledgeButton.setDisable(true);
        saveButton = new Button("Save");

        VBox layout2 = new VBox(10, taskLabel, timeLabel, pauseButton, acknowledgeButton, saveButton);
        scene2 = new Scene(layout2, 500, 500);

        pauseButton.setOnAction(e -> {
            if (p != null) p.pause();
            if (paused) {
                paused = false;
                pauseButton.setText("Pause");

                updateLabel();
            } else {
                paused = true;
                pauseButton.setText("Start");
                updateLabel();
            }
        });

        acknowledgeButton.setOnAction(e -> {
            if (p != null) p.acknowledge();
        });

        saveButton.setOnAction(e -> dumpToDatabase());


        startButton.setOnAction(e -> {
            try {
                int workMinutes = Integer.parseInt(workField.getText());
                int breakMinutes = Integer.parseInt(breakField.getText());
                taskName = taskField.getText();
                taskLabel.setText("Current Task: " + taskName);
                Task newTask = new Task(taskName, 0);


                if (workMinutes <= 0 || breakMinutes <= 0) {
                    throw new NumberFormatException();
                }

                p = new Pomodoro(workMinutes, breakMinutes, this::updateLabel);
                window.setScene(scene2);


            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter valid positive numbers.");
            }
        });

        layout1.setAlignment(Pos.TOP_LEFT);
        layout2.setAlignment(Pos.CENTER);
        layout1.getChildren().addAll(workField, breakField, taskField, startButton, errorLabel);
        scene1.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        scene2.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        window.setTitle("Pomodoro Timer");
        getTaskButtons(layout1);
        window.setScene(scene1);
        window.show();
    }

    private void updateLabel() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                timeLabel.setText(String.format("%02d:%02d", p.timeLeft / 60, p.timeLeft % 60));

                acknowledgeButton.setDisable(!p.isWaiting());

            }


        });
    }

    public static void dumpToDatabase() {
        try (Connection conn =
                     DriverManager.getConnection("jdbc:sqlite:pomodoro.db")) {

            String createSql =
                    "CREATE TABLE IF NOT EXISTS tasks (" +
                            "taskName TEXT NOT NULL, " +
                            "minutesWorked INTEGER NOT NULL" +
                            ")";
            conn.createStatement().execute(createSql);

            String insertSql =
                    "INSERT INTO tasks (taskName, minutesWorked) VALUES (?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, taskName);
                pstmt.setInt(2, minutesWorked);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection openIfExists(String dbPath) throws SQLException {
        File dbFile = new File(dbPath);

        if (!dbFile.exists()) {
            // Database does not exist → do nothing
            return null;
        }

        String url = "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(url);
    }

    private void getTaskButtons(VBox container) throws SQLException {
        File dbFile = new File("pomodoro.db");
        if (!dbFile.exists()) {
            return; // No DB → no buttons
        }

        String url = "jdbc:sqlite:pomodoro.db";
        String query = "SELECT DISTINCT taskName FROM tasks";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String task = rs.getString("taskName");

                Button taskButton = new Button(task);
                taskButton.setMaxWidth(Double.MAX_VALUE);

                taskButton.setOnAction(e -> {
                    taskName = task;
                    taskLabel.setText("Current Task: " + task);
                    p = new Pomodoro(workMinutes, breakMinutes, this::updateLabel);
                    window.setScene(scene2);
                });

                container.getChildren().add(taskButton);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
