package com.me.service;

import com.me.annotation.MyService;

@MyService("TestServiceImpl")
public class TestServiceImpl implements TestService {
    @Override
    public String query(String name, String age) {
        return "{name===" + name + ",age==="+age;
    }
}
