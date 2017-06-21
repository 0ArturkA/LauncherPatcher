package ru.Nano.gui.scenes;

import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import ru.Nano.gui.FXUtils;
import ru.Nano.patcher.Logic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainScene implements IScene {
    private List<File> launchers = new ArrayList<>();

    @Override
    public void onLoad(Scene scene) {
        AnchorPane pane = (AnchorPane) scene.lookup("#dragForm");

        pane.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });

        pane.setOnDragDropped(event -> {
            launchers.clear();

            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                db.getFiles().stream().
                        filter(file -> file.getName().endsWith(".jar") && file.exists()).
                        forEach(launchers::add);

                event.setDropCompleted(true);

                scene.lookup("#preloader").setVisible(true);
                pane.setDisable(true);
                processPatch();
            } else {
                event.consume();
            }
        });
    }

    private void processPatch() {
        new Thread(() -> {
            for (File launcher : launchers) {
                Logic patcher = new Logic(launcher);
                patcher.deobfuscate();
                patcher.patch();
                patcher.rebuild();
            }

            FXUtils.getCurrentScene().lookup("#preloader").setVisible(false);
            FXUtils.getCurrentScene().lookup("#dragForm").setDisable(false);
        }).start();
    }
}
