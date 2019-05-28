package com.learning.controller;

import com.learning.annotation.MyController;
import com.learning.annotation.MyRequestMapping;
import com.learning.annotation.MyResource;
import com.learning.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author bxd
 * @date 2019/5/28.
 */
@MyController
@MyRequestMapping("/con")
public class DemoController {

    @MyResource
    private IDemoService demoService;

    @MyRequestMapping("/getMessage")
    public void getMessage(HttpServletRequest req, HttpServletResponse resp){
        demoService.sayHello(req.getParameter("name"));
    }

}
