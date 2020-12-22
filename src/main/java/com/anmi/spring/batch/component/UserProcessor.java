package com.anmi.spring.batch.component;

import com.anmi.spring.batch.model.UserInput;
import com.anmi.spring.batch.model.UserOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UserProcessor implements ItemProcessor<UserInput, UserOutput> {

    private static final Map<String, String> deptMap = new HashMap<>();

    public UserProcessor() {
        deptMap.put("001", "Computers");
        deptMap.put("002", "Electronics");
        deptMap.put("003", "Mechanical");
    }

    @Override
    public UserOutput process(UserInput user) {
        String deptCode = user.getDepartment();
        String deptName = deptMap.getOrDefault(deptCode, "Miscellaneous");
        UserOutput output = new UserOutput(user.getId(), user.getName(), deptName, user.getSalary(), user.getCreatedAt());
        log.info(String.format("User dept converted from [%s] to [%s]", deptCode, deptName));
        return output;
    }
}
