package info.ankin.simplebackups;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public final class SimpleBackups {
    private SimpleBackups() {}

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Simple Backups needs a desktop display to run its Swing interface.");
            return;
        }
        SwingUtilities.invokeLater(SimpleBackups::open);
    }

    @SneakyThrows
    private static void lookAndFeel() {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private static void open() {
        try {
            lookAndFeel();
            new BackupFrame(new ConfigurationStore(ConfigurationStore.defaultRoot())).setVisible(true);
        } catch (Exception e) {
            log.error("Could not start Simple Backups", e);
            JOptionPane.showMessageDialog(null, e.getMessage(), "Could not start Simple Backups", JOptionPane.ERROR_MESSAGE);
        }
    }
}
