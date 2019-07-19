package me.paulschwarz.chart;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Reporter {

  private final long startedAt;
  private final List<DataPoint> dataPoints = new ArrayList<>();
  public Reporter() {
    startedAt = System.currentTimeMillis();
  }

  void addDataPoint(int time, int inFlight, int inQueue, int activeThreads) {
    dataPoints.add(new DataPoint(time, inFlight, inQueue, activeThreads));
  }

  public void addDataPoint(int inFlight, int inQueue, int activeThreads) {
    int time = (int) (System.currentTimeMillis() - startedAt);
    addDataPoint(time, inFlight, inQueue, activeThreads);
  }

  public DataPoint lastDataPoint() {
    return dataPoints.get(dataPoints.size() - 1);
  }

  public Function<Integer, DataPoint> getDataPoint() {
    System.out.println("render data: " + dataPoints);

    AtomicInteger i = new AtomicInteger(0);

    return new Function<Integer, DataPoint>() {
      DataPoint currentDataPoint;

      @Override
      public DataPoint apply(Integer time) {
        for (int j = i.get(); j < dataPoints.size(); j++) {
          if (dataPoints.get(j).time > time) {
            return Optional.ofNullable(currentDataPoint)
                .orElse(new DataPoint(time, 0, 0, 0));
          }

          currentDataPoint = dataPoints.get(j);
          i.incrementAndGet();
        }

        return currentDataPoint;
      }
    };
  }

  class DataPoint {

    private final int time;
    private final int inFlight;
    private final int inQueue;
    private final int activeThreads;

    public DataPoint(int time, int inFlight, int inQueue, int activeThreads) {
      this.time = time;
      this.inFlight = inFlight;
      this.inQueue = inQueue;
      this.activeThreads = activeThreads;
    }

    public int getTime() {
      return time;
    }

    public int inFlight() {
      return inFlight;
    }

    public int inQueue() {
      return inQueue;
    }

    public int activeThreads() {
      return activeThreads;
    }

    public String toString() {
      return String.format("(%d, %d, %d, %d)", time, inFlight, inQueue, activeThreads);
    }
  }
}
