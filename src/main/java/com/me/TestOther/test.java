package com.me.TestOther;

import com.me.annotation.MyService;

import java.lang.reflect.Method;

public class test {


    public static void main(String[] args) throws Exception{

        Demo demo = new Demo();
        Method method = demo.getClass().getMethod("select",null);
        System.out.println("me:" + method.getAnnotation(MyMethod.class).value());

        System.out.println(demo.getClass().isAnnotationPresent(MyMethod.class));
        System.out.println(demo.getClass().isAnnotationPresent(MyService.class));
    }
}



