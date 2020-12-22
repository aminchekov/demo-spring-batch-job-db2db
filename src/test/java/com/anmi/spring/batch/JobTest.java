package com.anmi.spring.batch;

import com.anmi.spring.batch.repository.UserOutputRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@SpringBatchTest
@RunWith(SpringRunner.class)
public class JobTest {
    @Autowired
    private JobLauncherTestUtils launcherUtils;
    @Autowired
    private JobRepositoryTestUtils repositoryUtils;
    @Autowired
    private UserOutputRepository userOutputRepository;

    @Test
    @Sql(statements = "DELETE FROM USER_INPUT")
    @Sql(value = "classpath:user-input.sql")
    public void shouldCompleteJob() throws Exception {
        JobExecution jobExecution;
        given:
        {
            userOutputRepository.deleteAll();
        }
        when:
        {
            Map<String, JobParameter> params = new HashMap<>();
            params.put("time", new JobParameter(System.currentTimeMillis()));
            params.put("createdAt", new JobParameter(Date.valueOf(LocalDate.of(2020, 12, 2))));
            jobExecution = launcherUtils.launchJob(new JobParameters(params));
        }
        then:
        {
            assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
            assertThat(userOutputRepository.findAll()).hasSize(3);
        }
    }
}
