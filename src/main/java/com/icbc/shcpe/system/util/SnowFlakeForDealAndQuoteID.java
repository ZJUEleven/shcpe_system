package com.icbc.shcpe.system.util;

import org.springframework.stereotype.Component;

/**
 * twitter的snowflake算法 -- java实现
 *
 * @author beyond
 * @date 2016/11/26
 */
@Component
public class SnowFlakeForDealAndQuoteID extends SnowFlakeForMsgID {

    //指定初始时间
    private static final String START_STMP_STR = "08:55:00";

    @Override
    public String getStartStmpStr() {
        return START_STMP_STR;
    }

    private long staStmp = getSecondTimestamp(new GetStartStmp().getStartStmp(getStartStmpStr()));//以指定初始时间作为开始时间戳

    @Override
    public long getStartStmp() {
        return staStmp;
    }

    /**
     * 每一部分占用的位数
     */
    private static final long SEQUENCE_BIT = 3; //序列号占用的位数
    private static final long MACHINE_BIT = 1;   //机器标识占用的位数
    private static final long DATACENTER_BIT = 1;//数据中心占用的位数

    @Override
    public long getSequenceBit() {
        return SEQUENCE_BIT;
    }

    @Override
    public long getMachineBit() {
        return MACHINE_BIT;
    }

    @Override
    public long getDatacenterBit() {
        return DATACENTER_BIT;
    }

    @Override
    public long getNewstmp() {
        return getSecondTimestamp(System.currentTimeMillis());
    }

    private int getSecondTimestamp(long millisecond) {
        String timestamp = String.valueOf(millisecond / 1000);
        return Integer.valueOf(timestamp);
    }
}
