package info.ankin.simplebackups;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/** Native file dialogs, including macOS's native directory picker. Call on the Swing EDT. */
final class FileDialogs {
    private static final boolean MAC = System.getProperty("os.name").startsWith("Mac");
    private static final String DIRECTORIES_PROPERTY = "apple.awt.fileDialogForDirectories";

    private FileDialogs() {}

    static Path folder(Component parent, String title, Path initial) {
        // AWT exposes native folder selection on macOS only. Keep folders selectable elsewhere.
        if (!MAC) {
            var chooser = new JFileChooser();
            chooser.setDialogTitle(title);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (initial != null) chooser.setCurrentDirectory(initial.toFile());
            return chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION
                    ? chooser.getSelectedFile().toPath() : null;
        }
        return choose(parent, title, FileDialog.LOAD, true, initial, false);
    }

    static Path open(Component parent, String title, Path initial, boolean json) {
        return choose(parent, title, FileDialog.LOAD, false, initial, json);
    }

    static Path saveJson(Component parent, String filename) {
        return choose(parent, "Export backup configuration", FileDialog.SAVE, false, Path.of(filename), true);
    }

    private static Path choose(Component parent, String title, int mode, boolean directories, Path initial, boolean json) {
        Window owner = parent instanceof Window window ? window : SwingUtilities.getWindowAncestor(parent);
        FileDialog dialog = owner instanceof Dialog modal
                ? new FileDialog(modal, title, mode) : new FileDialog((Frame) owner, title, mode);
        String previous = System.getProperty(DIRECTORIES_PROPERTY);
        try {
            if (MAC) System.setProperty(DIRECTORIES_PROPERTY, Boolean.toString(directories));
            if (initial != null) {
                if (Files.isDirectory(initial)) dialog.setDirectory(initial.toAbsolutePath().toString());
                else {
                    if (initial.getParent() != null) dialog.setDirectory(initial.toAbsolutePath().getParent().toString());
                    if (!directories) dialog.setFile(initial.getFileName().toString());
                }
            }
            if (json) dialog.setFilenameFilter((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"));
            dialog.setVisible(true);
            return dialog.getFile() == null ? null : Path.of(dialog.getDirectory(), dialog.getFile());
        } finally {
            dialog.dispose();
            if (MAC) {
                if (previous == null) System.clearProperty(DIRECTORIES_PROPERTY);
                else System.setProperty(DIRECTORIES_PROPERTY, previous);
            }
        }
    }
}
