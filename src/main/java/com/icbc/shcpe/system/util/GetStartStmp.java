package com.icbc.shcpe.system.util;

import com.icbc.shcpe.system.shcpe.service.impl.DealMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 获取当天日期零点对应的时间戳
 */
public class GetStartStmp {

    private Logger logger = LoggerFactory.getLogger(DealMsg.class);

    public long getStartStmp(String hms){
        Calendar now = Calendar.getInstance();//获取当前年月日
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTimeStr = year + "-" + month + "-" + day + " " + hms;  //要跟上面sdf定义的格式一样
        Date startTime = null;
        try {
            startTime = sdf.parse(startTimeStr);
            return startTime.getTime();
        } catch (ParseException e) {
            logger.info(e.getMessage());
            return -1l;
        }
    }
}
