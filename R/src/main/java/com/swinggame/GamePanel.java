package com.swinggame;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            if (System.nanoTime() - stateTimer > 2_000_000_000L) {
                currentMap++;
                if (currentMap >= TOTAL_MAPS) {
                    state = State.WIN;
                } else {
                    loadMap(currentMap);
                    state = State.PLAYING;
                }
            }
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
        long alive = players.stream().filter(p -> p.alive).count();
        if (alive == 0) {
            state = State.GAME_OVER;
            return;
        }
        if (allAliveAtDoor()) {
            if (keys.contains(KeyEvent.VK_ENTER) || keys.contains(KeyEvent.VK_SPACE)) {
                currentMap++;
                if (currentMap >= TOTAL_MAPS) {
                    state = State.WIN;
                } else {
                    loadMap(currentMap);
                    state = State.PLAYING;
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
            drawOverlay(g2, "LEVEL CLEARED!");
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
        String subtitle = "Найзуудтайгаа хамтран 4 map давна!";
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
        String[] menuOptions = { "Start Game", "Restart Current Map", "Quit" };
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
        g.drawString("Use Up/Down or W/S then ENTER. ESC returns to menu. R restarts.", WIDTH / 2 - 240, HEIGHT - 20);
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
                doorText = "Амьд үлдсэн бүх тоглогчид хаалган дээр ирлээ. [ENTER] дарж дараагийн үе рүү шилжинэ үү!";
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

    private void drawWin(Graphics2D g) {
        g.setColor(new Color(8, 8, 25));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        Color[] c = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.PINK };
        for (int i = 0; i < 30; i++) {
            g.setColor(c[i % c.length]);
            g.fillRect((i * 43 + 10) % WIDTH, (i * 71 + 20) % HEIGHT, 15, 15);
        }
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Arial", Font.BOLD, 72));
        drawCenteredString(g, "YOU WIN!", WIDTH / 2, 250);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        drawCenteredString(g, "Та чадлаа!", WIDTH / 2, 340);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.YELLOW);
        drawCenteredString(g, "THANK YOU FOR PLAYING!", WIDTH / 2, 420);
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
