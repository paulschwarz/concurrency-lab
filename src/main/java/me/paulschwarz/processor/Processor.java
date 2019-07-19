package me.paulschwarz.processor;

import java.util.List;
import java.util.function.Supplier;
import me.paulschwarz.Source.Job;

public interface Processor {

  void run(Supplier<List<Job>> jobsSupplier);
}
