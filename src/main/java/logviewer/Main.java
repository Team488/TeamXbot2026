package logviewer;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FileDialog;
import java.awt.Dimension;
import java.io.File;
import java.util.List;

/**
 * Essentially AdvantageScope but that you can search for string literals.
 * Useful for check if command ran in CommandTracer.
 */
class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }

    private final JFrame frame;
    private final JList<String> entryList;
    private final DefaultListModel<String> listModel;
    private final JTextArea textArea;

    private final JTextField searchField;
    private final JTextField dataSearchField;

    private final WPILogProcessor processor = new WPILogProcessor();

    private Main() {
        frame = new JFrame("WPILog Viewer");
        frame.setSize(1100, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        JButton openButton = new JButton("Open .wpilog");
        frame.add(openButton, BorderLayout.NORTH);
        openButton.addActionListener(e -> openFile());

        // LEFT
        searchField = new JTextField();
        listModel = new DefaultListModel<>();
        entryList = new JList<>(listModel);
        entryList.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        entryList.addListSelectionListener(e -> showData());

        searchField.getDocument().addDocumentListener(new SimpleListener(this::updateList));

        JPanel left = new JPanel(new BorderLayout());
        left.setBorder(new EmptyBorder(8, 8, 8, 4));
        left.setPreferredSize(new Dimension(320, 0));
        left.add(searchField, BorderLayout.NORTH);
        left.add(new JScrollPane(entryList), BorderLayout.CENTER);

        // RIGHT
        textArea = new JTextArea();
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        textArea.setEditable(false);

        dataSearchField = new JTextField();
        dataSearchField.getDocument().addDocumentListener(new SimpleListener(this::showData));

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(new EmptyBorder(8, 4, 8, 8));
        right.add(dataSearchField, BorderLayout.NORTH);
        right.add(new JScrollPane(textArea), BorderLayout.CENTER);

        frame.add(left, BorderLayout.WEST);
        frame.add(right, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void openFile() {
        FileDialog dialog = new FileDialog(frame, "Open .wpilog", FileDialog.LOAD);
        dialog.setFile("*.wpilog");
        dialog.setVisible(true);

        String file = dialog.getFile();
        String directory = dialog.getDirectory();

        if (file == null) {
            return;
        }

        try {
            processor.load(new File(directory, file));
            updateList();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void updateList() {
        List<String> entries = processor.getFilteredEntries(searchField.getText());

        listModel.clear();
        for (String e : entries) {
            listModel.addElement(e);
        }
    }

    private void showData() {
        int index = entryList.getSelectedIndex();
        if (index == -1) {
            return;
        }

        String selected = listModel.get(index);

        List<WPILogProcessor.DataPoint> data =
                processor.getFilteredData(selected, dataSearchField.getText());

        textArea.setText("");

        int count = 0;
        for (var p : data) {
            textArea.append(String.format("%.2fs: %s\n", p.time(), p.value()));
            if (++count >= 1000) {
                break;
            }
        }
    }

    private record SimpleListener(Runnable action) implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            action.run();
        }

        public void removeUpdate(DocumentEvent e) {
            action.run();
        }

        public void changedUpdate(DocumentEvent e) {
            action.run();
        }
    }
}