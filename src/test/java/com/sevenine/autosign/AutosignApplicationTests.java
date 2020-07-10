package com.sevenine.autosign;

import com.sevenine.autosign.config.Config;
import com.sevenine.autosign.domain.Account;
import com.sevenine.autosign.repository.AccountRepository;
import com.sevenine.autosign.utils.Sign;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;

import static com.sevenine.autosign.utils.ScheuledTask.sleepRandomTime;

@SpringBootTest
class AutosignApplicationTests {
    @Autowired
    AccountRepository accountRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Config config;

    @Autowired
    private Sign sign;

    @Test
    void contextLoads() {
        logger.info("==打卡启动==");
//        sleepRandomTime(20*1000);
        sign.sign(accountRepository.getAccountReadXml(config.getAccountPath()));
    }

}
