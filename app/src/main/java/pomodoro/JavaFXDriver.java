/**
 * GUI driver class for pomodoro.Pomodoro.java
 *
 * @author ShaelynnJNixon
 */
package pomodoro;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class JavaFXDriver extends Application {
    Stage window;
    Label timeLabel;
    Pomodoro p;
    Button pauseButton;
    Button startButton;
    Button acknowledgeButton;

    int workMinutes;
    int breakMinutes;

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

        Label errorLabel = new Label();

        startButton = new Button("Start");

        VBox layout1 = new VBox(10, workField, breakField, startButton, errorLabel);
        Scene scene1 = new Scene(layout1, 500, 500);

        //scene 2

        timeLabel = new Label("00:00");
        pauseButton = new Button("Pause");
        acknowledgeButton = new Button("Acknowledge");
        acknowledgeButton.setDisable(true);

        VBox layout2 = new VBox(10, timeLabel, pauseButton, acknowledgeButton);
        Scene scene2 = new Scene(layout2, 500, 500);

        pauseButton.setOnAction(e -> {
            if (p != null) p.pause();
        });

        acknowledgeButton.setOnAction(e -> {
            if (p != null) p.acknowledge();
        });


        startButton.setOnAction(e -> {
            try {
                int workMinutes = Integer.parseInt(workField.getText());
                int breakMinutes = Integer.parseInt(breakField.getText());

                if (workMinutes <= 0 || breakMinutes <= 0) {
                    throw new NumberFormatException();
                }

                p = new Pomodoro(workMinutes, breakMinutes, this::updateLabel);
                window.setScene(scene2);


            } catch (NumberFormatException ex) {
                errorLabel.setText("Please enter valid positive numbers.");
            }
        });

        window.setTitle("Pomodoro Timer");
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
}
