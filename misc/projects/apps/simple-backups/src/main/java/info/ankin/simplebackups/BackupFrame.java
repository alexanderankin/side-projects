package info.ankin.simplebackups;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

@Slf4j
final class BackupFrame extends JFrame {
    private static final int PAGE_SIZE = 10;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS z").withZone(ZoneId.systemDefault());
    private final ConfigurationStore configurations;
    private final BackupService service = new BackupService();
    private final JLabel status = new JLabel("Ready");
    private final JProgressBar progress = new JProgressBar();
    private final JTabbedPane tabs = new JTabbedPane();
    private final CardLayout cards = new CardLayout();
    private final JPanel backupCards = new JPanel(cards);
    private final JPanel settingsPanel = new JPanel(new BorderLayout());
    private final DefaultTableModel backupModel = model("ID", "Name", "From", "To", "Actions");
    private final JTable backupTable = new JTable(backupModel);
    private final DefaultTableModel snapshotModel = model("Taken at", "Time taken", "Method", "Actions");
    private final JTable snapshotTable = new JTable(snapshotModel);
    private final JLabel snapshotTitle = new JLabel();
    private final JLabel pageLabel = new JLabel();
    private final JButton previous = new JButton("Previous");
    private final JButton next = new JButton("Next");
    private final Timer statusTimer;
    private AppSettings settings = AppSettings.defaults();
    private List<BackupConfiguration> backups = List.of();
    private List<Snapshot> snapshots = List.of();
    private BackupConfiguration selectedBackup;
    private int page;
    private boolean busy;
    private volatile String progressText = "Ready";

