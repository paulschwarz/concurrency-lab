package me.paulschwarz.chart;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;
import me.paulschwarz.chart.Reporter.DataPoint;
import org.junit.Test;

public class ReporterTest {

  @Test
  public void getDataPoint() {
    Reporter reporter = new Reporter();
    reporter.addDataPoint(2, 9, 9, 2);
    reporter.addDataPoint(3, 8, 9, 2);
    reporter.addDataPoint(8, 7, 2, 2);
    reporter.addDataPoint(9, 0, 0, 1);

    Function<Integer, DataPoint> func = reporter.getDataPoint();

    expect(0, 0, 0, func.apply(0));
    expect(0, 0, 0, func.apply(1));
    expect(9, 9, 2, func.apply(2));
    expect(8, 9, 2, func.apply(3));
    expect(8, 9, 2, func.apply(4));
    expect(8, 9, 2, func.apply(5));
    expect(8, 9, 2, func.apply(6));
    expect(8, 9, 2, func.apply(7));
    expect(7, 2, 2, func.apply(8));
    expect(0, 0, 1, func.apply(9));
  }

  private void expect(int inFlight, int inQueue, int activeThreads, DataPoint dataPoint) {
    assertEquals(inFlight, dataPoint.inFlight());
    assertEquals(inQueue, dataPoint.inQueue());
    assertEquals(activeThreads, dataPoint.activeThreads());
  }
}
