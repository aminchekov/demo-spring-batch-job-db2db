package com.anmi.spring.batch.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.awaitility.Awaitility.await;
import static org.springframework.batch.core.BatchStatus.*;

@Component
@Slf4j
public class JobRestarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRestarter.class);
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final JobRepository jobRepository;
    private final long timeout;

    public JobRestarter(JobExplorer jobExplorer, JobRepository jobRepository, JobOperator jobOperator,
                        @Value("${job.timeout-millis}") long timeout) {
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.jobOperator = jobOperator;
        this.timeout = timeout;
    }

    @Scheduled(fixedRateString = "${job.timeout-millis}", initialDelayString = "${job.timeout-millis}")
    public void restartUncompletedJobs() {
        log.info("RestartUncompletedJobs is scheduled");
        try {
            List<String> jobs = jobExplorer.getJobNames();
            for (String job : jobs) {
                Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions(job);
                for (JobExecution runningJob : runningJobs) {
                    if (stopRunningJobByTimeout(runningJob)) {
                        jobOperator.restart(runningJob.getId());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @SneakyThrows
    public boolean stopRunningJobByTimeout(JobExecution runningJob) {
        boolean result = false;
        if (Instant.now().isAfter(runningJob.getStartTime().toInstant().plusMillis(timeout))) {
            Long executionId = runningJob.getId();
            BatchStatus status = jobExplorer.getJobExecution(executionId).getStatus();
            if(status == STOPPING) {
                status = FAILED;
                failStepExecution(runningJob);
                runningJob.setStatus(status);
                runningJob.setEndTime(new Date());
                jobRepository.update(runningJob);
            }
            if (status == STARTING || status == STARTED) {
                result = jobOperator.stop(executionId);
                await().until(() -> jobExplorer.getJobExecution(executionId).getStatus(),
                        s -> Objects.equals(s, FAILED) || Objects.equals(s, STOPPED));
            } else {
                result = true;
            }
            log.info("Timed out job execution with id {} has status {}", executionId, jobExplorer.getJobExecution(executionId).getStatus().name());
        }
        return result;
    }

    void failStepExecution(JobExecution lastExecution) {
        if (lastExecution != null) {
            for (StepExecution execution : lastExecution.getStepExecutions()) {
                BatchStatus status = execution.getStatus();
                if (status.isRunning() || status == BatchStatus.STOPPING) {
                    // set terminatesOnly and check how it's stop the whole job
                    execution.setStatus(BatchStatus.FAILED);
                    execution.setEndTime(new Date());
                    execution.setExitStatus(ExitStatus.FAILED);
                    jobRepository.update(execution);
                }
            }
        }
    }
}
