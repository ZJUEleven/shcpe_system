package com.icbc.shcpe_system;

import org.junit.Test;

public class ExtendsTest {
    String str = "这是父类";
    public void common(){
        System.out.println(str);
    }

    class child extends ExtendsTest{
        String str = "子类";
    }

    @Test
    public void test() {

        child s = new child();
        s.common();
    }
}

