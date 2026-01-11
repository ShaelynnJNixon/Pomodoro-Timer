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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
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
    static int workMinutes;
    static int breakMinutes;
    Scene scene1;
    int totalSecondsWorked = 0;
    Line clockHand;
    javafx.scene.shape.Circle clockFace;
    javafx.scene.shape.Circle clockSpacer;
    javafx.scene.transform.Rotate handRotation = new javafx.scene.transform.Rotate();
    javafx.scene.shape.Arc progressArc;
    javafx.scene.shape.Circle clockBorder;

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;

        // --- 1. SETUP UI (Scene 1) ---
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(150);
        sidebar.getStyleClass().add("sidebar");
        Label historyLabel = new Label("History");
        historyLabel.setStyle("-fx-font-weight: bold;");
        sidebar.getChildren().add(historyLabel);
        getTaskButtons(sidebar);

        TextField workField = new TextField();
        workField.setPromptText("Work minutes");
        TextField breakField = new TextField();
        breakField.setPromptText("Break minutes");
        TextField taskField = new TextField();
        taskField.setPromptText("Task name");
        startButton = new Button("Start");
        Label errorLabel = new Label();

        VBox setupCenter = new VBox(15, new Label("New Session"), taskField, workField, breakField, startButton, errorLabel);
        setupCenter.setAlignment(Pos.CENTER);
        setupCenter.setPadding(new Insets(30));

        BorderPane root1 = new BorderPane();
        root1.setLeft(sidebar);
        root1.setCenter(setupCenter);
        scene1 = new Scene(root1, 700, 500);

        // --- 2. TIMER UI (Scene 2) ---
        timeLabel = new Label("00:00");
        timeLabel.setId("timer-label");
        taskLabel = new Label("Current Task: ");
        pauseButton = new Button("Pause");
        acknowledgeButton = new Button("Acknowledge");
        acknowledgeButton.setDisable(true);
        saveButton = new Button("Save");

        StackPane stopwatch = createStopWatch();

        VBox timerCenter = new VBox(20, taskLabel, timeLabel, stopwatch, pauseButton, acknowledgeButton, saveButton);
        timerCenter.setAlignment(Pos.CENTER);

        BorderPane root2 = new BorderPane();
        root2.setCenter(timerCenter);
        scene2 = new Scene(root2, 700, 500);


        javafx.beans.binding.NumberBinding radiusBinding =
                javafx.beans.binding.Bindings.min(scene2.widthProperty(), scene2.heightProperty()).divide(5);

        clockFace.radiusProperty().bind(radiusBinding);
        clockSpacer.radiusProperty().bind(radiusBinding);
        clockHand.endYProperty().bind(radiusBinding.multiply(-0.99));
        progressArc.radiusXProperty().bind(radiusBinding);
        progressArc.radiusYProperty().bind(radiusBinding);
        clockBorder.radiusProperty().bind(radiusBinding);

        startButton.setOnAction(e -> {
            try {
                workMinutes = Integer.parseInt(workField.getText());
                breakMinutes = Integer.parseInt(breakField.getText());
                taskName = taskField.getText();

                totalSecondsWorked = workMinutes * 60;

                taskLabel.setText("Current Task: " + taskName);
                p = new Pomodoro(workMinutes, breakMinutes, this::updateLabel);
                updateLabel();
                window.setScene(scene2);
            } catch (NumberFormatException ex) {
                errorLabel.setText("Check your numbers!");
            }
        });

        pauseButton.setOnAction(e -> {
            if (p != null) {
                p.pause();
                paused = !paused;
                pauseButton.setText(paused ? "Start" : "Pause");
            }
        });

        saveButton.setOnAction(e -> {
            dumpToDatabase();
            if (p != null && p.timeLine != null) {
                p.timeLine.stop();
            }
            Platform.exit();
            System.exit(0);
        });

        acknowledgeButton.setOnAction(e -> {
            if (p != null) p.acknowledge();
        });

        String css = getClass().getResource("/style.css").toExternalForm();
        scene1.getStylesheets().add(css);
        scene2.getStylesheets().add(css);

        window.setTitle("Pomodoro Timer");
        window.setScene(scene1);
        window.show();
    }

    private void updateLabel() {
        Platform.runLater(() -> {
            timeLabel.setText(String.format("%02d:%02d", p.timeLeft / 60, p.timeLeft % 60));
            progressArc.setFill(p.inWork ? Color.LIGHTBLUE : Color.LIGHTGRAY);
            int currentTotalSeconds;
            if (p.inWork) {
                currentTotalSeconds = workMinutes * 60;
            } else {
                currentTotalSeconds = breakMinutes * 60;
            }
            if (!paused && !p.isWaiting() && currentTotalSeconds > 0) {
                int timePassed = currentTotalSeconds - p.timeLeft;
                double progress = (double) timePassed / currentTotalSeconds;
                double angle = progress * 360;
                handRotation.setAngle(angle);
                progressArc.setLength(-angle);
            } else if (p.isWaiting()) {
                handRotation.setAngle(0);
                progressArc.setLength(0);
            }
            if (!paused && p.inWork) {
                minutesWorked = (int) (workMinutes - p.timeLeft / 60.0);
            }
            acknowledgeButton.setDisable(!p.isWaiting());

        });
    }

    private StackPane createStopWatch() {
            clockFace = new javafx.scene.shape.Circle();
            clockFace.setFill(Color.TRANSPARENT);
            clockFace.setStroke(null);

            clockHand = new javafx.scene.shape.Line(0, 0, 0, 0);
            clockHand.setStroke(Color.BLACK);
            clockHand.setStrokeWidth(3);
            clockHand.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

            progressArc = new javafx.scene.shape.Arc();
            progressArc.setCenterX(0);
            progressArc.setCenterY(0);
            progressArc.setStartAngle(90);
            progressArc.setLength(0);
            progressArc.setType(javafx.scene.shape.ArcType.ROUND);
            progressArc.setFill(Color.LIGHTBLUE);

            clockBorder = new javafx.scene.shape.Circle();
            clockBorder.setFill(Color.TRANSPARENT);
            clockBorder.setStroke(Color.BLACK);
            clockBorder.setStrokeWidth(5);

            handRotation.setPivotX(0);
            handRotation.setPivotY(0);
            clockHand.getTransforms().add(handRotation);

            clockSpacer = new javafx.scene.shape.Circle();
            clockSpacer.setFill(javafx.scene.paint.Color.TRANSPARENT);


            javafx.scene.Group handContainer = new javafx.scene.Group(clockSpacer, progressArc, clockHand);


            StackPane icon = new StackPane(clockFace, handContainer, clockBorder);

            return icon;
    }

    public static void dumpToDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:pomodoro.db")) {

            String createSql =
                    "CREATE TABLE IF NOT EXISTS tasks (" +
                            "taskName TEXT NOT NULL UNIQUE, " +
                            "minutesWorked INTEGER DEFAULT 0, " +
                            "workMinutes INTEGER, " +
                            "breakMinutes INTEGER" +
                            ")";
            conn.createStatement().execute(createSql);


            String upsertSql =
                    "INSERT INTO tasks (taskName, minutesWorked, workMinutes, breakMinutes) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON CONFLICT(taskName) DO UPDATE SET " +
                            "minutesWorked = tasks.minutesWorked + excluded.minutesWorked, " +
                            "workMinutes = excluded.workMinutes, " +
                            "breakMinutes = excluded.breakMinutes";

            try (PreparedStatement pstmt = conn.prepareStatement(upsertSql)) {
                pstmt.setString(1, taskName);
                pstmt.setInt(2, minutesWorked);
                pstmt.setInt(3, workMinutes);
                pstmt.setInt(4, breakMinutes);
                pstmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection openIfExists(String dbPath) throws SQLException {
        File dbFile = new File(dbPath);

        if (!dbFile.exists()) {
            return null;
        }

        String url = "jdbc:sqlite:" + dbPath;
        return DriverManager.getConnection(url);
    }

    private void getTaskButtons(VBox container) throws SQLException {
        File dbFile = new File("pomodoro.db");
        if (!dbFile.exists()) return;

        String url = "jdbc:sqlite:pomodoro.db";


        String query = "SELECT taskName, workMinutes, breakMinutes FROM tasks";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String dbtaskName = rs.getString("taskName");
                int savedWork = rs.getInt("workMinutes");
                int savedBreak = rs.getInt("breakMinutes");

                Button taskButton = new Button(dbtaskName);
                taskButton.setMaxWidth(Double.MAX_VALUE);

                taskButton.setOnAction(e -> {
                    taskName = dbtaskName;
                    workMinutes = (savedWork > 0) ? savedWork : 25;
                    breakMinutes = (savedBreak > 0) ? savedBreak : 5;

                    taskLabel.setText("Current Task: " + taskName);
                    p = new Pomodoro(workMinutes, breakMinutes, this::updateLabel);

                    updateLabel();
                    window.setScene(scene2);
                });

                container.getChildren().add(taskButton);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


}
