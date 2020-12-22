package com.anmi.spring.batch.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserInput {
    private long id;
    private String name;
    private int salary;
    private String department;
    private Date createdAt;
}
