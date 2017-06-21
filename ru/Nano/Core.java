package ru.Nano;

import ru.Nano.gui.Gui;

public class Core {
    public static void main(String... args) {
        new Thread(new Gui()).start();
    }
}
