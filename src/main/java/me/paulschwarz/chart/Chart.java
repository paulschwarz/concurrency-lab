package me.paulschwarz.chart;

import java.awt.Color;
import java.io.IOException;
import java.util.function.Function;
import me.paulschwarz.chart.Reporter.DataPoint;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

/**
 * https://github.com/knowm/XChart
 */
public class Chart {

  private final Reporter reporter;

  public Chart(Reporter reporter) {
    this.reporter = reporter;
  }

  public void display() {
    XYChart chart = makeChart();

    final SwingWrapper<XYChart> sw = new SwingWrapper<>(chart);
    sw.displayChart();
  }

  public void save(String outputFileName) {
    XYChart chart = makeChart();

    try {
      BitmapEncoder.saveBitmapWithDPI(chart, outputFileName, BitmapFormat.PNG, 300);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private XYChart makeChart() {
    double[][] data = getThroughputData();

    final XYChart chart = QuickChart
        .getChart("Throughput Chart", "Time (ms)", "", "in flight", data[0], data[1]);

    XYSeries seriesInQueue = chart.addSeries("in queue", data[2]);
    seriesInQueue.setLineColor(Color.GREEN);
    seriesInQueue.setMarker(SeriesMarkers.NONE);

    XYSeries seriesActiveThreads = chart.addSeries("active threads", data[3]);
    seriesActiveThreads.setLineColor(Color.RED);
    seriesActiveThreads.setMarker(SeriesMarkers.NONE);

    return chart;
  }

  private double[][] getThroughputData() {
    int lastTime = reporter.lastDataPoint().getTime() + 1;

    double[] xData = new double[lastTime];
    double[] inFlightData = new double[lastTime];
    double[] inQueueData = new double[lastTime];
    double[] activeThreadsData = new double[lastTime];

    Function<Integer, DataPoint> func = reporter.getDataPoint();
    DataPoint currentDataPoint;

    for (int i = 0; i < lastTime; i++) {
      currentDataPoint = func.apply(i);

      xData[i] = i;
      inFlightData[i] = currentDataPoint.inFlight();
      inQueueData[i] = currentDataPoint.inQueue();
      activeThreadsData[i] = currentDataPoint.activeThreads();
    }

    return new double[][]{xData, inFlightData, inQueueData, activeThreadsData};
  }
}
