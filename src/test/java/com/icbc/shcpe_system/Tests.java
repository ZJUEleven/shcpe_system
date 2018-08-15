package com.icbc.shcpe_system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.icbc.shcpe_system.model.ShcpeDealInfo;
import com.icbc.shcpe_system.model.ShcpeXmlDetailInfo;
import com.icbc.shcpe_system.shcpe_service_impl.DealMsg;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import com.icbc.shcpe_system.dao.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import share.middle_service.MsgHandlerForShcpe;
import share.shcpe_service.InterfaceForOther;
import share.util.MsgHandleResult;

import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Tests {

    @Autowired
    private ShcpeXmlDetailInfoMapper shcpeXmlDetailInfoMapper;
    @Autowired
    private ShcpeDealInfoMapper shcpeDealInfoMapper;
    @Autowired
    private DealMsg MyTradeInfoReceive;
    @Autowired
    private InterfaceForOther interfaceForOther;
    @Autowired
    private MsgHandleResult msgHandleResult;

    @Reference(version = "${demo.shcpe_service.version}",
            application = "${dubbo.application.id}",
            url = "dubbo://localhost:12345")
    private MsgHandlerForShcpe msgHandlerForShcpe;

    @Test
    public void test() {
/*        ShcpeXmlDetailInfo shcpeXmlDetailInfo = new ShcpeXmlDetailInfo();
        shcpeXmlDetailInfo.setXmlInfo("3");
        ShcpeXmlDetailInfoMapper.insert(shcpeXmlDetailInfo);
        System.out.println(shcpeXmlDetailInfo.getXmlId());*/
/*        ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
        shcpeDealInfo.setId(3l);
        shcpeDealInfo.setDealId("3");
        shcpeDealInfo.setErrorReason("no error");
        shcpeDealInfo.setMsgStatus(Byte.parseByte("1"));
        shcpeDealInfo.setMsyType("CES.001.001");
        shcpeDealInfo.setMsgId("123");
        shcpeDealInfo.setTrdDir("买方");
        shcpeDealInfo.setQuoteId("12");
        shcpeDealInfo.setTrdStatus(Byte.parseByte("1"));
        shcpeDealInfo.setUpdateTime(new Date());
        shcpeDealInfo.setXmlId(6l);*/
/*        String xml = shcpeDealInfoMapper.selectNewestXmlByQuoteIdAndMsgType("12","CES.001.001");
        System.out.println(xml);*/
/*        msgHandleResult = interfaceForOther.tradeInfoReceive("CES.001.001","this is 001 message!");
        System.out.println(msgHandleResult.getResultCode());*/
    }
}
