package me.paulschwarz;

import static java.lang.Thread.sleep;

import java.util.Arrays;
import java.util.function.Consumer;
import me.paulschwarz.Source.Job;
import me.paulschwarz.chart.Chart;
import me.paulschwarz.chart.Reporter;
import me.paulschwarz.processor.BlockingQueueProcessor;
import me.paulschwarz.processor.ParallelProcessor;
import me.paulschwarz.processor.Processor;
import me.paulschwarz.processor.SequentialProcessor;
import me.paulschwarz.util.Args;

public class App {

  public static void main(String[] args) {
    Args arguments = new Args(Arrays.asList(args));

    ProcessorStrategy processorStrategy = ProcessorStrategy.valueOf(arguments.get("strategy",
            ProcessorStrategy.BLOCKING_QUEUE.name()).toUpperCase());

    Source source = new Source(arguments.get("jobs", 30), arguments.get("effort", 500));
    App app = new App();
    Reporter reporter = new Reporter();
    Processor processor = processorStrategy.getProcessor(app::work, reporter);

    long startedAt = System.currentTimeMillis();
    processor.run(() -> source.nextJobsBatch(10));
    long took = System.currentTimeMillis() - startedAt;
    System.out.println(String.format("took %sms", took));

    Chart chart = new Chart(reporter);

    if (arguments.get("output", "display").equals("save")) {
      chart.save("./output/" + processorStrategy.name());
    } else {
      chart.display();
    }
  }

  private void work(Job job) {
    try {
      sleep(job.effort);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println(
        String.format("[%s] %s took %dms", Thread.currentThread().getName(), job.name, job.effort));
  }

  enum ProcessorStrategy {

    SEQUENTIAL {
      @Override
      Processor getProcessor(Consumer<Job> work, Reporter reporter) {
        return new SequentialProcessor(work, reporter);
      }
    },

    PARALLEL {
      @Override
      Processor getProcessor(Consumer<Job> work, Reporter reporter) {
        return new ParallelProcessor(work, reporter);
      }
    },

    BLOCKING_QUEUE {
      @Override
      Processor getProcessor(Consumer<Job> work, Reporter reporter) {
        return new BlockingQueueProcessor(work, reporter);
      }
    };

    abstract Processor getProcessor(Consumer<Job> work, Reporter reporter);
  }
}
