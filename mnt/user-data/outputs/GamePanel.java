import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;


public class GamePanel extends JPanel implements Runnable {

    // ── 60 FPS тогтмол 
    private static final int  TARGET_FPS    = 60;
    private static final long OPTIMAL_TIME  = 1_000_000_000L / TARGET_FPS;

    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    // ── Double buffer 
    private BufferedImage buffer;
    private Graphics2D    g2d;

    // ── Game Loop thread 
    private Thread  gameThread;
    private boolean running = false;

    // ── FPS харуулах 
    private int  fps        = 0;
    private int  fpsCounter = 0;
    private long fpsTimer   = 0;

    // ── Анимацийн хувьсагч 
    private double time = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);

        buffer = new BufferedImage(WIDTH, HEIGHT,
                                   BufferedImage.TYPE_INT_RGB);
        g2d = buffer.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    //  GAME LOOP — секундэд 60 удаа: update → render → sleep
    public void startLoop() {
        running    = true;
        gameThread = new Thread(this, "GameLoop-Thread");
        gameThread.setDaemon(true);
        gameThread.start();
    }

    @Override
    public void run() {
        long lastTime  = System.nanoTime();
        long fpsStart  = System.nanoTime();
        int  frames    = 0;

        while (running) {
            long now   = System.nanoTime();
            double dt  = (now - lastTime) / 1_000_000_000.0;
            lastTime   = now;

            // 1. Update
            update(dt);

            // 2. Render → buffer
            render();

            // 3. Buffer → дэлгэц
            Graphics g = getGraphics();
            if (g != null) { g.drawImage(buffer, 0, 0, null); g.dispose(); }

            // FPS тооцоо
            frames++;
            if (now - fpsStart >= 1_000_000_000L) {
                fps = frames; frames = 0; fpsStart = now;
            }

            // 4. Sleep — 60 FPS хадгалах
            long sleep = (OPTIMAL_TIME - (System.nanoTime() - now)) / 1_000_000L;
            if (sleep > 0) {
                try { Thread.sleep(sleep); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        }
    }
    
    //  UPDATE
    private void update(double dt) {
        time += dt;
    }

    //  RENDER — үндсэн дэлгэц (суурь)
    
    private void render() {
        // Арын өнгө
        g2d.setColor(new Color(20, 20, 40));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Гарчиг
        double bob = Math.sin(time * 2) * 5;
        g2d.setFont(new Font("Arial Black", Font.BOLD, 52));
        g2d.setColor(new Color(80, 80, 80));
        g2d.drawString("Pixel Path", 202, (int)(202 + bob));
        g2d.setColor(new Color(100, 220, 255));
        g2d.drawString("Pixel Path", 200, (int)(200 + bob));

        // Дэд гарчиг
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.setColor(new Color(180, 180, 180));
        g2d.drawString("2D Cooperative Puzzle-Platformer", 250, (int)(250 + bob));

        // Хуваагч шугам
        g2d.setColor(new Color(100, 220, 255, 80));
        g2d.fillRect(150, 275, 500, 2);

        // "ENTER дарж эхлэх" — анивчдаг
        float blink = (float)(0.5 + 0.5 * Math.sin(time * 3));
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.setColor(new Color(1f, 1f, 1f, blink));
        g2d.drawString("ENTER дарж эхлэх", 300, 320);

        // Хүрэх үр дүн (баталгаа)
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2d.setColor(new Color(100, 255, 100, 180));
        g2d.drawString("Game Loop: АЖИЛЛАЖ БАЙНА", 270, 420);

        // FPS
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 13));
        g2d.setColor(new Color(255, 255, 0, 200));
        g2d.drawString("FPS: " + fps, WIDTH - 80, 20);

        // Огноо / хариуцагч
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(new Color(120, 120, 120));
        g2d.drawString("Б.Билгүүн | 2026.06.06 | Зорилт 1", 10, HEIGHT - 10);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(buffer, 0, 0, null);
    }
}
