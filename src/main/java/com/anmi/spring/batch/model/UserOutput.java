package com.anmi.spring.batch.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "USER_OUTPUT")
public class UserOutput {

	@Id
	public Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "DEPT")
	private String department;

	@Column(name = "SALARY")
	private Integer salary;

	@Column(name = "DATE")
	private Date createdAt;

	public UserOutput(Long id, String name, String department, Integer salary, Date date) {
		this.id = id;
		this.name = name;
		this.department = department;
		this.salary = salary;
		this.createdAt = date;
	}

	public UserOutput(){
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Integer getSalary() {
		return salary;
	}

	public void setSalary(Integer salary) {
		this.salary = salary;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", department='" + department + '\'' +
				", salary=" + salary +
				'}';
	}
}
