package pl.edu.pw.mini.hermant.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import pl.edu.pw.mini.hermant.audio.Clip;
import pl.edu.pw.mini.hermant.audio.Frame;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimeAnalyzerForm implements Form {
    private JPanel mainPanel;
    private JScrollPane amplitudeChartPanel;
    private JScrollPane volumeChartPanel;
    private JScrollPane shortTimeEnergyChartPanel;
    private JScrollPane zeroCrossingRateChartPanel;
    private JTabbedPane tabbedPane;
    private JTable characteristicsTable;
    private JScrollPane characteristicsPanel;
    private final MenuBar menuBar;
    private JFileChooser inputChooser;
    private Clip clip;
    private final HashMap<String, JFreeChart> charts = new HashMap<>();
    private final HashMap<String, Consumer<String>> characteristics = new HashMap<>();

    public TimeAnalyzerForm() {
        $$$setupUI$$$();
        setupFileChoosers();
        menuBar = new MenuBar();
        menuBar.getMenuItem("Open").addActionListener(this::open);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    private void setupFileChoosers() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        inputChooser = new JFileChooser();
        inputChooser.setCurrentDirectory(workingDirectory);
    }

    private void open(ActionEvent e) {
        int returnVal = inputChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = inputChooser.getSelectedFile();
            try {
                clip = new Clip(file.getAbsolutePath());
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
            drawChart(amplitudeChartPanel, "Amplitude", clip.getSamples().stream(), Clip.SAMPLE_TIME);
            drawChart(volumeChartPanel, "Volume", clip.getFrames().stream().map(Frame::getVolume), Clip.FRAME_TIME);
            drawChart(shortTimeEnergyChartPanel, "Short Time Energy", clip.getFrames().stream().map(Frame::getShortTimeEnergy), Clip.FRAME_TIME);
            drawChart(zeroCrossingRateChartPanel, "Zero Crossing Rate", clip.getFrames().stream().map(Frame::getZeroCrossingRate), Clip.FRAME_TIME);
            markCharts(new String[]{"Amplitude", "Volume", "Short Time Energy", "Zero Crossing Rate"}, clip.getFrames().stream().map(Frame::isSilence), Clip.FRAME_TIME, Color.BLUE);
            markCharts(new String[]{"Amplitude", "Volume", "Short Time Energy", "Zero Crossing Rate"}, clip.getFrames().stream().map(Frame::isVoiced), Clip.FRAME_TIME, Color.RED);
            markCharts(new String[]{"Amplitude", "Volume", "Short Time Energy", "Zero Crossing Rate"}, clip.getFrames().stream().map(Frame::isVoiceless), Clip.FRAME_TIME, Color.GREEN);
            setCharacteristic("Total volume", Float.toString(clip.getVolume()));
            setCharacteristic("Volume Dynamic Range", Float.toString(clip.getVolumeDynamicRange()));
            setCharacteristic("Average Short Time Energy", Float.toString(clip.getShortTimeEnergy()));
            setCharacteristic("Minimum volume", Float.toString(clip.getMinVolume()));
            setCharacteristic("Maximum volume", Float.toString(clip.getMaxVolume()));
            setCharacteristic("Average Zero Crossing Rate", Float.toString(clip.getAverageZeroCrossingRate()));
            setCharacteristic("Low Short Time Energy Ratio", Float.toString(clip.getLowShortTimeEnergyRatio()));
            setCharacteristic("High Zero Crossing Rate Ratio", Float.toString(clip.getHighZeroCrossingRateRatio()));
            setCharacteristic("Standard Deviation of the ZCR", Float.toString(clip.getStandardDeviationOfTheZCR()));
            setCharacteristic("Music or Speech", clip.isMusic() ? "Music" : "Speech");
            ((JFrame) SwingUtilities.getWindowAncestor(mainPanel)).setTitle(file.getName());
            SwingUtilities.getWindowAncestor(mainPanel).pack();
        }
    }

    private void drawChart(JScrollPane container, String chartName, Stream<Float> stream, double timeStep) {
        JFreeChart chart = ChartUtils.createTimeSeriesChart(chartName, stream, timeStep);
        ChartPanel chartPanel = new ChartPanel(chart);
        container.setSize(1920, 320);
        container.setPreferredSize(new Dimension(1900, 300));
        container.setViewportView(chartPanel);
        charts.put(chartName, chart);
    }

    private void markCharts(String[] charts, Stream<Boolean> stream, double timeStep, Color color) {
        List<Boolean> data = stream.collect(Collectors.toList());
        for (int i = 0, dataSize = data.size(); i < dataSize; i++) {
            if (data.get(i)) {
                IntervalMarker marker = new IntervalMarker(timeStep * i, timeStep * (i + 1), color);
                marker.setAlpha(0.25f);
                for (String chart : charts) this.charts.get(chart).getXYPlot().addDomainMarker(marker);
            }
        }
    }

    private void setCharacteristic(String characteristic, String value) {
        characteristics.get(characteristic).accept(value);
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane = new JTabbedPane();
        mainPanel.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        amplitudeChartPanel = new JScrollPane();
        tabbedPane.addTab("Amplitude", amplitudeChartPanel);
        volumeChartPanel = new JScrollPane();
        tabbedPane.addTab("Volume", volumeChartPanel);
        shortTimeEnergyChartPanel = new JScrollPane();
        tabbedPane.addTab("STE", shortTimeEnergyChartPanel);
        zeroCrossingRateChartPanel = new JScrollPane();
        tabbedPane.addTab("ZCR", zeroCrossingRateChartPanel);
        characteristicsPanel = new JScrollPane();
        tabbedPane.addTab("Clip-level Info", characteristicsPanel);
        characteristicsPanel.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        characteristicsTable.putClientProperty("Table.isFileList", Boolean.FALSE);
        characteristicsPanel.setViewportView(characteristicsTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private void createUIComponents() {
        String[] columnNames = {"Characteristic", "Value"};
        Object[][] data = {{"Total volume", "0"}, {"Volume Dynamic Range", "0"}, {"Average Short Time Energy", "0"},
                {"Minimum volume", "0"}, {"Maximum volume", "0"}, {"Average Zero Crossing Rate", "0"},
                {"Low Short Time Energy Ratio", "0"}, {"High Zero Crossing Rate Ratio", "0"},
                {"Standard Deviation of the ZCR", "0"}, {"Music or Speech", "0"}};
        for (int i = 0; i < data.length; i++) {
            int finalI = i;
            characteristics.put((String) data[i][0], s -> characteristicsTable.setValueAt(s, finalI, 1));
        }
        characteristicsTable = new JTable(data, columnNames);
        characteristicsTable.setFillsViewportHeight(true);
    }
}
