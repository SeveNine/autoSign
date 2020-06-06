package com.sevenine.autosign;

import com.sevenine.autosign.config.Config;
import com.sevenine.autosign.domain.Account;
import com.sevenine.autosign.repository.AccountRepository;
import com.sevenine.autosign.utils.Sign;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;

@SpringBootTest
class AutosignApplicationTests {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    Config config;
    @Test
    void contextLoads() {
        LinkedHashMap<String, Account> accountReadXml = accountRepository.getAccountReadXml(config.getAccountPath());
        accountReadXml.get(0);
    }

}
