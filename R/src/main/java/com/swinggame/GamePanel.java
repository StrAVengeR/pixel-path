package com.swinggame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;
    private Thread thread;
    private boolean running;
    private State state = State.MENU;
    private int currentMap = 0;
    private static final int TOTAL_MAPS = 4;
    private List<Player> players = new ArrayList<>();
    private List<Platform> platforms = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<>();
    private double[] goal = new double[2];
    private Set<Integer> keys = new HashSet<>();
    private long stateTimer = 0;
    private long menuStartTime = -1;
    private int menuSelection = 0; // 0=start, 1=restart, 2=quit
    private int mapClearSelection = 0; // 0=next, 1=restart, 2=menu
    private int starsEarned = 0;

    private enum State {
        MENU, PLAYING, MAP_CLEAR, GAME_OVER, WIN
    }

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        loadMap(currentMap);
    }

    public void startGame() {
        if (thread == null) {
            running = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerFrame = 1_000_000_000.0 / 60.0;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerFrame;
            lastTime = now;
            while (delta >= 1) {
                updateFrame();
                delta -= 1;
            }
            repaint();
            try {
                Thread.sleep(2);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void updateFrame() {
        handleGlobalShortcuts();
        if (state == State.MENU) {
            updateMenu();
        } else if (state == State.PLAYING) {
            updateGame();
        } else if (state == State.MAP_CLEAR) {
            updateMapClearMenu();
        }
    }

    private void handleGlobalShortcuts() {
        if (keys.contains(KeyEvent.VK_ESCAPE)) {
            state = State.MENU;
            menuSelection = 0;
            menuStartTime = System.nanoTime();
            keys.remove(KeyEvent.VK_ESCAPE);
        }
        if (keys.contains(KeyEvent.VK_R)) {
            loadMap(currentMap);
            state = State.PLAYING;
            keys.remove(KeyEvent.VK_R);
        }
        if (keys.contains(KeyEvent.VK_T)) {
            if (state == State.PLAYING) {
                if (currentMap >= TOTAL_MAPS - 1) {
                    state = State.WIN;
                } else {
                    currentMap++;
                    loadMap(currentMap);
                    state = State.PLAYING;
                }
            } else if (state == State.MENU) {
                currentMap = 0;
                loadMap(currentMap);
                state = State.PLAYING;
            }
            keys.remove(KeyEvent.VK_T);
        }
    }

    private void loadMap(int index) {
        platforms = MapData.getPlatforms(index);
        obstacles = MapData.getObstacles(index);
        goal = MapData.getGoalPosition(index);
        setupPlayers();
    }

    private void setupPlayers() {
        players.clear();
        players.add(new Player(100, 600, Color.RED, "P1"));
        players.add(new Player(160, 600, Color.BLUE, "P2"));
        players.add(new Player(220, 600, Color.GREEN, "P3"));
        players.add(new Player(280, 600, Color.YELLOW, "P4"));
    }

    private void handleInput() {
        Player p1 = players.get(0);
        p1.setLeft(keys.contains(KeyEvent.VK_A));
        p1.setRight(keys.contains(KeyEvent.VK_D));
        p1.setJump(keys.contains(KeyEvent.VK_W));

        Player p2 = players.get(1);
        p2.setLeft(keys.contains(KeyEvent.VK_LEFT));
        p2.setRight(keys.contains(KeyEvent.VK_RIGHT));
        p2.setJump(keys.contains(KeyEvent.VK_UP));

        Player p3 = players.get(2);
        p3.setLeft(keys.contains(KeyEvent.VK_J));
        p3.setRight(keys.contains(KeyEvent.VK_L));
        p3.setJump(keys.contains(KeyEvent.VK_I));

        Player p4 = players.get(3);
        p4.setLeft(keys.contains(KeyEvent.VK_C));
        p4.setRight(keys.contains(KeyEvent.VK_B));
        p4.setJump(keys.contains(KeyEvent.VK_F));
    }

    private void updateMenu() {
        if (keys.contains(KeyEvent.VK_UP) || keys.contains(KeyEvent.VK_W)) {
            menuSelection = Math.max(0, menuSelection - 1);
            keys.remove(KeyEvent.VK_UP);
            keys.remove(KeyEvent.VK_W);
        }
        if (keys.contains(KeyEvent.VK_DOWN) || keys.contains(KeyEvent.VK_S)) {
            menuSelection = Math.min(2, menuSelection + 1);
            keys.remove(KeyEvent.VK_DOWN);
            keys.remove(KeyEvent.VK_S);
        }
        if (keys.contains(KeyEvent.VK_ENTER) || keys.contains(KeyEvent.VK_SPACE)) {
            if (menuSelection == 0) {
                currentMap = 0;
                loadMap(currentMap);
                state = State.PLAYING;
            } else if (menuSelection == 1) {
                loadMap(currentMap);
                state = State.PLAYING;
            } else if (menuSelection == 2) {
                System.exit(0);
            }
            keys.remove(KeyEvent.VK_ENTER);
            keys.remove(KeyEvent.VK_SPACE);
        }
    }

    private void updateMapClearMenu() {
        if (keys.contains(KeyEvent.VK_UP) || keys.contains(KeyEvent.VK_W)) {
            mapClearSelection = Math.max(0, mapClearSelection - 1);
            keys.remove(KeyEvent.VK_UP);
            keys.remove(KeyEvent.VK_W);
        }
        if (keys.contains(KeyEvent.VK_DOWN) || keys.contains(KeyEvent.VK_S)) {
            mapClearSelection = Math.min(2, mapClearSelection + 1);
            keys.remove(KeyEvent.VK_DOWN);
            keys.remove(KeyEvent.VK_S);
        }
        if (keys.contains(KeyEvent.VK_ENTER) || keys.contains(KeyEvent.VK_SPACE)) {
            if (mapClearSelection == 0) {
                currentMap++;
                if (currentMap >= TOTAL_MAPS) {
                    state = State.WIN;
                } else {
                    loadMap(currentMap);
                    state = State.PLAYING;
                }
            } else if (mapClearSelection == 1) {
                loadMap(currentMap);
                state = State.PLAYING;
            } else if (mapClearSelection == 2) {
                state = State.MENU;
                menuSelection = 0;
                menuStartTime = System.nanoTime();
            }
            keys.remove(KeyEvent.VK_ENTER);
            keys.remove(KeyEvent.VK_SPACE);
        }
    }

    private void updateGame() {
        handleInput();
        for (Player p : players) {
            p.update(platforms, players);
        }
        for (Obstacle o : obstacles) {
            o.update(players);
            for (Player p : players) {
                if (p.alive && o.hitsPlayer(p)) {
                    p.alive = false;
                }
            }
        }
        long alivePlayers = players.stream().filter(p -> p.alive).count();
        if (alivePlayers == 0) {
            state = State.GAME_OVER;
            return;
        }
        if (allAliveAtDoor()) {
            if (keys.contains(KeyEvent.VK_ENTER) || keys.contains(KeyEvent.VK_SPACE)) {
                if (currentMap >= TOTAL_MAPS - 1) {
                    state = State.WIN;
                } else {
                    if (alivePlayers == 4) {
                        starsEarned = 3;
                    } else if (alivePlayers == 3) {
                        starsEarned = 2;
                    } else if (alivePlayers >= 1) {
                        starsEarned = 1;
                    } else {
                        starsEarned = 0;
                    }
                    // Star system: 4 alive = 3 stars, 3 alive = 2 stars, 2 alive = 1 star, 1 alive = 1 star
                    state = State.MAP_CLEAR;
                    stateTimer = System.nanoTime();
                    mapClearSelection = 0;
                }
                keys.remove(KeyEvent.VK_ENTER);
                keys.remove(KeyEvent.VK_SPACE);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (state == State.MENU) {
            drawMenu(g2);
        } else if (state == State.PLAYING) {
            drawGame(g2);
        } else if (state == State.MAP_CLEAR) {
            drawGame(g2);
            drawMapClearScreen(g2);
        } else if (state == State.GAME_OVER) {
            drawGame(g2);
            drawOverlay(g2, "GAME OVER");
        } else if (state == State.WIN) {
            drawWin(g2);
        }
        g2.dispose();
    }

    private void drawMenu(Graphics2D g) {
        if (menuStartTime < 0) {
            menuStartTime = System.nanoTime();
        }
        double t = (System.nanoTime() - menuStartTime) / 1_000_000_000.0;

        // subtle dark background with animated stars
        g.setColor(new Color(10, 14, 28));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        for (int i = 0; i < 60; i++) {
            float alpha = (float) (0.25f + 0.6f * Math.abs(Math.sin(t * 1.2 + i * 0.9)));
            g.setColor(new Color(1f, 1f, 1f, alpha));
            int x = (i * 197 + 45) % WIDTH;
            int y = (i * 127 + 40) % (HEIGHT / 2);
            int size = i % 4 == 0 ? 3 : 2;
            g.fillOval(x, y, size, size);
        }

        // Title centered with shadow and glow
        String title = "Pixel Path";
        g.setFont(new Font("Verdana", Font.BOLD, 78));
        FontMetrics tfm = g.getFontMetrics();
        int titleW = tfm.stringWidth(title);
        int titleX = WIDTH / 2 - titleW / 2;
        int titleY = 150;
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(title, titleX + 6, titleY + 6);
        g.setColor(new Color(255, 190, 60));
        g.drawString(title, titleX, titleY);

        // Subtitle
        String subtitle = "Найзуудтайгаа хамтран тоглоорой";
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        FontMetrics sfm = g.getFontMetrics();
        int sx = WIDTH / 2 - sfm.stringWidth(subtitle) / 2;
        g.setColor(new Color(200, 230, 255, 200));
        g.drawString(subtitle, sx, titleY + 36);

        // Controls panel (compact)
        int panelW = 820, panelH = 160;
        int panelX = WIDTH / 2 - panelW / 2;
        int panelY = 300;
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        g.setColor(new Color(255, 255, 255, 200));
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Тоглоомын удирдлага", panelX + 20, panelY + 30);
        String[][] controls = {
                { "W - Үсрэх", "A - Зүүн", "D - Баруун" },
                { "Up - Үсрэх", "Left - Зүүн", "Right - Баруун" },
                { "I - Үсрэх", "J - Зүүн", "L - Баруун" },
                { "F - Үсрэх", "C - Зүүн", "B - Баруун" }
        };
        Color[] pcolors = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
        int startX = panelX + 20;
        for (int i = 0; i < 4; i++) {
            int cx = startX + i * 200;
            g.setColor(pcolors[i]);
            g.fillRoundRect(cx, panelY + 40, 180, 100, 12, 12);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("P" + (i + 1), cx + 10, panelY + 62);
            g.setFont(new Font("Arial", Font.PLAIN, 13));
            for (int j = 0; j < controls[i].length; j++) {
                g.drawString(controls[i][j], cx + 10, panelY + 86 + j * 18);
            }
        }

        // Menu options centered below panel
        String[] menuOptions = { "Тоглоом эхлүүлэх", "Одоогийн үеийг дахин эхлүүлэх", "Гарах" };
        int menuX = WIDTH / 2;
        int menuY = panelY + panelH + 50;
        for (int i = 0; i < menuOptions.length; i++) {
            boolean sel = (i == menuSelection);
            int boxW = 360, boxH = 56;
            int bx = menuX - boxW / 2;
            int by = menuY + i * (boxH + 18);
            g.setColor(sel ? new Color(40, 170, 90) : new Color(40, 40, 40, 200));
            g.fillRoundRect(bx, by, boxW, boxH, 18, 18);
            g.setColor(sel ? Color.WHITE : new Color(220, 220, 220));
            g.setFont(new Font("Arial", sel ? Font.BOLD : Font.PLAIN, sel ? 22 : 18));
            FontMetrics m = g.getFontMetrics();
            int txtW = m.stringWidth(menuOptions[i]);
            g.drawString(menuOptions[i], menuX - txtW / 2, by + boxH / 2 + 8);
        }

        // Footer hint
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.setColor(new Color(200, 200, 200, 160));
        g.drawString("Use Up/Down or W/S then ENTER. ESC returns to menu. R restarts.", WIDTH / 2 - 320, HEIGHT - 20);
    }

    private void drawGame(Graphics2D g) {
        int[] bg = MapData.getBackgroundColors(currentMap);
        // draw vertical gradient sky
        java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0,
                new Color(Math.min(255, bg[0] + 30), Math.min(255, bg[1] + 40), Math.min(255, bg[2] + 50)),
                0, HEIGHT, new Color(bg[0], bg[1], bg[2]));
        g.setPaint(gp);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // soft clouds
        long t = System.nanoTime() / 1_000_000_000L;
        g.setColor(new Color(255, 255, 255, 200));
        int[] cloudXs = { 120 + (int) (t % 60), 420 - (int) (t % 40), 820 + (int) (t % 80), 1100 - (int) (t % 50) };
        int[] cloudYs = { 80, 60, 110, 90 };
        for (int i = 0; i < cloudXs.length; i++) {
            int cx = cloudXs[i] % (WIDTH + 200) - 100;
            int cy = cloudYs[i];
            g.fillOval(cx, cy, 140, 40);
            g.fillOval(cx + 30, cy - 8, 120, 48);
            g.fillOval(cx + 60, cy, 100, 36);
        }

        // draw platforms
        for (Platform p : platforms) {
            p.draw(g);
        }
        int gx = (int) goal[0];
        int gy = (int) goal[1];
        g.setColor(new Color(184, 134, 11));
        g.fillRect(gx, gy, 60, 80);
        g.setColor(Color.YELLOW);
        g.fillRect(gx + 5, gy + 5, 50, 70);
        g.setColor(new Color(255, 140, 0));
        g.fillOval(gx + 38, gy + 35, 10, 10);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("GOAL!", gx + 8, gy - 5);
        for (Obstacle o : obstacles) {
            o.draw(g);
        }
        for (Player p : players) {
            p.draw(g);
        }
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(Color.black);
        g.drawString(MapData.getMapNames()[currentMap], 20, 30);
        String[] pn = { "P1", "P2", "P3", "P4" };
        Color[] pc = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
        for (int i = 0; i < players.size(); i++) {
            g.setColor(players.get(i).alive ? pc[i] : Color.GRAY);
            g.fillRoundRect(WIDTH - 280 + i * 65, 10, 55, 30, 8, 8);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString(players.get(i).alive ? pn[i] + " v" : pn[i] + " x", WIDTH - 272 + i * 65, 30);
        }
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.setColor(new Color(255, 255, 255, 160));
        g.drawString("P1:WASD  P2:Arrows  P3:IJKL  P4:C/B/F", 20, HEIGHT - 10);

        // Draw door/goal prompt centered on the goal rectangle instead of bottom-left
        String doorText = "";
        if (aliveCount() > 0) {
            long atDoor = aliveAtDoorCount();
            if (allAliveAtDoor()) {
                doorText = "Амьд үлдсэн бүх тоглогчид хаалган дээр ирлээ. [ENTER] дарж дараагийн үе рүү шилжинэ!";
            } else if (atDoor > 0) {
                doorText = "Хаалган дээр: " + atDoor + "/" + aliveCount() + " тоглогч байна.";
            }
        }
        if (!doorText.isEmpty()) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(new Color(130, 130, 130, 220));
            FontMetrics fm = g.getFontMetrics();
            int textW = fm.stringWidth(doorText);
            int tx = gx + 30 - textW / 2; // center on goal (goal width 60)
            int ty = gy - 28; // slightly higher above the goal
            if (ty < 20) { // if too near top, place below the goal
                ty = gy + 120;
            }
            g.drawString(doorText, tx, ty);
        }
    }

    private void drawOverlay(Graphics2D g, String text) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 72));
        drawCenteredString(g, text, WIDTH / 2, HEIGHT / 2 - 50);
    }

    private void drawMapClearScreen_UNUSED(Graphics2D g) {

        drawOverlay(g, "Үе давлаа!");
        String[] options = { "Дараагийн үе", "Дахин эхлүүлэх", "Menu руу буцах" };
        int menuX = WIDTH / 2;
        int menuY = HEIGHT / 2 + 50;
        for (int i = 0; i < options.length; i++) {
            boolean sel = (i == mapClearSelection);
            int boxW = 360, boxH = 56;
            int bx = menuX - boxW / 2;
            int by = menuY + i * (boxH + 16);
            g.setColor(sel ? new Color(40, 170, 90) : new Color(40, 40, 40, 200));
            g.fillRoundRect(bx, by, boxW, boxH, 18, 18);
            g.setColor(sel ? Color.WHITE : new Color(220, 220, 220));
            g.setFont(new Font("Arial", sel ? Font.BOLD : Font.PLAIN, sel ? 22 : 18));
            FontMetrics m = g.getFontMetrics();
            int txtW = m.stringWidth(options[i]);
            g.drawString(options[i], menuX - txtW / 2, by + boxH / 2 + 8);
        }
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(220, 220, 220, 200));
        g.drawString("Use Up/Down or W/S then ENTER.", WIDTH / 2 - 140, menuY + options.length * 72);
    }

    private void drawMapClearOverlay_UNUSED(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 64));
        drawCenteredString(g, "LEVEL CLEARED!", WIDTH / 2, HEIGHT / 2 - 120);

        int starSize = 50;
        int spacing = 70;
        int startX = WIDTH / 2 - spacing;
        int startY = HEIGHT / 2 + 10;

        for (int i = 0; i < 3; i++) {
            boolean filled = (i < starsEarned);
            drawStar(g, startX + (i * spacing), startY, starSize / 2, starSize, filled);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
        drawCenteredString(g, "Амьд үлдсэн тоглогчид: " + aliveCount() + "  |  Авсан од: " + starsEarned,
                WIDTH / 2, startY + starSize + 40);
    }

    private void drawMapClearScreen(Graphics2D g) {
        // Dark semi-transparent background
        g.setColor(new Color(10, 15, 30, 215));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Animated shimmer for title
        double animT = (System.nanoTime() % 3_000_000_000L) / 3_000_000_000.0;
        int shimmer = (int)(Math.abs(Math.sin(animT * Math.PI)) * 40);

        // Title
        g.setFont(new Font("Verdana", Font.BOLD, 68));
        FontMetrics tfm = g.getFontMetrics();
        String title = "ҮЕ ДАВЛАА!";
        int titleX = WIDTH / 2 - tfm.stringWidth(title) / 2;
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(title, titleX + 4, 184);
        g.setColor(new Color(255, 210 + shimmer / 2, 50));
        g.drawString(title, titleX, 180);

        // Map progress label
        g.setFont(new Font("Arial", Font.PLAIN, 22));
        g.setColor(new Color(180, 200, 255, 200));
        String mapLabel = "Map " + (currentMap + 1) + " / " + TOTAL_MAPS;
        FontMetrics mlm = g.getFontMetrics();
        g.drawString(mapLabel, WIDTH / 2 - mlm.stringWidth(mapLabel) / 2, 222);

        // Stars
        int outerR = 48;
        int innerR = 20;
        int starSpacing = 130;
        int starsY = 315;
        int startX = WIDTH / 2 - starSpacing;
        for (int i = 0; i < 3; i++) {
            drawStar(g, startX + i * starSpacing, starsY, innerR, outerR, i < starsEarned);
        }

        // Star subtitle
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(new Color(210, 220, 240, 210));
        String starLabel = "Авсан од: " + starsEarned + " / 3     Амьд үлдсэн: " + aliveCount() + " / 4";
        FontMetrics slm = g.getFontMetrics();
        g.drawString(starLabel, WIDTH / 2 - slm.stringWidth(starLabel) / 2, starsY + outerR + 38);


        // Menu buttons
        String[] options = { "Дараагийн үе", "Дахин эхлүүлэх", "Menu руу буцах" };
        int menuX = WIDTH / 2;
        int menuStartY = starsY + outerR + 75;
        int boxW = 340, boxH = 52, gap = 14;
        for (int i = 0; i < options.length; i++) {
            boolean sel = (i == mapClearSelection);
            int bx = menuX - boxW / 2;
            int by = menuStartY + i * (boxH + gap);
            g.setColor(sel ? new Color(50, 180, 100) : new Color(35, 40, 60, 200));
            g.fillRoundRect(bx, by, boxW, boxH, 16, 16);
            if (!sel) {
                g.setColor(new Color(80, 90, 120, 160));
                g.drawRoundRect(bx, by, boxW, boxH, 16, 16);
            }
            g.setColor(sel ? Color.WHITE : new Color(190, 200, 220));
            g.setFont(new Font("Arial", sel ? Font.BOLD : Font.PLAIN, sel ? 20 : 17));
            FontMetrics bfm = g.getFontMetrics();
            g.drawString(options[i], menuX - bfm.stringWidth(options[i]) / 2,
                    by + boxH / 2 + bfm.getAscent() / 2 - 2);
        }

        // Footer
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.setColor(new Color(150, 155, 175, 150));
        String hint = "↑↓ эсвэл W/S → сонгох   |   ENTER → баталгаажуулах";
        FontMetrics hfm = g.getFontMetrics();
        g.drawString(hint, WIDTH / 2 - hfm.stringWidth(hint) / 2, HEIGHT - 22);
    }

    private void drawStar(Graphics2D g, double x, double y, double innerRadius, double outerRadius, boolean filled) {
        Path2D.Double path = new Path2D.Double();
        double numPoints = 5;
        double angleIncrement = Math.PI / numPoints;
        double currentAngle = -Math.PI / 2;

        path.moveTo(x + outerRadius * Math.cos(currentAngle), y + outerRadius * Math.sin(currentAngle));
        currentAngle += angleIncrement;

        for (int i = 1; i < numPoints * 2; i++) {
            double r = (i % 2 == 0) ? outerRadius : innerRadius;
            path.lineTo(x + r * Math.cos(currentAngle), y + r * Math.sin(currentAngle));
            currentAngle += angleIncrement;
        }
        path.closePath();

        if (filled) {
            g.setColor(Color.YELLOW);
            g.fill(path);
        }

        g.setColor(new Color(255, 200, 0));
        g.setStroke(new java.awt.BasicStroke(2));
        g.draw(path);
    }

    private void drawWin(Graphics2D g) {
        // Deep space background
        g.setColor(new Color(8, 10, 28));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // "YOU WIN!" title with glow
        g.setFont(new Font("Verdana", Font.BOLD, 88));
        FontMetrics tfm = g.getFontMetrics();
        String title = "YOU WIN!";
        int titleX = WIDTH / 2 - tfm.stringWidth(title) / 2;
        // glow layers
        for (int glow = 3; glow >= 1; glow--) {
            g.setColor(new Color(255, 150, 0, 40 * glow));
            g.drawString(title, titleX - glow, 185 - glow);
            g.drawString(title, titleX + glow, 185 + glow);
        }
        // shadow
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(title, titleX + 5, 190);
        // main
        g.setColor(new Color(255, 200, 50));
        g.drawString(title, titleX, 185);

        // Subtitle
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        g.setColor(new Color(200, 230, 255, 220));
        String sub = "Та бүх үеийг амжилттай давлаа!";
        FontMetrics sfm = g.getFontMetrics();
        g.drawString(sub, WIDTH / 2 - sfm.stringWidth(sub) / 2, 240);

        // 3 big gold stars
        int outerR = 56;
        int innerR = 24;
        int starSpacing = 140;
        int starsY = 340;
        int starStartX = WIDTH / 2 - starSpacing;
        for (int i = 0; i < 3; i++) {
            drawStar(g, starStartX + i * starSpacing, starsY, innerR, outerR, true);
        }

        // Final stats
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(new Color(220, 230, 255, 200));
        String stats = "Амьд үлдсэн: " + aliveCount() + " / 4";
        FontMetrics stfm = g.getFontMetrics();
        g.drawString(stats, WIDTH / 2 - stfm.stringWidth(stats) / 2, starsY + outerR + 44);

        // Bottom message
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.setColor(new Color(255, 220, 80));
        String thanks = "Тоглосонд баярлалаа!";
        FontMetrics bfm = g.getFontMetrics();
        g.drawString(thanks, WIDTH / 2 - bfm.stringWidth(thanks) / 2, starsY + outerR + 90);

        // ESC hint
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        g.setColor(new Color(150, 155, 175, 150));
        String hint = "ESC дарж Menu руу буцах";
        FontMetrics hfm = g.getFontMetrics();
        g.drawString(hint, WIDTH / 2 - hfm.stringWidth(hint) / 2, HEIGHT - 22);
    }

    private void drawCenteredString(Graphics2D g, String text, int centerX, int centerY) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g.drawString(text, centerX - textWidth / 2, centerY + textHeight / 2);
    }

    private long aliveCount() {
        return players.stream().filter(p -> p.alive).count();
    }

    private long aliveAtDoorCount() {
        // expanded door area: 40 pixels padding on each side
        double doorPadding = 80;
        double expandedX = goal[0] - doorPadding;
        double expandedY = goal[1] - doorPadding;
        double expandedW = 0 + (2 * doorPadding);
        double expandedH = 80 + (2 * doorPadding);
        return players.stream().filter(p -> p.alive && p.intersectsRect(expandedX, expandedY, expandedW, expandedH))
                .count();
    }

    private boolean allAliveAtDoor() {
        long alive = aliveCount();
        return alive > 0 && aliveAtDoorCount() == alive;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys.remove(e.getKeyCode());
    }
}
