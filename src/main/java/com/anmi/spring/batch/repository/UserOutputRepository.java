package com.anmi.spring.batch.repository;

import com.anmi.spring.batch.model.UserOutput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserOutputRepository extends JpaRepository<UserOutput, Long> {
}
