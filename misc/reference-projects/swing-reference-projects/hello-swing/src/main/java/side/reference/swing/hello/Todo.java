package side.reference.swing.hello;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class Todo {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShow(new Size(400, 500)));
    }

    @SneakyThrows
    private static void createAndShow(Size size) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        JFrame frame = new JFrame("Todo App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(size.width(), size.height());
        frame.setLocationRelativeTo(null);
        frame.add(new TodoPane());
        frame.pack();
        frame.setVisible(true);
    }

    static class TodoPane extends JPanel {
        int editingIndex = -1;

        public TodoPane() {
            super(new BorderLayout(10, 10));
            this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel label = new JLabel("Hello World - Todo List", SwingConstants.CENTER);

            DefaultListModel<String> model = new DefaultListModel<>();
            JList<String> list = new JList<>(model);
            list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scroll = new JScrollPane(list);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
            JButton edit = new JButton("Edit");
            buttons.add(edit);
            JButton delete = new JButton("Delete");
            buttons.add(delete);

            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            JTextField input = new JTextField();
            inputPanel.add(input, BorderLayout.CENTER);
            JButton add = new JButton("Add");
            inputPanel.add(add, BorderLayout.EAST);


            LaterAction submit = new LaterAction(() -> {
                var text = input.getText();
                input.setText("");
                if (text != null && !text.isBlank()) {
                    if (editingIndex == -1) {
                        model.addElement(text);
                    } else {
                        model.set(editingIndex, text);
                        editingIndex = -1;
                    }
                }
            });
            input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ADD");
            input.getActionMap().put("ADD", submit);
            add.addActionListener(submit);

            delete.addActionListener(new LaterAction(() -> {
                var selectedIndex = list.getSelectedIndex();
                if (selectedIndex == -1)
                    return;

                model.remove(selectedIndex);
                list.setSelectedIndex(selectedIndex);
            }));

            edit.addActionListener(new LaterAction(() -> {
                var selectedIndex = list.getSelectedIndex();
                if (selectedIndex == -1)
                    return;

                var value = model.get(selectedIndex);
                input.setText(value);
                editingIndex = selectedIndex;
            }));

            this.add(label, BorderLayout.PAGE_START);
            this.add(scroll, BorderLayout.CENTER);
            this.add(buttons, BorderLayout.LINE_END);
            this.add(inputPanel, BorderLayout.PAGE_END);
        }
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
