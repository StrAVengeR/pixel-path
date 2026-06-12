package com.swinggame;

import java.awt.Color;
import java.awt.Graphics2D;

public class Platform {
    public double x, y, width, height;

    public Platform(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(139, 69, 19));
        g.fillRoundRect((int) x, (int) y, (int) width, (int) height, 8, 8);
        g.setColor(new Color(51, 153, 25));
        g.fillRoundRect((int) x, (int) y, (int) width, 10, 8, 8);
    }
}
