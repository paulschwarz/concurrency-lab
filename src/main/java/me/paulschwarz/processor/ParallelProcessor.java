package me.paulschwarz.processor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.paulschwarz.Source.Job;
import me.paulschwarz.chart.Reporter;

public class ParallelProcessor implements Processor {

  private final Consumer<Job> work;
  private final ThreadPoolExecutor workersExecutor;
  private final Reporter reporter;

  public ParallelProcessor(Consumer<Job> work, Reporter reporter) {
    this(work, reporter, Runtime.getRuntime().availableProcessors());
  }

  public ParallelProcessor(Consumer<Job> work, Reporter reporter, int poolSize) {
    this.work = work;
    this.reporter = reporter;

    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("worker-%d/" + poolSize)
        .build();

    this.workersExecutor = (ThreadPoolExecutor) Executors
        .newFixedThreadPool(poolSize, namedThreadFactory);
  }

  @Override
  public void run(Supplier<List<Job>> jobsSupplier) {
    List<Job> batch = new ArrayList<>();
    AtomicInteger inFlight = new AtomicInteger(0);

    do {
      if (inFlight.get() == 0) {
        batch = jobsSupplier.get();
        reporter.addDataPoint(inFlight.addAndGet(batch.size()), 0, workersExecutor.getActiveCount());

        batch.forEach(job -> workersExecutor.submit(() -> {
          this.work.accept(job);
          reporter.addDataPoint(inFlight.decrementAndGet(), 0, workersExecutor.getActiveCount());
        }));
      }
    } while (batch.size() > 0);

    workersExecutor.shutdown();

    try {
      workersExecutor.awaitTermination(1, TimeUnit.MINUTES);
      reporter.addDataPoint(inFlight.get(), 0, workersExecutor.getActiveCount());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
