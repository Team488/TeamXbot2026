package competition;

import edu.wpi.first.util.datalog.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * This class was vibe-coded
 * And its primary purpose is so that we can view .wpilog files
 * except the fact that we can search for string literals
 * which AdvantageScope doesn't offer.
 *
 * Useful to see if we ever ran a specific command in the CommandTracer
 */
class WPILogViewer {

    private final JFrame frame;
    private final JList<String> entryList;
    private final DefaultListModel<String> listModel;
    private final JTextArea textArea;

    private final JTextField searchField;
    private final JTextField dataSearchField;

    private final Map<Integer, String> entryNames = new HashMap<>();
    private final Map<Integer, String> entryTypes = new HashMap<>();
    private final Map<Integer, List<DataPoint>> entryData = new HashMap<>();

    private final List<String> allEntries = new ArrayList<>();

    private static class DataPoint {
        double time;
        String value;

        DataPoint(double time, String value) {
            this.time = time;
            this.value = value;
        }
    }

    private WPILogViewer() {
        frame = new JFrame("WPILog Viewer");
        frame.setSize(1100, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        // ===== TOP BUTTON =====
        JButton openButton = new JButton("Open .wpilog");
        openButton.setFocusPainted(false);
        frame.add(openButton, BorderLayout.NORTH);
        openButton.addActionListener(e -> openFile());

        // ===== LEFT PANEL =====
        searchField = new JTextField();
        searchField.setToolTipText("Search fields (e.g. /swerve)");

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterList(); }
            public void removeUpdate(DocumentEvent e) { filterList(); }
            public void changedUpdate(DocumentEvent e) { filterList(); }
        });

        listModel = new DefaultListModel<>();
        entryList = new JList<>(listModel);
        entryList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        entryList.addListSelectionListener(e -> showData());

        JScrollPane listScroll = new JScrollPane(entryList);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(new EmptyBorder(8, 8, 8, 4));
        leftPanel.setPreferredSize(new Dimension(320, 0));

        leftPanel.add(new JLabel("Fields"), BorderLayout.NORTH);

        JPanel leftTop = new JPanel(new BorderLayout(3, 3));
        leftTop.add(searchField, BorderLayout.CENTER);

        leftPanel.add(leftTop, BorderLayout.NORTH);
        leftPanel.add(listScroll, BorderLayout.CENTER);

        // ===== RIGHT PANEL =====
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JScrollPane textScroll = new JScrollPane(textArea);

        dataSearchField = new JTextField();
        dataSearchField.setToolTipText("Search inside data...");

        dataSearchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }

            private void refresh() {
                SwingUtilities.invokeLater(() -> showData());
            }
        });

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.setBorder(new EmptyBorder(8, 4, 8, 8));

        rightPanel.add(new JLabel("Data Viewer"), BorderLayout.NORTH);

        JPanel rightTop = new JPanel(new BorderLayout(3, 3));
        rightTop.add(dataSearchField, BorderLayout.CENTER);

        rightPanel.add(rightTop, BorderLayout.NORTH);
        rightPanel.add(textScroll, BorderLayout.CENTER);

        // ===== MAIN =====
        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(rightPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        try {
            loadLog(chooser.getSelectedFile());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
        }
    }

    private void loadLog(File file) throws Exception {
        DataLogReader reader = new DataLogReader(file.getAbsolutePath());

        String currentSearch = searchField.getText();

        entryNames.clear();
        entryTypes.clear();
        entryData.clear();
        listModel.clear();
        allEntries.clear();

        textArea.setText("");
        dataSearchField.setText("");

        for (DataLogRecord record : reader) {

            if (record.isStart()) {
                var start = record.getStartData();

                entryNames.put(start.entry, start.name);
                entryTypes.put(start.entry, start.type);
                entryData.put(start.entry, new ArrayList<>());

                allEntries.add(start.name + " (" + start.type + ")");
            }

            else if (!record.isControl()) {
                int entry = record.getEntry();
                String type = entryTypes.get(entry);
                if (type == null) continue;

                String value;
                try {
                    value = switch (type) {
                        case "double" -> String.valueOf(record.getDouble());
                        case "int64" -> String.valueOf(record.getInteger());
                        case "float" -> String.valueOf(record.getFloat());
                        case "string" -> record.getString();
                        case "boolean" -> String.valueOf(record.getBoolean());
                        case "string[]" -> Arrays.toString(record.getStringArray());
                        case "double[]" -> Arrays.toString(record.getDoubleArray());
                        case "int64[]" -> Arrays.toString(record.getIntegerArray());
                        case "float[]" -> Arrays.toString(record.getFloatArray());
                        case "boolean[]" -> Arrays.toString(record.getBooleanArray());
                        default -> "[unsupported: " + type + "]";
                    };
                } catch (Exception e) {
                    value = "[decode error]";
                }

                double timeSec = record.getTimestamp() / 1_000_000.0;
                entryData.get(entry).add(new DataPoint(timeSec, value));
            }
        }

        searchField.setText(currentSearch);
        filterList();
    }

    private void filterList() {
        String query = searchField.getText().toLowerCase();
        listModel.clear();

        for (String entry : allEntries) {
            if (entry.toLowerCase().contains(query)) {
                listModel.addElement(entry);
            }
        }
    }

    private void showData() {
        int index = entryList.getSelectedIndex();
        if (index == -1) return;

        String selected = listModel.get(index);
        String selectedName = selected.split(" \\(")[0];

        Integer entryId = entryNames.entrySet().stream()
                .filter(e -> e.getValue().equals(selectedName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (entryId == null) return;

        List<DataPoint> data = entryData.get(entryId);

        textArea.setText("");

        String query = dataSearchField.getText();
        boolean hasQuery = query != null && !query.isBlank();
        String lowerQuery = hasQuery ? query.toLowerCase() : "";

        int count = 0;

        for (DataPoint p : data) {
            String line = String.format("%.3f s : %s", p.time, p.value);

            if (!hasQuery || line.toLowerCase().contains(lowerQuery)) {
                textArea.append(line + "\n");
                if (++count >= 1000) break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WPILogViewer::new);
    }
}