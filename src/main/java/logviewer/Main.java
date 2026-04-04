package logviewer;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FileDialog;
import java.awt.Dimension;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

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

    // MULTI FILE SUPPORT
    private final List<WPILogProcessor> processors = new ArrayList<>();
    private final List<String> fileNames = new ArrayList<>();

    private final DefaultListModel<String> fileListModel = new DefaultListModel<>();
    private final JList<String> fileList = new JList<>(fileListModel);

    private Main() {
        frame = new JFrame("XRAY - Post Match Analysis Tool");
        frame.setSize(1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        JPanel topBar = new JPanel();

        JButton openButton = new JButton("Add .wpilog");
        JButton removeButton = new JButton("Remove Selected");

        openButton.addActionListener(e -> openFile());
        removeButton.addActionListener(e -> removeSelectedFile());

        topBar.add(openButton);
        topBar.add(removeButton);

        frame.add(topBar, BorderLayout.NORTH);

        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBorder(new EmptyBorder(8, 8, 8, 4));
        filePanel.add(new JLabel("Files"), BorderLayout.NORTH);
        filePanel.add(new JScrollPane(fileList), BorderLayout.CENTER);

        filePanel.setMinimumSize(new Dimension(120, 100));

        searchField = new JTextField();

        listModel = new DefaultListModel<>();
        entryList = new JList<>(listModel);
        entryList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        entryList.addListSelectionListener(e -> showData());

        searchField.getDocument().addDocumentListener(new SimpleListener(this::updateList));

        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBorder(new EmptyBorder(8, 4, 8, 4));
        entryPanel.add(searchField, BorderLayout.NORTH);
        entryPanel.add(new JScrollPane(entryList), BorderLayout.CENTER);

        entryPanel.setMinimumSize(new Dimension(150, 100));

        /* =========================
           DATA PANEL
           ========================= */
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        dataSearchField = new JTextField();
        dataSearchField.getDocument().addDocumentListener(new SimpleListener(this::showData));

        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.setBorder(new EmptyBorder(8, 4, 8, 8));
        dataPanel.add(dataSearchField, BorderLayout.NORTH);
        dataPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);

        dataPanel.setMinimumSize(new Dimension(250, 100));

        JSplitPane mainSplit = getJSplitPane(filePanel, entryPanel, dataPanel);

        frame.add(mainSplit, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private static JSplitPane getJSplitPane(JPanel filePanel, JPanel entryPanel, JPanel dataPanel) {
        JSplitPane leftVertical = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                filePanel,
                entryPanel
        );
        leftVertical.setResizeWeight(0.3);
        leftVertical.setDividerLocation(150);
        leftVertical.setContinuousLayout(true);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftVertical,
                dataPanel
        );
        mainSplit.setResizeWeight(0.3);
        mainSplit.setDividerLocation(300);
        mainSplit.setContinuousLayout(true);

        return mainSplit;
    }

    private void openFile() {
        FileDialog dialog = new FileDialog(frame, "Open .wpilog", FileDialog.LOAD);
        dialog.setFile("*.wpilog");
        dialog.setVisible(true);

        if (dialog.getFile() == null) {
            return;
        }

        try {
            File file = new File(dialog.getDirectory(), dialog.getFile());

            WPILogProcessor processor = new WPILogProcessor();
            processor.load(file);

            processors.add(processor);
            fileNames.add(file.getName());
            fileListModel.addElement(file.getName());

            // --- NEW LOGIC ---
            String prev = entryList.getSelectedValue();

            updateList();

            if (prev != null && listModel.contains(prev)) {
                entryList.setSelectedValue(prev, true);
            } else if (!listModel.isEmpty()) {
                entryList.setSelectedIndex(0);
            }

            showData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void removeSelectedFile() {
        int index = fileList.getSelectedIndex();
        if (index == -1) {
            return;
        }

        processors.remove(index);
        fileNames.remove(index);
        fileListModel.remove(index);

        updateList();
        textArea.setText("");
    }

    private void updateList() {
        listModel.clear();

        Set<String> combined = new TreeSet<>();

        for (WPILogProcessor p : processors) {
            combined.addAll(p.getFilteredEntries(searchField.getText()));
        }

        for (String e : combined) {
            listModel.addElement(e);
        }
    }

    private void showData() {
        int index = entryList.getSelectedIndex();
        if (index == -1) {
            return;
        }

        String selected = listModel.get(index);

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = 0; i < processors.size(); i++) {
            var processor = processors.get(i);
            var data = processor.getFilteredData(selected, dataSearchField.getText());

            for (var p : data) {
                sb.append(String.format("[%s] %.2fs: %s%n",
                        fileNames.get(i),
                        p.time(),
                        p.value()
                ));

                if (++count > 1500) {
                    break;
                }
            }

            if (count > 1500) {
                break;
            }
        }

        textArea.setText(sb.toString());
    }

    private record SimpleListener(Runnable action) implements DocumentListener {
        public void insertUpdate(DocumentEvent e) { action.run(); }
        public void removeUpdate(DocumentEvent e) { action.run(); }
        public void changedUpdate(DocumentEvent e) { action.run(); }
    }
}