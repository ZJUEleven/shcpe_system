package com.icbc.shcpe_system.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 获取当天日期零点对应的时间戳
 */
public class GetStartStmp {
    public long getStartStmp(String hms){
        Calendar now = Calendar.getInstance();//获取当前年月日
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start_time_str = year + "-" + month + "-" + day + " " + hms;  //要跟上面sdf定义的格式一样
        Date start_time = null;
        try {
            start_time = sdf.parse(start_time_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return start_time.getTime();
    }
}
