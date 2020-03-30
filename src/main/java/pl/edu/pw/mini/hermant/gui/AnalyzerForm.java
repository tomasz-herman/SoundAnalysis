package pl.edu.pw.mini.hermant.gui;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import pl.edu.pw.mini.hermant.audio.Clip;
import pl.edu.pw.mini.hermant.audio.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalyzerForm {
    private JPanel mainPanel;
    private JScrollPane amplitudeChartPanel;
    private JScrollPane volumeChartPanel;
    private JScrollPane shortTimeEnergyChartPanel;
    private JScrollPane zeroCrossingRateChartPanel;
    private JTabbedPane chartPane1;
    private JTable characteristicsTable;
    private MenuBar menuBar;
    private JFileChooser inputChooser;
    private Clip clip;
    private HashMap<String, JFreeChart> charts;

    public AnalyzerForm() {
        $$$setupUI$$$();
        setupFileChoosers();
        menuBar = new MenuBar();
        menuBar.getMenuItem("Open").addActionListener(this::open);
        charts = new HashMap<>();
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
            ((JFrame) SwingUtilities.getWindowAncestor(mainPanel)).setTitle(file.getName());
            SwingUtilities.getWindowAncestor(mainPanel).pack();
        }
    }

    private void drawChart(JScrollPane container, String chartName, Stream<Float> stream, float timeStamp) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries timeSeries = new XYSeries("");
        List<Float> data = stream.collect(Collectors.toList());
        float time = 0;
        for (int i = 0, dataSize = data.size(); i < dataSize; i++, time += timeStamp) {
            timeSeries.add(time, data.get(i));
        }
        dataset.addSeries(timeSeries);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "Time",
                chartName,
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);
        container.setSize(1920, 320);
        ChartPanel chartPanel = new ChartPanel(chart);
        container.setPreferredSize(new Dimension(1900, 300));
        container.setViewportView(chartPanel);
        charts.put(chartName, chart);
    }

    private void markCharts(String[] charts, Stream<Boolean> stream, float timeStamp, Color color) {
        List<Boolean> data = stream.collect(Collectors.toList());
        float time = 0;
        for (int i = 0, dataSize = data.size(); i < dataSize; i++, time += timeStamp) {
            if (data.get(i)) {
                IntervalMarker marker = new IntervalMarker(time, time + timeStamp, color);
                marker.setAlpha(0.25f);
                for (String chart : charts) this.charts.get(chart).getXYPlot().addDomainMarker(marker);
            }
        }
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
        chartPane1 = new JTabbedPane();
        mainPanel.add(chartPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        amplitudeChartPanel = new JScrollPane();
        chartPane1.addTab("Amplitude", amplitudeChartPanel);
        volumeChartPanel = new JScrollPane();
        chartPane1.addTab("Volume", volumeChartPanel);
        shortTimeEnergyChartPanel = new JScrollPane();
        chartPane1.addTab("STE", shortTimeEnergyChartPanel);
        zeroCrossingRateChartPanel = new JScrollPane();
        chartPane1.addTab("ZCR", zeroCrossingRateChartPanel);
        final JScrollPane scrollPane1 = new JScrollPane();
        chartPane1.addTab("Clip-level Info", scrollPane1);
        scrollPane1.setBorder(BorderFactory.createTitledBorder(""));
        characteristicsTable.putClientProperty("Table.isFileList", Boolean.FALSE);
        scrollPane1.setViewportView(characteristicsTable);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return mainPanel;
    }

    private void createUIComponents() {
        String[] columnNames = {"Characteristic", "Value"};
        Object[][] data = {{"Total volume", "0"}, {"Volume Dynamic Range", "0"}, {"Average Short Time Energy", "0"}
                , {"Minimum volume", "0"}, {"Maximum volume", "0"}, {"Average Zero Crossing Rate", "0"}};
        characteristicsTable = new JTable(data, columnNames);
        characteristicsTable.setFillsViewportHeight(true);
    }
}
