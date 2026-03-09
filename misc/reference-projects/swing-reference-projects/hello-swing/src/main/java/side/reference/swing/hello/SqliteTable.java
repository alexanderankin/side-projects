package side.reference.swing.hello;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class SqliteTable {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String initialPath = args.length > 0 ? args[0] : "";
            createAndShow(new Size(700, 1000), initialPath);
        });
    }

    @SneakyThrows
    private static void createAndShow(Size size, String initialPath) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        JFrame frame = new JFrame("SQLite Table Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(size.width(), size.height());
        frame.setLocationRelativeTo(null);
        frame.setContentPane(new SqliteTablePane(initialPath));
        registerCloseKeys(frame);
        frame.setVisible(true);
    }

    private static void registerCloseKeys(JFrame frame) {
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        KeyStroke quit = KeyStroke.getKeyStroke(
                KeyEvent.VK_Q,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()
        );

        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "EXIT");
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(quit, "EXIT");
        frame.getRootPane().getActionMap().put("EXIT", new LaterAction(frame::dispose));
    }

    static class SqliteTablePane extends JPanel {
        private final JTextField fileField = new JTextField();
        private final JComboBox<String> tableSelector = new JComboBox<>();
        private final JTable table = new JTable();
        private final JLabel status = new JLabel("Open a SQLite file");

        private String currentFile;
        private String currentTable;
        private TableData currentTableData;
        private boolean reloadingModel = false;

        public SqliteTablePane(String initialPath) {
            super(new BorderLayout(10, 10));
            this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel top = new JPanel(new BorderLayout(5, 5));
            JPanel filePanel = new JPanel(new BorderLayout(5, 5));
            JPanel fileButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            JButton openButton = new JButton("Open");
            JButton browseButton = new JButton("Browse");

            fileButtons.add(openButton);
            fileButtons.add(browseButton);

            filePanel.add(new JLabel("SQLite File"), BorderLayout.WEST);
            filePanel.add(fileField, BorderLayout.CENTER);
            filePanel.add(fileButtons, BorderLayout.EAST);

            JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
            JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            JButton reloadButton = new JButton("Reload");
            JButton addRowButton = new JButton("Add Row");
            JButton deleteRowButton = new JButton("Delete Row");
            JButton saveButton = new JButton("Save");

            tableButtons.add(reloadButton);
            tableButtons.add(addRowButton);
            tableButtons.add(deleteRowButton);
            tableButtons.add(saveButton);

            tablePanel.add(new JLabel("Table"), BorderLayout.WEST);
            tablePanel.add(tableSelector, BorderLayout.CENTER);
            tablePanel.add(tableButtons, BorderLayout.EAST);

            JPanel north = new JPanel(new GridLayout(2, 1, 0, 5));
            north.add(filePanel);
            north.add(tablePanel);

            top.add(north, BorderLayout.CENTER);

            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setCellSelectionEnabled(true);
            JScrollPane scrollPane = new JScrollPane(table);

            this.add(top, BorderLayout.NORTH);
            this.add(scrollPane, BorderLayout.CENTER);
            this.add(status, BorderLayout.SOUTH);

            openButton.addActionListener(new LaterAction(this::openFileFromField));
            browseButton.addActionListener(new LaterAction(this::browseForFile));
            reloadButton.addActionListener(new LaterAction(this::reloadCurrentTable));
            addRowButton.addActionListener(new LaterAction(this::addRow));
            deleteRowButton.addActionListener(new LaterAction(this::deleteSelectedRow));
            saveButton.addActionListener(new LaterAction(this::saveCurrentTable));

            tableSelector.addActionListener(new LaterAction(() -> {
                if (tableSelector.getSelectedItem() == null) {
                    return;
                }
                String selected = String.valueOf(tableSelector.getSelectedItem());
                if (!selected.equals(currentTable)) {
                    loadTable(selected);
                }
            }));

            fileField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OPEN");
            fileField.getActionMap().put("OPEN", new LaterAction(this::openFileFromField));

            if (initialPath != null && !initialPath.isBlank()) {
                fileField.setText(initialPath);
                openFileFromField();
            }
        }

        @SneakyThrows
        private Connection connection() {
            if (currentFile == null || currentFile.isBlank()) {
                throw new IllegalStateException("No SQLite file selected");
            }
            return DriverManager.getConnection("jdbc:sqlite:" + currentFile);
        }

        private void browseForFile() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select SQLite File");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (!fileField.getText().isBlank()) {
                chooser.setSelectedFile(new File(fileField.getText()));
            }

            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                fileField.setText(chooser.getSelectedFile().getAbsolutePath());
                openFileFromField();
            }
        }

        private void openFileFromField() {
            String path = fileField.getText();
            if (path == null || path.isBlank()) {
                setStatus("Enter a SQLite file path");
                return;
            }

            Path p = Path.of(path).toAbsolutePath().normalize();
            if (!p.toFile().exists()) {
                setStatus("File does not exist: " + p);
                return;
            }

            currentFile = p.toString();
            refreshTables();
        }

        @SneakyThrows
        private void refreshTables() {
            reloadingModel = true;
            try (Connection conn = connection()) {
                List<String> tables = new ArrayList<>();

                try (PreparedStatement ps = conn.prepareStatement("""
                        select name
                        from sqlite_master
                        where type = 'table'
                          and name not like 'sqlite_%'
                        order by name
                        """);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tables.add(rs.getString("name"));
                    }
                }

                tableSelector.removeAllItems();
                for (String tableName : tables) {
                    tableSelector.addItem(tableName);
                }

                if (tables.isEmpty()) {
                    currentTable = null;
                    currentTableData = null;
                    table.setModel(new DefaultTableModel());
                    setStatus("No user tables found");
                    return;
                }

                tableSelector.setSelectedIndex(0);
                loadTable(tables.getFirst());
            } finally {
                reloadingModel = false;
            }
        }

        private void reloadCurrentTable() {
            if (currentTable == null) {
                setStatus("No table selected");
                return;
            }
            loadTable(currentTable);
        }

        @SneakyThrows
        private void loadTable(String tableName) {
            currentTable = tableName;

            try (Connection conn = connection()) {
                List<ColumnMeta> columns = readColumns(conn, tableName);
                List<Vector<Object>> rows = new ArrayList<>();

                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery("select * from " + quoteIdent(tableName))) {
                    while (rs.next()) {
                        Vector<Object> row = new Vector<>();
                        for (ColumnMeta column : columns) {
                            row.add(rs.getObject(column.name()));
                        }
                        rows.add(row);
                    }
                }

                Vector<String> columnNames = new Vector<>();
                for (ColumnMeta column : columns) {
                    columnNames.add(column.name());
                }

                DefaultTableModel model = new DefaultTableModel(new Vector<>(rows), columnNames);

                model.addTableModelListener(e -> {
                    if (!reloadingModel && e.getType() == TableModelEvent.UPDATE) {
                        setStatus("Edited cell. Press Save to persist.");
                    }
                });

                table.setModel(model);
                currentTableData = new TableData(columns, model);
                setStatus("Loaded table: " + tableName + " (" + rows.size() + " rows)");
            }
        }

        private void addRow() {
            if (currentTableData == null) {
                setStatus("No table loaded");
                return;
            }

            Vector<Object> row = new Vector<>();
            for (int i = 0; i < currentTableData.columns.size(); i++) {
                row.add(null);
            }
            currentTableData.model.addRow(row);
            setStatus("Row added. Press Save to persist.");
        }

        private void deleteSelectedRow() {
            if (currentTableData == null) {
                setStatus("No table loaded");
                return;
            }

            int selected = table.getSelectedRow();
            if (selected == -1) {
                setStatus("Select a row to delete");
                return;
            }

            currentTableData.model.removeRow(selected);
            setStatus("Row deleted in UI. Press Save to persist.");
        }

        @SneakyThrows
        private void saveCurrentTable() {
            if (currentTableData == null || currentTable == null) {
                setStatus("No table loaded");
                return;
            }

            try (Connection conn = connection()) {
                conn.setAutoCommit(false);
                try {
                    replaceWholeTable(conn, currentTable, currentTableData);
                    conn.commit();
                    setStatus("Saved table: " + currentTable);
                    loadTable(currentTable);
                } catch (Throwable t) {
                    conn.rollback();
                    throw t;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        }

        @SneakyThrows
        private void replaceWholeTable(Connection conn, String tableName, TableData data) {
            List<ColumnMeta> columns = data.columns;
            DefaultTableModel model = data.model;

            String quotedTable = quoteIdent(tableName);

            try (Statement st = conn.createStatement()) {
                st.executeUpdate("delete from " + quotedTable);
            }

            StringBuilder sql = new StringBuilder();
            sql.append("insert into ").append(quotedTable).append(" (");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append(quoteIdent(columns.get(i).name()));
            }
            sql.append(") values (");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
            }
            sql.append(")");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < columns.size(); col++) {
                        Object value = model.getValueAt(row, col);
                        ps.setObject(col + 1, normalizeEmpty(value));
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }

        private Object normalizeEmpty(Object value) {
            if (value instanceof String s && s.isBlank()) {
                return null;
            }
            return value;
        }

        @SneakyThrows
        private List<ColumnMeta> readColumns(Connection conn, String tableName) {
            List<ColumnMeta> columns = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement("pragma table_info(" + quoteSqlString(tableName) + ")");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    columns.add(new ColumnMeta(
                            rs.getString("name"),
                            rs.getString("type"),
                            rs.getInt("notnull") != 0,
                            rs.getInt("pk") != 0
                    ));
                }
            }

            return columns;
        }

        private String quoteIdent(String ident) {
            return "\"" + ident.replace("\"", "\"\"") + "\"";
        }

        private String quoteSqlString(String value) {
            return "'" + value.replace("'", "''") + "'";
        }

        private void setStatus(String message) {
            status.setText(message);
        }
    }

    record TableData(List<ColumnMeta> columns, DefaultTableModel model) {
    }

    record ColumnMeta(String name, String type, boolean notNull, boolean primaryKey) {
    }

    public record Size(int height, int width) {
    }

    @RequiredArgsConstructor
    private static class LaterAction extends AbstractAction {
        private final ThrowingConsumer<ActionEvent> consumer;

        public LaterAction(ThrowingRunnable throwingRunnable) {
            this(ignored -> throwingRunnable.run());
        }

        @SneakyThrows
        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(() -> consumer.unsafeAccept(e));
        }

        public interface ThrowingConsumer<T> {
            void accept(T t) throws Throwable;

            @SneakyThrows
            default void unsafeAccept(T t) {
                accept(t);
            }
        }

        public interface ThrowingRunnable {
            void run() throws Throwable;
        }
    }
}
