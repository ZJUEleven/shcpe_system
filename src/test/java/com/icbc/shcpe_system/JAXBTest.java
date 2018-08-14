package com.icbc.shcpe_system;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;

public class JAXBTest {

    /**
     * 将java对象转换成xml，并用注解指定生成规则，是生成属性还是生成节点
     * @throws Exception
     */
    @Test
    public void testMarshaller() throws Exception{
        /*
        //获得转换的上下文对象
        JAXBContext context = JAXBContext.newInstance(Employee.class);
        //获得Marshaller对象
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        //填充对象
        Employee employee = new Employee();
        Employee.Address address = new Employee.Address();
        employee.setId((byte)1);
        employee.setName("test");
        address.setAddressLine1("line1");
        address.setAddressLine2("line2");
        address.setCountry("cn");
        address.setState("nono");
        address.setZip((short)12122);
        employee.setAddress(address);

        //打印到控制台
        FileWriter writer=new FileWriter(new File("employee.xml"));
        marshaller.marshal(employee, writer);
        marshaller.marshal(employee, System.out);
        */
    }

    /**
     * 读取xml文档，并将xml文档反序列化为java对象
     * @throws Exception
     */
    @Test
    public void testUnMarshaller() throws Exception{
        /*
        JAXBContext context = JAXBContext.newInstance(Employee.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        File f=new File("employee.xml");
        Employee employee = (Employee) unmarshaller.unmarshal(f);
        System.out.println("id: " + employee.getId());
        System.out.println("name: " + employee.getName());
        System.out.println("address_line1: " + employee.getAddress().getAddressLine1());
        System.out.println("address_line2: " + employee.getAddress().getAddressLine2());
        System.out.println("address_country: " + employee.getAddress().getCountry());
        System.out.println("address_state: " + employee.getAddress().getState());
        System.out.println("address_zip: " + employee.getAddress().getZip());
        */
    }

    @Test
    public void test() {
        System.out.println((3L) ^ (-5L));
    }
}