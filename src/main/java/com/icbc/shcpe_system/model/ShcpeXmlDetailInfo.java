package com.icbc.shcpe_system.model;

public class ShcpeXmlDetailInfo {
    private Long xmlId;

    private String xmlInfo;

    public Long getXmlId() {
        return xmlId;
    }

    public void setXmlId(Long xmlId) {
        this.xmlId = xmlId;
    }

    public String getXmlInfo() {
        return xmlInfo;
    }

    public void setXmlInfo(String xmlInfo) {
        this.xmlInfo = xmlInfo == null ? null : xmlInfo.trim();
    }
}