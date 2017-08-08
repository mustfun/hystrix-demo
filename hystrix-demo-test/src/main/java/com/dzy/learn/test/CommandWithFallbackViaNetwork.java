package com.dzy.learn.test;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/4
 * @since 1.0
 * 带有网络请求的失败默认返回
 */
public class CommandWithFallbackViaNetwork  extends HystrixCommand<String>{


    private final int id;

    protected CommandWithFallbackViaNetwork(int id) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceX"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("GetValueCommand")));
        this.id = id;
    }

    @Override
    protected String run() {
        //        RemoteServiceXClient.getValue(id);
        //本来是一个网络请求的，但是网络请求那边抛异常啦
        System.out.println(Thread.currentThread().getName());

        throw new RuntimeException("直接失败吧~");
    }

    @Override
    protected String getFallback() {
        return new FallbackViaNetwork(id).execute();
    }


    /**
     * 这个类负责访问网络，在一个单独线程上面跑动RemoteServiceXFallback
     * 还是RemoteServiceX这个组里面
     */
    private static class FallbackViaNetwork extends HystrixCommand<String> {
        private final int id;

        public FallbackViaNetwork(int id) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("RemoteServiceX"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("GetValueFallbackCommand"))
                    // 使用一个新的线程
                    // 在执行的时候不会阻止返回值啦
                    .andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey("RemoteServiceXFallback")));
            this.id = id;
        }

        @Override
        protected String run() {
            //应该是从MemCache中去拿取值
            //MemCacheClient.getValue(id);

            System.out.println(Thread.currentThread().getName());

            throw new RuntimeException("失败在所难免，大侠重新来过~  我也失败啦……^____^……");
        }


        @Override
        protected String getFallback() {
            // 这个fallback也失败了
            // 所以这个会回退一个默认值回去，id*2
            return id*2+"";
        }
    }


    /**
     * 测试demo
     */


    public static class UnitTest {

        @Test
        public void test() {
            HystrixRequestContext context = HystrixRequestContext.initializeContext();
            try {
                assertEquals("2", new CommandWithFallbackViaNetwork(1).execute());

                //执行了GetValueCommand
                HystrixInvokableInfo<?> command1 = HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().toArray(new HystrixInvokableInfo<?>[2])[0];
                assertEquals("GetValueCommand", command1.getCommandKey().name());
                assertTrue(command1.getExecutionEvents().contains(HystrixEventType.FAILURE));

                //执行了GetValueFallbackCommand
                HystrixInvokableInfo<?> command2 = HystrixRequestLog.getCurrentRequest().getAllExecutedCommands().toArray(new HystrixInvokableInfo<?>[2])[1];
                assertEquals("GetValueFallbackCommand", command2.getCommandKey().name());
                assertTrue(command2.getExecutionEvents().contains(HystrixEventType.FAILURE));
            } finally {
                context.shutdown();
            }
        }
    }
}
