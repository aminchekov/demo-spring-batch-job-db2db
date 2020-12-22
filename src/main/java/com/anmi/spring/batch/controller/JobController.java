package com.anmi.spring.batch.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/run")
@Slf4j
public class JobController {

	private final JobLauncher  jobLauncher;
	private final Job job;

	public JobController(@Qualifier("asyncJobLauncher") JobLauncher jobLauncher, Job job) {
		this.jobLauncher = jobLauncher;
		this.job = job;
	}

	@GetMapping
	public JobResponse runJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
		Map<String, JobParameter> jobParametersMap = new HashMap<>();
		jobParametersMap.put("time", new JobParameter(System.currentTimeMillis()));
		jobParametersMap.put("createdAt", new JobParameter(Date.valueOf(LocalDate.of(2020, 12, 2))));
		JobParameters parameters = new JobParameters(jobParametersMap);
		JobExecution je = jobLauncher.run(job, parameters);

		log.info("Job Execution: " + je.getStatus());
		log.info("Batch job is still running");

		return new JobResponse(je.getJobId(),je.getJobInstance().getId(), je.getId(), je.getStatus()){};
	}

	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	static class JobResponse {
		private Long jobId;
		private Long jobInstanceId;
		private Long jobExecutionId;
		private BatchStatus jobStatus;
	}
}
