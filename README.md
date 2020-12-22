# demo-spring-batch-job-db2db

Spring Batch will always create new Job Execution and will not reuse a previous failed job execution to continue its execution.

Spring Batch concept: Job, Job Instance, Job Execution

Example:

- Job : End-Of-Day Batch
- Job Instance : End-Of-Day Batch for 2018-01-01
- Job Execution: End-Of-Day Batch for 2018-01-01, execution #1

In high-level, that's how Spring Batch's recovery works:

Assuming your first execution failed in the step 3. You can submit the same Job (End-of-Day Batch) with same Parameters (2018-01-01). 
Spring Batch will try to look up last Job Execution (End-Of-Day Batch for 2018-01-01, execution #1) of the submitted Job Instance (End-of-Day Batch for 2018-01-01), 
and found that it has previously failed in step 3. Spring Batch will then create a NEW execution, [End-Of-Day Batch for 2018-01-01, execution #2], and start the execution from step 3.

So by design, what Spring trying to recover is a previously failed Job Instance (instead of Job Execution). Spring batch will not reuse execution 
when you are re-running a previous-failed execution.

Resume of failed/interrupted job in Spring Batch is achieved by submitting same job with same job parameters.

Therefore we can do the following to resubmit failed jobs (assuming we are using DB to store job meta data):

By joining BATCH_JOB_INSTANCE and BATCH_JOB_EXECUTION table, find out all job instances with no completed job executions
find out the latest BATCH_JOB_EXECUTIONS for each of the above incomplete job instance, and lookup the corresponding job parameters from BATCH_JOB_EXECUTION_PARAMS.
Resubmit the job using job name from BATCH_JOB_INSTANCE, and job parameters from BATCH_JOB_EXECUTION_PARAMS.