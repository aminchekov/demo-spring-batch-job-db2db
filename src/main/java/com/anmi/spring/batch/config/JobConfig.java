package com.anmi.spring.batch.config;

import com.anmi.spring.batch.component.UserDbWriter;
import com.anmi.spring.batch.component.UserProcessor;
import com.anmi.spring.batch.model.UserInput;
import com.anmi.spring.batch.model.UserOutput;
import com.anmi.spring.batch.repository.UserOutputRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JobConfig {

    private static final int PAGE_SIZE = 2;
    private static final int RESTART_LIMIT = 3;

    @Bean
    public Job job(StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory,
                   ItemReader<UserInput> itemReader, ItemProcessor<UserInput, UserOutput> itemProcessor,
                   ItemWriter<UserOutput> itemWriter) {
        // Wait 10 seconds before retrying
        FixedBackOffPolicy policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(1000L);
        DefaultTransactionAttribute timeout = new DefaultTransactionAttribute();
        timeout.setTimeout(10_000);
        Step step = stepBuilderFactory.get("ETL-File-Load")
                //.allowStartIfComplete(true)
                .startLimit(RESTART_LIMIT)
                .<UserInput, UserOutput>chunk(PAGE_SIZE)
                .faultTolerant()
                //.skip(InterruptedException.class)
                .retry(Exception.class)
                .retryLimit(3)
                .backOffPolicy(policy)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .transactionAttribute(timeout)
                .build();

        return jobBuilderFactory.get("ETL-Load")
                .start(step)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<UserInput> pagingItemReader(DataSource dataSource,
                                                            @Value("#{jobParameters['createdAt']}") String date, RowMapper<UserInput> rowMapper) {
        JdbcPagingItemReader<UserInput> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(dataSource);
        reader.setPageSize(PAGE_SIZE);
        reader.setFetchSize(PAGE_SIZE);
        reader.setRowMapper(rowMapper);

        H2PagingQueryProvider queryProvider = new H2PagingQueryProvider();
        queryProvider.setSelectClause("ID, NAME, SALARY, DEPT, CREATED_AT");
        queryProvider.setFromClause("from USER_INPUT");
        queryProvider.setWhereClause("CREATED_AT::date = :createdAt");

        Map<String, Order> sortKeys = new HashMap<>(1);

        sortKeys.put("ID", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("createdAt", date);
        reader.setParameterValues(parameterValues);

        return reader;
    }

    @Bean
    @StepScope
    public UserDbWriter userDbWriter(UserOutputRepository userOutputRepository) {
        return new UserDbWriter(userOutputRepository);
    }

    @Bean
    @StepScope
    public UserProcessor userProcessor() {
        return new UserProcessor();
    }
}
