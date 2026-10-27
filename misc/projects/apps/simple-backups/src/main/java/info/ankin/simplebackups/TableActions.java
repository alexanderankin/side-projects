package info.ankin.simplebackups;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.function.BiConsumer;

final class TableActions extends AbstractCellEditor implements javax.swing.table.TableCellEditor, TableCellRenderer {
    private final JPanel renderer;
    private final JPanel editor;
    private int row;

    private TableActions(JTable table, String[] actions, BiConsumer<Integer, String> handler) {
        renderer = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        editor = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        for (String action : actions) {
            renderer.add(new JButton(action));
            JButton button = new JButton(action);
            button.addActionListener(e -> {
                int selected = table.convertRowIndexToModel(row);
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> handler.accept(selected, action));
            });
            editor.add(button);
        }
    }

    static void install(JTable table, String[] actions, BiConsumer<Integer, String> handler) {
        var cells = new TableActions(table, actions, handler);
        var column = table.getColumnModel().getColumn(table.getColumnCount() - 1);
        column.setCellRenderer(cells);
        column.setCellEditor(cells);
        int width = cells.renderer.getPreferredSize().width + 16;
        column.setMinWidth(width);
        column.setPreferredWidth(width);
        table.setRowHeight(cells.renderer.getPreferredSize().height + 6);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
        renderer.setBackground(selected ? table.getSelectionBackground() : table.getBackground());
        for (Component button : renderer.getComponents()) button.setEnabled(table.isEnabled());
        return renderer;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean selected, int row, int col) {
        this.row = row;
        editor.setBackground(table.getSelectionBackground());
        return editor;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}
