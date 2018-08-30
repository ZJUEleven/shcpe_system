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

    private static final long TIME_SPACE = 24 * 60 * 60 * 1000l;//24小时时间间隔的时间戳

    /**
     * 起始的时间戳
     */
    //指定初始时间
    private static final String START_STMP_STR = "00:00:00";

    public String getStartStmpStr() {
        return START_STMP_STR;
    }

    private long startStmp = new GetStartStmp().getStartStmp(getStartStmpStr());//以当天日期零点作为开始时间戳

    public void setStartStmp(long startStmp) {
        this.startStmp = startStmp;
    }

    public long getStartStmp() {
        return startStmp;
    }

    /**
     * 每一部分占用的位数
     */
    private static final long SEQUENCE_BIT = 3; //序列号占用的位数
    private static final long MACHINE_BIT = 2;   //机器标识占用的位数
    private static final long DATACENTER_BIT = 2;//数据中心占用的位数

    public long getSequenceBit() {
        return SEQUENCE_BIT;
    }

    public long getMachineBit() {
        return MACHINE_BIT;
    }

    public long getDatacenterBit() {
        return DATACENTER_BIT;
    }

    /**
     * 每一部分的最大值
     */
    private final long maxDatacenterNum = -1L ^ (-1L << getDatacenterBit());
    private final long maxMachineNum = -1L ^ (-1L << getMachineBit());
    private final long maxSequence = -1L ^ (-1L << getSequenceBit());

    /**
     * 每一部分向左的位移
     */
    private final long machineLeft = getMachineBit();
    private final long datacenterLeft = getSequenceBit() + getMachineBit();
    private final long timestmpLeft = datacenterLeft + getDatacenterBit();

    @Value("${data_center_id}")
    private long datacenterId;  //数据中心
    @Value("${machine_id}")
    private long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastStmp = -1L;//上一次时间戳

    public SnowFlakeForMsgID() {
        if (datacenterId > maxDatacenterNum || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than maxDatacenterNum or less than 0");
        }
        if (machineId > maxMachineNum || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than maxMachineNum or less than 0");
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
        if ((currStmp - getStartStmp()) >= TIME_SPACE) {
            setStartStmp(getStaStmp.getStartStmp(getStartStmpStr()));
        }

        if (currStmp < lastStmp) {
            throw new IllegalArgumentException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & maxSequence;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - getStartStmp()) << timestmpLeft //时间戳部分
                | datacenterId << datacenterLeft       //数据中心部分
                | machineId << machineLeft             //机器标识部分
                | sequence;                             //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    public long getNewstmp() {
        return System.currentTimeMillis();
    }
}
