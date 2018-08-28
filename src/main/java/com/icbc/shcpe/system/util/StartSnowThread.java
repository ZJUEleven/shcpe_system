package com.icbc.shcpe.system.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * springboot启动时自动加载雪花算法类和时间戳间隔线程，生成ID
 */
@Component
public class StartSnowThread implements ApplicationRunner {
    @Autowired
    SnowFlakeForMsgID snowFlakeForMsgID;
    @Autowired
    CalculateTimeSpace calculateTimeSpace;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ExecutorService executorService = Executors.newCachedThreadPool();
        executorService.execute(calculateTimeSpace);
    }
}
