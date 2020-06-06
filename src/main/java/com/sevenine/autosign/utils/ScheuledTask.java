package com.sevenine.autosign.utils;

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

    Logger logger = LoggerFactory.getLogger(Sign.class);

    @Autowired
    private Sign sign;

    // 周一到周六早上8：15：30
    @Scheduled(cron = "30 15 8 * * 2-7")
    public void morning(){
        sleepRandomTime(20*1000);
        sign.sign();

    }

    // 周一 三 五 六晚上17：32：30
    @Scheduled(cron = "30 32 17 * * 2,4,6,7")
    public void afternoon(){
        sleepRandomTime(20*1000);
        sign.sign();
    }
    // 周二 四晚上21：00：30
    @Scheduled(cron = "30 00 21 * * 3,5")
    public void night(){
        sleepRandomTime(20*1000);
        sign.sign();
    }

//    @Scheduled(cron = "00 52 15 * * ?")
//    public void test(){
//        logger.info("打卡启动！");\
//        sleepRandomTime(20*1000);
//        sign.sign();
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