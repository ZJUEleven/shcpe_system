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

    private long staStmp = getSecondTimestamp(new GetStartStmp().getStartStmp("00:00:00"));//以当天日期零点作为开始时间戳

    public long getStartStmp() {
        return this.staStmp;
    }

    /**
     * 每一部分占用的位数
     */
    private static final long SEQUENCE_BIT = 3; //序列号占用的位数
    private static final long MACHINE_BIT = 1;   //机器标识占用的位数
    private static final long DATACENTER_BIT = 1;//数据中心占用的位数

    public long getSequenceBit() {
        return SEQUENCE_BIT;
    }

    public long getMachineBit() {
        return MACHINE_BIT;
    }

    public long getDatacenterBit() {
        return DATACENTER_BIT;
    }

    public long getNewstmp() {
        return getSecondTimestamp(System.currentTimeMillis());
    }

    private int getSecondTimestamp(long millisecond) {
        String timestamp = String.valueOf(millisecond / 1000);
        return Integer.valueOf(timestamp);
    }
}
