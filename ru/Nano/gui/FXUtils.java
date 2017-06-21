package ru.Nano.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.Nano.gui.scenes.IScene;
import ru.Nano.gui.scenes.MainScene;

import java.util.HashMap;

public class FXUtils {
    private static Stage currentStage;
    private static Scene currentScene;

    private static HashMap<String, IScene> sceneHashMap = new HashMap<>();

    public static Stage getCurrentStage() {
        return currentStage;
    }

    public static void setCurrentStage(Stage stage) {
        currentStage = stage;
    }

    public static Scene getCurrentScene() {
        return currentScene;
    }

    public static void setCurrentScene(String name) {
        try {
            Scene scene = new Scene(FXMLLoader.load(FXUtils.class.getResource("/assets/" + name + ".fxml")));
            sceneHashMap.get(name).onLoad(scene);
            currentStage.setScene(scene);
            FXUtils.currentScene = scene;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static {
        sceneHashMap.put("Patcher", new MainScene());
    }
}
