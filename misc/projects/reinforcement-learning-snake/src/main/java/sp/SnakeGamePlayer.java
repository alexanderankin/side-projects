package sp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class SnakeGamePlayer extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    final Timer timer;
    final SnakeGame game;

    public SnakeGamePlayer(SnakeGame game) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        this.game = game;
        game.reset();

        timer = new Timer(100, this); // Control game speed
        timer.start();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game - Player");
        SnakeGamePlayer gamePanel = new SnakeGamePlayer(new SnakeGame(null, new Config()));
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.render(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        game.step(1);
        repaint();

        if (!game.done) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over! Score: " + game.snake.length);
            System.exit(0);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP) {
            // game.setDirection(0);
        } else if (key == KeyEvent.VK_DOWN) {
            // game.setDirection(1);
        } else if (key == KeyEvent.VK_LEFT) {
            // game.setDirection(2);
        } else if (key == KeyEvent.VK_RIGHT) {
            // game.setDirection(3);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
}

// Note: You'll need to implement the SnakeGame class with methods reset(), step(), render(Graphics g), isGameOver(), getSnakeLength(), and setDirection(int direction).
