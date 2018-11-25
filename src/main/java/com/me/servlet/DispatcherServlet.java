package com.me.servlet;

import com.me.annotation.MyController;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DispatcherServlet extends HttpServlet {

    List<String> classNames = new ArrayList<>();

    public DispatcherServlet() {

    }

    //启动tomcat时加载spring mvc
    public void init(ServletConfig config) {
        //ioc容器就是个map
        //需要扫描项目下所有class类
        doScanPackage("com.me");
        for (String name : classNames) {
            System.out.println("className:" + name);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

    }

    public void doScanPackage(String basePackage) {
        //根据配置的路径拿到class所在目录
        URL url = this.getClass()
                .getClassLoader()
                .getResource("/" + basePackage.replaceAll("\\.","/"));
        String fileStr = url.getFile();
        //路径转文件对象
        File file = new File(fileStr);
        String[] filesStr = file.list();
        //遍历路径下的文件
        for (String path : filesStr) {
            File filePath = new File(fileStr + path);

            //如果对象是文件夹, 递归
            if (filePath.isDirectory()) {
                doScanPackage(basePackage + "." + path);
            } else {
                //得到class 全类名路径 com.me.controller.TestController.class,com.me.service.TestService.lass,....
                classNames.add(basePackage + "." + filePath.getName());

            }
        }

    }

    public void doInstance() {
        //没有扫描到类
        if (classNames.size() <= 0) {
            System.out.println("scan  failed .......");
            return;
        }
        //遍历类数组
        for (String className : classNames) {
            String cn = className.replace(".class","");
            try{
                //使用Class的静态方法forName() 动态加载类,如果找不到会报异常CLassNotFund
                Class<?> clazz = Class.forName(cn);
                //判断类上是否有声明MyController这个注解
                if (clazz.isAnnotationPresent(MyController.class)) {
                    Object instance = clazz.newInstance();

                }





            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }
}
