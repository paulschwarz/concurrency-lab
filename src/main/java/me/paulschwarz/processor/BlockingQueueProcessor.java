package me.paulschwarz.processor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.paulschwarz.Source.Job;
import me.paulschwarz.chart.Reporter;

/**
 * See https://www.baeldung.com/java-blocking-queue
 */
public class BlockingQueueProcessor implements Processor {

  private final Consumer<Job> work;
  private final ThreadPoolExecutor workersExecutor;
  private final BlockingQueue<Job> jobsQueue;
  private final AtomicInteger inFlight = new AtomicInteger(0);
  private final Reporter reporter;

  public BlockingQueueProcessor(Consumer<Job> work, Reporter reporter) {
    this(work, reporter, Runtime.getRuntime().availableProcessors());
  }

  public BlockingQueueProcessor(Consumer<Job> work, Reporter reporter, int poolSize) {
    this.work = work;
    this.reporter = reporter;

    jobsQueue = new LinkedBlockingQueue<>(10); // TODO: experiment

    ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("worker-%d/" + poolSize)
        .build();

    this.workersExecutor = (ThreadPoolExecutor) Executors
        .newFixedThreadPool(poolSize, namedThreadFactory);

    for (int i = 0; i < poolSize; i++) {
      startWorker();
    }
  }

  @Override
  public void run(Supplier<List<Job>> jobsSupplier) {
    List<Job> batch;

    do {
      batch = jobsSupplier.get();
      reporter.addDataPoint(inFlight.addAndGet(batch.size()), jobsQueue.size(), workersExecutor.getActiveCount());

      batch.forEach(job -> {
        try {
          jobsQueue.put(job);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });

      System.out.println(String.format("added to queue. size: %d", jobsQueue.size()));
    } while (batch.size() > 0);

    // Send N poison pills to workers, where N = thread pool size
    for (int i = 0; i < workersExecutor.getMaximumPoolSize(); i++) {
      try {
        jobsQueue.put(new Job(-1, null));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    workersExecutor.shutdown();

    try {
      workersExecutor.awaitTermination(1, TimeUnit.MINUTES);
      reporter.addDataPoint(inFlight.get(), jobsQueue.size(), workersExecutor.getActiveCount());
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void startWorker() {
    workersExecutor.submit(() -> {
      try {
        while (true) {
          Job job = jobsQueue.take();

          if (job.isPoisonPill()) {
            System.out
                .println(String.format("[%s] shutting down", Thread.currentThread().getName()));
            return;
          }

          this.work.accept(job);
          inFlight.decrementAndGet();
          reporter.addDataPoint(inFlight.get(), jobsQueue.size(), workersExecutor.getActiveCount());
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });
  }
}
