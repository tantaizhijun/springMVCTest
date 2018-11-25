package com.me.controller;

import com.me.annotation.MyAutowired;
import com.me.annotation.MyController;
import com.me.annotation.MyRequestMapping;
import com.me.annotation.MyRequestParams;
import com.me.service.TestService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@MyController
@MyRequestMapping("/test")
public class TestController {

    @MyAutowired("TestServiceImpl")
    private TestService testService;


    @MyRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse res,
                      @MyRequestParams("name") String name,
                      @MyRequestParams("age") String age)
    {
        PrintWriter pw;
        try{
            pw = res.getWriter();
            String result = testService.query(name,age);
            pw.write(result);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
