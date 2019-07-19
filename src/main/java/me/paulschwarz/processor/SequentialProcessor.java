package me.paulschwarz.processor;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.paulschwarz.Source.Job;
import me.paulschwarz.chart.Reporter;

public class SequentialProcessor implements Processor {

  private final Consumer<Job> work;
  private final Reporter reporter;

  public SequentialProcessor(Consumer<Job> work, Reporter reporter) {
    this.work = work;
    this.reporter = reporter;
  }

  @Override
  public void run(Supplier<List<Job>> jobsSupplier) {
    List<Job> jobs;
    AtomicInteger inFlight = new AtomicInteger(0);

    do {
      jobs = jobsSupplier.get();
      reporter.addDataPoint(inFlight.addAndGet(jobs.size()), 0, 1);

      jobs.forEach(job -> {
        this.work.accept(job);
        reporter.addDataPoint(inFlight.decrementAndGet(), 0, 1);
      });
    } while (jobs.size() > 0);

    reporter.addDataPoint(inFlight.get(), 0, 0);
  }
}
