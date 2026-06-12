package com.swinggame;

import java.util.ArrayList;
import java.util.List;

public class MapData {
    public static List<Platform> getPlatforms(int i) {
        List<Platform> l = new ArrayList<>();
        switch (i) {
            case 0:
                l.add(new Platform(0, 680, 680, 40));
                l.add(new Platform(200, 520, 200, 20));
                l.add(new Platform(500, 420, 200, 20));
                l.add(new Platform(800, 320, 200, 20));
                l.add(new Platform(1050, 220, 180, 20));
                break;
            case 1:
                l.add(new Platform(0, 680, 300, 40));
                l.add(new Platform(400, 680, 200, 40));
                l.add(new Platform(700, 680, 150, 40));
                l.add(new Platform(950, 680, 330, 40));
                l.add(new Platform(150, 500, 150, 20));
                l.add(new Platform(500, 400, 150, 20));
                l.add(new Platform(650, 460, 150, 20));
                l.add(new Platform(750, 300, 150, 20));
                l.add(new Platform(900, 380, 140, 20));
                l.add(new Platform(1050, 500, 150, 20));
                break;
            case 2:
                l.add(new Platform(0, 680, 400, 40));
                l.add(new Platform(100, 560, 150, 20));
                l.add(new Platform(350, 460, 150, 20));
                l.add(new Platform(100, 360, 150, 20));
                l.add(new Platform(400, 260, 150, 20));
                l.add(new Platform(150, 160, 150, 20));
                l.add(new Platform(900, 680, 380, 40));
                l.add(new Platform(700, 500, 150, 20));
                l.add(new Platform(900, 380, 150, 20));
                l.add(new Platform(700, 260, 150, 20));
                l.add(new Platform(950, 160, 200, 20));
                break;
            case 3:
                l.add(new Platform(0, 680, 500, 40));
                l.add(new Platform(600, 680, 200, 40));
                l.add(new Platform(900, 680, 380, 40));
                l.add(new Platform(200, 500, 120, 20));
                l.add(new Platform(450, 400, 120, 20));
                l.add(new Platform(700, 500, 120, 20));
                l.add(new Platform(950, 350, 200, 20));
                l.add(new Platform(600, 250, 150, 20));
                l.add(new Platform(300, 150, 150, 20));
                l.add(new Platform(900, 150, 250, 20));
                break;
        }
        return l;
    }

    public static List<Obstacle> getObstacles(int i) {
        List<Obstacle> l = new ArrayList<>();
        switch (i) {
            case 0:
                l.add(new Obstacle(500, 50, 500, Obstacle.Type.FALLING_BLOCK));
                l.add(new Obstacle(900, 50, 900, Obstacle.Type.FALLING_BLOCK));
                l.add(new Obstacle(300, 500, Obstacle.Type.SPIKE));
                break;
            case 1:
                l.add(new Obstacle(400, 650, Obstacle.Type.SPIKE));
                l.add(new Obstacle(700, 650, Obstacle.Type.SPIKE));
                l.add(new Obstacle(300, 420, 300, 550, Obstacle.Type.MOVING_PLATFORM));
                l.add(new Obstacle(600, 50, 600, Obstacle.Type.FALLING_BLOCK));
                break;
            case 2:
                l.add(new Obstacle(180, 540, Obstacle.Type.SPIKE));
                l.add(new Obstacle(400, 440, Obstacle.Type.SPIKE));
                l.add(new Obstacle(500, 50, 500, Obstacle.Type.FALLING_BLOCK));
                l.add(new Obstacle(700, 460, 700, 900, Obstacle.Type.MOVING_PLATFORM));
                break;
            case 3:
                l.add(new Obstacle(400, 650, Obstacle.Type.SPIKE));
                l.add(new Obstacle(600, 650, Obstacle.Type.SPIKE));
                l.add(new Obstacle(400, 50, 400, Obstacle.Type.FALLING_BLOCK));
                l.add(new Obstacle(700, 50, 700, Obstacle.Type.FALLING_BLOCK));
                l.add(new Obstacle(900, 50, 900, Obstacle.Type.FALLING_BLOCK));
                l.add(new Obstacle(450, 360, 300, 700, Obstacle.Type.MOVING_PLATFORM));
                break;
        }
        return l;
    }

    public static double[] getGoalPosition(int i) {
        switch (i) {
            case 0:
                return new double[] { 1080, 140 };
            case 1:
                return new double[] { 1080, 420 };
            case 2:
                return new double[] { 980, 80 };
            case 3:
                return new double[] { 920, 70 };
            default:
                return new double[] { 1100, 100 };
        }
    }

    public static String[] getMapNames() {
        return new String[] { "Map 1", "Map 2", "Map 3", "Map 4" };

    }

    public static int[] getBackgroundColors(int i) {
        switch (i) {
            case 0:
                return new int[] { 135, 206, 235 };
            case 1:
                return new int[] { 255, 200, 100 };
            case 2:
                return new int[] { 100, 150, 200 };
            case 3:
                return new int[] { 60, 60, 80 };
            default:
                return new int[] { 135, 206, 235 };
        }
    }
}
