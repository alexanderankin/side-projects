package side.reference.swing.hello;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Example {
    static void main() {
        SwingUtilities.invokeLater(() -> createAndShow(new Size(300, 400)));
    }

    private static void createAndShow(Size size) {
        JFrame frame = new JFrame("Simple Swing App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(size.width(), size.height());
        frame.setLocationRelativeTo(null); // center on screen

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Hello World", SwingConstants.CENTER), BorderLayout.CENTER);
        panel.add(new JButton("Click Me"), BorderLayout.SOUTH);

        frame.setContentPane(panel);

        frame.getRootPane().getActionMap()
                .put("EXIT", new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        frame.dispose();
                    }
                });

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        "EXIT");

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                        "EXIT");

        frame.setVisible(true);
    }

    public record Size(int height, int width) {
    }
}
