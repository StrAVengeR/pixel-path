package com.swinggame;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

public class Player {
    public double x, y;
    public double velX = 0, velY = 0;
    public boolean onGround = false;
    public boolean alive = true;
    public final double width = 40, height = 40;
    private final double speed = 5, jumpForce = -14, gravity = 0.5;
    private Color color;
    private String name;
    private boolean leftPressed, rightPressed, jumpPressed;
    private boolean prevJump = false;

    public Player(double x, double y, Color color, String name) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.name = name;
    }

    public void setLeft(boolean v) {
        leftPressed = v;
    }

    public void setRight(boolean v) {
        rightPressed = v;
    }

    public void setJump(boolean v) {
        jumpPressed = v;
    }

    public void update(List<Platform> platforms, List<Player> allPlayers) {
        if (!alive) {
            return;
        }
        if (leftPressed) {
            velX = -speed;
        } else if (rightPressed) {
            velX = speed;
        } else {
            velX = 0;
        }
        if (jumpPressed && !prevJump && onGround) {
            velY = jumpForce;
            onGround = false;
        }
        prevJump = jumpPressed;
        velY += gravity;
        if (velY > 18) {
            velY = 18;
        }
        x += velX;
        y += velY;
        if (x < 0) {
            x = 0;
        }
        if (x + width > GamePanel.WIDTH) {
            x = GamePanel.WIDTH - width;
        }
        onGround = false;
        for (Platform p : platforms) {
            if (intersectsRect(p.x, p.y, p.width, p.height)) {
                if (velY >= 0 && y + height - velY <= p.y + 5) {
                    y = p.y - height;
                    velY = 0;
                    onGround = true;
                } else if (velY < 0 && y - velY >= p.y + p.height - 5) {
                    y = p.y + p.height;
                    velY = 0;
                } else if (velX > 0) {
                    x = p.x - width;
                    velX = 0;
                } else if (velX < 0) {
                    x = p.x + p.width;
                    velX = 0;
                }
            }
        }
        for (Player other : allPlayers) {
            if (other == this || !other.alive) {
                continue;
            }
            if (intersectsRect(other.x, other.y, other.width, other.height)) {
                if (velY >= 0 && y + height - velY <= other.y + 5) {
                    y = other.y - height;
                    velY = 0;
                    onGround = true;
                } else if (velX > 0 && x + width - velX <= other.x + 5) {
                    x = other.x - width;
                    velX = 0;
                } else if (velX < 0 && x - velX >= other.x + other.width - 5) {
                    x = other.x + other.width;
                    velX = 0;
                }
            }
        }
        if (y > GamePanel.HEIGHT + 50) {
            alive = false;
        }
    }

    public boolean intersectsRect(double rx, double ry, double rw, double rh) {
        return x < rx + rw && x + width > rx && y < ry + rh && y + height > ry;
    }

    public void draw(Graphics2D g) {
        if (!alive) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();

        // body with subtle vertical gradient
        java.awt.GradientPaint bodyPaint = new java.awt.GradientPaint((float) x, (float) y, color.brighter(),
                (float) x, (float) (y + height), color.darker());
        g2.setPaint(bodyPaint);
        g2.fillRoundRect((int) x, (int) y, (int) width, (int) height, 12, 12);

        // outline
        g2.setColor(color.darker().darker());
        g2.setStroke(new java.awt.BasicStroke(1));
        g2.drawRoundRect((int) x, (int) y, (int) width, (int) height, 12, 12);

        // draw name label above the player (white bold with subtle shadow)
        if (name != null && !name.isEmpty()) {
            int fontSize = Math.max(10, (int) (height * 0.25));
            Font font = new Font("SansSerif", Font.BOLD, fontSize);
            g2.setFont(font);
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(name);
            int tx = (int) (x + width / 2 - textW / 2);
            int ty = (int) (y) - 6; // baseline slightly above the player
            g2.setColor(new Color(0, 0, 0, 160));
            g2.drawString(name, tx + 1, ty + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(name, tx, ty);
        }

        g2.dispose();
    }
}
