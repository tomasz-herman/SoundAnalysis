package pl.edu.pw.mini.hermant.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.*;
import pl.edu.pw.mini.hermant.audio.FourierPoint;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChartUtils {

    public static JFreeChart createTimeSeriesChart(String chartName, Stream<Float> stream, double timeStep) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries timeSeries = new XYSeries("");
        List<Float> data = stream.collect(Collectors.toList());
        for (int i = 0, dataSize = data.size(); i < dataSize; i++) {
            timeSeries.add(i * timeStep, data.get(i));
        }
        dataset.addSeries(timeSeries);
        return ChartFactory.createXYLineChart(
                "",
                "Time",
                chartName,
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);
    }

    public static JFreeChart createXYSeriesChart(String chartName, Stream<FourierPoint> stream) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries timeSeries = new XYSeries("");
        List<FourierPoint> data = stream.collect(Collectors.toList());
        for (FourierPoint datum : data) timeSeries.add(datum.frequency, datum.amplitude);
        dataset.addSeries(timeSeries);
        return ChartFactory.createXYLineChart(
                "",
                "Frequency",
                "Amplitude",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);
    }

    public static JFreeChart createHeatMapChart(String chartName, List<Stream<FourierPoint>> streams, double timeStep) {
        DefaultXYZDataset dataset = new DefaultXYZDataset();
        for (int i = 0, dataSize = streams.size(); i < dataSize; i++) {
            double time = i * timeStep;
            List<FourierPoint> temp = streams.get(i).collect(Collectors.toList());
            double[][] data = new double[3][temp.size()];
            for (int i1 = 0; i1 < temp.size(); i1++) {
                FourierPoint fourierPoint = temp.get(i1);
                data[0][i1] = i;
                data[1][i1] = i1;
                data[2][i1] = Math.min(fourierPoint.amplitude, 25.0f);
            }
            dataset.addSeries("Series" + time, data);
        }
        return createChart(dataset);
    }

    private static JFreeChart createChart(XYDataset dataset) {
        NumberAxis xAxis = new NumberAxis("x Axis");
        NumberAxis yAxis = new NumberAxis("y Axis");
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        XYBlockRenderer r = new XYBlockRenderer();
        SpectrumPaintScale ps = new SpectrumPaintScale(0, 25);
        r.setPaintScale(ps);
        r.setBlockHeight(1.0f);
        r.setBlockWidth(1.0f);
        plot.setRenderer(r);
        JFreeChart chart = new JFreeChart("Title",
                JFreeChart.DEFAULT_TITLE_FONT, plot, false);
        NumberAxis scaleAxis = new NumberAxis("Scale");
        scaleAxis.setAxisLinePaint(Color.white);
        scaleAxis.setTickMarkPaint(Color.white);
        PaintScaleLegend legend = new PaintScaleLegend(ps, scaleAxis);
        legend.setSubdivisionCount(128);
        legend.setAxisLocation(AxisLocation.TOP_OR_RIGHT);
        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
        legend.setStripWidth(20);
        legend.setPosition(RectangleEdge.RIGHT);
        legend.setBackgroundPaint(Color.WHITE);
        chart.addSubtitle(legend);
        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    private static class SpectrumPaintScale implements PaintScale {

        private static final float H1 = 0f;
        private static final float H2 = 0.6f;
        private final double lowerBound;
        private final double upperBound;

        public SpectrumPaintScale(double lowerBound, double upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        @Override
        public double getLowerBound() {
            return lowerBound;
        }

        @Override
        public double getUpperBound() {
            return upperBound;
        }

        @Override
        public Paint getPaint(double value) {
            float scaledValue = (float) (value / (getUpperBound() - getLowerBound()));
            float scaledH = H1 + scaledValue * (H2 - H1);
            return Color.getHSBColor(scaledH, 1f, 1f);
        }
    }
}
