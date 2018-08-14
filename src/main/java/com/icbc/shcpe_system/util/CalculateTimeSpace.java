package com.icbc.shcpe_system.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用于判断时间戳间隔是否达到24小时的线程
 */
@Component
public class CalculateTimeSpace implements Runnable {

    @Autowired
    private SnowFlakeForMsgID snowFlakeForMsgID;
    private static final long TIME_SPACE = 24*60*60*1000;
    @Override
    public void run() {
        while(true){
            long currStmp = System.currentTimeMillis();
            GetStartStmp getStaStmp = new GetStartStmp();
            long startStmp = getStaStmp.getStartStmp("00:00:00");
            if((currStmp - startStmp) >= TIME_SPACE){
                snowFlakeForMsgID.setSTART_STMP(getStaStmp.getStartStmp("00:00:00"));
            }
        }
    }
}
