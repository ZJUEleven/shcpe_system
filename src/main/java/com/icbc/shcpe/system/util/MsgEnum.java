package com.icbc.shcpe.system.util;

public enum MsgEnum {
    CAS001("CAS.001.001", share.msg.cas001.MainBody.class),
    CES001("CES.001.001", share.msg.ces001.MainBody.class),
    CES002("CES.002.001", share.msg.ces002.MainBody.class),
    CES003("CES.003.001", share.msg.ces003.MainBody.class),
    CES010("CES.010.001", share.msg.ces010.MainBody.class),
    CES011("CES.011.001", share.msg.ces011.MainBody.class),
    CES012("CES.012.001", share.msg.ces012.MainBody.class);

    private String msgType;
    private Class cls;

    MsgEnum(String msgType, Class cls) {
        this.msgType = msgType;
        this.cls = cls;
    }

    public String getMsgType() {
        return msgType;
    }

    public Class getCls() {
        return cls;
    }
}
