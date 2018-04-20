package com.dzy.learn.service;


import com.dzy.learn.config.BaseTestConfig;
import com.dzy.learn.model.City;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BaseTestConfig.class,BaseTestConfig.DEV.class})
public class CostpersalesServiceImplTest {


    private static final Logger LOG = LoggerFactory.getLogger(CostpersalesServiceImplTest.class);

    @Autowired
    private CityService cityService;

    @Test
    @Transactional(rollbackFor = Exception.class)
    public void 测试服务降级() {
        City one = cityService.getOne(1);
    }

}

