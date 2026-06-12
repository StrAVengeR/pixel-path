package com.swinggame;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class SwingMain {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Pixel Path");
        GamePanel panel = new GamePanel();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.startGame();
    }
}
