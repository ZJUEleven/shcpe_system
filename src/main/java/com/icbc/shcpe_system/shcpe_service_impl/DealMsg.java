package com.icbc.shcpe_system.shcpe_service_impl;

import org.apache.ibatis.exceptions.PersistenceException;
import share.middle_service.MsgHandlerForShcpe;
import com.alibaba.dubbo.config.annotation.Reference;
import com.icbc.shcpe_system.dao.ShcpeDealInfoMapper;
import com.icbc.shcpe_system.dao.ShcpeXmlDetailInfoMapper;
import com.icbc.shcpe_system.model.ShcpeDealInfo;
import com.icbc.shcpe_system.model.ShcpeXmlDetailInfo;
import com.icbc.shcpe_system.util.MsgClass;
import com.icbc.shcpe_system.util.MsgType;
import com.icbc.shcpe_system.util.SnowFlakeForDealAndQuoteID;
import com.icbc.shcpe_system.util.SnowFlakeForMsgID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


@Service
public class DealMsg implements Runnable{
    private static final String MEMBERID = "000000";//会员代码（6位）
    private static final String BRANCHID = "000000000";//机构代码（9位）
    private static final String QUTOTYPE= "DT";//第一位为报价方式代码，第二位为品种代码
    private static final String DEALTYPE= "TD";//前2位为品种代码

    private String msgType;
    private String msg;

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    //Dubbo调用接口配置，作为Consumer
    @Reference(version = "${demo.shcpe_service.version}",
            application = "${dubbo.application.id}",
            url = "dubbo://localhost:23456")
    private MsgHandlerForShcpe msgHandlerForShcpe;

    @Autowired
    ShcpeXmlDetailInfoMapper shcpeXmlDetailInfoMapper;
    @Autowired
    ShcpeDealInfoMapper shcpeDealInfoMapper;
    @Autowired
    SnowFlakeForDealAndQuoteID snowFlakeForDealAndQuoteID;
    @Autowired
    SnowFlakeForMsgID snowFlakeForMsgID;

