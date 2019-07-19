package me.paulschwarz;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Source {

  private List<Job> jobs;

  public Source(int numJobs, int effortFactor) {
    jobs = IntStream.range(0, numJobs)
        .mapToObj(i -> new Job((long) ((Math.random() / 2 + 0.5) * effortFactor), "Job " + (i + 1)))
        .collect(Collectors.toList());
  }

  public List<Job> nextJobsBatch(int batchSize) {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    batchSize = jobs.size() < batchSize ? jobs.size() : batchSize;
    List<Job> result = jobs.subList(0, batchSize);
    jobs = jobs.subList(batchSize, jobs.size());
    System.out.println(String.format("Fetched batch of %d", result.size()));
    return result;
  }

  public static class Job {

    long effort;
    String name;

    public Job(long effort, String name) {
      this.effort = effort;
      this.name = name;
    }

    public boolean isPoisonPill() {
      return effort < 0;
    }
  }
}
