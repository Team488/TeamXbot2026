package logviewer;

import edu.wpi.first.util.datalog.DataLogReader;
import edu.wpi.first.util.datalog.DataLogRecord;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class WPILogProcessor {

    public record DataPoint(double time, String value) {}

    private final Map<Integer, String> entryNames = new HashMap<>();
    private final Map<Integer, String> entryTypes = new HashMap<>();
    private final Map<Integer, List<DataPoint>> entryData = new HashMap<>();

    private final List<String> allEntries = new ArrayList<>();

    public void load(File file) throws Exception {
        DataLogReader reader = new DataLogReader(file.getAbsolutePath());

        entryNames.clear();
        entryTypes.clear();
        entryData.clear();
        allEntries.clear();

        try {
            for (DataLogRecord record : reader) {
                if (record.isStart()) {
                    var start = record.getStartData();
                    entryNames.put(start.entry, start.name);
                    entryTypes.put(start.entry, start.type);
                    entryData.put(start.entry, new ArrayList<>());
                    allEntries.add(start.name);
                } else if (!record.isControl()) {
                    int entry = record.getEntry();
                    String type = entryTypes.get(entry);
                    if (type == null) {
                        continue;
                    }
                    String value = getString(record, type);
                    double timeSec = record.getTimestamp() / 1_000_000.0;
                    entryData.get(entry).add(new DataPoint(timeSec, value));
                }
            }
        } catch (Exception e) {
            System.out.println("Reached corrupted end of log, stopping early.");
        }
    }

    public List<String> getFilteredEntries(String query) {
        if (query == null || query.isBlank()) {
            return allEntries;
        }

        String q = query.toLowerCase();
        List<String> result = new ArrayList<>();

        for (String entry : allEntries) {
            if (entry.toLowerCase().contains(q)) {
                result.add(entry);
            }
        }
        return result;
    }

    public List<DataPoint> getFilteredData(String entryDisplay, String query) {
        String selectedName = entryDisplay.split(" \\(")[0];

        Integer entryId = entryNames.entrySet().stream()
                .filter(e -> e.getValue().equals(selectedName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (entryId == null) {
            return List.of();
        }

        List<DataPoint> data = entryData.get(entryId);

        if (query == null || query.isBlank()) {
            return data;
        }

        String q = query.toLowerCase();
        List<DataPoint> result = new ArrayList<>();

        for (DataPoint p : data) {
            String line = String.format("%.3f s : %s", p.time(), p.value());
            if (line.toLowerCase().contains(q)) {
                result.add(p);
            }
        }

        return result;
    }

    private static String getString(DataLogRecord record, String type) {
        try {
            return switch (type) {
                case "double" -> String.valueOf(record.getDouble());
                case "int64" -> String.valueOf(record.getInteger());
                case "float" -> String.valueOf(record.getFloat());
                case "string" -> record.getString();
                case "boolean" -> String.valueOf(record.getBoolean());
                case "string[]" -> {
                    String[] arr = record.getStringArray();
                    Arrays.sort(arr, String.CASE_INSENSITIVE_ORDER);
                    yield Arrays.toString(arr);
                }
                case "double[]" -> Arrays.toString(record.getDoubleArray());
                case "int64[]" -> Arrays.toString(record.getIntegerArray());
                case "float[]" -> Arrays.toString(record.getFloatArray());
                case "boolean[]" -> Arrays.toString(record.getBooleanArray());
                default -> "[unsupported: " + type + "]";
            };
        } catch (Exception e) {
            return "[decode error]";
        }
    }
}