    @Override
    public void run() {
        switch (msgType){
            case MsgType.CES001 :
                //解析报文
                //将报文转换为对应对象
                Class ces001MsgClass = null;
                try {
                    ces001MsgClass = Class.forName(MsgClass.CES001CLASS);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                share.msg_class.CES001Msg.MainBody ces001 = (share.msg_class.CES001Msg.MainBody) getJavaFromXmlStr(ces001MsgClass,msg);
                //存储CES001报文信息
                saveCes001ToMysql(msgType,msg,ces001);
                //组装“交易业务确认报文”ces010
                share.msg_class.CES010Msg.MainBody ces010 = new share.msg_class.CES010Msg.MainBody();
                String ces010XmlStr = createCes010(ces001,ces010);
                //存储ces010报文信息
                long ces010IdInMysql = saveCes010ToMysql(ces010XmlStr,ces010);
                try {
                    //发送确认报文
                    msgHandlerForShcpe.sendMsgToBusiSide(MsgType.CES010,ces010XmlStr);
                }catch(Exception e){
                    //若出现异常，将报文状态置为“发送失败”
                    ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
                    shcpeDealInfo.setId(ces010IdInMysql);
                    shcpeDealInfo.setMsgStatus((byte)-1);//报文状态置为“发送失败”
                    try {
                        shcpeDealInfoMapper.updateByPrimaryKeySelective(shcpeDealInfo);
                    }catch (PersistenceException pe){
                        e.printStackTrace();
                    }
                }

                //组装“转贴现对话报价转发报文”ces002
                share.msg_class.CES002Msg.MainBody ces002 = new  share.msg_class.CES002Msg.MainBody();
                String  ces002XmlStr = createCes002(ces001,ces002);
                //存储ces002报文信息
                long ces002IdInMysql = saveCes002ToMysql(ces002XmlStr,ces002);
                try {
                    //发送ces002报文
                    msgHandlerForShcpe.sendMsgToBusiSide(MsgType.CES002,ces002XmlStr);
                }catch(Exception e){
                    //若出现异常，将报文状态置为“发送失败”
                    ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
                    shcpeDealInfo.setId(ces002IdInMysql);
                    shcpeDealInfo.setMsgStatus((byte)-1);//报文状态置为“发送失败”
                    try {
                        shcpeDealInfoMapper.updateByPrimaryKeySelective(shcpeDealInfo);
                    }catch (PersistenceException pe){
                        e.printStackTrace();
                    }
                }

                break;
            case MsgType.CES011 :
                //将报文转换为对应对象
                Class ces011MsgClass = null;
                try {
                    ces011MsgClass = Class.forName(MsgClass.CES011CLASS);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                share.msg_class.CES011Msg.MainBody ces011 = (share.msg_class.CES011Msg.MainBody) getJavaFromXmlStr(ces011MsgClass,msg);
                //存储CES011报文信息
                saveCes011ToMysql(msgType,msg,ces011);
                if(ces011.getRecInf().getRecCmd().equals("0")){//应答标识为0：成交
                    //组装“转贴现成交通知报文”ces003
                    share.msg_class.CES003Msg.MainBody ces003 = new share.msg_class.CES003Msg.MainBody();
                    String ces003XmlStr = createCes003(ces011,ces003);
                    //存储ces012报文
                    long ces003IdInMysql = saveCes003ToMysql(ces003XmlStr,ces003);
                    try {
                        //发送ces012报文
                        msgHandlerForShcpe.sendMsgToBusiSide(MsgType.CES003,ces003XmlStr);
                    }catch(Exception e){
                        //若出现异常，将报文状态置为“发送失败”
                        ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
                        shcpeDealInfo.setId(ces003IdInMysql);
                        shcpeDealInfo.setMsgStatus((byte)-1);//报文状态置为“发送失败”
                        try {
                            shcpeDealInfoMapper.updateByPrimaryKeySelective(shcpeDealInfo);
                        }catch (PersistenceException pe){
                            e.printStackTrace();
                        }
                    }
                }
                if(ces011.getRecInf().getRecCmd().equals("1")){//应答标识为1：终止
                    //组装“对话报价终止通知报文”ces012
                    share.msg_class.CES012Msg.MainBody ces012 = new share.msg_class.CES012Msg.MainBody();
                    String ces012XmlStr = createCes012(ces011,ces012);
                    //存储ces012报文
                    long ces012IdInMysql = saveCes012ToMysql(ces012XmlStr,ces012);
                    try {
                        //发送ces012报文
                        msgHandlerForShcpe.sendMsgToBusiSide(MsgType.CES012,ces012XmlStr);
                    }catch(Exception e){
                        //若出现异常，将报文状态置为“发送失败”
                        ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
                        shcpeDealInfo.setId(ces012IdInMysql);
                        shcpeDealInfo.setMsgStatus((byte)-1);//报文状态置为“发送失败”
                        try {
                            shcpeDealInfoMapper.updateByPrimaryKeySelective(shcpeDealInfo);
                        }catch (PersistenceException pe){
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                System.out.println("报文类型不正确!");
                break;
        }
    }

    /**
     * 将ces002报文存入数据库
     * @param ces002XmlStr
     * @param ces002
     * @return
     */
    private long saveCes002ToMysql(String ces002XmlStr, share.msg_class.CES002Msg.MainBody ces002) {
        //将报文信息存入对应的表中
        ShcpeXmlDetailInfo shcpeXmlDetailInfo = new ShcpeXmlDetailInfo();
        shcpeXmlDetailInfo.setXmlInfo(ces002XmlStr);
        try {
            shcpeXmlDetailInfoMapper.insertSelective(shcpeXmlDetailInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
        shcpeDealInfo.setMsgId(ces002.getMsgId().getId());
        shcpeDealInfo.setTrdDir(ces002.getQuoteInf().getTrdDir().value());
        shcpeDealInfo.setMsyType(MsgType.CES002);
        shcpeDealInfo.setUpdateTime(new Date());
        shcpeDealInfo.setQuoteId(ces002.getQuoteInf().getQuoteId());
        shcpeDealInfo.setMsgStatus((byte)0);//报文状态置为“已发送”
        shcpeDealInfo.setXmlId(shcpeXmlDetailInfo.getXmlId());
        try {
            shcpeDealInfoMapper.insertSelective(shcpeDealInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        return shcpeDealInfo.getId();
    }

    /**
     * 组装ces002报文
     * @param ces001
     * @param ces002
     * @return
     */
    private String createCes002(share.msg_class.CES001Msg.MainBody ces001, share.msg_class.CES002Msg.MainBody ces002) {
        /*------设置报文标识（报文标识号+报文时间）--------*/
        String msgId = MEMBERID + BRANCHID + getDate() + String.format("%10d",snowFlakeForMsgID.nextId());//报文标识号
        ces002.setMsgId(new share.msg_class.CES002Msg.MsgId());
        ces002.getMsgId().setId(msgId);
        //生成XMLGregorianCalendar类
        GregorianCalendar gcal =new GregorianCalendar();
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal= DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        ces002.getMsgId().setCreDtTm(xgcal);//报文时间
        /*------------设置原报文标识------------*/
        ces002.setOrgnlMsgId(new share.msg_class.CES002Msg.OrgnlMsgId());
        ces002.getOrgnlMsgId().setId(ces001.getMsgId().getId());
        ces002.getOrgnlMsgId().setCreDtTm(ces001.getMsgId().getCreDtTm());
        /*----------设置报价单信息---------*/
        ces002.setQuoteInf(new share.msg_class.CES002Msg.QuoteInf());
        //报价单编号
        ces002.getQuoteInf().setQuoteId(ces001.getQuoteInf().getQuoteId());
        //报价单操作标识
        ces002.getQuoteInf().setQuoteOp("0");//0：发送；1：修改发送
        //业务类型
        share.msg_class.CES002Msg.BusiType ces002BusiType = share.msg_class.CES002Msg.BusiType.fromValue(ces001.getQuoteInf().getBusiType().value());
        ces002.getQuoteInf().setBusiType(ces002BusiType);
        //交易方向
        ces002.getQuoteInf().setTrdDir(share.msg_class.CES002Msg.TrdDir.fromValue(ces001.getQuoteInf().getTrdDir().value()));
        /*------设置本方信息-------*/
        ces002.setSlfInf(new share.msg_class.CES002Msg.SlfInf());
        //本方机构代码
        ces002.getSlfInf().setReqBranch(ces001.getSlfInf().getReqBranch());
        //本方非法人产品
        ces002.getSlfInf().setProductId(ces001.getSlfInf().getProductId());
        //本方交易员ID
        ces002.getSlfInf().setReqUser(ces001.getSlfInf().getReqUser());
        /*------设置对方信息-------*/
        ces002.setCpInf(new share.msg_class.CES002Msg.CpInf());
        //对方机构代码
        ces002.getCpInf().setCpBranch(ces001.getCpInf().getCpBranch());
        //对方非法人产品
        ces002.getCpInf().setCpProductId(ces001.getCpInf().getCpProductId());
        //对方交易员ID
        ces002.getCpInf().setCpUser(ces001.getCpInf().getCpUser());
        /*------设置报价信息-------*/
        ces002.setQuoteFctInf(new share.msg_class.CES002Msg.QuoteFctInf());
        //票据种类
        ces002.getQuoteFctInf().setCdType(share.msg_class.CES002Msg.CdType.fromValue(ces001.getQuoteFctInf().getCdType().value()));
        //票据介质
        ces002.getQuoteFctInf().setCdMedia(share.msg_class.CES002Msg.CdMedia.fromValue(ces001.getQuoteFctInf().getCdMedia().value()));
        //票据张数
        ces002.getQuoteFctInf().setDrftNm(ces001.getQuoteFctInf().getDrftNm());
        //票面总额
        ces002.getQuoteFctInf().setSumAmt(new share.msg_class.CES002Msg.CurrencyAndAmount());
        ces002.getQuoteFctInf().getSumAmt().setCcy(ces001.getQuoteFctInf().getSumAmt().getCcy());
        ces002.getQuoteFctInf().getSumAmt().setValue(ces001.getQuoteFctInf().getSumAmt().getValue());
        //加权平均剩余期限
        ces002.getQuoteFctInf().setTenorDays(ces001.getQuoteFctInf().getTenorDays());
        //部分成交选项
        ces002.getQuoteFctInf().setSubDeal(ces001.getQuoteFctInf().getSubDeal());
        //报价有效时间
        ces002.getQuoteFctInf().setQuoteTime(ces001.getQuoteFctInf().getQuoteTime());
        //清算速度
        ces002.getQuoteFctInf().setSetSpeed(share.msg_class.CES002Msg.SetSpeed.fromValue(ces001.getQuoteFctInf().getSetSpeed().value()));
        //清算类型
        ces002.getQuoteFctInf().setClrTp(share.msg_class.CES002Msg.ClrTp.fromValue(ces001.getQuoteFctInf().getClrTp().value()));
        //最晚结算时间
        ces002.getQuoteFctInf().setSetTime(ces001.getQuoteFctInf().getSetTime());
        //结算方式
        ces002.getQuoteFctInf().setSetMode(share.msg_class.CES002Msg.SetMode.fromValue(ces001.getQuoteFctInf().getSetMode().value()));
        //结算金额
        ces002.getQuoteFctInf().setSetAmt(new share.msg_class.CES002Msg.CurrencyAndAmount());
        ces002.getQuoteFctInf().getSetAmt().setCcy(ces001.getQuoteFctInf().getSetAmt().getCcy());
        ces002.getQuoteFctInf().getSetAmt().setValue(ces001.getQuoteFctInf().getSetAmt().getValue());
        //结算日
        ces002.getQuoteFctInf().setSetDate(ces001.getQuoteFctInf().getSetDate());
        //交易利率
        ces002.getQuoteFctInf().setTrdRate(ces001.getQuoteFctInf().getTrdRate());
        //应付利息
        ces002.getQuoteFctInf().setPayInt(new share.msg_class.CES002Msg.CurrencyAndAmount());
        ces002.getQuoteFctInf().getPayInt().setCcy(ces001.getQuoteFctInf().getPayInt().getCcy());
        ces002.getQuoteFctInf().getPayInt().setValue(ces001.getQuoteFctInf().getPayInt().getValue());
        //收益率
        ces002.getQuoteFctInf().setYieldRate(ces001.getQuoteFctInf().getYieldRate());
        /*------设置票据清单信息-------*/
        ces002.setBlist(new share.msg_class.CES002Msg.Blist());
        //票据
        share.msg_class.CES002Msg.Bill ces002Bill = new share.msg_class.CES002Msg.Bill();
        for(int i = 0 ; i < ces001.getBlist().getBill().size() ; i++){
            share.msg_class.CES001Msg.Bill ces001Bill = ces001.getBlist().getBill().get(i);
            //票据号码
            ces002Bill.setCdNo(ces001Bill.getCdNo());
            //票据金额
            ces002Bill.setCdAmt(new share.msg_class.CES002Msg.CurrencyAndAmount());
            ces002Bill.getCdAmt().setCcy(ces001Bill.getCdAmt().getCcy());
            ces002Bill.getCdAmt().setValue(ces001Bill.getCdAmt().getValue());
            //票据到期日
            ces002Bill.setDueDt(ces001Bill.getDueDt());
            //票据实际到期日
            ces002Bill.setMatDt(ces001Bill.getMatDt());
            //贴现日期
            ces002Bill.setDsctDt(xgcal);
            //出票日期
            ces002Bill.setIssDt(xgcal);
            //出票人名称
            ces002Bill.setDwrName("张三");
            //承兑人名称
            ces002Bill.setPayName("李四");
            //承兑人开户行机构代码
            ces002Bill.setAcptSvcrBrId(BRANCHID);
            //贴现行机构代码
            ces002Bill.setDsctBrId(BRANCHID);
            //保证增信行机构代码
            ces002Bill.setAddGrntBrId(BRANCHID);
            //承兑人开户行（确认）机构代码
            ces002Bill.setAcptCfmBrId(BRANCHID);
            //承兑保证行机构代码
            ces002Bill.setAcptGrntBrId(BRANCHID);
            //贴现保证人机构代码
            ces002Bill.setDsctGrntBrId(BRANCHID);
            //剩余期限
            ces002Bill.setTenorDays(ces001Bill.getTenorDays());
            //应付利息
            ces002Bill.setPayInt(new share.msg_class.CES002Msg.CurrencyAndAmount());
            ces002Bill.getPayInt().setCcy(ces001Bill.getPayInt().getCcy());
            ces002Bill.getPayInt().setValue(ces001Bill.getPayInt().getValue());
            //结算金额
            ces002Bill.setSetAmt(new share.msg_class.CES002Msg.CurrencyAndAmount());
            ces002Bill.getSetAmt().setCcy(ces001Bill.getSetAmt().getCcy());
            ces002Bill.getSetAmt().setValue(ces001Bill.getSetAmt().getValue());
            ces002.getBlist().getBill().add(ces002Bill);
        }

        return getXmlStrFromJava(ces002);
    }

    /**
     * 将ces003报文存入数据库
     * @param ces003XmlStr
     * @param ces003
     * @return
     */
    private long saveCes003ToMysql(String ces003XmlStr, share.msg_class.CES003Msg.MainBody ces003) {
        //将ces003报文信息填入表中
        ShcpeXmlDetailInfo shcpeXmlDetailInfo = new ShcpeXmlDetailInfo();
        shcpeXmlDetailInfo.setXmlInfo(ces003XmlStr);
        try {
            shcpeXmlDetailInfoMapper.insertSelective(shcpeXmlDetailInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        ShcpeDealInfo shcpeDealInfo  = new ShcpeDealInfo();
        shcpeDealInfo.setMsgId(ces003.getMsgId().getId());
        shcpeDealInfo.setTrdDir(ces003.getQuoteInf().getTrdDir().value());
        shcpeDealInfo.setMsyType(MsgType.CES003);
        shcpeDealInfo.setUpdateTime(new Date());
        shcpeDealInfo.setQuoteId(ces003.getQuoteInf().getQuoteId());
        shcpeDealInfo.setMsgStatus((byte)0);//状态置为已发送
        shcpeDealInfo.setDealId(ces003.getDealInf().getDealId());
        shcpeDealInfo.setTrdStatus((byte)1);//交易状态置为交易成功（-1:交易失败/终止，0:交易中，1：交易成功）
        shcpeDealInfo.setXmlId(shcpeXmlDetailInfo.getXmlId());
        try {
            shcpeDealInfoMapper.insertSelective(shcpeDealInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        return shcpeDealInfo.getId();
    }

    /**
     * 组装ces003报文
     * @param ces011
     * @param ces003
     * @return
     */
    private String createCes003(share.msg_class.CES011Msg.MainBody ces011, share.msg_class.CES003Msg.MainBody ces003) {
        /*-----------设置报文中所有对象实例---------------------*/
        ces003.setMsgId(new share.msg_class.CES003Msg.MsgId());
        ces003.setBlist(new share.msg_class.CES003Msg.Blist());
        ces003.setCpInf(new share.msg_class.CES003Msg.CpInf());
        ces003.setDealInf(new share.msg_class.CES003Msg.DealInf());
        ces003.setQuoteFctInf(new share.msg_class.CES003Msg.QuoteFctInf());
        ces003.setQuoteInf(new share.msg_class.CES003Msg.QuoteInf());
        ces003.setSlfInf(new share.msg_class.CES003Msg.SlfInf());
        share.msg_class.CES003Msg.Bill ces003Bill = new share.msg_class.CES003Msg.Bill();
        ces003.getQuoteFctInf().setSumAmt(new share.msg_class.CES003Msg.CurrencyAndAmount());
        ces003.getQuoteFctInf().setSetAmt(new share.msg_class.CES003Msg.CurrencyAndAmount());
        ces003.getQuoteFctInf().setPayInt(new share.msg_class.CES003Msg.CurrencyAndAmount());
        ces003Bill.setCdAmt(new share.msg_class.CES003Msg.CurrencyAndAmount());
        ces003Bill.setPayInt(new share.msg_class.CES003Msg.CurrencyAndAmount());
        ces003Bill.setSetAmt(new share.msg_class.CES003Msg.CurrencyAndAmount());
        /*------------------------------设置报文标识（报文标识号+报文时间）---------------------------------*/
        String msgId = MEMBERID + BRANCHID + getDate() + String.format("%10d",snowFlakeForMsgID.nextId());//报文标识号
        ces003.getMsgId().setId(msgId);
        //生成XMLGregorianCalendar类
        GregorianCalendar gcal =new GregorianCalendar();
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal= DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        ces003.getMsgId().setCreDtTm(xgcal);//报文时间
        /*------------------------------设置成交信息--------------------------------*/
        //成交单编号
        String dealId = DEALTYPE + getDate() + String.format("%6d",snowFlakeForDealAndQuoteID.nextId());
        ces003.getDealInf().setDealId(dealId);
        //成交方式
        ces003.getDealInf().setTrdType(share.msg_class.CES003Msg.TrdType.TT_01);//TT01：询价成交；TT02：匿名点击；TT01：点击成交；TT01：应急成交
        //成交日
        ces003.getDealInf().setTrdDate(xgcal);
        //成交时间
        ces003.getDealInf().setDealTime(xgcal);
        //成交状态
        ces003.getDealInf().setDealSta(share.msg_class.CES003Msg.DealSta.DS_01);//DS01：已成交；DS01：已撤销
        /*------------------------------设置报价单信息--------------------------------*/
        //报价单编号
        ces003.getQuoteInf().setQuoteId(ces011.getQuoteInf().getQuoteId());
        //业务类型
        share.msg_class.CES003Msg.BusiType ces003BusiType = share.msg_class.CES003Msg.BusiType.fromValue(ces011.getQuoteInf().getBusiType().value());
        ces003.getQuoteInf().setBusiType(ces003BusiType);
        //交易方向
        ces003.getQuoteInf().setTrdDir(share.msg_class.CES003Msg.TrdDir.TDD_01);//TDD01:买入；TDD02:卖出
        /*-----------设置本方信息---------------*/
        //从表中查找到最新的ces001报文信息，从中提取本方信息
        String ces001Xml = shcpeDealInfoMapper.selectNewestXmlByQuoteIdAndMsgType(ces003.getQuoteInf().getQuoteId(),MsgType.CES001);
        Class ces001MsgClass = null;
        try {
            ces001MsgClass = Class.forName(MsgClass.CES001CLASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        share.msg_class.CES001Msg.MainBody ces001 = (share.msg_class.CES001Msg.MainBody)getJavaFromXmlStr(ces001MsgClass,ces001Xml);
        //本方机构代码
        ces003.getSlfInf().setReqBranch(ces001.getSlfInf().getReqBranch());
        //本方非法人产品
        ces003.getSlfInf().setProductId(ces001.getSlfInf().getProductId());
        //本方交易员ID
        ces003.getSlfInf().setReqUser(ces001.getSlfInf().getReqUser());
        /*-----------设置对方信息---------------*/
        //对方机构代码
        ces003.getCpInf().setCpBranch(ces001.getCpInf().getCpBranch());
        //对方非法人产品
        ces003.getCpInf().setCpProductId(ces001.getCpInf().getCpProductId());
        //对方交易员ID
        ces003.getCpInf().setCpUser(ces001.getCpInf().getCpUser());
        /*-----------设置报价信息---------------*/
        //票据种类
        ces003.getQuoteFctInf().setCdType(share.msg_class.CES003Msg.CdType.fromValue(ces001.getQuoteFctInf().getCdType().value()));
        //票据介质
        ces003.getQuoteFctInf().setCdMedia(share.msg_class.CES003Msg.CdMedia.fromValue(ces001.getQuoteFctInf().getCdMedia().value()));
        //票据张数
        ces003.getQuoteFctInf().setDrftNm(ces001.getQuoteFctInf().getDrftNm());
        //票面总额
        ces003.getQuoteFctInf().getSumAmt().setCcy(ces001.getQuoteFctInf().getSumAmt().getCcy());
        ces003.getQuoteFctInf().getSumAmt().setValue(ces001.getQuoteFctInf().getSumAmt().getValue());
        //加权平均剩余期限
        ces003.getQuoteFctInf().setTenorDays(ces001.getQuoteFctInf().getTenorDays());
        //清算速度
        ces003.getQuoteFctInf().setSetSpeed(share.msg_class.CES003Msg.SetSpeed.fromValue(ces001.getQuoteFctInf().getSetSpeed().value()));
        //清算类型
        ces003.getQuoteFctInf().setClrTp(share.msg_class.CES003Msg.ClrTp.fromValue(ces001.getQuoteFctInf().getClrTp().value()));
        //最晚结算时间
        ces003.getQuoteFctInf().setSetTime(ces001.getQuoteFctInf().getSetTime());
        //结算方式
        ces003.getQuoteFctInf().setSetMode(share.msg_class.CES003Msg.SetMode.fromValue(ces001.getQuoteFctInf().getSetMode().value()));
        //结算金额
        ces003.getQuoteFctInf().getSetAmt().setValue(ces001.getQuoteFctInf().getSetAmt().getValue());
        ces003.getQuoteFctInf().getSetAmt().setCcy(ces001.getQuoteFctInf().getSetAmt().getCcy());
        //结算日
        ces003.getQuoteFctInf().setSetDate(ces001.getQuoteFctInf().getSetDate());
        //交易利率
        ces003.getQuoteFctInf().setTrdRate(ces001.getQuoteFctInf().getTrdRate());
        //应付利息
        ces003.getQuoteFctInf().getPayInt().setCcy(ces001.getQuoteFctInf().getPayInt().getCcy());
        ces003.getQuoteFctInf().getPayInt().setValue(ces001.getQuoteFctInf().getPayInt().getValue());
        //收益率
        ces003.getQuoteFctInf().setYieldRate(ces001.getQuoteFctInf().getYieldRate());
        /*-----------设置票据清单---------------*/
        //票据
        for (int i = 0; i < ces001.getBlist().getBill().size(); i++){
            share.msg_class.CES001Msg.Bill ces001Bill = ces001.getBlist().getBill().get(i);
            //票据号码
            ces003Bill.setCdNo(ces001Bill.getCdNo());
            //票据金额
            ces003Bill.getCdAmt().setValue(ces001Bill.getCdAmt().getValue());
            ces003Bill.getCdAmt().setCcy(ces001Bill.getCdAmt().getCcy());
            //票据到期日
            ces003Bill.setDueDt(ces001Bill.getDueDt());
            //票据实际到期日
            ces003Bill.setMatDt(ces001Bill.getMatDt());
            //剩余期限
            ces003Bill.setTenorDays(ces001Bill.getTenorDays());
            //应付利息
            ces003Bill.getPayInt().setCcy(ces001Bill.getPayInt().getCcy());
            ces003Bill.getPayInt().setValue(ces001Bill.getPayInt().getValue());
            //结算金额
            ces003Bill.getSetAmt().setValue(ces001Bill.getSetAmt().getValue());
            ces003Bill.getSetAmt().setCcy(ces001Bill.getSetAmt().getCcy());
            //每一张票据放入Blist
            ces003.getBlist().getBill().add(ces003Bill);
        }
        return getXmlStrFromJava(ces003);
    }

    /**
     * 将ces012报文存入数据库
     * @param ces012XmlStr
     * @param ces012
     * @return
     */
    private long saveCes012ToMysql(String ces012XmlStr, share.msg_class.CES012Msg.MainBody ces012) {
        //将ces012报文信息填入表中
        ShcpeXmlDetailInfo shcpeXmlDetailInfo = new ShcpeXmlDetailInfo();
        shcpeXmlDetailInfo.setXmlInfo(ces012XmlStr);
        try {
            shcpeXmlDetailInfoMapper.insertSelective(shcpeXmlDetailInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        ShcpeDealInfo shcpeDealInfo  = new ShcpeDealInfo();
        shcpeDealInfo.setMsgId(ces012.getMsgId().getId());
        shcpeDealInfo.setMsyType(MsgType.CES012);
        shcpeDealInfo.setUpdateTime(new Date());
        shcpeDealInfo.setQuoteId(ces012.getQuoteInf().getQuoteId());
        shcpeDealInfo.setMsgStatus((byte)0);//状态置为已发送
        shcpeDealInfo.setTrdStatus((byte)1);//交易状态置为交易成功（-1:交易失败/终止，0:交易中，1：交易成功）
        shcpeDealInfo.setXmlId(shcpeXmlDetailInfo.getXmlId());
        try {
            shcpeDealInfoMapper.insertSelective(shcpeDealInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        return shcpeDealInfo.getId();
    }

    /**
     * 组装ces012报文信息
     * @param ces011
     * @param ces012
     * @return
     */
    private String createCes012(share.msg_class.CES011Msg.MainBody ces011, share.msg_class.CES012Msg.MainBody ces012) {
        //设置报文标识（报文标识号+报文时间）
        String msgId = MEMBERID + BRANCHID + getDate() + String.format("%10d",snowFlakeForMsgID.nextId());//报文标识号
        ces012.setMsgId(new share.msg_class.CES012Msg.MsgId());
        ces012.getMsgId().setId(msgId);
        //生成XMLGregorianCalendar类
        GregorianCalendar gcal =new GregorianCalendar();
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal= DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        ces012.getMsgId().setCreDtTm(xgcal);//报文时间
        //设置报价单信息（报价单编号+业务类型）
        ces012.setQuoteInf(new share.msg_class.CES012Msg.QuoteInf());
        ces012.getQuoteInf().setQuoteId(ces011.getQuoteInf().getQuoteId());//报价单编号
        share.msg_class.CES012Msg.BusiType ces012BusiType = share.msg_class.CES012Msg.BusiType.fromValue(ces011.getQuoteInf().getBusiType().value());
        ces012.getQuoteInf().setBusiType(ces012BusiType);//设置业务类型

        //设置终止原因
        ces012.setRefInf(new share.msg_class.CES012Msg.RefInf());
        ces012.getRefInf().setRefCmd("对方终止交易！");

        return getXmlStrFromJava(ces012);
    }

    /**
     * 将ces011报文存入数据库
     * @param msgType
     * @param msg
     * @param ces011
     * @return
     */
    private long saveCes011ToMysql(String msgType, String msg, share.msg_class.CES011Msg.MainBody ces011) {
        //将报文信息存入对应的表中
        ShcpeXmlDetailInfo shcpeXmlDetailInfo = new ShcpeXmlDetailInfo();
        shcpeXmlDetailInfo.setXmlInfo(msg);
        try {
            shcpeXmlDetailInfoMapper.insertSelective(shcpeXmlDetailInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
        shcpeDealInfo.setMsgId(ces011.getMsgId().getId());
        shcpeDealInfo.setMsyType(msgType);
        shcpeDealInfo.setUpdateTime(new Date());
        shcpeDealInfo.setMsgStatus((byte)1);//报文状态置为已接收
        shcpeDealInfo.setXmlId(shcpeXmlDetailInfo.getXmlId());
        shcpeDealInfo.setQuoteId(ces011.getQuoteInf().getQuoteId());
        try {
            shcpeDealInfoMapper.insertSelective(shcpeDealInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        return  shcpeDealInfo.getId();
    }

    /**
     * 将ces010报文存入数据库
     * @param ces010XmlStr
     * @param ces010
     * @return
     */
    private long saveCes010ToMysql(String ces010XmlStr, share.msg_class.CES010Msg.MainBody ces010) {
        //将报文信息存入对应的表中
        ShcpeXmlDetailInfo shcpeXmlDetailInfo = new ShcpeXmlDetailInfo();
        shcpeXmlDetailInfo.setXmlInfo(ces010XmlStr);
        try {
            shcpeXmlDetailInfoMapper.insertSelective(shcpeXmlDetailInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
        shcpeDealInfo.setMsgId(ces010.getMsgId().getId());
        shcpeDealInfo.setMsyType(MsgType.CES010);
        shcpeDealInfo.setUpdateTime(new Date());
        shcpeDealInfo.setMsgStatus((byte)0);//报文状态置为“已发送”
        shcpeDealInfo.setXmlId(shcpeXmlDetailInfo.getXmlId());
        shcpeDealInfo.setQuoteId(ces010.getQuoteInf().getQuoteId());
        try {
            shcpeDealInfoMapper.insertSelective(shcpeDealInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        return shcpeDealInfo.getId();
    }

    /**
     * 生成ces010报文
     * @param ces001
     * @param ces010
     * @return
     */
    private String createCes010(share.msg_class.CES001Msg.MainBody ces001, share.msg_class.CES010Msg.MainBody ces010) {
       //设置报文标识（报文标识号+报文时间）
        String msgId = MEMBERID + BRANCHID + getDate() + String.format("%10d",snowFlakeForMsgID.nextId());//报文标识号
        ces010.setMsgId(new share.msg_class.CES010Msg.MsgId());
        ces010.getMsgId().setId(msgId);
        //生成XMLGregorianCalendar类
        GregorianCalendar gcal =new GregorianCalendar();
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal= DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        ces010.getMsgId().setCreDtTm(xgcal);//报文时间
        //设置原报文标识
        ces010.setOrgnlMsgId(new share.msg_class.CES010Msg.OrgnlMsgId());
        ces010.getOrgnlMsgId().setId(ces001.getMsgId().getId());
        ces010.getOrgnlMsgId().setCreDtTm(ces001.getMsgId().getCreDtTm());
        //设置处理结果信息
        ces010.setBizCtrlInf(new share.msg_class.CES010Msg.BizCtrlInf());
        ces010.getBizCtrlInf().setPrcCd("aaaaaaaaa");//随意设定一个处理结果码
        ces010.getBizCtrlInf().setPrcMsg("处理结果说明");
        //设置业务单信息(业务单编号+业务类型)
        ces010.setQuoteInf(new share.msg_class.CES010Msg.QuoteInf());
        ces010.getQuoteInf().setQuoteId(ces001.getQuoteInf().getQuoteId());
        share.msg_class.CES010Msg.BusiType ces010BusiType = share.msg_class.CES010Msg.BusiType.fromValue(ces001.getQuoteInf().getBusiType().value());
        ces010.getQuoteInf().setBusiType(ces010BusiType);
        return getXmlStrFromJava(ces010);
    }

    /**
     *将java类转换为对应的String类型报文
     * @param object
     * @return
     */
    private String getXmlStrFromJava(Object object) {
        try {
            StringWriter stringWriter = new StringWriter();
            String className = object.getClass().getSimpleName();
            //获得转换的上下文对象
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            //获得Marshaller对象
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            QName qName = new QName("", className);
            JAXBElement<Object> root = new JAXBElement<>(qName, Object.class, object);
            marshaller.marshal(root, stringWriter);
            return stringWriter.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }


    private long saveCes001ToMysql(String msgType, String msg, share.msg_class.CES001Msg.MainBody ces001) {
        //将报文信息存入对应的表中
        ShcpeXmlDetailInfo shcpeXmlDetailInfo = new ShcpeXmlDetailInfo();
        shcpeXmlDetailInfo.setXmlInfo(msg);
        try {
            shcpeXmlDetailInfoMapper.insertSelective(shcpeXmlDetailInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        ShcpeDealInfo shcpeDealInfo = new ShcpeDealInfo();
        shcpeDealInfo.setMsgId(ces001.getMsgId().getId());
        shcpeDealInfo.setTrdDir(ces001.getQuoteInf().getTrdDir().value());
        shcpeDealInfo.setMsyType(msgType);
        shcpeDealInfo.setUpdateTime(new Date());
        shcpeDealInfo.setMsgStatus((byte)1);//报文状态置为已接收
        shcpeDealInfo.setXmlId(shcpeXmlDetailInfo.getXmlId());
        if(ces001.getQuoteInf().getQuoteId()== null){
            String dateString = getDate();//获取年月日
            String qutoid = QUTOTYPE + dateString + String.format("%06d",snowFlakeForDealAndQuoteID.nextId());//成交单号
            ces001.getQuoteInf().setQuoteId(qutoid);
        }
        shcpeDealInfo.setQuoteId(ces001.getQuoteInf().getQuoteId());
        try {
            shcpeDealInfoMapper.insertSelective(shcpeDealInfo);
        }catch (PersistenceException e){
            e.printStackTrace();
        }
        return  shcpeDealInfo.getId();
    }

    /**
     * 获取当前年月日的String类型
     * @return
     */
    private String getDate() {
        Calendar now = Calendar.getInstance();//获取当前年月日
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(year) + String.format("%02d", month) + String.format("%02d", day);
    }


    /**
     * 将接收到的string类型报文转换为对应对象
     * @param msgClass
     * @param msg
     * @return
     */
    private Object getJavaFromXmlStr(Class msgClass, String msg){
        Object xmlObject = null;
        try {
            JAXBContext context = JAXBContext.newInstance(msgClass);
            // 进行将Xml转成对象的核心接口
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader sr = new StringReader(msg);
            xmlObject = ((JAXBElement)unmarshaller.unmarshal(sr)).getValue();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return xmlObject;

    }
}
