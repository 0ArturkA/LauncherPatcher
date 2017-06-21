package ru.Nano.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Gui extends Application implements Runnable {
    @Override
    public void run() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Launcher patcher v3.0 by Nan0");
        primaryStage.setResizable(false);
        FXUtils.setCurrentStage(primaryStage);
        FXUtils.setCurrentScene("Patcher");
        primaryStage.show();
    }
}
