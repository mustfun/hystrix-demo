package com.dzy.learn.test;
import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
/**
 * @author dengzhiyuan
 * @version 1.0
 * @date 2017/8/4
 * @since 1.0
 */



/**
 *  {@link HystrixCommand} 使用信号量独立策略run方法不会有网络故障发生
 *   总结：没有网络访问的时候，建议设置一下隔离策略，使用信号量模式
 */
public class CommandUsingSemaphoreIsolation extends HystrixCommand<String> {

    private final int id;

    public CommandUsingSemaphoreIsolation(int id) {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                // 执行非常快，用信号量策略
                .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                        .withExecutionIsolationStrategy(ExecutionIsolationStrategy.SEMAPHORE)));
        this.id = id;
    }

    @Override
    protected String run() {
        //一个真正的实现，会接受数据从内存里面或者数据库里面，或者其他非网络部分
        return "ValueFromHashMap_" + id;
    }


    public static class UnitTest {

        @Test
        public void testPrimary() {
            HystrixRequestContext context = HystrixRequestContext.initializeContext();
            try {
                //设置属性，对应最上面的参数设置，然后拿出来，好棒哈
                assertEquals("ValueFromHashMap_20", new CommandUsingSemaphoreIsolation(20).execute());
            } finally {
                context.shutdown();
                ConfigurationManager.getConfigInstance().clear();
            }
        }

    }
}