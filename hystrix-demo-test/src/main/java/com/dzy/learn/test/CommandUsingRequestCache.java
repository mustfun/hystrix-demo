package com.dzy.learn.test;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/7/28
 * @since 1.0
 */
public class CommandUsingRequestCache extends HystrixCommand<Boolean> {

    private final int value;

    protected CommandUsingRequestCache(int value) {
        super(HystrixCommandGroupKey.Factory.asKey("CacheExample"));
        this.value=value;
    }

    @Override
    protected Boolean run() throws Exception {

        return value==0||value %2 ==0;
    }

    @Override
    protected String getCacheKey() {
        return String.valueOf(value);
    }

    /**
     * 请求上下文里面需要初始化 HystrixRequestContext
     */
    public static class UnitTest {

        @Test
        public void testWithoutCacheHits() {
            HystrixRequestContext context = HystrixRequestContext.initializeContext();
            try {
                assertTrue(new CommandUsingRequestCache(2).execute());
                assertFalse(new CommandUsingRequestCache(1).execute());
                assertTrue(new CommandUsingRequestCache(0).execute());
                assertTrue(new CommandUsingRequestCache(58672).execute());
            } finally {
                context.shutdown();
            }
        }

        /**
         * 测试缓存命中情况
         */
        @Test
        public void testWithCacheHits(){
            HystrixRequestContext requestContext=HystrixRequestContext.initializeContext();

            CommandUsingRequestCache cmda=new CommandUsingRequestCache(2);
            CommandUsingRequestCache cmdb=new CommandUsingRequestCache(2);

            try {
                //System.out.println(cmda.execute());
                assertTrue(cmda.execute());
                // 首次执行2
                assertFalse(cmda.isResponseFromCache());

                assertTrue(cmdb.execute());
                // 第二次执行2，从cache中拿到值,
                assertTrue(cmdb.isResponseFromCache());
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                requestContext.shutdown();
            }
            //重新来了一个请求，那么就又要计算，解决幂等性？？？
            requestContext=HystrixRequestContext.initializeContext();

            try {
                CommandUsingRequestCache cmdc=new CommandUsingRequestCache(2);
                assertTrue(cmdc.execute());
                assertFalse(cmdc.isResponseFromCache());
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                requestContext.shutdown();
            }
        }
    }
}
