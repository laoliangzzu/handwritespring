package com.learning.service.impl;

import com.learning.annotation.MyService;
import com.learning.service.IDemoService;

/**
 * @author bxd
 * @date 2019/5/28.
 */
@MyService
public class DemoService implements IDemoService {
    @Override
    public String sayHello(String name) {
        return "From DemoService,hello :"+ name;
    }
}
