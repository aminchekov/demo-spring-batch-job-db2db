package com.anmi.spring.batch.model;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;


@Slf4j
@Component
public class UserRowMapper implements RowMapper<UserInput> {
  @Override
  public UserInput mapRow(ResultSet resultSet, int i) throws SQLException {
    return new UserInput(resultSet.getLong("ID"),
      resultSet.getString("NAME"),resultSet.getInt("SALARY"),
      resultSet.getString("DEPT"), resultSet.getDate("CREATED_AT"));
  }
}