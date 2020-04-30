package pl.edu.pw.mini.hermant.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class FrequencyAnalyzerForm {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane1;
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
    private Clip clip;
    private float overlap = 0.0f;
    private AudioWindow window = new VanHannAudioWindow();

    private MenuBar menuBar;
    private JFileChooser inputChooser;
    private HashMap<String, JFreeChart> charts = new HashMap<>();

    public FrequencyAnalyzerForm() {
        $$$setupUI$$$();
        setupFileChoosers();
        menuBar = new MenuBar();
        menuBar.getMenuItem("Open").addActionListener(this::open);
        overlapSlider.addChangeListener(e -> overlapLabel.setText(String.valueOf(overlap = (float) overlapSlider.getValue() / 100.0f)));
        redrawButton.addActionListener(e -> {
            if (clip == null) return;
            drawHeatMapChart(spectrumChartPanel, "Spectrum", clip.getOverlappingFrames(overlap), Clip.SAMPLE_TIME);
            drawTimeSeriesChart(baseToneChartPanel, "Base Tone", clip.getOverlappingFrames(overlap).stream().map(f -> f.calculateBasicTone(window)), Clip.SAMPLE_TIME * (1.0f - overlap));
        });
        windowFunctionCombo.addActionListener(e -> {
            if (windowFunctionCombo.getSelectedItem().equals("Rectangle Audio Window"))
                window = new RectangleAudioWindow();
            if (windowFunctionCombo.getSelectedItem().equals("Van Hann Audio Window"))
                window = new VanHannAudioWindow();
            if (windowFunctionCombo.getSelectedItem().equals("Hamming Audio Window")) window = new HammingAudioWindow();
        });
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
            drawTimeSeriesChart(amplitudeChartPanel, "Amplitude", clip.getSamples().stream(), Clip.SAMPLE_TIME);
            drawXYSeriesChart(fourierChartPanel, "Fourier", clip.getFrames().get(0).getFrequencies().stream());
            drawHeatMapChart(spectrumChartPanel, "Spectrum", clip.getOverlappingFrames(overlap), Clip.SAMPLE_TIME);
            drawTimeSeriesChart(baseToneChartPanel, "Base Tone", clip.getOverlappingFrames(overlap).stream().map(f -> f.calculateBasicTone(window)), Clip.SAMPLE_TIME * (1.0f - overlap));
            ((JFrame) SwingUtilities.getWindowAncestor(mainPanel)).setTitle(file.getName());
            SwingUtilities.getWindowAncestor(mainPanel).pack();
        }
    }

    private void drawHeatMapChart(JScrollPane container, String chartName, List<Frame> frames, double timeStep) {
        List<Stream<FourierPoint>> data = new ArrayList<>();
        for (Frame frame : frames) data.add(frame.getFrequencies().stream());
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

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        amplitudeChartPanel = new JScrollPane();
        mainPanel.add(amplitudeChartPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(-1, 160), new Dimension(-1, 160), new Dimension(-1, 160), 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        mainPanel.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tabbedPane1 = new JTabbedPane();
        panel1.add(tabbedPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(200, 200), null, 0, false));
        fourierChartPanel = new JScrollPane();
        tabbedPane1.addTab("Transformata Fouriera", fourierChartPanel);
        spectrumChartPanel = new JScrollPane();
        tabbedPane1.addTab("Spektrum", spectrumChartPanel);
        baseToneChartPanel = new JScrollPane();
        tabbedPane1.addTab("Ton podstawowy", baseToneChartPanel);
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
        defaultComboBoxModel1.addElement("Rectangle Audio Window");
        defaultComboBoxModel1.addElement("Van Hann Audio Window");
        defaultComboBoxModel1.addElement("Hamming Audio Window");
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
        toFrameField.setText("1");
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
