package com.dzy.learn.test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/7/27
 * @since 1.0
 */
public class CommondHelloFailure extends HystrixCommand<String> {

    private String name;

    protected CommondHelloFailure(String name) {
        //定义一个CommandGroup把一类command放在一起，比如说异常的，报告的，请求的，警示的，后台的等等
        super(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"));
        this.name=name;
    }

    @Override
    protected String run(){
        throw  new RuntimeException("我在测试抛异常ok");
    }

    @Override
    protected String getFallback() {
        System.out.println("我曾经也到过山河大海~");
        return "错误之后的返回~"+name;
    }

    public static class testHello{

        @Test
        public void testFailture(){
            CommondHelloFailure little_mi = new CommondHelloFailure("小明");
            Assert.assertEquals("错误之后的返回~小明", little_mi.execute());
            System.out.println(little_mi.getClass().getSimpleName());
        }

    }
}
