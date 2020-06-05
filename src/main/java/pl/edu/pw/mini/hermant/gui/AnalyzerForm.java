package pl.edu.pw.mini.hermant.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import pl.edu.pw.mini.hermant.audio.Clip;
import pl.edu.pw.mini.hermant.audio.FourierPoint;
import pl.edu.pw.mini.hermant.audio.Frame;
import pl.edu.pw.mini.hermant.audio.window.AudioWindow;
import pl.edu.pw.mini.hermant.audio.window.HammingAudioWindow;
import pl.edu.pw.mini.hermant.audio.window.RectangleAudioWindow;
import pl.edu.pw.mini.hermant.audio.window.VanHannAudioWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalyzerForm implements Form {
    private JPanel mainPanel;
    private JTabbedPane mainTabbedPanel;
    private JSlider overlapSlider;
    private JComboBox<String> windowFunctionCombo;
    private JTextField toFrameField;
    private JScrollPane amplitudeChartPanel;
    private JTextField fromFrameField;
    private JScrollPane fourierChartPanel;
    private JLabel frameRangeLabel;
    private JScrollPane spectrumChartPanel;
    private JButton redrawButton;
    private JLabel overlapLabel;
    private JScrollPane baseToneChartPanel;
    private JScrollPane selectedAmplitudeChartPanel;
    private JTabbedPane timeVolumeTabbedPanel;
    private JScrollPane volumePanel;
    private JScrollPane timeVolumeChartPanel;
    private JTable characteristicsTable;
    private JScrollPane shortTimeEnergyChartPanel;
    private JScrollPane zeroCrossingRateChartPanel;
    private JScrollPane frequencyVolumeChartPanel;
    private JScrollPane frequencyCentroidChartPanel;
    private JScrollPane effectiveBandwithChartPanel;
    private JTabbedPane tabbedPane1;
    private JScrollPane ersb1ChartPanel;
    private JScrollPane ersb2ChartPanel;
    private JScrollPane ersb3ChartPanel;
    private Clip clip;
    private float overlap = 0.0f;
    private int from = 0;
    private int to = 1;
    private int max = 1;
    private AudioWindow window = new RectangleAudioWindow();

    private MenuBar menuBar;
    private JFileChooser inputChooser;
    private HashMap<String, JFreeChart> charts = new HashMap<>();
    private final HashMap<String, Consumer<String>> characteristics = new HashMap<>();

    public AnalyzerForm() {
        $$$setupUI$$$();
        setupFileChoosers();
        menuBar = new MenuBar();
        menuBar.getMenuItem("Open").addActionListener(this::open);
        overlapSlider.addChangeListener(e -> {
            overlapLabel.setText(String.valueOf(overlap = (float) overlapSlider.getValue() / 100.0f));
            frameRangeLabel.setText(String.format("frame range(<from> <to>, max: %d):", clip == null ? 999999 : (max = clip.getFramesNum(overlap))));
        });
        redrawButton.addActionListener(e -> {
            if (clip == null) return;
            drawCharts();
        });
        windowFunctionCombo.addActionListener(e -> {
            String selected = (String) Objects.requireNonNull(windowFunctionCombo.getSelectedItem());
            switch (selected) {
                case "Rectangle":
                    window = new RectangleAudioWindow();
                    break;
                case "Van Hann":
                    window = new VanHannAudioWindow();
                    break;
                case "Hamming":
                    window = new HammingAudioWindow();
                    break;
            }
        });
    }

    void updateFrameRange() {
        max = clip.getFramesNum(overlap);
        try {
            from = Integer.parseInt(fromFrameField.getText());
        } catch (NumberFormatException exception) {
            from = 0;
        }
        try {
            to = Integer.parseInt(toFrameField.getText());
        } catch (NumberFormatException exception) {
            to = 1;
        }
        if (from < 0) from = 0;
        if (to > max) to = max;
        if (to < from) to = from;
    }

    private void drawCharts() {
        drawTimeSeriesChart(amplitudeChartPanel, "Amplitude", clip.getSamples().stream(), Clip.SAMPLE_TIME);
        updateFrameRange();
        List<Frame> frames = clip.getOverlappingFrames(overlap).subList(from, to);
        Map<Float, Float> frequencies = new HashMap<>();
        for (Frame frame : frames) {
            Map<Float, Float> f = frame.calculateFrequencies(window).stream().collect(Collectors.toMap(FourierPoint::getFrequency, FourierPoint::getAmplitude));
            for (Map.Entry<Float, Float> entry : f.entrySet()) {
                if (!frequencies.containsKey(entry.getKey())) frequencies.put(entry.getKey(), entry.getValue());
                else frequencies.put(entry.getKey(), frequencies.get(entry.getKey()) + entry.getValue());
            }
        }
        frames.parallelStream().forEach(Frame::calculateVolume);
        frames.parallelStream().forEach(Frame::calculateZeroCrossingRate);
        List<Float> selectedSamples = clip.getSamples().subList(frames.get(0).getFrameStart(), frames.get(frames.size() - 1).getFrameStart() + Frame.SAMPLES_PER_FRAME);
        drawTimeSeriesChart(selectedAmplitudeChartPanel, "Range Amplitude", selectedSamples.stream(), Clip.SAMPLE_TIME);
        drawXYSeriesChart(fourierChartPanel, "Frequencies", frequencies.entrySet().stream().map(entry -> new FourierPoint(entry.getKey(), entry.getValue() / frames.size())));
        drawHeatMapChart(spectrumChartPanel, "Spectrum", frames, Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(baseToneChartPanel, "Base Tone", frames.stream().map(f -> f.calculateBasicTone(window)), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(timeVolumeChartPanel, "Volume", frames.stream().map(Frame::getVolume), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(frequencyVolumeChartPanel, "Frequency Volume", frames.stream().map(f -> f.calculateFrequencyVolume(window, 0, 11025)), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(frequencyCentroidChartPanel, "Frequency Centroid", frames.stream().map(f -> f.calculateFrequencyCentroid(window)), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(effectiveBandwithChartPanel, "Effective Bandwidth", frames.stream().map(f -> f.calculateEffectiveBandwidth(window)), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(shortTimeEnergyChartPanel, "Short Time Energy", frames.stream().map(Frame::getShortTimeEnergy), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(zeroCrossingRateChartPanel, "Zero Crossing Rate", frames.stream().map(Frame::getZeroCrossingRate), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(ersb1ChartPanel, "ERSB1(0 - 630Hz)", frames.stream().map(f -> f.calculateFrequencyVolume(window, 0, 630) / f.calculateFrequencyVolume(window, 0, 11025)), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(ersb2ChartPanel, "ERSB2(630 - 1720Hz)", frames.stream().map(f -> f.calculateFrequencyVolume(window, 630, 1720) / f.calculateFrequencyVolume(window, 0, 11025)), Clip.FRAME_TIME * (1.0f - overlap));
        drawTimeSeriesChart(ersb3ChartPanel, "ERSB3(1720 - 4400Hz)", frames.stream().map(f -> f.calculateFrequencyVolume(window, 1720, 4400) / f.calculateFrequencyVolume(window, 0, 11025)), Clip.FRAME_TIME * (1.0f - overlap));
        markCharts(new String[]{"Amplitude", "Volume", "Short Time Energy", "Zero Crossing Rate", "Frequency Volume"}, frames.stream().map(Frame::isSilence), Clip.FRAME_TIME * (1.0f - overlap), Color.BLUE);
        markCharts(new String[]{"Amplitude", "Volume", "Short Time Energy", "Zero Crossing Rate", "Frequency Volume"}, frames.stream().map(Frame::isVoiced), Clip.FRAME_TIME * (1.0f - overlap), Color.RED);
        markCharts(new String[]{"Amplitude", "Volume", "Short Time Energy", "Zero Crossing Rate", "Frequency Volume"}, frames.stream().map(Frame::isVoiceless), Clip.FRAME_TIME * (1.0f - overlap), Color.GREEN);
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
            frameRangeLabel.setText(String.format("frame range(<from> <to>, max: %d):", clip.getFramesNum()));
            drawCharts();
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

    private void markCharts(String[] charts, Stream<Boolean> stream, double timeStep, Color color) {
        List<Boolean> data = stream.collect(Collectors.toList());
        for (int i = 0, dataSize = data.size(); i < dataSize; i++) {
            if (data.get(i)) {
                IntervalMarker marker = new IntervalMarker(timeStep * i, timeStep * (i + 1), color);
                marker.setAlpha(0.12f);
                for (String chart : charts) this.charts.get(chart).getXYPlot().addDomainMarker(marker);
            }
        }
    }

    private void setCharacteristic(String characteristic, String value) {
        characteristics.get(characteristic).accept(value);
    }

    private void drawHeatMapChart(JScrollPane container, String chartName, List<Frame> frames, double timeStep) {
        List<Stream<FourierPoint>> data = new ArrayList<>();
        for (Frame frame : frames) data.add(frame.calculateFrequencies(window).stream());
        JFreeChart chart = ChartUtils.createHeatMapChart(chartName, data, timeStep);
        ChartPanel chartPanel = new ChartPanel(chart);
        container.setViewportView(chartPanel);
        chartPanel.setPreferredSize(new Dimension(container.getWidth() - 24, container.getHeight() - 24));
        charts.put(chartName, chart);
    }

    private void drawXYSeriesChart(JScrollPane container, String chartName, Stream<FourierPoint> stream) {
        JFreeChart chart = ChartUtils.createXYSeriesChart(chartName, stream);
        ChartPanel chartPanel = new ChartPanel(chart);
        container.setViewportView(chartPanel);
        chartPanel.setPreferredSize(new Dimension(container.getWidth() - 24, container.getHeight() - 24));
        charts.put(chartName, chart);
    }

    private void drawTimeSeriesChart(JScrollPane container, String chartName, Stream<Float> stream, double timeStep) {
        JFreeChart chart = ChartUtils.createTimeSeriesChart(chartName, stream, timeStep);
        ChartPanel chartPanel = new ChartPanel(chart);
        container.setViewportView(chartPanel);
        chartPanel.setPreferredSize(new Dimension(container.getWidth() - 24, container.getHeight() - 24));
        charts.put(chartName, chart);
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
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        amplitudeChartPanel = new JScrollPane();
        mainPanel.add(amplitudeChartPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 160), new Dimension(-1, 160), new Dimension(-1, 160), 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        mainTabbedPanel = new JTabbedPane();
        panel1.add(mainTabbedPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), null, 0, false));
        fourierChartPanel = new JScrollPane();
        mainTabbedPanel.addTab("Frequencies", fourierChartPanel);
        selectedAmplitudeChartPanel = new JScrollPane();
        mainTabbedPanel.addTab("Amplitude", selectedAmplitudeChartPanel);
        spectrumChartPanel = new JScrollPane();
        mainTabbedPanel.addTab("Spectrum", spectrumChartPanel);
        baseToneChartPanel = new JScrollPane();
        mainTabbedPanel.addTab("Base Tone", baseToneChartPanel);
        volumePanel = new JScrollPane();
        mainTabbedPanel.addTab("Volume", volumePanel);
        timeVolumeTabbedPanel = new JTabbedPane();
        volumePanel.setViewportView(timeVolumeTabbedPanel);
        timeVolumeChartPanel = new JScrollPane();
        timeVolumeTabbedPanel.addTab("time", timeVolumeChartPanel);
        frequencyVolumeChartPanel = new JScrollPane();
        timeVolumeTabbedPanel.addTab("frequency", frequencyVolumeChartPanel);
        zeroCrossingRateChartPanel = new JScrollPane();
        mainTabbedPanel.addTab("ZCR", zeroCrossingRateChartPanel);
        shortTimeEnergyChartPanel = new JScrollPane();
        mainTabbedPanel.addTab("STE", shortTimeEnergyChartPanel);
        final JScrollPane scrollPane1 = new JScrollPane();
        mainTabbedPanel.addTab("Clip-Level Info", scrollPane1);
        scrollPane1.setViewportView(characteristicsTable);
        frequencyCentroidChartPanel = new JScrollPane();
        mainTabbedPanel.addTab("Frequency Centroid", frequencyCentroidChartPanel);
        effectiveBandwithChartPanel = new JScrollPane();
        mainTabbedPanel.addTab("Effective Bandwidth", effectiveBandwithChartPanel);
        final JScrollPane scrollPane2 = new JScrollPane();
        mainTabbedPanel.addTab("Band Energy Ratio", scrollPane2);
        tabbedPane1 = new JTabbedPane();
        scrollPane2.setViewportView(tabbedPane1);
        ersb1ChartPanel = new JScrollPane();
        tabbedPane1.addTab("ERSB1", ersb1ChartPanel);
        ersb2ChartPanel = new JScrollPane();
        tabbedPane1.addTab("ERSB2", ersb2ChartPanel);
        ersb3ChartPanel = new JScrollPane();
        tabbedPane1.addTab("ERSB3", ersb3ChartPanel);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        overlapSlider = new JSlider();
        overlapSlider.setMajorTickSpacing(10);
        overlapSlider.setMaximum(90);
        overlapSlider.setPaintTicks(true);
        overlapSlider.setValue(0);
        panel2.add(overlapSlider, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, new Dimension(480, -1), null, null, 0, false));
        windowFunctionCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Rectangle");
        defaultComboBoxModel1.addElement("Van Hann");
        defaultComboBoxModel1.addElement("Hamming");
        windowFunctionCombo.setModel(defaultComboBoxModel1);
        panel2.add(windowFunctionCombo, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("overlap:");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("window func:");
        panel2.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        frameRangeLabel = new JLabel();
        frameRangeLabel.setText("frame range(<from> <to>, max: 1):");
        panel2.add(frameRangeLabel, new GridConstraints(0, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toFrameField = new JTextField();
        toFrameField.setText("999999");
        panel2.add(toFrameField, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        overlapLabel = new JLabel();
        overlapLabel.setText("0");
        panel2.add(overlapLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fromFrameField = new JTextField();
        fromFrameField.setText("0");
        panel2.add(fromFrameField, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        redrawButton = new JButton();
        redrawButton.setText("Redraw");
        panel2.add(redrawButton, new GridConstraints(0, 5, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

}
