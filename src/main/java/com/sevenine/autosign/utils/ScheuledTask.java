package com.sevenine.autosign.utils;

import com.sevenine.autosign.config.Config;
import com.sevenine.autosign.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 定时考勤
 * created by xu-jp on 2020/5/23
 **/
@Component
public class ScheuledTask {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Config config;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private Sign sign;

    // 周一到周六早上8：15：30
    @Scheduled(cron = "30 15 8 * * 1-6")
    public void morning(){
        logger.info("==早间打卡启动==");
        sleepRandomTime(20*1000);
        sign.sign(accountRepository.getAccountReadXml(config.getAccountPath()));
    }

    // 周一 三 五 六晚上17：32：30
    @Scheduled(cron = "30 32 17 * * 1,3,5,6")
    public void afternoon(){
        logger.info("==晚间打卡启动==");
        sleepRandomTime(20*1000);
        sign.sign(accountRepository.getAccountReadXml(config.getAccountPath()));
    }

    // 周二 四晚上21：00：30
    @Scheduled(cron = "30 00 21 * * 2,4")
    public void night(){
        logger.info("==加班打卡启动==");
        sleepRandomTime(20*1000);
        sign.sign(accountRepository.getAccountReadXml(config.getAccountPath()));
    }

    // 周一 周三 周五晚上21：00：30
    @Scheduled(cron = "30 00 21 * * 1,3,5")
    public void nightNNS(){
        logger.info("==加班打卡启动996==");
        sleepRandomTime(20*1000);
        sign.sign(accountRepository.getAccountReadXml(config.getAccountPathNNS()));
    }

//    @Scheduled(cron = "* * * * * 4")
//    public void test(){
//        logger.info("打卡启动！");
//        sleepRandomTime(20*1000);
//        sign.sign(accountRepository.getAccountReadXml(config.getAccountPath()));
//    }

//    @Scheduled(cron = "00 03 21 * * WED")
//    public void test2(){
//        logger.info("打卡启动！");
//        sleepRandomTime(20*1000);
//        sign.sign(accountRepository.getAccountReadXml(config.getAccountPathNNS()));
//    }


    // 随机睡眠
    public static void sleepRandomTime(int time){
        try {
            Random r = new Random();
            int mini = r.nextInt(time);
            Thread.sleep(mini);
        }catch(Exception ex){
            System.out.println("睡眠失败");
        }
    }



}
