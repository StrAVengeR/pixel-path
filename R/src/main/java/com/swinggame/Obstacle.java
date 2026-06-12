package com.swinggame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class Obstacle {
    public enum Type {
        FALLING_BLOCK, SPIKE, MOVING_PLATFORM
    }

    public double x, y, width, height;
    private Type type;
    private double velY = 0, gravity = 0.8;
    private boolean active = false;
    private double triggerX, startX, endX;
    private double speed = 2;
    private int dir = 1;

    public Obstacle(double x, double y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.width = 40;
        this.height = 30;
    }

    public Obstacle(double x, double y, double triggerX, Type type) {
        this.x = x;
        this.y = y;
        this.triggerX = triggerX;
        this.type = type;
        this.width = 50;
        this.height = 50;
    }

    public Obstacle(double x, double y, double startX, double endX, Type type) {
        this.x = x;
        this.y = y;
        this.startX = startX;
        this.endX = endX;
        this.type = type;
        this.width = 80;
        this.height = 20;
    }

    public void update(List<Player> players) {
        switch (type) {
            case FALLING_BLOCK:
                for (Player p : players) {
                    if (p.alive && Math.abs(p.x - triggerX) < 50) {
                        active = true;
                    }
                }
                if (active) {
                    velY += gravity;
                    y += velY;
                }
                break;
            case MOVING_PLATFORM:
                x += speed * dir;
                if (x > endX) {
                    dir = -1;
                }
                if (x < startX) {
                    dir = 1;
                }
                break;
            case SPIKE:
                break;
        }
    }

    public boolean hitsPlayer(Player p) {
        if (type == Type.FALLING_BLOCK && !active) {
            return false;
        }
        return p.intersectsRect(x, y, width, height);
    }

    public void draw(Graphics2D g) {
        switch (type) {
            case FALLING_BLOCK:
                g.setColor(active ? Color.DARK_GRAY : Color.GRAY);
                g.fillRect((int) x, (int) y, (int) width, (int) height);
                break;
            case SPIKE:
                g.setColor(Color.LIGHT_GRAY);
                for (int i = 0; i < 3; i++) {
                    int sx = (int) (x + i * 14);
                    int[] xs = { sx, sx + 7, sx + 14 };
                    int[] ys = { (int) (y + height), (int) y, (int) (y + height) };
                    g.fillPolygon(xs, ys, 3);
                }
                break;
            case MOVING_PLATFORM:
                g.setColor(new Color(255, 140, 0));
                g.fillRoundRect((int) x, (int) y, (int) width, (int) height, 6, 6);
                break;
        }
    }
}
