package com.icbc.shcpe.system.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * twitter的snowflake算法 -- java实现
 * 
 * @author beyond
 * @date 2016/11/26
 */
@Component
public class SnowFlakeForMsgID {

    private static final long TIME_SPACE = 24*60*60*1000l;//24小时时间间隔的时间戳

    public void setSTART_STMP(long START_STMP) {
        this.START_STMP = START_STMP;
    }

    /**
     * 起始的时间戳
     */

    private  long START_STMP = new GetStartStmp().getStartStmp("00:00:00");//以当天日期零点作为开始时间戳

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 3; //序列号占用的位数
    private final static long MACHINE_BIT = 2;   //机器标识占用的位数
    private final static long DATACENTER_BIT = 2;//数据中心占用的位数

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    @Value("${data_center_id}")
    private long datacenterId;  //数据中心
    @Value("${machine_id}")
    private long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastStmp = -1L;//上一次时间戳

    public SnowFlakeForMsgID() {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        //判断当前时间与开始时间间隔是否达到24小时，若达到指定间隔，重置开始时间
        GetStartStmp getStaStmp = new GetStartStmp();
        if((currStmp - START_STMP) >= TIME_SPACE){
            setSTART_STMP(getStaStmp.getStartStmp("00:00:00"));
        }

        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;                             //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

//    public static void main(String[] args) {
//        SnowFlake snowFlake = new SnowFlake(0, 0);
//
//        for (int i = 0; i < (1 << 10); i++) {
//            System.out.println(snowFlake.nextId());
//        }
//
//    }
}
