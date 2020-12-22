package com.anmi.spring.batch.component;

import com.anmi.spring.batch.model.UserOutput;
import com.anmi.spring.batch.repository.UserOutputRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
public class UserDbWriter implements ItemWriter<UserOutput> {

    private final UserOutputRepository userRepository;

    public UserDbWriter(UserOutputRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void write(List<? extends UserOutput> users) throws InterruptedException {
        userRepository.saveAll(users);
        log.info("Saved user list: " + users);
      //  Thread.sleep(10000);
    }
}
