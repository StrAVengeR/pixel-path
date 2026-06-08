import javax.swing.JFrame;
import javax.swing.SwingUtilities;
 
public class GameApp {
     public static final int    WIDTH  = 800;
     public static final int    HEIGHT = 600;
     public static final String TITLE  = "Pixel Path";
 
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
             JFrame frame = new JFrame(TITLE);
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             frame.setResizable(false);
 
             GamePanel panel = new GamePanel();
             frame.add(panel);
             frame.pack();
             frame.setLocationRelativeTo(null); // дэлгэцийн төвд
             frame.setVisible(true);
 
             panel.startLoop(); // Game Loop эхлүүлэх
         });
     }
}

class GamePanel extends javax.swing.JPanel {
    public void startLoop() {
        // Placeholder for the game loop implementation.
    }
}
