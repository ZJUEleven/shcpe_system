package com.icbc.shcpe.system.shcpe.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.icbc.shcpe.system.util.MsgType;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXValidator;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.XMLErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import share.shcpe.service.InterfaceForOther;
import share.util.MsgHandleResult;
import share.util.MsgRestriction;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Dubbo调用接口配置，作为Provider
@Service(
        version = "${demo.service.version}",
        application = "${dubbo.application.id}",
        protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}"
)
public class InterfaceForOtherImpl implements InterfaceForOther {
    @Autowired
    MsgHandleResult msgHandleResult;
    @Autowired
    DealMsg dealMsg;
    public MsgHandleResult tradeInfoReceive(String msgType, String msg){
        switch (msgType){
            case MsgType.CES001:
                //校验报文格式
                Boolean isCes001XML = validateXMLByXSD(msgType,msg);
                isContinueDeal(msgType, msg, isCes001XML);
                return msgHandleResult;
            case MsgType.CES011:
                //校验报文格式
                Boolean isCes011XML = validateXMLByXSD(msgType,msg);
                isContinueDeal(msgType, msg, isCes011XML);
                return msgHandleResult;
            default:
                msgHandleResult.setResultCode("-1");
                msgHandleResult.setErrorReason("报文类型错误！");
                return msgHandleResult;
        }
    }

    /**
     * 判断是否继续执行业务代码，如果校验通过，则开线程处理业务；否则不执行
     * @param msgType   报文类型
     * @param msg       报文信息（String类型）
     * @param isXML     报文校验是否通过，通过为true
     * @return          返回是否继续执行业务处理结果
     */
    private boolean isContinueDeal(String msgType, String msg, Boolean isXML) {
        if(isXML){
            msgHandleResult.setResultCode("1");
            msgHandleResult.setErrorReason(null);
            //报文格式没错就开新线程处理业务
            dealMsg.setMsg(msg);
            dealMsg.setMsgType(msgType);
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.execute(dealMsg);
            return true;
        }else {
            msgHandleResult.setResultCode("-1");
            msgHandleResult.setErrorReason(msgType + "报文格式不正确！");
            System.out.println(msgType + "报文校验失败！");
            return false;
        }
    }

    /**
     * 校验接收到的报文是否符合规则
     * @param msgType   报文类型
     * @param msg       报文信息（String类型）
     */
    private Boolean validateXMLByXSD(String msgType, String msg) {
        String xmlStr = msg;
        String xsdFileName = MsgRestriction.XSD_PATH + msgType + ".xsd";
        Boolean isXML = false;
        try {
            //创建默认的XML错误处理器
            XMLErrorHandler errorHandler = new XMLErrorHandler();
            //获取基于 SAX 的解析器的实例
            SAXParserFactory factory = SAXParserFactory.newInstance();
            //解析器在解析时验证 XML 内容。
            factory.setValidating(true);
            //指定由此代码生成的解析器将提供对 XML 名称空间的支持。
            factory.setNamespaceAware(true);
            //使用当前配置的工厂参数创建 SAXParser 的一个新实例。
            SAXParser parser = factory.newSAXParser();
            //创建一个读取工具
//            SAXReader xmlReader = new SAXReader();
            //获取要校验xml文档实例
            Document xmlDocument = DocumentHelper.parseText(xmlStr);
            //设置 XMLReader 的基础实现中的特定属性。核心功能和属性列表可以在 [url]http://sax.sourceforge.net/?selected=get-set[/url] 中找到。
            parser.setProperty(
                    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            parser.setProperty(
                    "http://java.sun.com/xml/jaxp/properties/schemaSource",
                    "file:" + xsdFileName);
            //创建一个SAXValidator校验工具，并设置校验工具的属性
            SAXValidator validator = new SAXValidator(parser.getXMLReader());
            //设置校验工具的错误处理器，当发生错误时，可以从处理器对象中得到错误信息。
            validator.setErrorHandler(errorHandler);
            //校验
            validator.validate(xmlDocument);

            XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
            //如果错误信息不为空，说明校验失败，打印错误信息
            if (errorHandler.getErrors().hasContent()) {
                System.out.println("XML信息通过XSD文件校验失败！");
                writer.write(errorHandler.getErrors());
                isXML = false;
            } else {
                System.out.println("Good! XML信息通过XSD文件校验成功！");
                isXML = true;
            }
        } catch (Exception ex) {
            System.out.println("XML文件: " + xmlStr + " 通过XSD文件:" + xsdFileName + "检验失败。/n原因： " + ex.getMessage());
            ex.printStackTrace();
        }
        return isXML;
    }
}