    BackupFrame(ConfigurationStore configurations) {
        super("Simple Backups");
        this.configurations = configurations;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (busy) {
                    JOptionPane.showMessageDialog(BackupFrame.this, "An operation is running. Wait for it to finish before closing.");
                    return;
                }
                statusTimer.stop();
                dispose();
            }
        });
        setMinimumSize(new Dimension(980, 480));
        setSize(1320, 700);
        setLocationRelativeTo(null);
        backupCards.add(backupsPanel(), "backups");
        backupCards.add(snapshotsPanel(), "snapshots");
        tabs.addTab("Backups", backupCards);
        tabs.addTab("Settings", settingsPanel);
        add(tabs, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        progress.setPreferredSize(new Dimension(140, 16));
        footer.add(status, BorderLayout.CENTER);
        footer.add(progress, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
        statusTimer = new Timer(150, e -> status.setText(progressText));
        statusTimer.start();
        rebuildSettings();
        run("Loading settings", configurations::settings, loaded -> {
            settings = loaded;
            rebuildSettings();
            reloadBackups();
        }, this::reloadBackups);
    }

    private JPanel backupsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(button("New", () -> editBackup(null)));
        toolbar.add(button("Import", this::importBackup));
        toolbar.add(button("Refresh", this::reloadBackups));
        toolbar.add(new JLabel("Run backups manually; choose the method in Settings."));
        panel.add(toolbar, BorderLayout.NORTH);
        backupTable.setAutoCreateRowSorter(true);
        backupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableActions.install(backupTable, new String[]{"Run", "Edit", "Export", "Snapshots"}, (row, action) -> {
            var backup = backups.get(row);
            switch (action) {
                case "Run" -> run("Starting backup", () -> service.backup(backup, settings, this::report), snapshot ->
                    JOptionPane.showMessageDialog(this, "Backup complete: " + snapshot.prefix()));
                case "Edit" -> editBackup(backup);
                case "Export" -> exportBackup(backup);
                case "Snapshots" -> showSnapshots(backup);
                default -> throw new IllegalArgumentException(action);
            }
        });
        panel.add(new JScrollPane(backupTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel snapshotsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(button("Back to backups", () -> cards.show(backupCards, "backups")));
        toolbar.add(button("Refresh", () -> showSnapshots(selectedBackup)));
        toolbar.add(snapshotTitle);
        panel.add(toolbar, BorderLayout.NORTH);
        TableActions.install(snapshotTable, new String[]{"Restore", "Diff"}, (row, action) -> {
            Snapshot snapshot = snapshots.get(page * PAGE_SIZE + row);
            if (action.equals("Restore")) restore(snapshot);
            else
                run("Comparing snapshot with current source",
                    () -> service.diff(selectedBackup, settings, snapshot, this::report),
                    changes -> showText("Diff against current source",
                        changes.isEmpty() ? "No differences." : String.join("\n", changes)));
        });
        panel.add(new JScrollPane(snapshotTable), BorderLayout.CENTER);
        JPanel pagination = new JPanel(new FlowLayout(FlowLayout.CENTER));
        previous.addActionListener(e -> {
            page--;
            renderPage();
        });
        next.addActionListener(e -> {
            page++;
            renderPage();
        });
        pagination.add(previous);
        pagination.add(pageLabel);
        pagination.add(next);
        panel.add(pagination, BorderLayout.SOUTH);
        return panel;
    }

    private void reloadBackups() {
        run("Loading backup configurations", configurations::list, listing -> {
            backups = listing.backups();
            backupModel.setRowCount(0);
            for (var backup : backups)
                backupModel.addRow(new Object[]{backup.id(), backup.name(), backup.from(), backup.to(), ""});
            if (!listing.errors().isEmpty())
                showText("Some configurations could not be loaded", String.join("\n", listing.errors()));
        });
    }

    private void showSnapshots(BackupConfiguration backup) {
        run("Loading snapshots", () -> service.snapshots(backup, settings), loaded -> {
            selectedBackup = backup;
            snapshots = loaded;
            page = 0;
            snapshotTitle.setText("Backup '" + backup.name() + "' (latest first)");
            renderPage();
            cards.show(backupCards, "snapshots");
        });
    }

    private void renderPage() {
        snapshotModel.setRowCount(0);
        snapshots.stream().skip((long) page * PAGE_SIZE).limit(PAGE_SIZE).forEach(snapshot ->
            snapshotModel.addRow(new Object[]{TIME.format(Instant.ofEpochMilli(snapshot.epochMillis())),
                String.format("%.3f s", snapshot.durationMillis() / 1000.0), snapshot.method(), ""}));
        pageLabel.setText(snapshots.isEmpty() ? "No snapshots yet" : "Page " + (page + 1) + " of "
            + ((snapshots.size() + PAGE_SIZE - 1) / PAGE_SIZE) + " • " + snapshots.size() + " snapshots");
        previous.setEnabled(!busy && page > 0);
        next.setEnabled(!busy && (page + 1) * PAGE_SIZE < snapshots.size());
    }

    private void editBackup(BackupConfiguration existing) {
        JTextField name = new JTextField(existing == null ? "" : existing.name(), 42);
        JTextField from = new JTextField(existing == null ? "" : existing.from().toString(), 42);
        JTextField to = new JTextField(existing == null ? "" : existing.to().toString(), 42);
        JPanel form = form();
        field(form, "Name", name);
        field(form, "From", folderField(from));
        field(form, "To", folderField(to));
        field(form, "Source formats", new JLabel("file:///folder or ssh://user@host:22/folder"));
        field(form, "Destination formats", new JLabel("file:///folder or s3://bucket/prefix"));
        while (JOptionPane.showConfirmDialog(this, form, existing == null ? "New backup" : "Edit backup",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                var backup = new BackupConfiguration(existing == null ? UUID.randomUUID() : existing.id(),
                    name.getText().trim(), URI.create(from.getText().trim()), URI.create(to.getText().trim()));
                run("Saving backup configuration", () -> {
                    configurations.save(backup);
                    return backup;
                }, ignored -> reloadBackups());
                return;
            } catch (Exception e) {
                error(e);
            }
        }
    }

    private JPanel folderField(JTextField text) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        panel.add(text, BorderLayout.CENTER);
        panel.add(button("Browse…", () -> {
            Path initial = null;
            try {
                URI uri = URI.create(text.getText().trim());
                if ("file".equals(uri.getScheme())) initial = Path.of(uri);
            } catch (IllegalArgumentException ignored) { /* A new or unfinished URI has no initial folder. */ }
            Path selected = FileDialogs.folder(text, "Choose a local folder", initial);
            if (selected != null) text.setText(selected.toUri().toString());
        }), BorderLayout.EAST);
        return panel;
    }

    private void importBackup() {
        Path path = FileDialogs.open(this, "Import backup configuration", null, true);
        if (path == null) return;
        run("Reading configuration", () -> ConfigurationStore.read(path, BackupConfiguration.class), backup -> {
            if (backups.stream().anyMatch(b -> b.id().equals(backup.id())) && JOptionPane.showConfirmDialog(this,
                "Replace the existing configuration with ID " + backup.id() + "?", "Import backup", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;
            run("Importing configuration", () -> {
                configurations.save(backup);
                return backup;
            }, ignored -> reloadBackups());
        });
    }

    private void exportBackup(BackupConfiguration backup) {
        Path file = FileDialogs.saveJson(this, backup.id() + ".json");
        if (file == null) return;
        if (Files.exists(file) && JOptionPane.showConfirmDialog(this, "Replace " + file.getFileName() + "?",
            "Export backup", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        run("Exporting configuration", () -> {
            ConfigurationStore.write(file, backup);
            return file;
        }, ignored -> {
        });
    }

    private void restore(Snapshot snapshot) {
        if (snapshot.method() == BackupMethod.restic) {
            run("Restoring snapshot", () -> {
                service.restore(selectedBackup, settings, snapshot, null);
                return null;
            }, ignored -> {
            });
            return;
        }
        Path destination = FileDialogs.folder(this, "Restore into an empty local folder", null);
        if (destination == null) return;
        run("Restoring snapshot", () -> {
                service.restore(selectedBackup, settings, snapshot, destination);
                return destination;
            },
            restored -> JOptionPane.showMessageDialog(this, "Restored to " + restored));
    }

    private void rebuildSettings() {
        JPanel form = form();
        var auth = new JComboBox<>(AppSettings.AwsAuthMethod.values());
        auth.setSelectedItem(settings.awsAuthMethod());
        var profile = new JTextField(settings.awsProfile(), 32);
        var accessKey = new JTextField(settings.awsAccessKeyId(), 32);
        var secret = new JPasswordField(settings.awsSecretAccessKey(), 32);
        var region = new JTextField(settings.awsRegion(), 32);
        var username = new JTextField(settings.sshUsername(), 32);
        var privateKey = new JTextField(settings.sshPrivateKey(), 32);
        var method = new JComboBox<>(BackupMethod.values());
        method.setSelectedItem(settings.preferredMethod());
        field(form, "AWS authentication", auth);
        field(form, "AWS profile", profile);
        field(form, "AWS access key ID", accessKey);
        field(form, "AWS secret access key", secret);
        field(form, "AWS region", region);
        field(form, "SSH default username", username);
        JPanel keyPanel = new JPanel(new BorderLayout(4, 0));
        keyPanel.add(privateKey, BorderLayout.CENTER);
        keyPanel.add(button("Browse…", () -> {
            Path selected = FileDialogs.open(privateKey, "Choose SSH private key",
                FilesSupport.expandHome(privateKey.getText().trim()), false);
            if (selected != null) privateKey.setText(selected.toAbsolutePath().toString());
        }), BorderLayout.EAST);
        field(form, "SSH private key file", keyPanel);
        field(form, "Preferred backup method", method);
        field(form, "", new JLabel("SSH uses ~/.ssh/known_hosts; a URI username overrides the default."));
        field(form, "", new JLabel("SSH accepts new host keys, rejects changed keys, and ignores ~/.ssh/config and agents."));
        field(form, "", new JLabel("Credentials are saved as plain JSON in config.json. Restic uses a fixed password."));
        field(form, "", button("Save settings", () -> {
            var updated = new AppSettings((AppSettings.AwsAuthMethod) auth.getSelectedItem(), profile.getText().trim(),
                accessKey.getText().trim(), new String(secret.getPassword()), region.getText().trim(),
                username.getText().trim(), privateKey.getText().trim(), (BackupMethod) method.getSelectedItem());
            if (updated.awsAuthMethod() == AppSettings.AwsAuthMethod.basic
                && (updated.awsAccessKeyId().isBlank() || updated.awsSecretAccessKey().isBlank())) {
                error(new IllegalArgumentException("Basic AWS authentication needs an access key ID and secret access key."));
                return;
            }
            run("Saving settings", () -> {
                configurations.saveSettings(updated);
                return updated;
            }, saved -> {
                settings = saved;
                report("Settings saved");
            });
        }));
        settingsPanel.removeAll();
        settingsPanel.add(form, BorderLayout.NORTH);
        settingsPanel.revalidate();
        settingsPanel.repaint();
    }

    private <T> void run(String description, Callable<T> task, Consumer<T> success) {
        run(description, task, success, () -> {
        });
    }

    private <T> void run(String description, Callable<T> task, Consumer<T> success, Runnable failure) {
        if (busy) return;
        busy = true;
        report(description);
        progress.setIndeterminate(true);
        enabled(tabs, false);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                busy = false;
                enabled(tabs, true);
                progress.setIndeterminate(false);
                renderPage();
                try {
                    T value = get();
                    report("Ready");
                    success.accept(value);
                } catch (Exception e) {
                    Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
                    report("Operation failed");
                    error(cause);
                    failure.run();
                }
            }
        }.execute();
    }

    private void report(String text) {
        progressText = text;
    }

    private void error(Throwable error) {
        log.warn("Backup operation failed: {}", error.getMessage());
        log.debug("Backup operation failure", error);
        JOptionPane.showMessageDialog(
            this,
            error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage(),
            error instanceof UnsupportedOperationException ? "Not implemented" : "Operation failed",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void showText(String title, String text) {
        var dialog = new JDialog(this, title, false);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        for (var shortcut : List.of(
            KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)
        )) {
            dialog.getRootPane().registerKeyboardAction(
                _ -> dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING)),
                shortcut,
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        }
        var area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setCaretPosition(0);
        dialog.add(new JScrollPane(area));
        dialog.setSize(850, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == getColumnCount() - 1;
            }
        };
    }

    private static JButton button(String label, Runnable action) {
        var button = new JButton(label);
        button.addActionListener(_ -> action.run());
        return button;
    }

    private static JPanel form() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return panel;
    }

    private static void field(JPanel form, String label, Component component) {
        var constraints = new GridBagConstraints();
        constraints.gridy = form.getComponentCount() / 2;
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5, 5, 5, 12);
        form.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        form.add(component, constraints);
    }

    private static void enabled(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container container)
            for (Component child : container.getComponents())
                enabled(child, enabled);
    }

}
