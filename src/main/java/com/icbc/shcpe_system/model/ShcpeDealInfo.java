package com.icbc.shcpe_system.model;

import java.util.Date;

public class ShcpeDealInfo {
    private Long id;

    private String msgId;

    private String trdDir;

    private String msgType;

    private Date updateTime;

    private String quoteId;

    private Byte msgStatus;

    private String errorReason;

    private Byte trdStatus;

    private String dealId;

    private Long xmlId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId == null ? null : msgId.trim();
    }

    public String getTrdDir() {
        return trdDir;
    }

    public void setTrdDir(String trdDir) {
        this.trdDir = trdDir == null ? null : trdDir.trim();
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType == null ? null : msgType.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(String quoteId) {
        this.quoteId = quoteId == null ? null : quoteId.trim();
    }

    public Byte getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(Byte msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason == null ? null : errorReason.trim();
    }

    public Byte getTrdStatus() {
        return trdStatus;
    }

    public void setTrdStatus(Byte trdStatus) {
        this.trdStatus = trdStatus;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId == null ? null : dealId.trim();
    }

    public Long getXmlId() {
        return xmlId;
    }

    public void setXmlId(Long xmlId) {
        this.xmlId = xmlId;
    }
}