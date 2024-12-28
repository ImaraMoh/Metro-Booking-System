import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class MetroBookingSystem extends JFrame {
    private static final int[][] distances = {
        {0, 10, 22, Integer.MAX_VALUE, 8, Integer.MAX_VALUE},
        {10, 0, 15, 9, Integer.MAX_VALUE, 7},
        {22, 15, 0, 9, Integer.MAX_VALUE, Integer.MAX_VALUE},
        {Integer.MAX_VALUE, 9, 9, 0, 5, 12},
        {8, Integer.MAX_VALUE, Integer.MAX_VALUE, 5, 0, 16},
        {Integer.MAX_VALUE, 7, Integer.MAX_VALUE, 12, 16, 0}
    };
    private static final int TRAIN_SPEED_KMH = 30;
    private static final int START_TIME = 6 * 60; // 6 AM in minutes
    private static final int END_TIME = 20 * 60;  // 8 PM in minutes

    private JComboBox<String> startStation, endStation;
    private JTextField startTimeField;
    private JTextArea output;

    public MetroBookingSystem() {
        setTitle("Inter-City Metro Ticket Booking System");

        // Input Panel
        JPanel inputPanel = new JPanel();
        startStation = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});
        endStation = new JComboBox<>(new String[]{"A", "B", "C", "D", "E", "F"});

        // Start Time Field with a dropdown button
        startTimeField = new JTextField(8);
        startTimeField.setText("06:00 AM");
        startTimeField.setEditable(false);
        
        JButton timePickerButton = new JButton("▼"); // Dropdown arrow button
        timePickerButton.addActionListener(e -> openTimePicker());

        // Panel to hold the time field and the dropdown button
        JPanel timePanel = new JPanel();
        timePanel.add(startTimeField);
        timePanel.add(timePickerButton);

        JButton searchButton = new JButton("Find Trip");

        inputPanel.add(new JLabel("Start Station:"));
        inputPanel.add(startStation);
        inputPanel.add(new JLabel("End Station:"));
        inputPanel.add(endStation);
        inputPanel.add(new JLabel("Start Time:"));
        inputPanel.add(timePanel); // Use the new time panel
        inputPanel.add(searchButton);

        // Output Panel
        output = new JTextArea(15, 40);
        output.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(output);

        // Button Action
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                output.setText("");  // Clear output before each search
                int start = startStation.getSelectedIndex();
                int end = endStation.getSelectedIndex();
                int time = parseTime(startTimeField.getText());

                if (start != end && time >= START_TIME && time <= END_TIME) {
                    List<Integer> path = findShortestPath(start, end);
                    if (path != null) {
                        String tripDetails = buildTripDetails(path, time);
                        output.setText(tripDetails);
                    } else {
                        output.setText("No path found between selected stations.");
                    }
                } else {
                    output.setText("Invalid input or time out of operating hours.");
                }
            }
            
        });

        // Main Layout
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        add(inputPanel);
        add(scrollPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);

        setLocationRelativeTo(null);
    }

    // Dijkstra's Algorithm to find the shortest path
    private List<Integer> findShortestPath(int start, int end) {
        int n = distances.length;
        int[] dist = new int[n];
        boolean[] visited = new boolean[n];
        int[] prev = new int[n];

        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        dist[start] = 0;

        for (int i = 0; i < n - 1; i++) {
            int u = -1;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && (u == -1 || dist[j] < dist[u])) u = j;
            }
            if (dist[u] == Integer.MAX_VALUE) break; // No reachable vertex left

            visited[u] = true;

            for (int v = 0; v < n; v++) {
                if (!visited[v] && distances[u][v] != Integer.MAX_VALUE &&
                    dist[u] + distances[u][v] < dist[v]) {
                    dist[v] = dist[u] + distances[u][v];
                    prev[v] = u;
                }
            }
        }

        List<Integer> path = new ArrayList<>();
        for (int at = end; at != -1; at = prev[at]) path.add(at);
        Collections.reverse(path);
        return path.get(0) == start ? path : null;
    }

    private int calculateTravelTime(int distance) {
        return (distance * 60) / TRAIN_SPEED_KMH;
    }

    private String buildTripDetails(List<Integer> path, int startTime) {
        StringBuilder tripDetails = new StringBuilder();
        tripDetails.append("\nTrip ").append((char) ('A' + path.get(0))).append(" to ")
                   .append((char) ('A' + path.get(path.size() - 1))).append("\n")
                   .append("--------------\n");

        int currentTime = startTime;
        int totalTime = 0;

        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            int travelTime = calculateTravelTime(distances[from][to]);

            tripDetails.append((char) ('A' + from)).append(" to ")
                       .append((char) ('A' + to)).append(" : Start at ")
                       .append(formatTime(currentTime)).append(" - Stops at ")
                       .append(formatTime(currentTime + travelTime)).append("\n");

            currentTime += travelTime;

            if (i < path.size() - 2) {
                currentTime += 10; // add 10 mins waiting time
            }
        }

        totalTime = currentTime - startTime;
        tripDetails.append("Total time = ").append(totalTime).append(" minutes\n");
        return tripDetails.toString();
    }

    private int parseTime(String timeStr) {
        String[] parts = timeStr.split(" ");
        String[] hm = parts[0].split(":");
        int hours = Integer.parseInt(hm[0]);
        int minutes = Integer.parseInt(hm[1]);

        if (parts[1].equalsIgnoreCase("PM") && hours != 12) hours += 12;
        if (parts[1].equalsIgnoreCase("AM") && hours == 12) hours = 0;

        return hours * 60 + minutes;
    }

    private String formatTime(int minutes) {
        int hrs = minutes / 60;
        int mins = minutes % 60;
        String amPm = (hrs >= 12) ? "PM" : "AM";
        if (hrs > 12) hrs -= 12;
        if (hrs == 0) hrs = 12;
        return String.format("%02d:%02d %s", hrs, mins, amPm);
    }

    private void openTimePicker() {
        JDialog timePicker = new JDialog(this, "Select Time", true);
        timePicker.setLayout(new GridLayout(1, 3));

        // Create hour spinner
        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
        // Create minute spinner
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        // Create AM/PM spinner
        String[] amPmOptions = {"AM", "PM"};
        JComboBox<String> amPmComboBox = new JComboBox<>(amPmOptions);

        JButton confirmButton = new JButton("Set Time");
        confirmButton.addActionListener(e -> {
            String selectedTime = hourSpinner.getValue() + ":" +
                                  String.format("%02d", minuteSpinner.getValue()) + " " +
                                  amPmComboBox.getSelectedItem();
            startTimeField.setText(selectedTime);
            timePicker.dispose();
        });

        timePicker.add(hourSpinner);
        timePicker.add(minuteSpinner);
        timePicker.add(amPmComboBox);
        timePicker.add(confirmButton);

        timePicker.setLayout(new FlowLayout());
        timePicker.pack();
        timePicker.setLocationRelativeTo(this);
        timePicker.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MetroBookingSystem::new);
    }
}
