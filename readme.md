# Concurrency Lab

## Maximising workload throughput

This is a lab environment to simulate processing workloads. This is a handy tool for testing out 
strategies to compare throughput and thread utilisation. There are two output modes; you can view the results as a chart on screen, or you can have the chart write to a file as a PNG image.

### Running

The following runtime arguments are possible.

`--output=STRING`

Specifying "save" will save the chart to "./output/". The default is to display the chart on screen.

*Default is to display on screen*. 

`--strategy=STRING`

This is the processing strategy to test. The value must be one of `[sequential/parallel/blocking_queue]`.
Of course, if you create new processor strategies, then you would use this argument to select it to run.

*Default is "blocking_queue"*.

`--jobs=INTEGER`

The total size of the workload in terms of number of jobs.

*Default is 70*.

`--effort=INTEGER`

The maximal of the jitter in milliseconds representing the simulated workload time for a single job.

*Default is 500*.

